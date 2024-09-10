# To develop

# DSL first v Code First (AKA outside in v inside out)

What is wrong with this code first statement ?
```kotlin
@Controller("/hello") // (1)
class HelloController {

    @Get(produces = [MediaType.TEXT_PLAIN]) // (2)
    fun index(): String {
        return "Hello World" // (3)
    }
}
```
- the requirement is to be able to call method index() : String over the network but this assumes clients want to call a rest service
- if the client really wants to call a rest service the server side wrapper the client and server rest wrapper could have been auto generated
- this hard codes policies that are subject to change
  - the path name "hello"
  - the media type
  - the HTTP method
- schemact DSL aims for minimum commitment on policy and minimum application code
- TODO add DSL example

## Inside Out

Coorporate development follow inside out e.g.
  - team builds component then integrates with other team i.e. dependencies are made concrete
  - the component might be a service, db etc
  - declaration of dependencies are ad hoc
  - there is no consensus as to what is a component is. 

## Outside In
Schemact is outside in.
    - The connections + dependencies between components are defined from the start in a DSL in a str
    - teams are fluid because the technical requirements are fully materialised constantly
        - TODO define tests in DSL (so ) 
    - everything definable in the DSL is a component - Entity s are on a par with functions (code) etc
    - systematic definition of what is included in DSL
   

## Where does inside out meet outside in
  - TODO put in context of dependency injection

## Service un-oriented architecture

# progress by . . project
## commit plugin