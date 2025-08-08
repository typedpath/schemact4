package schemact.gradleplugin.injection

import schemact.domain.Function
import schemact.gradleplugin.injection.ParameterDependencyGrapher.checkForUnresolved
import schemact.gradleplugin.injection.ParameterDependencyGrapher.expandParamRequirements
import schemact.gradleplugin.injection.ParameterDependencyGrapher.orderLeastDependantToMost
import schemact.gradleplugin.injection.resolvers.AwsAuthHeaderResolver
import schemact.gradleplugin.injection.resolvers.AwsResolvers.LambdaResolvers
import schemact.gradleplugin.injection.resolvers.AwsResolvers.RestBodyParamResolver
import schemact.gradleplugin.injection.resolvers.AwsResolvers.RestUrlParamResolver
import schemact.gradleplugin.injection.resolvers.AwsResolvers.restMultiBodyElementResolver

object AwsLambdaDependencyGrapher {
    fun expandAndCheckRequirements(function: Function ): ParameterDependencyGraph {

        val context = orderLeastDependantToMost(expandParamRequirements(function, LambdaResolvers))
        checkForUnresolved(context)

        val restBodyParamRequirements = context.filter{RestBodyParamResolver.resolve(it)!=null}
        val multiPartBodyParamRequirements = context.filter{restMultiBodyElementResolver.resolve(it)!=null}
        val urlParamRequirements = context.filter{RestUrlParamResolver.resolve(it)!=null}
        val restHeaderRequirements = context.filter { AwsAuthHeaderResolver.resolve(it)!=null }
        val mapperFunctions = LambdaResolvers.filterIsInstance<MapperResolver>().map{it.mapperFunction}

        return ParameterDependencyGraph(sortedContext = context, restBodyParamRequirements=restBodyParamRequirements,
            restMultiPartBodyParamRequirements=multiPartBodyParamRequirements, restUrlParamRequirements=urlParamRequirements,
            restHeaderRequirements = restHeaderRequirements,
            mapperFunctions=mapperFunctions)
    }

}