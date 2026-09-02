# Backend API (`backend/`)

Spring Boot REST API for Aura Fresh (Neon Postgres, Stripe, Cloudinary).

**UIs:** `../frontend` (client), `../admin` (staff dashboard)

## Run locally

```bash
cp .env.example .env   # DATABASE_URL, JWT_SECRET, …
./mvnw spring-boot:run
```

→ http://localhost:8080/api

## Config

| Variable | Purpose |
|----------|---------|
| `DATABASE_URL` | Neon `postgresql://…?sslmode=require` |
| `JWT_SECRET` | 32+ chars |
| `APP_FRONTEND_BASE_URL` | Client origin (Stripe + CORS) |
| `APP_ADMIN_BASE_URL` | Admin origin (CORS) |
| `ADMIN_SEED_USERNAME` / `ADMIN_SEED_PASSWORD` | First admin account |
| `STRIPE_*` / `CLOUDINARY_*` | Payments / images |

Production API: [DEPLOY.md](DEPLOY.md) (Cloud Run).
