package schemact.gradleplugin.injection

import schemact.domain.Function
import schemact.domain.InfrastructureInjectables
import schemact.domain.asString
import schemact.gradleplugin.injection.AwsResolvers.VerifiedUserResolver
import schemact.gradleplugin.injection.AwsResolvers.WriteUserPrivateBucketDataResolver
import schemact.gradleplugin.injection.ParameterResolver.expandParamRequirements
import schemact.gradleplugin.injection.ParameterResolver.orderLeastDependantToMost

object Template {
    fun template(function: Function): String {

        val resolvers = listOf<Resolver>(WriteUserPrivateBucketDataResolver, AwsAuthHeaderResolver,
            AwsResolvers.DynamoDBTablenameResolver, AwsResolvers.PrivateBucketNameResolver,

            AwsResolvers.CognitoClientDetailsResolver, VerifiedUserResolver    )

        val context = orderLeastDependantToMost(expandParamRequirements(function, resolvers))

        val unresolvedRequirementsByValue = unresolvedRequirementsByValue(context)

        if (!unresolvedRequirementsByValue.isEmpty()) {
            throw Exception("""some values are not resolved : 
                ${unresolvedRequirementsByValue.map{"""${it.key.connectionFrom.asString()} ${if (it.key.requirements==null) "unknown requirements " else ""} ${it.value.map { it.name }.joinToString(",")} """}
                .joinToString ( System.lineSeparator() )}
            """.trimMargin())
        }

        // suppliers are the rest policy and infrastructure
        // isConstructedPreInjection, isFromInfrastructure determines if web suppler works


        // some sort of recursive dependancy search of params
        // check everything is resolvable
        // sort dependancies
        // render dependancies as code
        //     find dependency mapper for each dependancy
        //        if there is more than 1
        fun dependencies(value: Value, context: List<Value>) : Map<String, Value> {
            return value.requirements!!.map {
                    requirement ->
                val matches = context.filter { requirement.match(it) }
                if (matches.size == 0) {
                    throw Exception("resolving value for ${value.connectionFrom.asString()} ${requirement.name} got no matches }")
                }
                if (matches.size > 1) {
                    throw Exception("resolving value for ${value.connectionFrom.asString()} ${requirement.name} got multiple matches : ${matches.map { it.connectionFrom.asString() }.joinToString (", ")}  MERGE not available")
                }
                Pair(requirement.name, matches[0])
            }.toMap()
        }

        return """
               ${
            context.map{ value -> value.renderer?.renderKotlin(value=value, dependencies(value, context))
            }.joinToString("\n")
        }
           """.trimIndent()
    }



    fun unresolvedRequirementsByValue(values: List<Value>) : Map<Value, List<Value.Requirement>> {
        val valuesToUnresolvedRequirements = mutableMapOf<Value, List<Value.Requirement>>()
        for (value in values) {
            if (value.requirements!=null) {
                val unresolved =
                    value.requirements!!.filter { requirement ->
                        !values.any {
                            requirement.match(it)
                        }
                    }
                if (unresolved.size > 0) valuesToUnresolvedRequirements.put(value, unresolved)
            } else {
                valuesToUnresolvedRequirements.put(value, emptyList())
            }
        }
        return  valuesToUnresolvedRequirements
    }

}