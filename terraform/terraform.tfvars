# Default Variable Assignments for Development Environment

# Specify the local Kubernetes configuration path
kubeconfig_path = "~/.kube/config"

# Target environment profile
environment = "dev"

# Kubernetes namespace dedicated for the development deployment
namespace = "mail-and-media-shop-dev"

# Helm release name in the cluster
release_name = "mail-and-media-shop"

# Relative path to the local Helm chart directory
chart_path = "../helm/mail-and-media-shop"
