git clone git@github.com:typedpath/schemact4.git
cd schemact4
. ./gradlew publishToMavenLocal
cd schemact-plugin/
. ./gradlew publishToMavenLocal
cd ../samples/golambda
. ./gradlew gofunctions_genGoCode
cd gofunctions/schemactgosourcegen/
go mod tidy
make
cd ../..
. ./gradlew golambda_buildAndDeploy
