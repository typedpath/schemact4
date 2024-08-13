package com.typedpath.stack.util
//TODO put this in utility project or cloudformation2kotlin - already copied from schemact3,2,1

import com.amazonaws.AmazonClientException
import com.amazonaws.AmazonServiceException
import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.regions.Regions
import com.amazonaws.services.cloudformation.AmazonCloudFormation
import com.amazonaws.services.cloudformation.AmazonCloudFormationClientBuilder
import com.amazonaws.services.cloudformation.model.*
import com.typedpath.awscloudformation.CloudFormationTemplate
import com.typedpath.awscloudformation.toYaml
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun defaultCredentialsProvider(): AWSCredentialsProvider {
    try {
        return ProfileCredentialsProvider()
    } catch (e: Exception) {
        throw AmazonClientException(
                "Cannot load the credentials from the credential profiles file. " +
                        "Please make sure that your credentials file is at the correct " +
                        "location (~/.aws/credentials), and is in valid format.",
                e
        )
    }
}

fun getStack(stackName: String, timeoutMiliSeconds: Int, stackbuilder: AmazonCloudFormation): Stack? {
    var completeStack: Stack? = null
    val describeStacksRequest = DescribeStacksRequest().withStackName(stackName);
    val startTime = System.currentTimeMillis()
    while (completeStack == null) {

        var theStacks: List<Stack>
        try {
            theStacks = stackbuilder.describeStacks(describeStacksRequest).stacks
        } catch (ex: Exception) {
            return null
        }

        val stackStatus = if (theStacks != null && theStacks.size > 0) theStacks.get(0).stackStatus.toUpperCase() else "unknown"
        if (!stackStatus.contains("PROGRESS")
        ) {
            completeStack = theStacks.get(0)
        } else {
            println("stack status: " + stackStatus)
            Thread.sleep(2000)
        }
        if ((System.currentTimeMillis() - startTime) > timeoutMiliSeconds) {
            println("timed out wait for stack")
            throw RuntimeException("times out waiting for stack $stackName to complete")
        }
    }
    return completeStack
}

private fun requestCreateStack(stackName: String, strTemplate: String, client: AmazonCloudFormation):
        CreateStackResult {
    // Create a stack
    val createRequest = CreateStackRequest()
    //createRequest.
    createRequest.setStackName(stackName)
    println(strTemplate)
    createRequest.setTemplateBody(strTemplate)
    println("Creating a stack called " + createRequest.getStackName() + ".")
    createRequest.withCapabilities(
            Capability.CAPABILITY_IAM,
            Capability.CAPABILITY_NAMED_IAM,
            Capability.CAPABILITY_AUTO_EXPAND
    )
    return client.createStack(createRequest)
}


private fun requestUpdateStackAcceptNoChange(stackName: String, templateBody: String?, templateUrl: String?, params: Collection<Parameter?>?,
                                             tags: Collection<Tag?>?, client: AmazonCloudFormation): UpdateStackResult? {
    try {
        return requestUpdateStack(
                stackName, templateBody, templateUrl, params,
                tags, client
        )
    } catch (ex: java.lang.Exception) {
        if (ex.message != null && ex.message!!.toLowerCase().contains("no updates are to be performed")) {
            return null
        } else {
            throw ex
        }
    }
}

private fun requestUpdateStack(
        stackName: String, templateBody: String?, templateUrl: String?, params: Collection<Parameter?>?,
        tags: Collection<Tag?>?, client: AmazonCloudFormation
): UpdateStackResult? {
    // Template validation error: No updates are to be performed
    val req = UpdateStackRequest()
            .withStackName(stackName)
            .withCapabilities(
                    Capability.CAPABILITY_IAM,
                    Capability.CAPABILITY_NAMED_IAM,
                    Capability.CAPABILITY_AUTO_EXPAND)
            .withParameters(params)
            .withTags(tags) /*from www. j  av  a 2 s . c om*/
    if (templateBody != null && !templateBody.isEmpty()) {
        req.templateBody = templateBody
    } else if (templateUrl != null && !templateUrl.isEmpty()) {
        req.templateURL = templateUrl
    } else {
        req.isUsePreviousTemplate = true
    }
    val result: UpdateStackResult = client.updateStack(req)
    return result
}

