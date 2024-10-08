# SCHEMA Complete over Time

Schemact is a DSL for describing software architecture including:
- Entities and Connections i.e. data and relationships in 
- Modules i.e. deployable code artifacts
- Functions i.e. code with a defined interface that can typically be called across a network 
- (Root) Domain e.g. *.testedsoftware.org
- Deployments i.e deployed instances
- Schemact - the root container

The criteria for inclusion of an element type is: Does it simplify a sample project ?

Its backed by a gradle plugin that converts those elements into cloud deployments.
The plugin emits:
- aws cloudformation stacks
- server and client source code
- tasks to deploy and build

These are key objectives:
- automate everything that can be automated
- solve long term problems such as
  - versioning + dependencies between services

## Sample Projects
 - [paramicons.testedsoftware.org](https://paramicons.testedsoftware.org), the code is [here](https://github.com/typedpath/paramicons) - a react + aws lambda backend
 - golambda - go aws lambda (code is [here](samples/golambda))


## Progress Metrics

### Module Types
- Standalone Function - started - paramicons/functions (uses aws lambda functions)
- Spring Boot 
- React (NPM) Libs - TODO

### Infrastructure Items
- Function 
- Relational Databases
   -  Entity Bean Generation
- Topics / Queues / Streams

### Application Language Support
- Kotlin
- Java
- Typescript

### Cloud Providers
- AWS started
- GCP TODO

### DSL Language Support
- Kotlin / Gradle - started
- Typescript / NPM

### Release/ Control / Management
- release / procedures


## TODO
### Moving Code From Paramicons ( https://github.com/typedpath/paramicons ) to Schemact4 

- ~~make function support multiple functions instead of hardcoded thumbnail~~
  - ~~move to aws CDK libraru~~
  - ~~TODO : cloudfront distribution infrastructure~~
  - ~~TODO: test ui deploy~~
  - ~~TODO: cleanout cloudformation2kotlin library references~~ 
- ~~generate ~~aws~~ wrapper for function code~~
  - ~~reorganise paramicons project so schemact definition can be shared among modules~~ 
  - ~~add (sample) functions module~~
  - ~~reorganise plugin so functions can be specified in functions/gradle.buid.kts~~ +
       ~~plugin options are optional~~ 
  - ~~generate kotlin source~~
  - ~~plugin should add in aws gateway gradle dependancies~~
  - ~~plugin should add in aws fatJar~~
  - ~~generate typescript client code~~
  - ~~include multiple function test~~ 
  - unnecessary dependnecies should be removed from fat jar 
    - probably aws jars
- ~~simplify deployment with a buildAndDeploy task~~
  - ~~add (module) task schemact/packageCode dependencies (on build and genCode tasks)~~  
  - ~~incorporate build function code into buildAndDeploy~~
     - ~~incorporate module concept - ie location for functions~~
       - ~~this will make binaries locateable by convention~~
- ~~bug fix required for task dependencies for packageCode task~~
- support react modules / dependencies 
  - objects libs- e.g. paramicons/ lib
  - viewer libs hosts -  e.g. paramicons/example 
  - injection of meta data,w support for named properties
- incorporate build ui code into fullDeploy
- build paramicons crud service from plugin
  - create typescript for entities 
  - create database service
  - create typescript bindings for database service
 
- create build pipeline 
  - attach permissions to plugin actions, deployments  
  - pipeline ui is a web app that proxies the plugin tasks + checks permissions
    - refactor plugin to separate tasks from gradle plugin specific code 
- support versioning
  - create version - locates functions and static content at version sensitive locations
     - e.g. <bucketName/version>
     - ~~e.g. functions - create jars with required version~~
     - create git branch
     - ~~modules have a specific version~~ ? 
- add  lambda configuration
  - per deployment provisioned and reserver lambda function concurrency
    - for controlling costs / eliminating cold starts 
      - https://docs.aws.amazon.com/lambda/latest/dg/provisioned-concurrency.html 
  - switch on snapStart ? - https://docs.aws.amazon.com/lambda/latest/dg/snapstart.html
  - memory



