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
  - ~~plugin should add in aws fatJar~~
  - generate typescript client code
  - include multiple function test 
- incorporate build function code into plugin
- incorporate build ui code into plugin
- build paramicons crud service from plugin
  - create typescript for entities 
  - create database service
  - create typescript bindings for database service
- incorporate module concept - ie location for functions
   - this will make binaries locateable by convention  
- create build pipeline 
  - attach permissions to plugin actions, deployments  
  - pipeline ui is a web app that proxies the plugin tasks + checks permissions
    - refactor plugin to separate tasks from gradle plugin specific code 
- support versioning
  - create version - locates functions and static content at version sensitive locations
     - e.g. <bucketName/version>
     - e.g. functions - create jars with required version
     - create git branch
     - do functions have a specific version or everything is at the same version ? 
     




