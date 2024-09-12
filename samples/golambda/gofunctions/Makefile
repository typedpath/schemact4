linux-build:
	GOARCH=arm64 GOOS=linux go build -tags lambda.norpc -o ./bin/bootstrap
	jar cvf bin/gofunctions-1.0.03-SNAPSHOT-schemact-aws-lambda.zip -C bin bootstrap

run:
	go run main.go

#    chmod 755 ./bin/HelloWorldHandler


