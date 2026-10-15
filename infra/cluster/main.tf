terraform {
  required_version = ">= 1.5, < 2.0"
}

variable "cluster_name" {
  type    = string
  default = "oficina-fase2"
  validation {
    condition     = can(regex("^[a-z][a-z0-9-]{0,40}$", var.cluster_name))
    error_message = "Use letras minúsculas, números e hífens no nome do cluster."
  }
}

resource "terraform_data" "cluster" {
  input = {
    name       = var.cluster_name
    kubeconfig = abspath("${path.module}/kubeconfig")
  }
  triggers_replace = [filesha256("${path.module}/kind.yaml"), var.cluster_name]

  provisioner "local-exec" {
    command     = "kind create cluster --name ${self.input.name} --config kind.yaml --kubeconfig kubeconfig --wait 180s"
    working_dir = path.module
  }
  provisioner "local-exec" {
    when    = destroy
    command = "kind delete cluster --name ${self.input.name} --kubeconfig \"${self.input.kubeconfig}\""
  }
}

output "kubeconfig" {
  value = terraform_data.cluster.output.kubeconfig
}
