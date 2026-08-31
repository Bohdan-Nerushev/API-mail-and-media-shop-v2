#!/bin/bash
set -eo pipefail
source "$(dirname "${BASH_SOURCE[0]}")/utils.sh"
source "$(dirname "${BASH_SOURCE[0]}")/env_loader.sh"
export IMAGE_TAG="${IMAGE_TAG:-$(git rev-parse --short HEAD 2>/dev/null || echo "latest")}"
NODES="${CLUSTER_NODES:-${SSH_USER}@${SSH_HOST}}"

$SSH_CMD "rm -rf ~/mam-deployments/mail-and-media-shop"

log_info "Building Docker image ${IMAGE_NAME}:${IMAGE_TAG}..."
docker build -t "${IMAGE_NAME}:${IMAGE_TAG}" .

log_info "Saving Docker image archive ${IMAGE_NAME}-${IMAGE_TAG}.tar..."
docker save -o "${IMAGE_NAME}-${IMAGE_TAG}.tar" "${IMAGE_NAME}:${IMAGE_TAG}"

for NODE in $NODES; do
    log_info "Distributing image to node: ${NODE}..."
    rsync -avz -e "ssh -o StrictHostKeyChecking=no" "${IMAGE_NAME}-${IMAGE_TAG}.tar" "${NODE}:~/"
    
    log_info "Importing image into containerd on node: ${NODE}..."
    ssh -t -o StrictHostKeyChecking=no "${NODE}" "sudo ctr -n k8s.io images import ~/${IMAGE_NAME}-${IMAGE_TAG}.tar && rm -f ~/${IMAGE_NAME}-${IMAGE_TAG}.tar"
done

log_info "Cleaning up local image archive..."
rm -f "${IMAGE_NAME}-${IMAGE_TAG}.tar"

log_info "Image ${IMAGE_NAME}:${IMAGE_TAG} successfully deployed to all cluster nodes!"
