package schemact.gradleplugin.injection

class ParameterDependencyGraph(val sortedContext: List<Value>,
                               val   restHeaderRequirements: List<Value>,
                               val restBodyParamRequirements: List<Value>,
                               val restMultiPartBodyParamRequirements: List<Value>,
                               val restUrlParamRequirements: List<Value>,
                               val systemPropertyRequirements: List<Value>,
                               val mapperFunctions: List<MapperFunction>

    )