package domain

class Function(val name: String, val description: String,
               val paramType: Entity, val returnType: Entity,
               val environment: MutableMap<String, ForwardReference<String>>) {

}