#!/bin/bash
set -eo pipefail
source "$(dirname "${BASH_SOURCE[0]}")/utils.sh"
source "$(dirname "${BASH_SOURCE[0]}")/env_loader.sh"
export IMAGE_TAG="${IMAGE_TAG:-$(git rev-parse --short HEAD 2>/dev/null || echo "latest")}"
log_info "Preparing deployment directory on target host..."
$SSH_CMD "mkdir -p ~/mam-deployments"

log_info "Transferring Helm chart and Terraform configuration..."
$SCP_CMD

log_info "Deploying 'dev' environment via Terraform..."
$SSH_CMD "cd ~/mam-deployments/terraform && terraform init -input=false && terraform apply -auto-approve -input=false -var=\"environment=dev\" -var=\"namespace=mam-dev\" -var=\"release_name=mam-dev\" -var=\"image_tag=${IMAGE_TAG}\""

log_info "Verifying Keycloak configuration job completion in namespace 'mam-dev'..."
$SSH_CMD "kubectl wait --for=condition=complete job/mam-dev-keycloak-setup -n mam-dev --timeout=180s || (kubectl logs -n mam-dev job/mam-dev-keycloak-setup && exit 1)"

log_info "Deployment to 'dev' completed successfully!"
