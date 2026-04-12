# Rydvrse Server

Spring Boot backend for the Rydvrse MVP.

This service is implemented as a modular monolith and is responsible for:

- authentication and session flows
- customer profile and booking flows
- pricing and quote generation
- driver onboarding, offers, and trip execution
- payments, refunds, and invoices
- support, incidents, admin operations, and audit visibility

## Tech Stack

- Java `21`
- Spring Boot `3.5`
- Maven
- PostgreSQL + PostGIS
- Flyway
- Redis
- Spring Security + JWT
- OpenAPI / Swagger

## Prerequisites

Install these before running locally:

- Java `21+`
- Maven `3.9+`
- Docker Desktop or compatible Docker engine

Optional but useful:

- `psql` for local DB checks
- IntelliJ IDEA or VS Code with Java support

## Project Structure

- [pom.xml](/Users/prince/Documents/CoDiN/Princevrse/STARTUP/wORk/rydvrse/rydvrse-server/pom.xml): Maven build
- [docker-compose.yml](/Users/prince/Documents/CoDiN/Princevrse/STARTUP/wORk/rydvrse/rydvrse-server/docker-compose.yml): local PostgreSQL and Redis
- [src/main/resources/application.yml](/Users/prince/Documents/CoDiN/Princevrse/STARTUP/wORk/rydvrse/rydvrse-server/src/main/resources/application.yml): app configuration defaults
- [Backend_Implementation_Guide.md](/Users/prince/Documents/CoDiN/Princevrse/STARTUP/wORk/rydvrse/rydvrse-server/Backend_Implementation_Guide.md): implementation overview

## Local Defaults

The backend is configured to work locally out of the box with the provided Docker setup.

- PostgreSQL host: `localhost`
- PostgreSQL host port: `5433`
- PostgreSQL database: `rydvrse`
- PostgreSQL username: `rydvrse`
- PostgreSQL password: `rydvrse_secret`
- Redis host: `localhost`
- Redis port: `6379`
- App port: `8080`
- Default OTP code: `123456`

## Start Local Dependencies

From [rydvrse-server](/Users/prince/Documents/CoDiN/Princevrse/STARTUP/wORk/rydvrse/rydvrse-server):

```bash
docker compose up -d postgres redis
```

To stop them later:

```bash
docker compose down
```

To stop them and remove local volumes:

```bash
docker compose down -v
```

Use the volume removal only if you intentionally want a fresh local database.

## Run the Backend

From [rydvrse-server](/Users/prince/Documents/CoDiN/Princevrse/STARTUP/wORk/rydvrse/rydvrse-server):

```bash
mvn -q -Dmaven.repo.local=/tmp/rydvrse-m2 spring-boot:run
```

If port `8080` is already in use, run on another port:

```bash
PORT=8084 mvn -q -Dmaven.repo.local=/tmp/rydvrse-m2 spring-boot:run
```

## Verify the Service

Default URLs:

- Health: [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)
- OpenAPI JSON: [http://localhost:8080/api/docs](http://localhost:8080/api/docs)
- Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

If you start on another port, replace `8080` accordingly.

Quick health check:

```bash
curl -s http://localhost:8080/actuator/health
```

Expected result:

```json
{"status":"UP"}
```

## Run Tests

From [rydvrse-server](/Users/prince/Documents/CoDiN/Princevrse/STARTUP/wORk/rydvrse/rydvrse-server):

```bash
mvn -q -Dmaven.repo.local=/tmp/rydvrse-m2 test
```

## Configuration Overrides

The app reads environment variables, with safe local defaults already defined in [application.yml](/Users/prince/Documents/CoDiN/Princevrse/STARTUP/wORk/rydvrse/rydvrse-server/src/main/resources/application.yml).

Most relevant variables:

- `PORT`
- `RYDVRSE_DB_URL`
- `RYDVRSE_DB_USERNAME`
- `RYDVRSE_DB_PASSWORD`
- `RYDVRSE_REDIS_HOST`
- `RYDVRSE_REDIS_PORT`
- `RYDVRSE_JWT_SECRET`
- `RYDVRSE_ADMIN_EMAIL`
- `RYDVRSE_ADMIN_PASSWORD`
- `RYDVRSE_DEFAULT_OTP_CODE`
- `RYDVRSE_PAYMENTS_SANDBOX_AUTO_CAPTURE`
- `RYDVRSE_PAYMENTS_WEBHOOK_SECRET`
- `RYDVRSE_JOBS_ENABLED`

Example custom run:

```bash
PORT=8084 \
RYDVRSE_JWT_SECRET=local-dev-secret-change-this \
RYDVRSE_ADMIN_EMAIL=admin@local.rydvrse \
RYDVRSE_ADMIN_PASSWORD=ChangeMe123! \
mvn -q -Dmaven.repo.local=/tmp/rydvrse-m2 spring-boot:run
```

## Local Development Workflow

Recommended order:

1. Start PostgreSQL and Redis with Docker Compose.
2. Run the backend with Maven.
3. Check the health endpoint.
4. Open Swagger UI and verify routes.
5. Run the test suite before committing changes.

## Common Troubleshooting

### Port `5433` already in use

Another PostgreSQL instance is likely using that port. Either stop the conflicting service or override the DB URL and Docker mapping.

### Port `8080` already in use

Run the app with another port:

```bash
PORT=8084 mvn -q -Dmaven.repo.local=/tmp/rydvrse-m2 spring-boot:run
```

### Docker containers already exist with stale state

If local containers are stuck, stop them first:

```bash
docker stop rydvrse-postgres rydvrse-redis
```

Then start again with Compose:

```bash
docker compose up -d postgres redis
```

### Database migration failure

Check:

- PostgreSQL is actually running on `5433`
- credentials match the defaults
- the existing database is not from an incompatible older schema state

### Redis connection failure

Check that Redis is running on `6379`:

```bash
docker compose ps
```

## Notes

- The schema expects PostgreSQL with PostGIS support.
- Flyway runs automatically on startup.
- JPA `ddl-auto` is `validate`, so schema drift is surfaced instead of silently patched.
- Redis is used as infrastructure support; Spring Data Redis repositories are intentionally disabled.
- For production-like environments, always override secrets and do not rely on local defaults.
