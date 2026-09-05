#!/bin/bash
set -eo pipefail
source "$(dirname "${BASH_SOURCE[0]}")/utils.sh"
source "$(dirname "${BASH_SOURCE[0]}")/env_loader.sh"
ENV_NAME="${DEV_ENV_NAME}"
NAMESPACE="${DEV_NAMESPACE}"
RELEASE_NAME="${DEV_RELEASE_NAME}"
JOB_SUFFIX="${KEYCLOAK_SETUP_JOB_SUFFIX:-mail-and-media-shop-keycloak-setup}"
JOB_TIMEOUT="${DEPLOY_TIMEOUT:-600s}"

if [ -z "${ENV_NAME}" ] || [ -z "${NAMESPACE}" ] || [ -z "${RELEASE_NAME}" ]; then
    error_exit "DEV_ENV_NAME, DEV_NAMESPACE, or DEV_RELEASE_NAME is missing in .env!"
fi

export IMAGE_TAG="${IMAGE_TAG:-$(git rev-parse --short HEAD 2>/dev/null || echo "latest")}"
TERRAFORM_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../terraform" && pwd)"

# Rollback function on failure
rollback_on_failure() {
    local exit_code=$?
    log_error "Deployment to '${ENV_NAME}' failed (Exit code: ${exit_code})! Initiating rollback..."
    
    kubectl logs -n "${NAMESPACE}" "job/${RELEASE_NAME}-${JOB_SUFFIX}" --tail=50 2>/dev/null || true
    
    if helm status "${RELEASE_NAME}" -n "${NAMESPACE}" &>/dev/null; then
        local current_status
        current_status=$(helm status "${RELEASE_NAME}" -n "${NAMESPACE}" -o json 2>/dev/null | jq -r '.info.status' || echo "unknown")
        
        if [ "$current_status" = "pending-install" ]; then
            log_warn "Release '${RELEASE_NAME}' is stuck in 'pending-install'. Uninstalling to restore clean state..."
            helm uninstall "${RELEASE_NAME}" -n "${NAMESPACE}" || true
        else
            log_info "Attempting Helm rollback for '${RELEASE_NAME}' in namespace '${NAMESPACE}'..."
            helm rollback "${RELEASE_NAME}" 0 -n "${NAMESPACE}" --wait || {
                log_warn "Helm rollback failed or no previous revision found. Re-executing cleanup..."
                helm uninstall "${RELEASE_NAME}" -n "${NAMESPACE}" || true
            }
        fi
    fi
    
    error_exit "Rollback completed. Environment '${ENV_NAME}' reverted to safe state."
}

trap 'rollback_on_failure' ERR

# Pre-flight check for stuck pending releases
if helm status "${RELEASE_NAME}" -n "${NAMESPACE}" &>/dev/null; then
    STATUS=$(helm status "${RELEASE_NAME}" -n "${NAMESPACE}" -o json 2>/dev/null | jq -r '.info.status' || echo "unknown")
    if [[ "$STATUS" == pending-* ]]; then
        log_warn "Detected stuck Helm status '${STATUS}'. Cleaning up before deployment..."
        helm uninstall "${RELEASE_NAME}" -n "${NAMESPACE}" || true
    fi
fi

log_info "Deploying '${ENV_NAME}' environment via Terraform..."
cd "$TERRAFORM_DIR"
export TF_LOG="${TF_LOG:-INFO}"

# Launch background pod watch during terraform apply
( kubectl get pods -n "${NAMESPACE}" -w 2>/dev/null ) &
KUBE_WATCH_PID=$!

terraform init -input=false
terraform apply -auto-approve -input=false \
  -var="environment=${ENV_NAME}" \
  -var="namespace=${NAMESPACE}" \
  -var="release_name=${RELEASE_NAME}" \
  -var="image_tag=${IMAGE_TAG}"

# Stop background pod watch after terraform apply completes
kill $KUBE_WATCH_PID 2>/dev/null || true

log_info "Verifying Keycloak configuration job completion in namespace '${NAMESPACE}'..."
kubectl wait --for=condition=complete "job/${RELEASE_NAME}-${JOB_SUFFIX}" -n "${NAMESPACE}" --timeout="${JOB_TIMEOUT}"

log_info "Deployment to '${ENV_NAME}' completed successfully!"
