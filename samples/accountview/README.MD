# Account View

This is a sample app for schemact4 (https://github.com/typedpath/schemact4)
- Its objective is to analyze barclays account spending.
- Its probably secure, however, without a security audit the safety of data is not gauranteed.
- Note all the AWS stuff is handled by the schemact4 plugin based on the DSL spec in buildSrc folder

To host separate, independant versions:

- change the rootDomain object in **AccountViewDomain.kt** to match a separate domain.
- run gradle task **accountview_buildAndDeploy**
- run ui deployment script **deploy.sh**
- the above will result in deployment of infrastructure and code for a react web client fronted app 
- you will need aws admin rights
- check your cloudformation templates to see what you got ! 

## How to use app

**TODO** Add expanded version of this as help into app:

1. logon / create user account at **accountview.testedsoftware.org**
2. create a bank account
3. upload a barclays transaction export file as downloaded from Barclays web app
4. optionally create categories
5. optionally create auto-categorisation filters

** TODO ** tons more stuff
