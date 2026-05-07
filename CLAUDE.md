# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Spring Boot 4.0.6 backend for a healthcare EMR (Electronic Medical Records) system. Java 17, Gradle 9.4.1, MySQL, Redis, Spring Security + JWT.

The server runs on **port 3000**.

## Commands

```bash
# Build
./gradlew build

# Run (dev)
./gradlew bootRun

# Test all
./gradlew test

# Single test class
./gradlew test --tests "com.emr.medicare.SomeTest"

# Clean build
./gradlew clean build
```

On Windows use `gradlew.bat` instead of `./gradlew`.

## Architecture

Domain-module structure under `com.emr.medicare`:

| Module | Responsibility |
|---|---|
| `auth` | JWT issuance/validation, login/logout, token refresh |
| `patient` | Patient registration, lookup, medical history |
| `nurse` | Nurse/staff management |
| `prescription` | Prescription creation and management |
| `reservation` | Appointment scheduling |
| `ai` | AI/ML integration (external API client) |
| `audit` | Audit log entity, repository, service |
| `common/config` | Cross-cutting Spring configuration beans |

Each module follows the layered pattern: `controller → service → repository → entity`, with DTOs for request/response.

## Database

- MySQL via Spring Data JPA; `ddl-auto: update` (Hibernate manages schema automatically)
- `show-sql: true` in dev — SQL is logged to stdout
- No migration tool (Flyway/Liquibase) — schema is managed by Hibernate
- Entities live in each module's `entity/` sub-package

## Security

- Spring Security + JWT (planned in `auth/jwt/`)
- Stateless session; tokens issued at login, validated per request
- `spring-boot-starter-security` is on the classpath — any new controller is secured by default unless explicitly permitted in the security config

## Redis

Reactive Redis (`spring-boot-starter-data-redis-reactive`) is included. Intended use: token blacklisting / session cache. Configure `spring.data.redis.*` in `application.yml` before use.

## Configuration

`src/main/resources/application.yml` — currently minimal. Add environment-specific overrides via `application-dev.yml` / `application-prod.yml` and activate with `spring.profiles.active`.

## Testing

Test starters for JPA, Redis, Security, Validation, and Web MVC are all included. Integration tests should use `@SpringBootTest`; slice tests (e.g. `@WebMvcTest`, `@DataJpaTest`) are preferred for unit-level coverage of individual layers.
