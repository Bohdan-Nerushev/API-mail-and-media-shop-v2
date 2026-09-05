# Main Deployment Configuration

# Create a Kubernetes Namespace for isolating the deployment resources
resource "kubernetes_namespace_v1" "app_namespace" {
  metadata {
    name = var.namespace

    labels = {
      # Label to identify the environment and managed-by tool
      "environment" = var.environment
      "managed-by"  = "terraform"
    }
  }
}

# Deploy the Mail and Media Shop Helm chart using the Helm provider
resource "helm_release" "mail_and_media_shop" {
  # Name of the Helm release instance in Kubernetes
  name = var.release_name

  # Path to the local Helm chart directory
  chart = "${path.module}/${var.chart_path}"

  # Target namespace created by the kubernetes_namespace_v1 resource
  namespace = kubernetes_namespace_v1.app_namespace.metadata[0].name

  # Wait for all resources to become ready before completing the apply operation
  wait            = true
  timeout         = 600
  recreate_pods   = false
  cleanup_on_fail = true

  force_update    = true
  reset_values    = true
  atomic          = true
  replace         = true
  upgrade_install = true
  # Inject custom values files into the Helm release
  # Primary values.yaml is always loaded first, followed by environment-specific values (e.g., values-dev.yaml)
  values = [
    file("${path.module}/${var.chart_path}/values.yaml"),
    fileexists("${path.module}/${var.chart_path}/values-${var.environment}.yaml") ? file("${path.module}/${var.chart_path}/values-${var.environment}.yaml") : ""
  ]

  set = [
    {
      name  = "app.image.tag"
      value = var.image_tag
    }
  ]
}