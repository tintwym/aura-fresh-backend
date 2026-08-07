# Aura Fresh backend

Spring Boot **REST API** for Aura Fresh. Runs with **Docker** against **Neon PostgreSQL** (no local Postgres container).

## Requirements

- [Docker Desktop](https://www.docker.com/products/docker-desktop/) (or Docker Engine + Compose)
- A [Neon](https://neon.tech) PostgreSQL database (free tier works)

## Configuration

1. Create a Neon project and copy the connection string from **Connect** (`postgresql://…?sslmode=require`).
2. Copy the env template and fill in values:

```bash
cp .env.example .env
# Edit .env — set DATABASE_URL to your Neon URL, plus JWT_SECRET and other keys
```

| Variable | Purpose |
|----------|---------|
| `DATABASE_URL` | Neon connection string (`postgresql://…?sslmode=require`) — **required** |
| `JWT_SECRET` | JWT signing key (32+ chars) — **required** |
| `STRIPE_API_KEY` | Stripe secret key |
| `STRIPE_WEBHOOK_SECRET` | Stripe webhook signing secret |
| `APP_FRONTEND_BASE_URL` | Aura Fresh frontend origin (default `http://localhost:3000`) |
| `CLOUDINARY_*` | Product image uploads |
| `ADMIN_SEED_USERNAME` / `ADMIN_SEED_PASSWORD` | Optional first admin on startup |

## Run with Docker + Neon

```bash
docker compose --env-file .env up --build
```

API base: **http://localhost:8080/api**  
Health: **http://localhost:8080/actuator/health**

Stop:

```bash
docker compose down
```

## Optional: Maven (local JVM)

Use this only if you have Java 21 installed and prefer hot-reload:

```bash
export $(grep -v '^#' .env | xargs)
./mvnw spring-boot:run
```

## Google Cloud Run deploy

```bash
export GCP_PROJECT_ID=your-project-id
./scripts/deploy-cloud-run.sh
```

See **[DEPLOY.md](DEPLOY.md)** for env vars, IAM, and Stripe webhook setup.

## Tests

```bash
./mvnw test
```

Integration tests use **Testcontainers** with PostgreSQL 16.

## Tech stack

- Spring Boot 4.1 (Web, Data JPA, Actuator)
- Docker + Neon PostgreSQL
- JWT (jjwt), Stripe Java SDK, Cloudinary, Lombok

## License

MIT — see [LICENSE](LICENSE).
