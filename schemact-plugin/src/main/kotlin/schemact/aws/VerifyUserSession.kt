package schemact.aws
import com.auth0.jwk.JwkProvider
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import java.security.interfaces.RSAPublicKey
import com.auth0.jwk.UrlJwkProvider

object VerifyUserSession {
           fun verifyUserSession (/*TODO take these off the function*/token: String, cognitoClientDetails: CognitoClientDetails): VerifiedCognitoUser {
        // Decode JWT to get kid (key ID)
        val decodedJwt = JWT.decode(token)
        val kid = decodedJwt.keyId ?: throw JWTVerificationException("Missing kid in JWT header")
        println("using cognitoDetails.jwksUrl "+ cognitoClientDetails.jwksUrl)
        // Fetch JWKS and get public key
        val jwkProvider: JwkProvider = UrlJwkProvider(cognitoClientDetails.jwksUrl)
        val jwk = jwkProvider.get(kid)
        val publicKey = jwk.publicKey as RSAPublicKey

        // Verify JWT
        val verifier = JWT.require(Algorithm.RSA256(publicKey, null))

            .withIssuer("https://cognito-idp.${cognitoClientDetails.region}.amazonaws.com/${cognitoClientDetails.userPoolId}")
            .withAudience(cognitoClientDetails.clientId)
            .withClaim("token_use", "id") // Ensure ID token
            .build()

        val verifiedJwt = verifier.verify(token)

        println("claims" + verifiedJwt.claims.entries.map { "${it.key} => ${it.value}" }.joinToString (","))

        // Extract user info
        return VerifiedCognitoUser(
            sub=  verifiedJwt.getClaim("sub").asString(),
            email=  verifiedJwt.getClaim("email")?.asString()?:"",
            username = verifiedJwt.getClaim("cognito:username")?.asString()?:""
        )
    }
  }            