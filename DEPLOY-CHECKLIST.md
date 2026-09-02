# Backend deploy checklist

Use with [DEPLOY.md](DEPLOY.md).

## Architecture

- Client shop → `frontend/` (Vercel)
- Admin → `admin/` (Vercel)
- API → `backend/` (Cloud Run)

## Before deploy

1. Secrets from `.env.example`:
   - `DATABASE_URL` (Neon)
   - `JWT_SECRET`
   - `STRIPE_API_KEY`, `STRIPE_WEBHOOK_SECRET`
   - `APP_FRONTEND_BASE_URL` = Vercel **client** URL
   - `APP_ADMIN_BASE_URL` = Vercel **admin** URL
   - Cloudinary + `ADMIN_SEED_*`
2. Deploy API (`Dockerfile` / `scripts/deploy-cloud-run.sh`)
3. Stripe webhook → `https://<api-host>/api/stripe/webhook` (`checkout.session.completed`)
4. Deploy `frontend` and `admin` on Vercel with `API_PROXY_TARGET` / `VITE_API_BASE_URL` pointing at the API host
