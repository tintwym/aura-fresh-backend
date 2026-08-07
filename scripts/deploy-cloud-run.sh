#!/usr/bin/env bash
set -euo pipefail

PROJECT_ID="${GCP_PROJECT_ID:-$(gcloud config get-value project 2>/dev/null)}"
REGION="${GCP_REGION:-asia-southeast1}"
SERVICE="${CLOUD_RUN_SERVICE:-shopping-cart-backend}"

if [[ -z "$PROJECT_ID" || "$PROJECT_ID" == "(unset)" ]]; then
  echo "ERROR: Set GCP_PROJECT_ID or run: gcloud config set project YOUR_PROJECT_ID"
  exit 1
fi

echo "Project:  $PROJECT_ID"
echo "Region:   $REGION"
echo "Service:  $SERVICE"

gcloud services enable run.googleapis.com cloudbuild.googleapis.com --project "$PROJECT_ID"

DEPLOY_ARGS=(
  run deploy "$SERVICE"
  --source .
  --region "$REGION"
  --project "$PROJECT_ID"
  --allow-unauthenticated
  --memory 512Mi
  --port 8080
)

if [[ ! -f .env.cloudrun && -f .env ]]; then
  echo "Generating .env.cloudrun from .env (not committed to git)..."
  : > .env.cloudrun
  while IFS= read -r line || [[ -n "$line" ]]; do
    [[ -z "$line" || "$line" =~ ^[[:space:]]*# ]] && continue
    key="${line%%=*}"
    value="${line#*=}"
    value="${value%\"}"
    value="${value#\"}"
    value="${value%\'}"
    value="${value#\'}"
    printf '%s: "%s"\n' "$key" "$value" >> .env.cloudrun
  done < .env
fi

if [[ -f .env.cloudrun ]]; then
  echo "Using env vars from .env.cloudrun"
  DEPLOY_ARGS+=(--env-vars-file .env.cloudrun)
else
  echo "ERROR: No .env.cloudrun or .env found."
  echo "Create .env with DATABASE_URL, JWT_SECRET, etc., then redeploy."
  exit 1
fi

gcloud "${DEPLOY_ARGS[@]}"

echo ""
echo "Done. Copy the service URL above into the frontend API_BASE_URL / env config."
