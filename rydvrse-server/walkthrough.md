# RYDVRSE Backend — Complete Local Setup Guide

> **"Your Car. Our Driver. Your Destination."**
>
> This guide walks you through everything needed to build and run the RYDVRSE backend on your local machine — from zero to Swagger UI.

---

## Table of Contents

1. [Prerequisites](#1-prerequisites)
2. [Clone & Navigate](#2-clone--navigate)
3. [Environment Setup (.env)](#3-environment-setup-env)
4. [Start Infrastructure (Docker)](#4-start-infrastructure-docker)
5. [Build the Project](#5-build-the-project)
6. [Run the Application](#6-run-the-application)
7. [Verify Everything Works](#7-verify-everything-works)
8. [Swagger API Documentation](#8-swagger-api-documentation)
9. [Stop & Cleanup](#9-stop--cleanup)
10. [Troubleshooting](#10-troubleshooting)

---

## 1. Prerequisites

Install these before starting:

| Tool | Version | Check Command | Install |
|------|---------|---------------|---------|
| **JDK 21** | 21.x | `java -version` | `brew install openjdk@21` |
| **Maven** | 3.9+ | `mvn -version` | `brew install maven` |
| **Docker** | 20+ | `docker --version` | [Docker Desktop](https://docker.com/products/docker-desktop) |
| **Docker Compose** | v2+ | `docker compose version` | Included with Docker Desktop |
| **Git** | any | `git --version` | `brew install git` |

> [!IMPORTANT]
> **Java 25 will NOT work.** Lombok 1.18.36 is incompatible with JDK 25. You must use **JDK 21**.
>
> If you have multiple JDKs, explicitly set:
> ```bash
> export JAVA_HOME=/opt/homebrew/Cellar/openjdk@21/21.0.10/libexec/openjdk.jdk/Contents/Home
> ```
> Add this to your `~/.zshrc` to make it permanent:
> ```bash
> echo 'export JAVA_HOME=/opt/homebrew/Cellar/openjdk@21/21.0.10/libexec/openjdk.jdk/Contents/Home' >> ~/.zshrc
> source ~/.zshrc
> ```

---

## 2. Clone & Navigate

```bash
git clone git@github.com:iprincekumark/rydvrse.git
cd rydvrse/rydvrse-server
```

**All commands below should be run from the `rydvrse-server/` directory.**

---

## 3. Environment Setup (.env)

The application reads all configuration from environment variables. A working `.env` for local development is already provided.

### What to do:

```bash
# Copy the example template (if .env doesn't exist yet)
cp .env.example .env
```

### What to update in `.env` before running:

#### ✅ Required for basic local run (ALREADY SET — no changes needed):

| Variable | Default Value | Notes |
|----------|---------------|-------|
| `DB_HOST` | `localhost` | Docker PostgreSQL |
| `DB_PORT` | `5432` | Default PostgreSQL port |
| `DB_NAME` | `rydvrse` | Auto-created by Docker |
| `DB_USERNAME` | `rydvrse` | Matches docker-compose |
| `DB_PASSWORD` | `rydvrse_secret` | Matches docker-compose |
| `REDIS_HOST` | `localhost` | Docker Redis |
| `REDIS_PORT` | `6379` | Default Redis port |
| `KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` | Docker Kafka |
| `JWT_SECRET` | `rydvrse-local-dev-jwt...` | Long enough for HS256 |
| `APP_PORT` | `8080` | Application port |

> [!TIP]
> **For local development, the default `.env` works out of the box.** You don't need to change anything to run locally.

#### 🔄 Optional — update only when integrating external services:

| Variable | When Needed | How to Get |
|----------|-------------|------------|
| `SMS_ACCOUNT_SID` | Real OTP/SMS | [Twilio Console](https://console.twilio.com) → Account SID |
| `SMS_AUTH_TOKEN` | Real OTP/SMS | Twilio Console → Auth Token |
| `SMS_FROM_NUMBER` | Real OTP/SMS | Twilio → Phone Numbers → Buy a number |
| `RAZORPAY_KEY_ID` | Real payments | [Razorpay Dashboard](https://dashboard.razorpay.com) → API Keys |
| `RAZORPAY_KEY_SECRET` | Real payments | Razorpay Dashboard → API Keys |
| `FCM_PROJECT_ID` | Push notifications | [Firebase Console](https://console.firebase.google.com) → Project Settings |
| `SENDGRID_API_KEY` | Email notifications | [SendGrid](https://app.sendgrid.com) → API Keys |
| `AWS_ACCESS_KEY_ID` | S3 file uploads | [AWS Console](https://aws.amazon.com) → IAM → Access Keys |
| `AWS_SECRET_ACCESS_KEY` | S3 file uploads | Same as above |
| `AWS_S3_BUCKET` | S3 file uploads | AWS S3 → Create Bucket |

> [!NOTE]
> **For local dev**, all external services (SMS, payments, push notifications, email) are set to `mock`/disabled. The app runs perfectly without them.

#### 🚫 NEVER change these unless you also update `docker-compose.yml`:

| Variable | Must Match |
|----------|------------|
| `DB_USERNAME` | `POSTGRES_USER` in docker-compose.yml |
| `DB_PASSWORD` | `POSTGRES_PASSWORD` in docker-compose.yml |
| `DB_NAME` | `POSTGRES_DB` in docker-compose.yml |

---

## 4. Start Infrastructure (Docker)

```bash
# Make sure Docker Desktop is running, then:
docker-compose up -d
```

This starts 4 containers:

| Container | Port | Purpose |
|-----------|------|---------|
| `rydvrse-postgres` | 5432 | PostgreSQL 16 database |
| `rydvrse-redis` | 6379 | Redis 7 cache & location store |
| `rydvrse-kafka` | 9092 | Kafka event streaming |
| `rydvrse-zookeeper` | 2181 | Kafka coordination |

### Verify infrastructure is healthy:

```bash
# Check all containers are running
docker ps

# Ping PostgreSQL
docker exec rydvrse-postgres pg_isready -U rydvrse
# Expected: /var/run/postgresql:5432 - accepting connections

# Ping Redis
redis-cli ping
# Expected: PONG
```

> [!WARNING]
> **Port conflicts:** If ports 5432, 6379, or 9092 are already in use by another service, either stop that service or change the ports in `docker-compose.yml` AND `.env`.

---

## 5. Build the Project

```bash
# Set JDK 21 (critical — JDK 25 will fail)
export JAVA_HOME=/opt/homebrew/Cellar/openjdk@21/21.0.10/libexec/openjdk.jdk/Contents/Home

# Verify Java version
$JAVA_HOME/bin/java -version
# Expected: openjdk version "21.x.x"

# Build all 19 modules
mvn clean install -DskipTests
```

### Expected output:

```
RYDVRSE Platform ................................... SUCCESS
RYDVRSE Shared Kernel .............................. SUCCESS
RYDVRSE Auth Module ................................ SUCCESS
RYDVRSE User Module ................................ SUCCESS
RYDVRSE Driver Module .............................. SUCCESS
RYDVRSE Vehicle Module ............................. SUCCESS
RYDVRSE Location Module ............................ SUCCESS
RYDVRSE Trip Module ................................ SUCCESS
RYDVRSE Dispatch Module ............................ SUCCESS
RYDVRSE Pricing Module ............................. SUCCESS
RYDVRSE Payment Module ............................. SUCCESS
RYDVRSE Wallet Module .............................. SUCCESS
RYDVRSE Notification Module ........................ SUCCESS
RYDVRSE Support Module ............................. SUCCESS
RYDVRSE Operations Module .......................... SUCCESS
RYDVRSE Admin Module ............................... SUCCESS
RYDVRSE Safety Module .............................. SUCCESS
RYDVRSE Analytics Module ........................... SUCCESS
RYDVRSE Application ................................ SUCCESS
BUILD SUCCESS
```

---

## 6. Run the Application

```bash
# Option A: Run from the JAR (recommended)
export JAVA_HOME=/opt/homebrew/Cellar/openjdk@21/21.0.10/libexec/openjdk.jdk/Contents/Home
$JAVA_HOME/bin/java -jar rydvrse-app/target/rydvrse-app-1.0.0-SNAPSHOT.jar

# Option B: Run with Maven
cd rydvrse-app
mvn spring-boot:run
```

### Startup log — what to look for:

```
✓ HikariPool-1 - Start completed.              ← Database connected
✓ Initialized JPA EntityManagerFactory          ← Tables auto-created
✓ Filter 'jwtAuthenticationFilter' configured   ← Security active
✓ Tomcat started on port 8080 with context '/api' ← Server ready
✓ Started RydvrseApplication in X.XXX seconds   ← DONE!
```

---

## 7. Verify Everything Works

### Health Check:

```bash
curl http://localhost:8080/api/actuator/health
# Expected: {"status":"UP"}
```

### Test OTP Endpoint:

```bash
curl -X POST http://localhost:8080/api/v1/auth/otp/send \
  -H "Content-Type: application/json" \
  -d '{"phoneNumber":"+919876543210"}'
```

Expected response:
```json
{
  "success": true,
  "message": "OTP sent",
  "data": {
    "phoneNumber": "+919876543210",
    "message": "OTP sent successfully",
    "expiresInSeconds": 300
  }
}
```

### Open Swagger UI in browser:

```bash
open http://localhost:8080/api/swagger-ui.html
```

---

## 8. Swagger API Documentation

| Resource | URL |
|----------|-----|
| **Swagger UI** | http://localhost:8080/api/swagger-ui.html |
| **OpenAPI JSON** | http://localhost:8080/api/v3/api-docs |
| **Health Check** | http://localhost:8080/api/actuator/health |
| **Metrics** | http://localhost:8080/api/actuator/metrics |

### API Modules:

| Tag | Endpoints | Description |
|-----|-----------|-------------|
| **Auth** | `/v1/auth/*` | OTP send/verify, token refresh, logout |
| **Customers** | `/v1/customers/*` | Profile CRUD, saved locations |
| **Drivers** | `/v1/drivers/*` | Onboarding, availability, documents |
| **Trips** | `/v1/trips/*` | Create → assign → start → complete → rate |
| **Locations** | `/v1/locations/*` | GPS updates, nearby drivers |
| **Pricing** | `/v1/pricing/*` | Fare estimation with surge |
| **Payments** | `/v1/payments/*` | Processing, history, refunds |
| **Wallet** | `/v1/wallet/*` | Balance, transactions, payouts |
| **Support** | `/v1/support/*` | Ticket management |
| **Safety** | `/v1/safety/*` | Emergency SOS, incident reports |
| **Admin** | `/v1/admin/*` | Dashboard stats, driver approval/rejection |

### How to authenticate in Swagger:

1. Call `POST /v1/auth/otp/send` with a phone number
2. Check server logs for the OTP (printed in console in dev mode)
3. Call `POST /v1/auth/otp/verify` with the OTP to get a JWT token
4. Click **"Authorize"** button in Swagger → paste the token → click **"Authorize"**
5. All subsequent requests will include the JWT automatically

---

## 9. Stop & Cleanup

```bash
# Stop the Spring Boot app
Ctrl+C

# Stop Docker containers (keeps data)
docker-compose stop

# Stop and remove containers + data (fresh start)
docker-compose down -v
```

---

## 10. Troubleshooting

### `Lombok annotation processing error` / `TypeTag :: UNKNOWN`

**Cause:** You're using JDK 25. Lombok 1.18.36 doesn't support it.

**Fix:**
```bash
export JAVA_HOME=/opt/homebrew/Cellar/openjdk@21/21.0.10/libexec/openjdk.jdk/Contents/Home
mvn clean install -DskipTests
```

### `Port 5432 already in use`

**Cause:** Another PostgreSQL instance is running.

**Fix:**
```bash
# Find what's using the port
lsof -i :5432
# Kill it or change the port in docker-compose.yml and .env
```

### `Connection refused` to database

**Cause:** Docker containers aren't running.

**Fix:**
```bash
docker-compose up -d
docker ps  # verify all 4 containers show "Up"
```

### `BUILD FAILURE — Could not resolve dependencies`

**Cause:** Running `mvn -rf :module` without building parent first.

**Fix:** Always build from root:
```bash
cd /path/to/rydvrse/rydvrse-server
mvn clean install -DskipTests
```

### `double cannot be dereferenced` in PaymentService

**Status:** Already fixed. If you see this, pull latest code.

---

## Quick Reference — One-Liner Setup

```bash
# Full setup from scratch (copy-paste all at once):
cd rydvrse/rydvrse-server
cp .env.example .env
docker-compose up -d
export JAVA_HOME=/opt/homebrew/Cellar/openjdk@21/21.0.10/libexec/openjdk.jdk/Contents/Home
mvn clean install -DskipTests
$JAVA_HOME/bin/java -jar rydvrse-app/target/rydvrse-app-1.0.0-SNAPSHOT.jar
```

Then open: http://localhost:8080/api/swagger-ui.html 🚀
