package main

// GET /name/{name}
// GET /

import (
	"context"
	"encoding/json"
	"log/slog"
	"net/http"
	"os"

	"github.com/aws/aws-lambda-go/events"
	"github.com/aws/aws-lambda-go/lambda"
	"github.com/typedpath/test-aws-lambda-function/module"
)

// JSON response structure
type JsonResponse struct {
	Message string `json:"message"`
}

// Root handler for the home page
func rootHandler(ctx context.Context, r events.APIGatewayProxyRequest) (events.APIGatewayProxyResponse, error) {
	value, _ := name.FormatMessage()
	response := JsonResponse{Message: value}
	return writeJsonResponse(http.StatusOK, response)
}

// Handler for the /hello/{name} path
func helloHandler(ctx context.Context, r events.APIGatewayProxyRequest) (events.APIGatewayProxyResponse, error) {
	param := r.PathParameters["name"]
	value, _ := name.FormatName(param)
	response := JsonResponse{Message: value}
	return writeJsonResponse(http.StatusOK, response)
}

func notFoundHandler() (events.APIGatewayProxyResponse, error) {
	response := JsonResponse{Message: "Not found"}
	return writeJsonResponse(http.StatusNotFound, response)
}

func writeJsonResponse(statusCode int, response JsonResponse) (events.APIGatewayProxyResponse, error) {
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
	logger := slog.New(slog.NewJSONHandler(os.Stdout, nil))
	logger.Info("Received request", slog.String("path", r.Path))

	switch {
	case r.Path == "/" && r.HTTPMethod == "GET":
		return rootHandler(ctx, r)
	case r.Path == "/hello/{name}" && r.HTTPMethod == "GET":
		return helloHandler(ctx, r)
	default:
		return notFoundHandler()
	}

}

func main() {
	lambda.Start(router)
}
