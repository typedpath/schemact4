package schemact.gradleplugin.injection

import schemact.domain.Function
import schemact.domain.asString

object ParameterResolver {
    // distinguish between expansion and resolution
    fun expandParamRequirements(function: Function, resolvers: List<Resolver>) : List<Value> {
        val unexpandedValues = function.paramType.connections.map { Value(connectionFrom = it) }.toMutableList()
        val expandedValues = mutableListOf<Value>()
        var loopAlert = false
        // only top level map to REST params hene need to know iteration count
        var iterationIndex = 0
        while (unexpandedValues.isNotEmpty() && !loopAlert) {
            // should be new requirements this iteration
            val newRequirementsThisIteration = mutableListOf<Value.Requirement>()
            val valuesExpandedThsIteration = mutableListOf<Value>()
            for (unexpandedValue in unexpandedValues) {
                val expandedValuesResults: List<List<Value.Requirement>> =
                    resolvers.map { it.resolve(unexpandedValue) }.filter { it!=null }.map{
                        unexpandedValue.requirements = it
                        it as List<Value.Requirement>
                    }
                if (expandedValuesResults.size>0) {
                    val newRequirements = expandedValuesResults.flatMap { it  }
                    // filter based on whether exists or not
                    newRequirementsThisIteration.addAll(newRequirements)
                }
                valuesExpandedThsIteration.add(unexpandedValue)
            }
            unexpandedValues.removeAll(valuesExpandedThsIteration)
            expandedValues.addAll(valuesExpandedThsIteration)
            for (requirement in newRequirementsThisIteration) {
                if (!expandedValues.any {requirement.match(it)} && !unexpandedValues.any { requirement.match(it) }) {
                    unexpandedValues.add(requirement.creator())
                }
            }
//            unexpandedValues.addAll(newValuesThisIteration)
            if (!unexpandedValues.isEmpty() && valuesExpandedThsIteration.size ==0) {
                loopAlert = true
            }
            iterationIndex++
        }
        if (loopAlert) {
            throw Exception("""resolveParams: unexpandedValues for function: ${function.name}:
                 ${unexpandedValues.map{it.connectionFrom.asString()}.joinToString(System.lineSeparator())}""".trimMargin())
        }
        //now sort in dependency order ?
        return expandedValues
    }

    fun orderLeastDependantToMost(values: List<Value>) : List<Value> {
         return values.sortedWith { v1, v2 ->
                 if (v1.requirements == null) {
                     throw Exception("cant sort value: ${v1.varName} : unknown requirements")
                 }
                 if (v2.requirements == null) {
                    throw Exception("cant sort value: ${v2.varName} : unknown requirements")
                 }
                 if (v1.requirements!!.any { it.match(v2)   }) {
                     1
                 } else if (v2.requirements!!.any { it.match(v1)   })  {
                     -1
                 } else {0}
         }
    }

}