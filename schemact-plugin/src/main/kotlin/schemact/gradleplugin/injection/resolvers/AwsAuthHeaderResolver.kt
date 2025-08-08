package schemact.gradleplugin.injection.resolvers

import schemact.domain.RestInjectables
import schemact.gradleplugin.injection.Renderer
import schemact.gradleplugin.injection.Resolver
import schemact.gradleplugin.injection.Value
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