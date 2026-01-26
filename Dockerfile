# ============================================================================
# STAGE 1: BUILD
# ============================================================================
# Image de base pour la compilation
FROM maven:3.9-eclipse-temurin-21-alpine AS build
# 🎯 Rôle: Environnement pour compiler l'application
# 📖 Décryptage:
#    - maven:3.9              → Maven version 3.9
#    - eclipse-temurin-21     → Distribution OpenJDK 21 (compatible Java 21)
#    - alpine                 → OS ultra-léger (5 MB vs 100+ MB pour Ubuntu)
#    - AS build               → Nom du stage (référencé plus tard)
# 💡 Pourquoi Eclipse Temurin ? Distribution OpenJDK officielle et maintenue

# Définir le répertoire de travail dans le container
WORKDIR /app
# 🎯 Rôle: Tous les chemins relatifs seront basés sur /app
# 📖 Équivalent à: cd /app
# 💡 Sans cela, on travaillerait dans / (racine) → désordonné

# --------------------------------------------------
# Optimisation: Mise en cache des dépendances Maven
# --------------------------------------------------
# Copier uniquement pom.xml en premier
COPY pom.xml .
# 🎯 Rôle: Permet à Docker de cacher cette layer si pom.xml ne change pas
# 📖 Docker fonctionne en layers (couches):
#    Layer 1: Image de base
#    Layer 2: COPY pom.xml      ← Cachée si pom.xml inchangé
#    Layer 3: Download deps     ← Réutilisée si Layer 2 cachée
#    Layer 4: COPY src          ← Invalidée si code change
#    Layer 5: Build             ← Réexécutée si Layer 4 change
# 💡 Résultat: Builds beaucoup plus rapides en développement

# Télécharger les dépendances Maven
RUN mvn dependency:go-offline -B
# 🎯 Rôle: Télécharge toutes les dependencies du pom.xml
# 📖 Explication:
#    - mvn dependency:go-offline  → Télécharge sans compiler
#    - -B (batch mode)            → Pas d'output interactif
# 💡 Cette layer est réutilisée tant que pom.xml ne change pas
# ⏱️ Gain de temps: ~2 minutes économisées sur chaque build suivant

# Copier le code source
COPY src ./src
# 🎯 Rôle: Copie votre code Java dans le container
# 📖 ./src depuis votre machine → /app/src dans le container
# 💡 Exécuté à chaque build car le code change souvent

# Compiler l'application et créer le JAR
RUN mvn clean package -DskipTests -B
# 🎯 Rôle: Compile et package l'application en fichier .jar
# 📖 Décryptage:
#    - mvn clean           → Supprime target/ (ancien build)
#    - package             → Compile + crée le .jar
#    - -DskipTests         → Skip les tests (exécutés en CI/CD)
#    - -B                  → Batch mode
# 📁 Résultat: target/yowyob-feedback-api-0.0.1-SNAPSHOT.jar
# ⏱️ Durée: ~2-3 minutes

# ============================================================================
# STAGE 2: RUNTIME
# ============================================================================
# Image de base pour l'exécution (plus légère)
FROM eclipse-temurin:21-jre-alpine
# 🎯 Rôle: Environnement minimal pour exécuter l'application
# 📖 Différences avec le stage BUILD:
#    - JRE au lieu de JDK  → Pas de compilateur (plus léger)
#    - Pas de Maven        → Pas nécessaire pour exécuter
# 💾 Taille: ~200 MB vs ~700 MB du stage BUILD
# ✅ Résultat: Image finale 3.5x plus petite

WORKDIR /app
# 🎯 Rôle: Répertoire de travail pour le runtime

# --------------------------------------------------
# Sécurité: Créer un utilisateur non-root
# --------------------------------------------------
RUN addgroup -S spring && adduser -S spring -G spring
# 🎯 Rôle: Créer un utilisateur système "spring" pour exécuter l'app
# 📖 Explication:
#    - addgroup -S spring        → Crée groupe système "spring"
#    - adduser -S spring -G spring → Crée user "spring" dans le groupe
# 🔒 Sécurité: CRITIQUE !
#    ❌ Sans cela: L'app s'exécute en root (dangereuse si compromise)
#    ✅ Avec cela: L'app ne peut pas modifier le système
# 💡 Best practice Docker: JAMAIS exécuter en root

