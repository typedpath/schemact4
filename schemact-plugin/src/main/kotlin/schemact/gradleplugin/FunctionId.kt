package schemact.gradleplugin

import schemact.domain.Function
import schemact.domain.Module

fun functionId(module: Module, function: Function) = "${module.name}/${function.name}"

const val FunctionIdKey = "FunctionId_"