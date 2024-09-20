package schemact.gradleplugin.golang.templates

import schemact.domain.Module

fun goMod(module: Module) = """
module github.com/typedpath/${module.name}-aws-lambda-function

go 1.18

require github.com/aws/aws-lambda-go v1.47.0    
""".trimIndent()