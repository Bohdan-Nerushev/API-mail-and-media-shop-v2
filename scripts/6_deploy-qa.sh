#!/bin/bash
set -eo pipefail
source "$(dirname "${BASH_SOURCE[0]}")/utils.sh"
source "$(dirname "${BASH_SOURCE[0]}")/env_loader.sh"
export IMAGE_TAG="${IMAGE_TAG:-$(git rev-parse --short HEAD 2>/dev/null || echo "latest")}"
log_info "Preparing deployment directory on target host..."
$SSH_CMD "mkdir -p ~/mam-deployments"

log_info "Transferring Helm chart and Terraform configuration..."
$SCP_CMD

log_info "Deploying 'qa' environment via Terraform..."
$SSH_CMD "cd ~/mam-deployments/terraform && terraform init -input=false && terraform apply -auto-approve -input=false -var=\"environment=qa\" -var=\"namespace=mam-qa\" -var=\"release_name=mam-qa\" -var=\"image_tag=${IMAGE_TAG}\""

log_info "Verifying Keycloak configuration job completion in namespace 'mam-qa'..."
$SSH_CMD "kubectl wait --for=condition=complete job/mam-qa-keycloak-setup -n mam-qa --timeout=180s || (kubectl logs -n mam-qa job/mam-qa-keycloak-setup && exit 1)"

log_info "Deployment to 'qa' completed successfully!"
