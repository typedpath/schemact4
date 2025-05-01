package schemact.domain

class Function(val name: String, val description: String,
               val paramType: Entity, val returnType: Entity,
               val auth: Auth? = null
)