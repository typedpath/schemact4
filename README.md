# SCHEMA Complete over Time


## TODO
### Moving Code From Paramicons ( https://github.com/typedpath/paramicons ) to Schemact4 

- make function support multiple functions instead of hardcoded thumbnail
  - ~~move to aws CDK libraru~~
  - ~~TODO : cloudfront distribution infrastructure~~
  - ~~TODO: test ui deploy~~
  - ~~TODO: cleanout cloudformation2kotlin library references~~ 
- generate ~~aws~~ wrapper for function code
  - ~~reorganise paramicons project so schemact definition can be shared among modules~~ 
  - ~~add (sample) functions module~~
  - ~~reorganise plugin so functions can be specified in functions/gradle.buid.kts~~ +
       ~~plugin options are optional~~ 
  - ~~generate kotlin source~~
  - plugin should add in aws gateway gradle dependancies
  - plugin should add in aws fatJar
  - generate typescript client code
  - include multiple function test 
- incorporate build function code into plugin
- incorporate build ui code into plugin
- build paramicons crud service from plugin
  - create typescript for entities 
  - create database service
  - create typescript bindings for database service