//TODO remmove this
fun createOrUpdateStack(
    template: CloudFormationTemplate, stackName: String, region: Regions = Regions.US_EAST_1,
    cleanup: Boolean = true,
    blockUpdate: Boolean = false,
    onSuccess: (
        credentialsProvider: AWSCredentialsProvider,
        outputs: List<Output>
    ) -> Unit = { a, b -> {} }
): Map<String, String> =
    createOrUpdateStackFromString( toYaml(template), stackName, region,cleanup,blockUpdate,onSuccess)

/**
 * this is based on https://github.com/aws/aws-sdk-java/tree/master/src/samples/AwsCloudFormation
 */
fun createOrUpdateStackFromString(
        strTemplate: String, stackName: String, region: Regions = Regions.US_EAST_1,
        cleanup: Boolean = true,
        blockUpdate: Boolean = false,
        onSuccess: (
                credentialsProvider: AWSCredentialsProvider,
                outputs: List<Output>
        ) -> Unit = { a, b -> {} }
): Map<String, String> {

    val credentialsProvider = defaultCredentialsProvider()

    val client = AmazonCloudFormationClientBuilder.standard()
            .withCredentials(credentialsProvider)
            .withRegion(region)
            .build()

    val logicalResourceName = "SampleNotificationTopic"

    try {

        val existingStack =
                getStack(stackName, 10000, client)

        println(strTemplate)

        if (blockUpdate) {
            if (existingStack==null) {
                throw Exception("updates blocked but stack not found")
            } else {
                return existingStack.outputs.map { Pair(it.outputKey, it.outputValue) }.toMap()
            }
        }
        else if (existingStack != null) {
            requestUpdateStackAcceptNoChange(
                    stackName = stackName,
                    templateBody = strTemplate,
                    templateUrl = null,
                    params = null,
                    tags = null,
                    client = client
            )
        } else {
            requestCreateStack(
                    stackName,
                    strTemplate,
                    client
            )
        }

        // Wait for stack to be created
        // Note that you could use SNS notifications on the CreateStack call to track the progress of the stack creation
        val waitResult = waitForCompletion(
                client,
                stackName
        )

        println(
                "Stack creation completed, the stack $stackName completed with ${waitResult.status} ${waitResult.statusReason}")

        if (waitResult.status.contains("FAILED") || waitResult.status.contains("ROLLBACK")) {
            throw Exception("stack changed failed: ${waitResult.status} ${waitResult.statusReason}")
        }

        // Show all the stacks for this account along with the resources for each stack
        // Lookup a resource by its logical name
        val logicalNameResourceRequest = DescribeStackResourcesRequest()
        logicalNameResourceRequest.setStackName(stackName)
        logicalNameResourceRequest.setLogicalResourceId(logicalResourceName)
        println("Looking up resource name ${logicalNameResourceRequest.getLogicalResourceId()} from stack ${logicalNameResourceRequest.getStackName()}")

        var completeStack: Stack? = null

        val timeoutMiliSeconds = 180 * 1000
        completeStack = getStack(
                stackName,
                timeoutMiliSeconds,
                client
        )
        if (completeStack == null) {
            throw Exception("failed to create stack $stackName in $timeoutMiliSeconds")
        }

        println("stack ${completeStack.stackName} status :  ${completeStack.stackStatus}")

        val outputs = completeStack.outputs

        onSuccess(credentialsProvider, outputs)

        return outputs.map { Pair(it.outputKey, it.outputValue) }.toMap()

    } catch (ase: AmazonServiceException) {
        println(("Caught an AmazonServiceException, which means your request made it " + "to AWS CloudFormation, but was rejected with an error response for some reason."))
        println("Error Message:    " + ase.message)
        println("HTTP Status Code: " + ase.statusCode)
        println("AWS Error Code:   " + ase.errorCode)
        println("Error Type:       " + ase.errorType)
        println("Request ID:       " + ase.requestId)
        throw RuntimeException(ase)
    } catch (ace: AmazonClientException) {
        println(
                ("Caught an AmazonClientException, which means the client encountered "
                        + "a serious internal problem while trying to communicate with AWS CloudFormation, "
                        + "such as not being able to access the network.")
        )
        println("Error Message: " + ace.message)
        throw RuntimeException(ace)
    } finally {
        if (cleanup) {
            // Delete the stack
            val deleteRequest = DeleteStackRequest()
            deleteRequest.setStackName(stackName)
            println("Deleting the stack called " + deleteRequest.getStackName() + ".")
            client.deleteStack(deleteRequest)

            // Wait for stack to be deleted
            // Note that you could used SNS notifications on the original CreateStack call to track the progress of the stack deletion
            println(
                    "Stack creation completed, the stack " + stackName + " completed with " + waitForCompletion(
                            client,
                            stackName
                    )
            )
        }
    }

}

