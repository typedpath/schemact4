package schemact.gradleplugin.injection

import schemact.domain.RestInjectables
import kotlin.String

val AwsAuthHeaderResolver = object : Resolver () {
    override fun resolve(value: Value): List<Value.Requirement>? {

        if (value.connectionFrom.entity2 == RestInjectables.AuthorizationHeaderType) {
            value.renderer = object : Renderer() {
                override fun renderKotlin(value: Value, dependencies: Map<String, Value>) : String{
                    //TODO handle not present !
                    return """val ${value.varName} = input.headers["Authorization".lowercase()]!!"""
                }
            }
            return emptyList()
        } else return null
    }

}