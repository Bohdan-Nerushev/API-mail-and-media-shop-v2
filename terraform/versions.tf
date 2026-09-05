# Terraform and Provider Requirements Configuration
# Defines minimum required Terraform version and required providers for Helm and Kubernetes

terraform {
  # Require Terraform version 1.0 or higher
  required_version = ">= 1.0.0"

  required_providers {
    # Helm provider for managing Helm chart releases on Kubernetes
    helm = {
      source  = "hashicorp/helm"
      version = ">= 2.12.0"
    }

    # Kubernetes provider for managing Kubernetes resources (e.g. namespaces)
    kubernetes = {
      source  = "hashicorp/kubernetes"
      version = ">= 2.26.0"
    }
  }
}
