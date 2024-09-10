package name

import "fmt"

func FormatName(value string) (string, error) {
	return fmt.Sprintf("Hello, %s!", value), nil
}

func FormatMessage() (string, error) {
	return "Welcome to the home page!", nil
}

// type TestStruct struct {
//   value string
// }
//
//
// func (t * TestStruct) makeMeGood() {
//
// }
//
//
// test = TestStruct{}
// test.makeMeGood()
