# YowYob Feedback Backend

API backend pour un système de gestion de retours d'expérience (feedback) permettant aux utilisateurs de créer des projets, de partager des feedbacks et d'interagir via des commentaires et des likes.

## 🚀 Technologies Utilisées

### Backend
- **Java 17+** - Langage de programmation
- **Spring Boot 3.x** - Framework principal
- **Spring WebFlux** - Architecture réactive non-bloquante
- **Spring Security** - Sécurité et authentification
- **R2DBC** - Accès réactif aux bases de données
- **Liquibase** - Gestion des migrations de base de données

### Base de données
- **PostgreSQL** - Base de données relationnelle

### Sécurité
- **JWT (JSON Web Tokens)** - Authentification stateless
- **BCrypt** - Hashage des mots de passe
- **2FA (Two-Factor Authentication)** - Authentification à deux facteurs

### Documentation
- **Swagger/OpenAPI** - Documentation interactive de l'API

### Build & Déploiement
- **Maven** - Gestion des dépendances
- **Docker** - Conteneurisation
- **Render** - Plateforme de déploiement cloud

## 📋 Prérequis

- **Java 17** ou supérieur
- **Maven 3.8+**
- **PostgreSQL 14+**
- **Git**

## 🔧 Installation et Configuration Locale

### 1. Cloner le projet

```bash
git clone https://github.com/Git-Tomson/YowYobFeedback_Backend.git
cd YowYobFeedback_Backend
```

### 2. Configurer la base de données

Créez une base de données PostgreSQL locale :

```sql
CREATE DATABASE feedback_db;
CREATE USER tomson WITH PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE feedback_db TO tomson;
```

### 3. Configurer les variables d'environnement

Créez un fichier `application-dev.yml` dans `src/main/resources/` :

```yaml
spring:
  r2dbc:
    url: r2dbc:postgresql://localhost:5432/feedback_db
    username: tomson
    password: your_password
  
jwt:
  secret: your_secret_key_for_development_only_change_in_production
  expiration: 86400000

server:
  port: 8080
```

### 4. Compiler et lancer l'application

```bash
# Compiler le projet
mvn clean install

# Lancer l'application
mvn spring-boot:run
```

L'API sera accessible sur `http://localhost:8080`

## 📚 Documentation API

### Documentation Swagger locale
Une fois l'application lancée localement, accédez à :
- **Swagger UI** : http://localhost:8080/swagger-ui.html

### Documentation en production
- **Swagger UI Production** : https://yowyob-feedback-api-8h8f.onrender.com/swagger-ui.html

## 🌐 Déploiement

### Branche déployée
- **main** - Branche de production

### Service de déploiement
- **Render** - https://render.com

### URL de l'API en production
- https://yowyob-feedback-api-8h8f.onrender.com

## 📁 Structure du Projet

```
src/
├── main/
│   ├── java/com/yowyob/feedback/
│   │   ├── config/          # Configurations (Security, CORS, OpenAPI)
│   │   ├── controller/      # Contrôleurs REST
│   │   ├── dto/            # Data Transfer Objects
│   │   │   ├── request/    # DTOs de requête
│   │   │   └── response/   # DTOs de réponse
│   │   ├── entity/         # Entités JPA
│   │   ├── mapper/         # Mappers manuels (Entity ↔ DTO)
│   │   ├── repository/     # Repositories R2DBC
│   │   ├── security/       # Filtres et utilitaires de sécurité
│   │   ├── service/        # Logique métier
│   │   └── util/           # Classes utilitaires
│   └── resources/
│       ├── db/changelog/   # Scripts Liquibase
│       └── application.yml # Configuration principale
└── test/                   # Tests unitaires et d'intégration
```

## 🔑 Fonctionnalités Principales

### Authentification & Sécurité
- ✅ Inscription et connexion utilisateurs (Personnes & Organisations)
- ✅ Authentification JWT
- ✅ Authentification à deux facteurs (2FA)
- ✅ Réinitialisation de mot de passe
- ✅ Hashage sécurisé des mots de passe (BCrypt)

### Gestion des Projets
- ✅ Création et gestion de projets
- ✅ Invitation de membres
- ✅ Gestion des rôles et permissions

### Système de Feedback
- ✅ Publication de feedbacks
- ✅ Commentaires sur les feedbacks
- ✅ Système de likes
- ✅ Pièces jointes

### Social
- ✅ Abonnements entre utilisateurs
- ✅ Certification des utilisateurs

## 👥 Auteur(Superviseur académique du projet)

- **Thomas Djotio Ndié** - Prof Dr_Eng.

## 📄 Licence

Ce projet est développé dans un cadre pédagogique.

## 🤝 Contribution

Les contributions sont les bienvenues ! Veuillez suivre la charte de développement du projet disponible dans `Charte_de_Développement_et_contraintes_Technologiques.pdf`.

### Convention de nommage des branches
- `feature/nom_fonctionnalite` - Nouvelles fonctionnalités
- `bugfix/description_bug` - Corrections de bugs
- `hotfix/description_rapide` - Corrections urgentes

### Commits
- Messages en anglais
- Format impératif : "Add feature" plutôt que "Added feature"
- Messages clairs et concis

---

**Note** : L'application utilise le plan gratuit de Render. La base de données PostgreSQL gratuite expire après 90 jours et l'API peut se mettre en veille après 15 minutes d'inactivité.
