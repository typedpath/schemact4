package schemact.domain

class Domain(val name: String, val wildcardCertificateRef: String,
             val cdnZoneReference: String,
             val deployments: List<Deployment> = mutableListOf()) {

}