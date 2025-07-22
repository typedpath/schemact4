package schemact.gradleplugin

import TemplateConstants.dollarChar
import schemact.domain.InfrastructureInjectables.VerifiedCognitoUser
import schemact.domain.InfrastructureInjectables.verifyUserSession

val dollarChar = '$'
// TODO put src impl in a submodule (injectedtransforms, from which source is copied)
val verifyUserSession = TransformFunction( function = verifyUserSession, src=
      """
           fun ${verifyUserSession.name}() (/*TODO take these off the function*/token: String, cognitoDetails: CognitoClientDetails): CognitoResult {
        // Decode JWT to get kid (key ID)
        val decodedJwt = JWT.decode(token)
        val kid = decodedJwt.keyId ?: throw JWTVerificationException("Missing kid in JWT header")
        println("using cognitoDetails.jwksUrl "+ cognitoDetails.jwksUrl)
        // Fetch JWKS and get public key
        val jwkProvider: JwkProvider = UrlJwkProvider(cognitoDetails.jwksUrl)
        val jwk = jwkProvider.get(kid)
        val publicKey = jwk.publicKey as RSAPublicKey

        // Verify JWT
        val verifier = JWT.require(Algorithm.RSA256(publicKey, null))

            .withIssuer("https://cognito-idp.${dollarChar}{cognitoDetails.region}.amazonaws.com/${dollarChar}{cognitoDetails.userPoolId}")
            .withAudience(cognitoDetails.clientId)
            .withClaim("token_use", "id") // Ensure ID token
            .build()

        val verifiedJwt = verifier.verify(token)

        println("claims" + verifiedJwt.claims.entries.map { "${dollarChar}{it.key} => ${dollarChar}{it.value}" }.joinToString (","))

        // Extract user info
        return CognitoResult(
            sub=  verifiedJwt.getClaim("sub").asString(),
            email=  verifiedJwt.getClaim("email")?.asString(),
            username = verifiedJwt.getClaim("cognito:username")?.asString()
        )
    }
      """.trimIndent() )
