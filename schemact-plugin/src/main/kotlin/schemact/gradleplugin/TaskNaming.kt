package schemact.gradleplugin

import schemact.domain.Deployment
import schemact.domain.Module

object TaskNaming {
    fun groupName(module: Module) : String = "${TASK_GROUP_NAME}_module_${module.name}"
    fun groupName(deployment: Deployment) : String = "${TASK_GROUP_NAME}_deployment_${deployment.subdomain}"
}