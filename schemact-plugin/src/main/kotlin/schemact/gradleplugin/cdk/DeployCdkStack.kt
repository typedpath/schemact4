package schemact.gradleplugin.cdk

import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.regions.Regions
import com.amazonaws.services.cloudformation.model.Output
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.PutObjectRequest

import com.typedpath.stack.util.createOrUpdateStackFromString
import schemact.domain.Deployment
import schemact.domain.Domain
import software.amazon.awscdk.App
import software.amazon.awscdk.StageSynthesisOptions
import software.constructs.Construct
import java.io.File

fun deployCdkStack(stackName: String,
                  region: Regions = Regions.US_EAST_1,
                   create: (Construct) -> Unit) {
    val app = App()
    create(app)
    val stageSynthesisOptions = StageSynthesisOptions.builder()
        .force(false)
        .skipValidation(true)
        .validateOnSynthesis(false)
        .build()

    val ca = app.synth(stageSynthesisOptions)
    val strTemplate = File(ca.stacks.get(0).templateFullPath).readText()

    fun onSuccess(credentialsProvider: AWSCredentialsProvider, outputs: List<Output>) = println("created stack $stackName}")

    createOrUpdateStackFromString(
        strTemplate =  strTemplate,
        stackName = stackName,
        region = region,
        cleanup = false,
        onSuccess = ::onSuccess/*,
                    blockUpdate = blockUpdate*/
    )

    }