USER spring:spring
# 🎯 Rôle: Basculer vers l'utilisateur "spring"
# 📖 Toutes les commandes suivantes s'exécutent en tant que "spring"
# ✅ L'application n'a plus les privilèges root

# --------------------------------------------------
# Copier le JAR depuis le stage BUILD
# --------------------------------------------------
COPY --from=build /app/target/*.jar app.jar
# 🎯 Rôle: Copie le fichier .jar compilé depuis le stage BUILD
# 📖 Décryptage:
#    - --from=build              → Depuis le stage nommé "build"
#    - /app/target/*.jar         → Chemin source (dans stage build)
#    - app.jar                   → Nom de destination (simplifié)
# 💡 Seul le .jar est copié, pas Maven, pas le code source
# 📁 Fichier copié: yowyob-feedback-api-0.0.1-SNAPSHOT.jar → app.jar

# --------------------------------------------------
# Exposer le port de l'application
# --------------------------------------------------
EXPOSE 8080
# 🎯 Rôle: Indique que l'application écoute sur le port 8080
# 📖 C'est une DOCUMENTATION, pas une action
# ❌ N'ouvre PAS le port (fait par Koyeb avec -p)
# 💡 Utile pour que d'autres développeurs sachent quel port utiliser

# --------------------------------------------------
# Health check
# --------------------------------------------------
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1
# 🎯 Rôle: Vérifier automatiquement si l'application est en bonne santé
# 📖 Décryptage:
#    - --interval=30s        → Teste toutes les 30 secondes
#    - --timeout=3s          → Abandonne si pas de réponse en 3s
#    - --start-period=40s    → Attend 40s au démarrage (app démarre)
#    - --retries=3           → 3 échecs consécutifs = container "unhealthy"
#    - wget ... /actuator/health → Appelle l'endpoint de santé Spring
#    - || exit 1             → Marque comme "unhealthy" si échec
# 💡 Koyeb utilise cela pour:
#    - Redémarrer le container si unhealthy
#    - Ne pas router le trafic vers container unhealthy
#    - Afficher l'état dans le dashboard

# --------------------------------------------------
# Commande de démarrage de l'application
# --------------------------------------------------
ENTRYPOINT ["java", \
  "-Dspring.profiles.active=prod", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-jar", \
  "app.jar"]
# 🎯 Rôle: Commande exécutée quand le container démarre
# 📖 Décryptage ligne par ligne:

# java
#   └─ Commande pour exécuter un JAR

# -Dspring.profiles.active=prod
#   └─ Active le profil "prod" (charge application-prod.yml)
#   └─ Peut être surchargé par variable d'env SPRING_PROFILES_ACTIVE

# -XX:+UseContainerSupport
#   └─ Active la détection automatique des limites du container
#   └─ Java détecte la RAM/CPU allouée par Docker/Koyeb
#   └─ Ajuste automatiquement le heap size

# -XX:MaxRAMPercentage=75.0
#   └─ Java utilisera max 75% de la RAM disponible
#   └─ Exemple: Container avec 512 MB → Java max 384 MB
#   └─ Les 25% restants pour: OS, buffer, métadata
#   └─ Évite les OOM (Out Of Memory) kills

# -jar app.jar
#   └─ Exécute le fichier app.jar

# 💡 Pourquoi ENTRYPOINT et pas CMD ?
#    - ENTRYPOINT : Commande principale, difficile à surcharger
#    - CMD : Peut être facilement surchargée au runtime
#    - Ici on veut forcer l'exécution avec les bonnes options JVM

# 📊 Exemple d'exécution:
#    Si container a 512 MB de RAM:
#    ├─ Java heap max: 384 MB (75%)
#    ├─ OS + buffer: 128 MB (25%)
#    └─ Protection contre OOM kill

