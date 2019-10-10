provider "google" {
  version = "2.0.0"
# credentials = "${file("../private/hack-tf-sa.js")}"
  project = "${var.project}"
  region = "${var.zone}"
}

# resource "google_compute_address" "static" {
#   name = "terraform-phil-xu-sandpit-gke-cluster-external-ip"
#   region = "${var.region}"
# }

# resource "google_container_cluster" "primary" {
#   name     = "terraform-gke-cluster"
#   zone = "${var.zone}"

#   # We can't create a cluster with no node pool defined, but we want to only use
#   # separately managed node pools. So we create the smallest possible default
#   # node pool and immediately delete it.
#   remove_default_node_pool = true
#   initial_node_count = 1

#   master_auth {
#     username = "phil"
#     password = "123456789123456789"

#     client_certificate_config {
#       issue_client_certificate = false
#     }
#   }
# }

# resource "google_container_node_pool" "primary_preemptible_nodes" {
#   name       = "terraform-my-preemptible-node-pool"
#   zone   = "${var.zone}"
#   cluster    = "${google_container_cluster.primary.name}"
#   node_count = 1

#   node_config {
#     preemptible  = true
#     machine_type = "n1-standard-1"

#     metadata = {
#       disable-legacy-endpoints = "true"
#     }

#     oauth_scopes = [
#       "https://www.googleapis.com/auth/logging.write",
#       "https://www.googleapis.com/auth/monitoring",
#     ]
#   }
# }

# resource "helm_release" "gitlab" {
#     name      = "gitlab"
#     chart     = "stable/gitlab"
#     version   = "2.3.6",
#     set {
#         name  = "globalHostsExternalIP"
#         value = "${google_compute_address.static.address}"
#     }

#     set {
#         name = "globalHostsDomain"
#         value = "philsplayground.com"
#     }

#     set {
#         name = "certmanager-issuerEmail"
#         value = "phillip.xu@servian.com"
#     }
#     # set_string {
#     #     name = "image.tags"
#     #     value = "registry\\.io/terraform-provider-helm\\,example\\.io/terraform-provider-helm"
#     # }
# }


# resource "google_cloudbuild_trigger" "filename-trigger" {
#   trigger_template {
#     branch_name = ".*"
#     # branch_name = "master"
#     repo_name   = "github_zippoobbiz_gcp-streaming-tweets-pubsub-java"
#   }

#   filename = "cloudbuild.yaml"
# }


resource "google_bigquery_dataset" "default" {
  dataset_id                  = "rtda"
  description                 = "This is a test dataset for tweets"
  location                    = "australia-southeast1"
  default_table_expiration_ms = 3600000

  labels = {
    env = "default"
  }
}

resource "google_storage_bucket" "phil-xu-sandpit-stage-bucket" {
  name     = "phil-xu-sandpit-stage-bucket"
  location = "australia-southeast1"
  storage_class = "STANDARD"
}