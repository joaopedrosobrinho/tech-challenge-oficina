terraform {
  required_version = ">= 1.5, < 2.0"
  required_providers {
    kubernetes = {
      source  = "hashicorp/kubernetes"
      version = "~> 2.38.0"
    }
    random = {
      source  = "hashicorp/random"
      version = "~> 3.7.2"
    }
  }
}

variable "kubeconfig" {
  type    = string
  default = "../cluster/kubeconfig"
}
provider "kubernetes" {
  config_path = var.kubeconfig
}
resource "random_password" "database" {
  length  = 32
  special = false
}
resource "kubernetes_namespace_v1" "oficina" {
  metadata { name = "oficina" }
}
resource "kubernetes_secret_v1" "database" {
  metadata {
    name      = "postgres-secret"
    namespace = kubernetes_namespace_v1.oficina.metadata[0].name
  }
  data = { POSTGRES_PASSWORD = random_password.database.result }
}
resource "kubernetes_manifest" "volume" {
  manifest   = yamldecode(file("${path.module}/../../k8s/database/volume.yaml"))
  depends_on = [kubernetes_namespace_v1.oficina]
}
resource "kubernetes_manifest" "service" {
  manifest   = yamldecode(file("${path.module}/../../k8s/database/service.yaml"))
  depends_on = [kubernetes_namespace_v1.oficina]
}
resource "kubernetes_manifest" "postgres" {
  manifest   = yamldecode(file("${path.module}/../../k8s/database/deployment.yaml"))
  depends_on = [kubernetes_secret_v1.database, kubernetes_manifest.volume]
}
