package gofunctions

import (
	"bytes"
	"fmt"
)

func HelloWorldExtraHandler(protagonist HelloWorldExtra_name, aliases []HelloWorldExtra_name) (HelloWorldExtra_GreetingPolicy, error) {
	var akasBuffer bytes.Buffer

	for i := 0; i < len(aliases); i++ {
		if i != 0 {
			akasBuffer.WriteString(", ")
		}
		akasBuffer.WriteString("aka ")
		akasBuffer.WriteString(toString(aliases[i]))
	}

	return HelloWorldExtra_GreetingPolicy{
		Hello:   fmt.Sprintf("Good Day %s %s", toString(protagonist), akasBuffer.String()),
		Goodbye: fmt.Sprintf("See Ya %s", protagonist.LastName),
	}, nil

}

func toString(name HelloWorldExtra_name) string {
	return fmt.Sprintf("Good Day %s %s %s", name.FirstName, name.MiddleNames, name.LastName)
}
