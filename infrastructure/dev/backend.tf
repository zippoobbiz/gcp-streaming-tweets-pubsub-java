terraform {
  backend "gcs" {
    bucket = "phil-xu-tf-state"
    region = "australia-southeast1-a"
    prefix = "terraform/state/dev"
  }
}