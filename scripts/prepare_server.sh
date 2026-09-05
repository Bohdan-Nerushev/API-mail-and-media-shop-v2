#!/bin/bash

set -euo pipefail

export DEBIAN_FRONTEND=noninteractive

echo "Updating package lists..."
sudo apt-get update

echo "Upgrading system packages..."
sudo apt-get upgrade -y

echo "Installing essential tools..."
sudo apt-get install -y \
  curl wget git unzip htop iotop net-tools \
  ca-certificates gnupg lsb-release \
  software-properties-common apt-transport-https \
  jq bash-completion tree

if ! command -v docker &>/dev/null; then
  echo "Installing Docker Engine..."
  curl -fsSL https://get.docker.com | sudo sh
fi

if ! groups bnerushev | grep -q docker; then
  echo "Adding bnerushev to docker group..."
  sudo usermod -aG docker bnerushev
fi

if ! command -v kubeadm &>/dev/null; then
  echo "Installing kubeadm, kubelet, and kubectl..."
  sudo mkdir -p /etc/apt/keyrings
  curl -fsSL https://pkgs.k8s.io/core:/stable:/v1.31/deb/Release.key | sudo gpg --dearmor -o /etc/apt/keyrings/kubernetes-apt-keyring.gpg
  echo "deb [signed-by=/etc/apt/keyrings/kubernetes-apt-keyring.gpg] https://pkgs.k8s.io/core:/stable:/v1.31/deb/ /" | sudo tee /etc/apt/sources.list.d/kubernetes.list
  sudo apt-get update
  sudo apt-get install -y kubelet kubeadm kubectl
  sudo apt-mark hold kubelet kubeadm kubectl
fi

if ! command -v helm &>/dev/null; then
  echo "Installing Helm..."
  curl https://raw.githubusercontent.com/helm/helm/main/scripts/get-helm-3 | bash
fi


echo "Installing Python 3 and build tools..."
sudo apt-get install -y python3 python3-pip python3-venv maven

if ! dpkg -l temurin-25-jdk &>/dev/null; then
  echo "Adding Adoptium repository..."
  wget -qO - https://packages.adoptium.net/artifactory/api/gpg/key/public | sudo apt-key add -
  echo "deb https://packages.adoptium.net/artifactory/deb $(lsb_release -cs) main" | sudo tee /etc/apt/sources.list.d/adoptium.list
  sudo apt-get update
  echo "Installing Java 25 (Eclipse Temurin)..."
  sudo apt-get install -y temurin-25-jdk
fi

echo "Installing Nginx..."
sudo apt-get install -y nginx

echo "Installing UFW..."
sudo apt-get install -y ufw

echo "Server preparation script completed successfully."
