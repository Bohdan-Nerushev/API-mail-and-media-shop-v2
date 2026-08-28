# Provider Initialization and Configuration

# Configure the Kubernetes provider using local kubeconfig
provider "kubernetes" {
  config_path = var.kubeconfig_path
}

# Configure the Helm provider targeting the Kubernetes cluster defined by kubeconfig
provider "helm" {
  kubernetes = {
    config_path = var.kubeconfig_path
  }
}
