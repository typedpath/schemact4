package schemact.gradleplugin.golang.templates

import org.gradle.configurationcache.extensions.capitalized
import schemact.domain.Module
import schemact.gradleplugin.FunctionIdKey
import schemact.gradleplugin.RestPolicy
import schemact.gradleplugin.functionId
import schemact.gradleplugin.golang.templates.Util.cardinalityToken
import schemact.gradleplugin.golang.templates.Util.propertyTypeName
import java.time.LocalDateTime

fun mainGo(module: Module) = """
package main
// created by mainGo.kt at ${LocalDateTime.now()}
import (
        "fmt"
        "os"
        "log"
    	"context"
    	"encoding/json"
    	"github.com/aws/aws-lambda-go/events"
    	"github.com/aws/aws-lambda-go/lambda"
    	"net/http"
         ${module.name} "github.com/typedpath/${module.name}-aws-lambda-function/module"    
    )

${module.functions.map{"""
var ${it.name}Impl ${module.name}.${it.name.capitalized()} = ${module.name}.${it.name.capitalized()}Handler   
""".trimIndent()}.joinToString (System.lineSeparator())}

type JsonResponse struct {
	Message string `json:"message"`
}

func notFoundHandler(path string) (events.APIGatewayProxyResponse, error) {
	response := JsonResponse{Message: fmt.Sprintf("Bad luck path: '%s'", path)}
	return writeJsonResponse(http.StatusNotFound, response)
}

func writeJsonResponse(statusCode int, response any) (events.APIGatewayProxyResponse, error) {
	responseBody, _ := json.Marshal(response)
	return events.APIGatewayProxyResponse{
		StatusCode: statusCode,
		Headers: map[string]string{
			"Content-Type": "application/json",
		},
		Body: string(responseBody),
	}, nil
}


func router(ctx context.Context, r events.APIGatewayProxyRequest) (events.APIGatewayProxyResponse, error) {
	//logger := slog.New(slog.NewJSONHandler(os.Stdout, nil))
	//logger.Info("Received request", slog.String("path", r.Path))

    var functionId = os.Getenv("$FunctionIdKey") 

	switch {
${module.functions.map {
val function = it
    """
    case functionId == "${functionId(module, it)}" :
             ${RestPolicy(it.paramType).argsFromParams.map {
    """
        var ${it.name} = r.QueryStringParameters["${it.name}"]"""
}.joinToString ()} 
${if (RestPolicy(function.paramType).argsFromBody.size>0)"""
        type ${function.name}_Body struct{ 
              ${RestPolicy(function.paramType).argsFromBody.map{ """ 
              ${it.name.capitalized()} ${cardinalityToken(it)}${propertyTypeName(module, function, it.entity2)} `json:"${it.name}"`""".trimIndent()   }.joinToString (System.lineSeparator())}
        }
        var body ${function.name}_Body 
        json.Unmarshal([]byte(r.Body), &body)  
 ${RestPolicy(function.paramType).argsFromBody.map{ """
        var ${it.name} = body.${it.name.capitalized()}"""   }.joinToString (System.lineSeparator())}
""" else 
"""""" }    
        var result, _ = ${it.name}Impl(${it.paramType.connections.map { it.name }.joinToString(", ")})
        return writeJsonResponse(http.StatusOK, result)
"""    
}.joinToString (System.lineSeparator())}      
	default:
        requestJson, _ := json.Marshal(r) 
        contextJson, _ := json.Marshal(ctx)
        log.Printf("failed to process: request: %s context: %s", requestJson, contextJson)
		return notFoundHandler(r.Path)
	}
}

func main() {
	lambda.Start(router)
}
    
""".trimIndent()


/*

		var name = r.QueryStringParameters["name"]
		var result, _ = helloWorldImpl(name)
		// extract the params
		return writeJsonResponse(http.StatusOK, result)
 */