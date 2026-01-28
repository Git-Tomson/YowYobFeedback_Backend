FROM maven:3.9-eclipse-temurin-21-alpine AS build

WORKDIR /app

# Tout copier d'un coup
COPY . .

# Build rapide
RUN mvn clean package -DskipTests -B -q

# Runtime
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN apk add --no-cache wget && \
    addgroup -S spring && \
    adduser -S spring -G spring

COPY --from=build /app/target/*.jar app.jar

RUN chown spring:spring app.jar

USER spring:spring

EXPOSE 8080

HEALTHCHECK CMD wget -qO- http://localhost:8080/api/v1/health || exit 1

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

CMD java $JAVA_OPTS -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:-prod} -jar app.jar
