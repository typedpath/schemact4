package schemact.gradleplugin.golang.templates

import schemact.domain.Module
import java.time.LocalDateTime

fun Makefile(module: Module) =
"""
# created by Makefile.kt on ${LocalDateTime.now()}    
linux-build:
	GOARCH=arm64 GOOS=linux go build -tags lambda.norpc -o ./bin/bootstrap
	jar cvf bin/${module.name}-${module.version}-schemact-aws-lambda.zip -C bin bootstrap

run:
	go run main.go
     
 """.trimIndent()