# Production deployment (Google Cloud Run)

Use this after pushing this repo to GitHub. **Redeploy** when env vars change.

## Repository layout

| Path | Role |
|------|------|
| `LICENSE`, `README.md`, `DEPLOY.md` | Repo docs |
| `Dockerfile`, `.dockerignore` | API Docker image |
| `scripts/deploy-cloud-run.sh` | One-command deploy helper |

The Aura Fresh frontend lives in `aura_fresh_frontend/` — set `APP_FRONTEND_BASE_URL` to your deployed frontend URL.

Database: **[Neon](https://neon.tech)** PostgreSQL (external — not hosted on Cloud Run).

## Prerequisites

1. [Google Cloud](https://cloud.google.com) account (free tier covers small hobby traffic).
2. Install [Google Cloud CLI](https://cloud.google.com/sdk/docs/install): `gcloud`
3. Create or select a project:

```bash
gcloud auth login
gcloud config set project YOUR_PROJECT_ID
gcloud services enable run.googleapis.com cloudbuild.googleapis.com artifactregistry.googleapis.com
```

## Deploy API to Cloud Run

From the **repo root** (`aura_fresh_backend/`):

```bash
export GCP_PROJECT_ID=your-gcp-project-id
export GCP_REGION=asia-southeast1   # or your preferred region

./scripts/deploy-cloud-run.sh
```

The script builds the Docker image and deploys to Cloud Run using your local `.env` (auto-converted to `.env.cloudrun`).

Copy the service URL from the output (e.g. `https://shopping-cart-backend-860670068354.asia-southeast1.run.app`).

## Environment variables (Cloud Run)

**Cloud Console:** Cloud Run → your service → **Edit & deploy new revision** → **Variables & secrets**

| Variable | Example / notes |
|----------|-----------------|
| `DATABASE_URL` | Neon `postgresql://…?sslmode=require` |
| `JWT_SECRET` | `openssl rand -base64 32` |
| `STRIPE_API_KEY` | `sk_test_…` or live key |
| `STRIPE_WEBHOOK_SECRET` | From Stripe → Webhooks (`whsec_…`) |
| `APP_FRONTEND_BASE_URL` | `https://your-app.vercel.app` |
| `CLOUDINARY_CLOUD_NAME` | Cloudinary cloud name |
| `CLOUDINARY_API_KEY` | API key |
| `CLOUDINARY_API_SECRET` | API secret |
| `ADMIN_SEED_USERNAME` | Optional first admin |
| `ADMIN_SEED_PASSWORD` | Optional admin password |

Cloud Run sets `PORT` automatically — do not hardcode it.

**Deploy with env vars from a local file** (not committed):

The deploy script auto-generates `.env.cloudrun` from `.env`. Format if you create it manually:

```yaml
DATABASE_URL: postgresql://user:pass@host/db?sslmode=require
JWT_SECRET: your-secret
STRIPE_API_KEY: sk_test_xxx
APP_FRONTEND_BASE_URL: https://your-app.vercel.app
```

## Frontend (Aura Fresh)

After deploy, point the frontend at this API:

1. Set `API_BASE_URL` (or your frontend env equivalent) to the Cloud Run URL + `/api`
2. Set `APP_FRONTEND_BASE_URL` on Cloud Run to your frontend origin
3. Redeploy the frontend if needed

## Stripe webhook

1. Stripe Dashboard → Developers → Webhooks → Add endpoint  
2. URL: `https://shopping-cart-backend-860670068354.asia-southeast1.run.app/api/stripe/webhook`  
3. Event: `checkout.session.completed`  
4. Copy signing secret → `STRIPE_WEBHOOK_SECRET` on Cloud Run → deploy new revision  

## Verify API

```bash
curl https://shopping-cart-backend-860670068354.asia-southeast1.run.app/actuator/health
curl https://shopping-cart-backend-860670068354.asia-southeast1.run.app/api/products/index
```

Expect JSON from `/api/products/index`, not HTML.

## Admin catalog

1. Set `ADMIN_SEED_USERNAME` / `ADMIN_SEED_PASSWORD` once, or promote a user in Neon:

   ```sql
   UPDATE users SET role_id = (SELECT id FROM roles WHERE name = 'Admin') WHERE username = 'youruser';
   ```

2. Sign in on the app → **Me** → **Manage products**

## Troubleshooting

| Symptom | Fix |
|---------|-----|
| CORS error | `APP_FRONTEND_BASE_URL` must match your Vercel URL |
| `DATABASE_URL is not set` on startup | Add `DATABASE_URL` in Cloud Run variables |
| 403 on API URL | Deploy with `--allow-unauthenticated` |
| Cold start slow | Normal on free tier after idle; retry after a few seconds |
| Build fails | Run `docker build .` locally; ensure repo root has `Dockerfile` |
| Container failed to start / PORT 8080 timeout | Usually **missing env vars** — check logs; ensure `DATABASE_URL` is set (see below) |
| `PERMISSION_DENIED` / default service account missing IAM | See [Fix Cloud Build IAM](#fix-cloud-build-iam) below |

### Fix Cloud Build IAM

If deploy fails with `PERMISSION_DENIED` and mentions  
`860670068354-compute@developer.gserviceaccount.com`, grant roles to the **default Compute Engine service account** (replace project ID/number if yours differ):

```bash
export GCP_PROJECT_ID=shopping-cart-501808
export GCP_PROJECT_NUMBER=860670068354
SA="${GCP_PROJECT_NUMBER}-compute@developer.gserviceaccount.com"

for ROLE in \
  roles/cloudbuild.builds.builder \
  roles/storage.objectViewer \
  roles/artifactregistry.writer \
  roles/run.admin \
  roles/iam.serviceAccountUser; do
  gcloud projects add-iam-policy-binding "$GCP_PROJECT_ID" \
    --member="serviceAccount:${SA}" \
    --role="$ROLE"
done
```

Wait ~1 minute, then run `./scripts/deploy-cloud-run.sh` again.
