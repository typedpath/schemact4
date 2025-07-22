package com.example

object ResolveDependency {
    data class Dependency<T> (val from:  T, val to: T)
    data class Dependant<T> (val from :  T, val on: List<T>)
    fun <T> orderLeastToMostDependantNoLoops(dependencies: List<Dependency<T>>): List<Dependant<T>> {
        val resolved = mutableListOf<Dependant<T>>()
        val unprocessedDependencies = dependencies.toMutableList()
        var processingFinished = false
        while (!processingFinished) {
            val unprocessedDependenciesTodo = unprocessedDependencies.toList()
            for (dependency in unprocessedDependencies) {
                   if (resolved.any{ it.from == dependency.to} ) {

                   }
            }
        }
return resolved
    }
}