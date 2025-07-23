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
        val value2DependencyL: List<Pair<Value, List<Value>>> = values.map{
            value ->
            val requirements = value.requirements
            if (requirements==null) {
                throw Exception("cant sort value: ${value.varName} : unknown requirements")
            }
            Pair(value,  requirements.flatMap{requirement -> values.filter {requirement.match(it)}})
        }

        val value2Dependency = value2DependencyL.toMap()

        fun isTransitivelyDependantOn(vFrom: Value, vTo: Value) : Boolean {
            val dependencies = value2Dependency.get(vFrom)!!
            if (dependencies.contains(vTo)) {
                println("${vFrom.connectionFrom.name} is directly dependant on ${vTo.connectionFrom.name}")
                return true
            }
            if (dependencies.any{isTransitivelyDependantOn(it, vTo)}) {
                println("${vFrom.connectionFrom.name} is transitively dependant on ${vTo.connectionFrom.name}")
                return true
            }
            println("${vFrom.connectionFrom.name} is not dependant on ${vTo.connectionFrom.name}")
            return false
        }

         return values.sortedWith( Comparator<Value>{ v1, v2 ->
                when {
                    isTransitivelyDependantOn(v2, v1) -> 1
                    isTransitivelyDependantOn(v1, v2) -> -1
                    else -> 0
                }
         }).reversed()
    }

}