fun defaultCurrentDateTimePattern() =
        "${(DateTimeFormatter.ofPattern("ddMMMyy-HHmmss")).format(LocalDateTime.now()).toLowerCase()}"

fun defaultStackName(cloudFormationTemplateClass: Class<*>): String =
        "${cloudFormationTemplateClass.name.toLowerCase().replace('.', '-')
                .replace('$', '-')}-${defaultCurrentDateTimePattern()}"

fun defaultStackName(cloudFormationTemplate: CloudFormationTemplate): String =
        defaultStackName(cloudFormationTemplate.javaClass)


// Wait for a stack to complete transitioning
// End stack states are:
//    CREATE_COMPLETE
//    CREATE_FAILED
//    DELETE_FAILED
//    ROLLBACK_FAILED
// OR the stack no longer exists

class WaitForCompletionResult(val status: String, val statusReason: String?)

@Throws(Exception::class)
fun waitForCompletion(stackbuilder: AmazonCloudFormation, stackName: String): WaitForCompletionResult {

    val wait = DescribeStacksRequest()
    wait.stackName = stackName
    var completed: Boolean = false
    var stackStatus = "Unknown"
    var stackReason: String? = ""

    print("Waiting")

    while (!completed) {
        var stacks: List<Stack>
        try {
            stacks = stackbuilder.describeStacks(wait).stacks
        } catch (ace: AmazonCloudFormationException) {
            //describe stacks bombs out if the stack doesnt exist
            if (ace.message!!.contains("does not exist")) {
                stacks = emptyList()
            } else throw ace
        }
        if (stacks.isEmpty()) {
            completed = true
            stackStatus = "NO_SUCH_STACK"
            stackReason = "Stack has been deleted"
        } else {
            for (stack in stacks) {
                if (stack.stackStatus == StackStatus.CREATE_COMPLETE.toString() ||
                        stack.stackStatus == StackStatus.UPDATE_COMPLETE.toString() ||
                        stack.stackStatus == StackStatus.CREATE_FAILED.toString() ||
                        stack.stackStatus == StackStatus.ROLLBACK_FAILED.toString() ||
                        stack.stackStatus == StackStatus.ROLLBACK_COMPLETE.toString() ||
                        stack.stackStatus == StackStatus.UPDATE_ROLLBACK_COMPLETE.toString() ||
                        stack.stackStatus == StackStatus.DELETE_FAILED.toString()
                ) {
                    completed = true
                    stackStatus = stack.stackStatus
                    try {
                        stackReason = stack.stackStatusReason
                    } catch (tw: Throwable) {
                        stackReason = tw.toString()
                    }
                }
            }
        }

        // Show we are waiting
        print(".")

        // Not done yet so sleep for 10 seconds.
        if (!completed) Thread.sleep(5000)
    }

    // Show we are done
    print("done\n")
//TODO return structure
    return WaitForCompletionResult(
            stackStatus,
            stackReason
    )
}
