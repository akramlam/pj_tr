# Binome Matcher

This project provides a Spring Boot 3 backend (multi‐module) for matching student binômes based on skills, profiles, and messaging.

## Modules
* `core` – JPA entities (User, Profile, Message)
* `security` – JWT authentication filter + token provider
* `api` – Spring Boot application (controllers, services, repositories)

## Running for Development

### Prerequisites
* Java 17
* Gradle 7.6+

### 1) Run Locally without Docker Builds
```bash
# From project root, launch the API directly (auto‑reload on code changes requires additional setup)
cd api
./gradlew bootRun
```
The backend will start on http://localhost:8080 using your local PostgreSQL or configure `application.properties`.

### 2) Using Docker Compose
```bash
docker compose up --build
```
This builds the Docker image (slow on first run), then starts the API and PostgreSQL.

To avoid rebuilding images on every code change, prefer the [Run Locally](#1-run-locally-without-docker-builds) method during active development.

## API Endpoints
* `POST /api/auth/register` – Register user, returns JWT
* `POST /api/auth/login` – Login, returns JWT
* `POST /api/profile` – Create/update profile (authenticated)
* `GET /api/profile` – Get current user profile
* `GET /api/match?limit=10` – Find matching binômes by shared skills
* `POST /api/messages` – Send a message (body: `{recipient, content}`)
* `GET /api/messages?user=otherUsername` – Get conversation with another user

Include `Authorization: Bearer <token>` header for all authenticated calls.