package schemact.gradleplugin.injection

import schemact.domain.InfrastructureInjectables
import kotlin.String

val AwsAuthHeaderResolver = object : Resolver () {
    override fun resolve(value: Value): List<Value.Requirement>? {

        if (value.connectionFrom.entity2 == InfrastructureInjectables.AuthorizationHeaderType) {
            value.renderer = object : Renderer() {
                override fun renderKotlin(value: Value, dependencies: Map<String, Value>) : String{
                    return """val ${value.varName} = input["Authorization".lowercase()]"""
                }
                override fun requiredImports(): List<String> =  emptyList()
            }
            return emptyList()
        } else return null
    }

}