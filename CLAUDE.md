# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Java Spring Boot microservices project (Maven multi-module). Stack: Java 17, Spring Boot 3.4.5, Spring Cloud Gateway, PostgreSQL, Redis, Liquibase, JWT (JJWT), Cloudflare R2 (S3-compatible).

## Modules

| Module | Port | Role |
|---|---|---|
| `common-lib` | — | Shared DTOs, exceptions, utilities (no Spring Boot app) |
| `auth-service` | 8080 | Authentication, JWT issuance, user management. Context: `/api/auth-service` |
| `course-service` | 8081 | Courses, sections, lessons, video uploads (Cloudflare R2). Context: `/api/course-service` |
| `gateway-service` | 8000 | Spring Cloud Gateway — routes traffic, validates JWT via `spring-boot-starter-oauth2-resource-server` |

## Build & Run Commands

```bash
# Build all modules (from root)
./mvnw clean install -DskipTests

# Build a single service
./mvnw clean install -pl course-service -am -DskipTests

# Run tests
./mvnw test -pl auth-service

# Run a service locally (after build)
cd auth-service && ../mvnw spring-boot:run
```

## Key Architecture Patterns

- **common-lib must be installed first** — all services depend on it. `-am` flag handles this automatically.
- **Gateway is the single entry point** — clients hit port 8000; gateway forwards to internal service ports.
- **JWT flow**: auth-service issues tokens; gateway validates them using the same `jwt.secret`; course-service also validates via `oauth2-resource-server`.
- **Database migrations via Liquibase** — changelogs at `src/main/resources/db/changelog/db.changelog-master.xml`. Schema is validated (`ddl-auto=validate`) not auto-updated in production.
- **Cloudflare R2** (S3-compatible) used for video storage in course-service — configured via `R2_*` env vars.

## Environment Variables Pattern

All services use env-var overrides with defaults. Key vars:

```
# Shared
DB_URL, DB_USERNAME, DB_PASSWORD
REDIS_HOST, REDIS_PORT, REDIS_PASSWORD
JWT_SECRET, JWT_EXPIRATION, JWT_REFRESH_EXPIRATION

# course-service only
R2_ACCOUNT_ID, R2_ACCESS_KEY_ID, R2_SECRET_ACCESS_KEY, R2_BUCKET, R2_PUBLIC_URL
MULTIPART_MAX_FILE_SIZE, MULTIPART_MAX_REQUEST_SIZE

# Swagger toggle
SWAGGER_ENABLED, SWAGGER_UI_ENABLED, GATEWAY_URL
```

## Service Ports (local dev)

- Gateway: `http://localhost:8000`
- Auth: `http://localhost:8080/api/auth-service`
- Course: `http://localhost:8081/api/course-service`
- Swagger UI (aggregated): `http://localhost:8000/swagger-ui.html`