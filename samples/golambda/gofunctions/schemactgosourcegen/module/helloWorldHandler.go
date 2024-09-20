package gofunctions

import (
	"fmt"
)

func HelloWorldHandler(name string) (string, error) {
	return fmt.Sprintf("Hello %s", name), nil
}
