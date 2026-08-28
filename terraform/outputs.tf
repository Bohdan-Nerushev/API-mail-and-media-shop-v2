# Output Definitions for Deployment Details

output "release_name" {
  value       = helm_release.mail_and_media_shop.name
  description = "The deployed Helm release name"
}

output "namespace" {
  value       = helm_release.mail_and_media_shop.namespace
  description = "Kubernetes namespace where the application was deployed"
}

output "chart_version" {
  value       = helm_release.mail_and_media_shop.version
  description = "Version of the deployed Helm chart"
}

output "release_status" {
  value       = helm_release.mail_and_media_shop.status
  description = "Current status of the Helm release deployment"
}
