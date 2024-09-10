# Language Support

## Target Languages
- Kotlin / Java - because its everywhere
- Typescript - because its also everywhere
- Go - because it is fast
- SQL DDL - also everywhere
- SQL DML - also everywhere

## Target Ide s
- System Architecture Definition i.e. DSL i.e schemact.domain
  - Within Idea / Gradle
    - This is naturally a Kotlin DSL  
  - Within Visual Studio
    - Could be any language Visual Studio supports 
    - Typescript ?

## Mapping Approach

A root definition in the preferred language is mapped to supported languages. The mapping includes
- A language specific version of the Schemact DSL so a target language can be used to define arcitecture in DSL
- Language specific support for binary / serialization this will include:
  - Compiled Property meta data so that serialization/deserialization doesnt require reflection 
- Language specific version of entities e.g.
  - Java classes
  - SQL Tables
- Language Specific versions of functions e.g.
  - as REST services 


## Example Scenarios

## Intelij User
User defines a schemact (in kotlin) that contains a GO module for AWS lambda. 
The gradle build must:
- Create the (GO) lambda wrapper code including the function definition.
- Create client code (e.g. typescript)
- Build and deploy the GO code module
- https://docs.aws.amazon.com/lambda/latest/dg/lambda-golang.html

## Visual Studio User
User defines a schemact (in Typescript) that contains a Typescript
Npm scripts must:
- create the (Typescript) lambda wrapper code including function definition.
- build and deploy the Typescript code






