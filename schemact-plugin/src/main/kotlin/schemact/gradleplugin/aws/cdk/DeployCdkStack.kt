package schemact.gradleplugin.aws.cdk

import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.regions.Regions
import com.amazonaws.services.cloudformation.model.Output
import schemact.gradleplugin.aws.util.CloudFormationUtil.createOrUpdateStackFromString

import software.amazon.awscdk.App
import software.amazon.awscdk.StageSynthesisOptions
import software.constructs.Construct
import java.io.File

object DeployCdkStack {
    fun deployCdkStack(
        stackName: String,
        region: Regions = Regions.US_EAST_1,
        create: (Construct) -> Unit
    ) : List<Output>{
        val app = App()
        create(app)
        val stageSynthesisOptions = StageSynthesisOptions.builder()
            .force(false)
            .skipValidation(true)
            .validateOnSynthesis(false)
            .build()

        val ca = app.synth(stageSynthesisOptions)
        val strTemplate = File(ca.stacks.get(0).templateFullPath).readText()

        var returnOutputs: List<Output> = emptyList()

        fun onSuccess(credentialsProvider: AWSCredentialsProvider, outputs: List<Output>) {
            println("created stack $stackName}")
            returnOutputs = outputs
        }

        createOrUpdateStackFromString(
            strTemplate = strTemplate,
            stackName = stackName,
            region = region,
            cleanup = false,
            onSuccess = ::onSuccess/*,
                    blockUpdate = blockUpdate*/
        )
        return returnOutputs

    }
}