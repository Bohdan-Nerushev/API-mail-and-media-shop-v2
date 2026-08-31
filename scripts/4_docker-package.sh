#!/bin/bash
set -eo pipefail
source "$(dirname "${BASH_SOURCE[0]}")/utils.sh"
source "$(dirname "${BASH_SOURCE[0]}")/env_loader.sh"
export IMAGE_TAG="${IMAGE_TAG:-$(git rev-parse --short HEAD 2>/dev/null || echo "latest")}"
$SSH_CMD "rm -rf ~/mam-deployments/mail-and-media-shop"
docker build -t "${IMAGE_NAME}:${IMAGE_TAG}" .
docker save -o "${IMAGE_NAME}-${IMAGE_TAG}.tar" "${IMAGE_NAME}:${IMAGE_TAG}"
rsync -avz -e "ssh -o StrictHostKeyChecking=no" "${IMAGE_NAME}-${IMAGE_TAG}.tar" "${SSH_USER}@${SSH_HOST}:~/"
# Import image into containerd (kubeadm uses k8s.io namespace)
$SSH_CMD "sudo ctr -n k8s.io images import ~/${IMAGE_NAME}-${IMAGE_TAG}.tar"
$SSH_CMD "rm -f ~/${IMAGE_NAME}-${IMAGE_TAG}.tar"
rm "${IMAGE_NAME}-${IMAGE_TAG}.tar"
