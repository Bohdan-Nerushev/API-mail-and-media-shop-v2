#!/bin/bash
# ==============================================================================
# Script to upload GitHub Secrets from local .env via GitHub CLI (gh)
# ==============================================================================
set -e

ENV_FILE=".env"
if [ -n "$PROJECT_ROOT" ] && [ -f "$PROJECT_ROOT/.env" ]; then
    ENV_FILE="$PROJECT_ROOT/.env"
fi

if [ ! -f "$ENV_FILE" ]; then
    echo "ERROR: .env file not found ($ENV_FILE). Please create a .env file before running this script."
    exit 1
fi

# Check if GitHub CLI is installed
if ! command -v gh &> /dev/null; then
    echo "ERROR: GitHub CLI ('gh') is not installed."
    echo "Please install it from https://cli.github.com/ and login using 'gh auth login'."
    exit 1
fi

# Check if authenticated
if ! gh auth status &>/dev/null; then
    echo "ERROR: You are not authenticated with GitHub CLI."
    echo "Please run 'gh auth login' to authenticate."
    exit 1
fi

echo "Reading secrets from: $ENV_FILE..."

# List of secrets we actually want to upload (excluding monitoring/observability/etc.)
# matching the github_secrets_list.md
ALLOWED_KEYS=(
  "APP_PORT" "APP_HOST" "APP_IMAGE_NAME" "APP_CONTAINER_NAME" "IMAGE_NAME"
  "POSTGRES_IMAGE_VERSION" "SHOP_DB_CONTAINER_NAME" "SHOP_DB_NAME" "SHOP_DB_USER" "SHOP_DB_PASSWORD" "SHOP_DB_PORT"
  "SHOP_REDIS_CONTAINER_NAME" "REDIS_PORT"
  "KEYCLOAK_PORT" "KEYCLOAK_HTTPS_PORT" "KC_URL" "KC_REALM" "KC_BOOTSTRAP_ADMIN_USERNAME" "KC_BOOTSTRAP_ADMIN_PASSWORD"
  "KC_ADMIN_USER" "KC_ADMIN_PASS" "KC_CLIENT_ID" "KC_CLIENT_SECRET" "KC_GRANT_TYPE" "KC_USERNAME" "KC_PASSWORD"
  "KEYCLOAK_DB_IMAGE_VERSION" "KEYCLOAK_DB_CONTAINER_NAME" "KEYCLOAK_DB_NAME" "KEYCLOAK_DB_USER" "KEYCLOAK_DB_PASSWORD" "KEYCLOAK_DB_PORT"
  "JWT_ISSUER" "JWT_EXPIRATION_MS" "JWT_EXPECTED_AUDIENCE" "TRUSTSTORE_PASSWORD"
  "USER_NAME" "USER_EMAIL" "USER_PASSWORD"
  "SSH_USER" "SSH_HOST" "SSH_PRIVATE_KEY"
)

# Helper to check if array contains element
contains_element() {
  local e match="$1"
  shift
  for e; do [[ "$e" == "$match" ]] && return 0; done
  return 1
}

while IFS= read -r line || [ -n "$line" ]; do
    # Strip leading/trailing whitespace
    line=$(echo "$line" | xargs)

    # Skip comments and empty lines
    [[ "$line" =~ ^#.*$ ]] && continue
    [[ -z "$line" ]] && continue
    [[ "$line" != *"="* ]] && continue

    KEY=$(echo "$line" | cut -d'=' -f1 | xargs)
    VALUE=$(echo "$line" | cut -d'=' -f2- | sed -e 's/^"//' -e 's/"$//' -e "s/^'//" -e "s/'$//")

    # Only upload keys that are allowed (to avoid uploading local Vault details, Sentry etc. if they are not needed)
    if contains_element "$KEY" "${ALLOWED_KEYS[@]}"; then
        if [ -z "$VALUE" ]; then
            echo "Skipping $KEY (value is empty)"
            continue
        fi

        echo -n "Uploading secret $KEY... "
        # Run gh secret set
        echo "$VALUE" | gh secret set "$KEY"
        echo "✅ Done"
    fi
done < "$ENV_FILE"

echo "All allowed secrets uploaded successfully to GitHub!"
