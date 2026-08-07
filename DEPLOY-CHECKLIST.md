# Docs

## Architecture

- **Frontend** — React shop + admin UI (`frontend/`)
- **Backend** — Spring Boot REST API (`backend/`) — Neon Postgres, Stripe, Cloudinary
- **iOS** — SwiftUI client (`ios/`)
- **Repos** — UUID primary keys via `BaseEntity`

## Local run

```bash
# API
cd backend
docker compose --env-file .env up --build
# → http://127.0.0.1:8080

# Web
cd frontend
npm install
npm run dev
# → http://127.0.0.1:3000  (proxies /api → :8080 except /api/recipes)
```

Admin Hub: http://127.0.0.1:3000/admin  
(use `ADMIN_SEED_USERNAME` / `ADMIN_SEED_PASSWORD` from `backend/.env`)

## Production deploy checklist

### Backend (Cloud Run / Docker)

1. Set secrets from `backend/.env.example`:
   - `DATABASE_URL` (Neon)
   - `JWT_SECRET`
   - `STRIPE_API_KEY`, `STRIPE_WEBHOOK_SECRET`
   - `APP_FRONTEND_BASE_URL` = live shop origin (no trailing slash preferred)
   - Cloudinary keys
   - `ADMIN_SEED_*` for first admin
2. Deploy image from `backend/Dockerfile` / `scripts/deploy-cloud-run.sh`
3. Stripe Dashboard → webhook → `https://<api-host>/api/stripe/webhook`  
   Events: `checkout.session.completed`
4. Confirm success URL defaults to `{APP_FRONTEND_BASE_URL}/payment/success?session_id={CHECKOUT_SESSION_ID}`

### Frontend

1. Build: `npm run build` → `npm start` (or host `dist/` + Node `server.cjs`)
2. Env:
   - `API_PROXY_TARGET=https://<api-host>` (production Node proxy)
   - Optional `VITE_API_BASE_URL=https://<api-host>/api` if calling API directly (CORS must allow shop origin)
3. `GEMINI_API_KEY` only if Smart Recipes is enabled

### iOS

1. Point `APIConfig` base URL at production API `/api`
2. Stripe return scheme: `aurafresh://…` (see backend `.env.example`)

## Admin APIs

| Action | Endpoint |
|--------|----------|
| Login | `POST /api/auth/admins/login` |
| Restock / edit product | `PUT /api/products/update/{id}` (multipart) |
| All orders | `GET /api/orders/admin` |
| Update status | `PUT /api/orders/admin/{id}/status` `{ "status": "PROCESSING" }` |

Status values: `PENDING`, `PROCESSING`, `OUT_FOR_DELIVERY`, `COMPLETED` (or `DELIVERED`), `CANCELLED`

## Backend package map

| Concern | Location |
|---------|----------|
| Controllers | `.../controller/api/` |
| Entities | `.../entity/` |
| Auth filter | `.../filter/` |
| Services | `.../service/` |
