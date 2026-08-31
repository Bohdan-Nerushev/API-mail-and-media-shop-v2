# Input Variables Definition

variable "kubeconfig_path" {
  type        = string
  default     = "~/.kube/config"
  description = "Path to the local kubeconfig file for Kubernetes cluster authentication"
}

variable "environment" {
  type        = string
  default     = "dev"
  description = "Target deployment environment (e.g. dev, qa, live). Used to resolve values-<env>.yaml files"
}

variable "namespace" {
  type        = string
  default     = "mail-and-media-shop-dev"
  description = "Kubernetes namespace where the application components will be deployed"
}

variable "release_name" {
  type        = string
  default     = "mail-and-media-shop"
  description = "Name of the Helm release instance"
}

variable "chart_path" {
  type        = string
  default     = "../helm/mail-and-media-shop"
  description = "Path to the directory containing the local Helm chart"
}

variable "image_tag" {
  type        = string
  default     = "latest"
  description = "Docker image tag to deploy for the Spring Boot application"
}

