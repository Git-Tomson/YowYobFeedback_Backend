# YowyobFeedback API

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![WebFlux](https://img.shields.io/badge/WebFlux-Reactive-blue.svg)](https://docs.spring.io/spring-framework/reference/web/webflux.html)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg)](https://www.postgresql.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

A modern, reactive feedback management platform built with Spring Boot WebFlux and PostgreSQL. This application enables users to create projects, collect feedback, and manage collaborative interactions in a real-time, low-latency environment.

## 📋 Table of Contents

- [Features](#-features)
- [Architecture](#-architecture)
- [Technology Stack](#-technology-stack)
- [Prerequisites](#-prerequisites)
- [Getting Started](#-getting-started)
- [Project Structure](#-project-structure)
- [API Documentation](#-api-documentation)
- [Database Schema](#-database-schema)
- [Development Guidelines](#-development-guidelines)
- [Testing](#-testing)
- [Deployment](#-deployment)
- [Contributing](#-contributing)
- [License](#-license)
- [Contact](#-contact)

## ✨ Features

### Core Functionality
- **User Management**
    - User registration with dual types (Person/Organization)
    - JWT-based authentication
    - Two-factor authentication (2FA) support
    - Password reset with token-based security
    - User profile management with certifications

- **Project Management**
    - Create and manage projects with unique codes
    - Project member management with custom pseudonyms
    - Project logos and descriptions
    - Member role assignments

- **Feedback System**
    - Submit feedback with attachments
    - Like/unlike feedback
    - Comment on feedback with nested discussions
    - Real-time feedback counters
    - Feedback attachments support

- **Social Features**
    - User subscriptions (follow/unfollow)
    - Activity feeds
    - User certifications
    - Organization profiles with locations

### Technical Features
- Reactive programming with Spring WebFlux
- Non-blocking database operations with R2DBC
- JWT token authentication
- Database versioning with Liquibase
- Comprehensive API documentation with OpenAPI/Swagger
- Health monitoring with Spring Boot Actuator

## 🏗 Architecture

This application follows a **layered microservices architecture** with clear separation of concerns:

```
┌─────────────────────────────────────┐
│         Presentation Layer          │
│    (Controllers, DTOs, Mappers)     │
└─────────────────────────────────────┘
                 ↓
┌─────────────────────────────────────┐
│          Service Layer              │
│     (Business Logic, Security)      │
└─────────────────────────────────────┘
                 ↓
┌─────────────────────────────────────┐
│        Repository Layer             │
│      (R2DBC, Data Access)           │
└─────────────────────────────────────┘
                 ↓
┌─────────────────────────────────────┐
│         Database Layer              │
│    (PostgreSQL with Liquibase)      │
└─────────────────────────────────────┘
```

### Design Patterns
- **Repository Pattern**: Data access abstraction
- **DTO Pattern**: Data transfer between layers
- **Builder Pattern**: Object construction
- **Strategy Pattern**: Authentication strategies
- **Factory Pattern**: Entity creation

## 🛠 Technology Stack

### Backend Framework
- **Java 21**: Latest LTS version with modern language features
- **Spring Boot 3.x**: Application framework
- **Spring WebFlux**: Reactive web framework
- **Spring Security**: Authentication and authorization
- **Spring Data R2DBC**: Reactive database access
- **Project Reactor**: Reactive programming library

### Database
- **PostgreSQL 16**: Primary relational database
- **PostGIS**: Spatial database extensions (for future map features)
- **Liquibase**: Database migration and versioning
- **R2DBC PostgreSQL**: Reactive database driver

### Security
- **JJWT (JSON Web Token)**: Token-based authentication
- **BCrypt**: Password hashing
- **Spring Security WebFlux**: Reactive security

### Documentation & Testing
- **SpringDoc OpenAPI**: API documentation (Swagger UI)
- **JUnit 5**: Unit testing framework
- **Reactor Test**: Reactive streams testing
- **Mockito**: Mocking framework

### Code Quality
- **Lombok**: Boilerplate code reduction
- **Jakarta Validation**: Bean validation
- **SLF4J/Logback**: Logging framework

### Build & DevOps (Planned)
- **Maven/Gradle**: Build automation
- **Docker**: Containerization
- **GitHub Actions**: CI/CD pipeline
- **Redis**: Caching layer (future)
- **Kafka**: Event streaming (future)
- **Elasticsearch**: Search functionality (future)

### Frontend (Future Integration)
- **Next.js**: Server-side rendering and SEO
- **OpenStreetMap/MapLibre**: Interactive maps
- **PWA**: Progressive web application features

## 📦 Prerequisites

Before you begin, ensure you have the following installed:

- **Java Development Kit (JDK) 21** or higher
  ```bash
  java -version
  ```

- **Maven 3.8+** or **Gradle 8.0+**
  ```bash
  mvn -version
  ```

- **PostgreSQL 16+**
  ```bash
  psql --version
  ```

- **Git**
  ```bash
  git --version
  ```

- **Docker** (optional, for containerized deployment)
  ```bash
  docker --version
  ```

## 🚀 Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/Git-Tomson/YowYobFeedback_Backend.git
cd YowYobFeedback_Backend
```

### 2. Database Setup

#### Option A: Local PostgreSQL Installation

1. Create a new database:
```sql
CREATE DATABASE yowyob_feedback;
CREATE USER yowyob_user WITH ENCRYPTED PASSWORD 'your_secure_password';
GRANT ALL PRIVILEGES ON DATABASE yowyob_feedback TO yowyob_user;
```

2. Enable UUID extension:
```sql
\c yowyob_feedback
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
```

#### Option B: Docker PostgreSQL

```bash
docker run --name yowyob-postgres \
  -e POSTGRES_DB=yowyob_feedback \
  -e POSTGRES_USER=yowyob_user \
  -e POSTGRES_PASSWORD=your_secure_password \
  -p 5432:5432 \
  -d postgres:16-alpine
```

### 3. Configure Application Properties

Create or update `src/main/resources/application.yml`:

```yaml
spring:
  application:
    name: yowyob-feedback
  
  r2dbc:
    url: r2dbc:postgresql://localhost:5432/yowyob_feedback
    username: yowyob_user
    password: your_secure_password
    pool:
      initial-size: 10
      max-size: 20
      max-idle-time: 30m
      max-acquire-time: 3s
  
  liquibase:
    change-log: classpath:db/changelog/db.changelog-master.yaml
    url: jdbc:postgresql://localhost:5432/yowyob_feedback
    user: yowyob_user
    password: your_secure_password
  
  security:
    jwt:
      secret-key: ${JWT_SECRET_KEY:your_very_secret_and_long_key_for_hmac_signature_256_bits}
      expiration: 3600000 # 1 hour in milliseconds

server:
  port: 8080

logging:
  level:
    com.yowyob.feedback: DEBUG
    org.springframework.r2dbc: DEBUG
    io.r2dbc.postgresql.QUERY: DEBUG

springdoc:
  api-docs:
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui.html
    enabled: true

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: always
```

### 4. Build the Project

#### Using Maven:
```bash
mvn clean install
```

#### Using Gradle:
```bash
./gradlew clean build
```

### 5. Run Database Migrations

Liquibase will automatically run migrations on application startup. To run manually:

```bash
mvn liquibase:update
```

### 6. Run the Application

#### Using Maven:
```bash
mvn spring-boot:run
```

#### Using Gradle:
```bash
./gradlew bootRun
```

#### Using JAR:
```bash
java -jar target/yowyob-feedback-0.0.1-SNAPSHOT.jar
```

### 7. Verify Installation

Once the application starts, verify it's running:

- **API Base URL**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API Docs**: http://localhost:8080/v3/api-docs
- **Health Check**: http://localhost:8080/actuator/health

Expected health check response:
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "PostgreSQL",
        "validationQuery": "isValid()"
      }
    }
  }
}
```

## 📁 Project Structure

```
yowyob-feedback-backend/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── yowyob/
│   │   │           └── feedback/
│   │   │               ├── config/           # Configuration classes
│   │   │               │   ├── SecurityConfig.java
│   │   │               │   └── R2dbcConfig.java
│   │   │               ├── constant/         # Application constants
│   │   │               │   └── AppConstants.java
│   │   │               ├── controller/       # REST controllers
│   │   │               │   └── AuthController.java
│   │   │               ├── dto/              # Data Transfer Objects
│   │   │               │   ├── request/
│   │   │               │   │   ├── LoginRequestDTO.java
│   │   │               │   │   └── RegisterRequestDTO.java
│   │   │               │   └── response/
│   │   │               │       ├── AuthResponseDTO.java
│   │   │               │       └── UserResponseDTO.java
│   │   │               ├── entity/           # JPA entities
│   │   │               │   ├── AppUser.java
│   │   │               │   ├── Person.java
│   │   │               │   ├── Organization.java
│   │   │               │   ├── Project.java
│   │   │               │   ├── Member.java
│   │   │               │   ├── Feedback.java
│   │   │               │   ├── Comments.java
│   │   │               │   ├── Likes.java
│   │   │               │   └── Subscription.java
│   │   │               ├── exception/        # Custom exceptions
│   │   │               │   └── GlobalExceptionHandler.java
│   │   │               ├── mapper/           # Entity-DTO mappers
│   │   │               │   └── UserMapper.java
│   │   │               ├── repository/       # R2DBC repositories
│   │   │               │   ├── AppUserRepository.java
│   │   │               │   ├── PersonRepository.java
│   │   │               │   └── OrganizationRepository.java
│   │   │               ├── service/          # Business logic
│   │   │               │   ├── AuthService.java
│   │   │               │   └── JwtService.java
│   │   │               └── FeedbackApplication.java
│   │   └── resources/
│   │       ├── application.yml               # Main configuration
│   │       ├── application-dev.yml           # Development profile
│   │       ├── application-prod.yml          # Production profile
│   │       └── db/
│   │           └── changelog/                # Liquibase migrations
│   │               ├── db.changelog-master.yaml
│   │               ├── changelog-001-create-tables.yaml
│   │               ├── changelog-002-alter-table-project.yaml
│   │               ├── changelog-003-add-2fa-and-password-reset.yaml
│   │               └── changelog-004-add-counters.yaml
│   └── test/
│       └── java/
│           └── com/
│               └── yowyob/
│                   └── feedback/
│                       ├── service/          # Service tests
│                       └── controller/       # Controller tests
├── .gitignore
├── pom.xml                                   # Maven configuration
├── README.md
└── LICENSE
```

## 📚 API Documentation

### Authentication Endpoints

#### Register User
```http
POST /api/v1/auth/register
Content-Type: application/json

{
  "user_type": "PERSON",
  "user_firstname": "John",
  "user_lastname": "Doe",
  "email": "john.doe@example.com",
  "password": "SecurePass123!",
  "contact": "+237675518880",
  "occupation": "Software Developer"
}
```

**Response (201 Created):**
```json
{
  "message": "Registration successful",
  "user_response_dto": {
    "user_type": "PERSON",
    "user_firstname": "John",
    "user_lastname": "Doe",
    "email": "john.doe@example.com",
    "contact": "+237675518880",
    "certified": false,
    "registration_date_time": "2025-01-26T10:30:00Z",
    "occupation": "Software Developer"
  },
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

#### Login
```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "identifier": "john.doe@example.com",
  "password": "SecurePass123!"
}
```

**Response (200 OK):**
```json
{
  "message": "Login successful",
  "user_response_dto": { ... },
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

### Interactive API Documentation

Access the full interactive API documentation at:
- **Swagger UI**: http://localhost:8080/swagger-ui.html

## 🗄 Database Schema

### Entity Relationship Diagram

```
┌─────────────┐         ┌──────────────┐
│   AppUser   │◄────────┤    Person    │
│             │         │              │
│ - user_id   │         │ - person_id  │
│ - email     │         │ - occupation │
│ - password  │         └──────────────┘
│ - user_type │
│ - certified │         ┌──────────────────┐
└─────────────┘◄────────┤  Organization    │
       ▲                │                  │
       │                │ - org_id         │
       │                │ - location       │
       │                └──────────────────┘
       │
       │ creates         ┌──────────────┐
       └────────────────►│   Project    │
                         │              │
                         │ - project_id │
                         │ - code       │
                         │ - creator_id │
                         └──────────────┘
                                ▲
                                │
                                │ belongs to
                         ┌──────────────┐
                         │    Member    │
                         │              │
                         │ - member_id  │
                         │ - user_id    │
                         │ - project_id │
                         └──────────────┘
                                │
                                │ submits
                                ▼
                         ┌──────────────┐
                         │   Feedback   │
                         │              │
                         │ - feedback_id│
                         │ - content    │
                         │ - member_id  │
                         └──────────────┘
```

### Key Tables

#### app_user
Base table for all users (Person and Organization).

| Column | Type | Description |
|--------|------|-------------|
| user_id | UUID | Primary key |
| user_type | VARCHAR(20) | PERSON or ORGANIZATION |
| email | TEXT | Unique email address |
| password | TEXT | BCrypt hashed password |
| contact | TEXT | Unique contact number |
| certified | BOOLEAN | Certification status |
| two_fa_enabled | BOOLEAN | 2FA activation status |

#### project
Projects created by users.

| Column | Type | Description |
|--------|------|-------------|
| project_id | UUID | Primary key |
| project_name | TEXT | Project name |
| code | VARCHAR(6) | Unique project code |
| creator_id | UUID | Foreign key to app_user |
| creation_date_time | TIMESTAMP | Creation timestamp |

#### feedback
User feedback on projects.

| Column | Type | Description |
|--------|------|-------------|
| feedback_id | UUID | Primary key |
| content | TEXT | Feedback content |
| target_project_id | UUID | Foreign key to project |
| member_id | UUID | Foreign key to member |
| number_of_likes | INTEGER | Cached like count |
| number_of_comments | INTEGER | Cached comment count |

## 👨‍💻 Development Guidelines

### Coding Standards

This project follows the **YowYob Development Charter** (see `Charte de Développement.pdf`).

#### Naming Conventions
- **Variables**: `snake_case` (e.g., `user_name`, `order_date`)
- **Constants**: `UPPER_SNAKE_CASE` (e.g., `MAX_SIZE`, `DEFAULT_TIMEOUT`)
- **Classes**: `PascalCase` (e.g., `CustomerService`, `OrderManager`)
- **Methods**: `camelCase` (e.g., `calculateTotal()`, `getUserName()`)
- **Packages**: lowercase with dots (e.g., `com.yowyob.feedback.service`)

#### Code Organization
- One class per file
- Maximum line length: 120 characters
- Indentation: 4 spaces (no tabs)
- Braces on same line: `if (condition) {`
- Specific imports only (no `import java.util.*;`)

#### Documentation
- Javadoc for all public classes and methods
- Comments in English
- Include `@author`, `@since`, `@version` in class headers

#### Exception Handling
- Use specific exceptions (never generic `Exception`)
- Propagate exceptions only when relevant
- Error messages in English

#### Best Practices
- Follow SOLID principles
- Clear separation: Controller / Service / Repository
- Use constants instead of magic values
- Prefer primitives over wrapper objects (`int` vs `Integer`)
- Use `Optional` instead of `null`
- Never log sensitive data (passwords, tokens)

### Git Workflow

#### Branch Naming
- `feature/feature_name` - New features
- `bugfix/bug_description` - Bug fixes
- `hotfix/quick_description` - Critical fixes

#### Commit Messages
Write in English, imperative mood:
```
Add payment gateway integration
Fix order calculation bug
Update user authentication flow
```

### Code Quality Tools
- **Checkstyle**: Code style verification
- **SonarLint**: Code quality analysis
- **PMD**: Static code analysis

## 🧪 Testing

### Running Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=AuthServiceTest

# Run with coverage
mvn test jacoco:report
```



## 🚢 Deployment

### Docker Deployment

#### 1. Build Docker Image

```bash
docker build -t yowyob-feedback:latest .
```

#### 2. Docker Compose

Create `docker-compose.yml`:

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:16-alpine
    environment:
      POSTGRES_DB: yowyob_feedback
      POSTGRES_USER: yowyob_user
      POSTGRES_PASSWORD: ${DB_PASSWORD}
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
    networks:
      - yowyob-network

  app:
    image: yowyob-feedback:latest
    depends_on:
      - postgres
    environment:
      SPRING_R2DBC_URL: r2dbc:postgresql://postgres:5432/yowyob_feedback
      SPRING_R2DBC_USERNAME: yowyob_user
      SPRING_R2DBC_PASSWORD: ${DB_PASSWORD}
      JWT_SECRET_KEY: ${JWT_SECRET}
    ports:
      - "8080:8080"
    networks:
      - yowyob-network

volumes:
  postgres_data:

networks:
  yowyob-network:
    driver: bridge
```

#### 3. Run with Docker Compose

```bash
docker-compose up -d
```

### Production Checklist

- [ ] Set strong JWT secret key
- [ ] Configure production database credentials
- [ ] Enable HTTPS/TLS
- [ ] Set up proper logging (centralized)
- [ ] Configure monitoring and alerts
- [ ] Set up database backups
- [ ] Configure rate limiting
- [ ] Review security configurations
- [ ] Set up CI/CD pipeline
- [ ] Configure environment-specific profiles

## 🤝 Contributing

We welcome contributions! Please follow these steps:

1. **Fork the repository**
2. **Create a feature branch**
   ```bash
   git checkout -b feature/amazing_feature
   ```
3. **Commit your changes**
   ```bash
   git commit -m "describe the amazing feature"
   ```
4. **Push to the branch**
   ```bash
   git push origin feature/amazing_feature
   ```
5. **Open a Pull Request**

### Pull Request Guidelines
- Follow the coding standards
- Add tests for new features
- Update documentation as needed
- Ensure all tests pass
- Keep commits atomic and meaningful

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 📧 Contact

**Author**: Thomas Djotio Ndié, Prof Dr_Eng.

- **Email**: 
- **Phone**: 
- **GitHub**: [@Git-Tomson](https://github.com/Git-Tomson)
- **Project Repository**: [YowYobFeedback_Backend](https://github.com/Git-Tomson/YowYobFeedback_Backend)

## 🙏 Acknowledgments

- Spring Framework team for excellent reactive programming support
- PostgreSQL community for a robust database system
- All contributors and testers

---

**Educational Project** - Part of a microservices and DevOps learning curriculum focusing on:
- Distributed systems architecture
- Reactive programming with WebFlux
- Modern Java development practices
- Low-latency application design
- Network-resilient application development

---

Made with ❤️ by the YowYob Team


    

 
# Architecture

┌─────────────────────────────────────────────────────────────┐
│                    KOYEB PLATFORM                            │
│  ┌────────────────────────────────────────────────────────┐ │
│  │              Load Balancer (HTTPS/SSL)                 │ │
│  └───────────────────────┬────────────────────────────────┘ │
│                          │                                   │
│                          ▼                                   │
│  ┌───────────────────────────────────┐                      │
│  │  Docker Container (App Service)   │                      │
│  │  ┌─────────────────────────────┐  │                      │
│  │  │  Spring Boot Application    │  │                      │
│  │  │  Port: 8080                 │  │                      │
│  │  └──────────┬──────────────────┘  │                      │
│  └─────────────┼──────────────────────┘                      │
│                │                                             │
│                │ Private Network                             │
│                │ R2DBC Connection                            │
│                ▼                                             │
│  ┌─────────────────────────────┐                            │
│  │  PostgreSQL Database        │                            │
│  │  (Koyeb Managed Service)    │                            │
│  │  - Private DNS              │                            │
│  │  - Automated backups        │                            │
│  │  - SSL enabled              │                            │
│  └─────────────────────────────┘                            │
│                                                               │
└───────────────────────────────────────────────────────────────┘
