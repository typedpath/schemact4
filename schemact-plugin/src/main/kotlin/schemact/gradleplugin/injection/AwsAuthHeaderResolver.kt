package schemact.gradleplugin.injection

import schemact.domain.InfrastructureInjectables
import kotlin.String

val AwsAuthHeaderResolver = object : Resolver () {
    override fun resolve(value: Value): List<Value.Requirement>? {

        if (value.connectionFrom.entity2 == InfrastructureInjectables.AuthorizationHeaderType) {
            value.renderer = object : Renderer() {
                override fun renderKotlin(value: Value, dependencies: Map<String, Value>) : String{
                    //TODO handle not present !
                    return """val ${value.varName} = input.headers["Authorization".lowercase()]!!"""
                }
                override fun requiredImports(): List<String> =  emptyList()
            }
            return emptyList()
        } else return null
    }

}