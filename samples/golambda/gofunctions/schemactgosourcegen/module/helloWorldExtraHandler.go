package gofunctions

import "fmt"

func HelloWorldExtraHandler(protagonist HelloWorldExtra_name) (HelloWorldExtra_GreetingPolicy, error) {
	return HelloWorldExtra_GreetingPolicy{
		Hello: fmt.Sprintf("Good Day %s %s %s", protagonist.FirstName,
			protagonist.MiddleNames, protagonist.LastName),
		Goodbye: fmt.Sprintf("See Ya %s", protagonist.LastName),
	}, nil
}
