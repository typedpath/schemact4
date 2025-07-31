package schemact.gradleplugin.injection.mappers

import schemact.aws.awsHelperRoot
import schemact.domain.InfrastructureInjectables.verifyUserSession
import schemact.gradleplugin.injection.MapperFunction

// TODO put infer package and location from function
val verifyUserSession = MapperFunction(
      function = verifyUserSession, src=getResourceAsText(root= awsHelperRoot,"/schemact/aws/VerifyUserSession.kt"),
      classLocation = listOf("schemact", "aws", "VerifyUserSession")
)
