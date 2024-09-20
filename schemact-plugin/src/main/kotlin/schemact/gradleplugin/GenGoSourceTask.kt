package schemact.gradleplugin

import org.gradle.api.Project
import schemact.domain.Domain
import schemact.domain.Module
import schemact.domain.Schemact
import schemact.domain.StaticWebsite
import schemact.gradleplugin.golang.templates.*
import java.io.File


object GenGoSourceTask {
    fun createGenGoSourceTask(
        project: Project, schemact: Schemact, domain: Domain, module: Module,
        staticWebSiteToSourceRoot: Map<StaticWebsite, File>?
    ) {
        System.out.println("createGenGoSourceTask")


        val sourceGenDir = "${project.projectDir}/${module.name}/schemactgosourcegen"

        project.tasks.create("${module.name}_genGoCode") { task ->
            task.group = TaskNaming.groupName(module)
            task.actions.add {
               createGenGoSource(sourceGenDir, module)
            }
        }
    }

private fun createGenGoSource(sourceGenDir: String, module: Module) {
    System.out.println("createGenGoSourceTask")
    File(sourceGenDir).mkdirs()

    File(sourceGenDir, "Makefile").printWriter().use { writer ->
        writer.write(Makefile(module))
    }
    File(sourceGenDir, "main.go").printWriter().use { writer ->
        writer.write(mainGo(module))
    }
    File(sourceGenDir, "go.mod").printWriter().use { writer ->
        writer.write(goMod(module))
    }
    val goModuleDir = File(sourceGenDir, "module")
    goModuleDir.mkdirs()
    File(goModuleDir, "${module.name}Int.go").printWriter().use { writer ->
        writer.write(goInterface(module))
    }
    module.functions.forEach {
        val functionSourceFile = File(goModuleDir, "${it.name}Handler.go")
        if (!functionSourceFile.exists()) {
            functionSourceFile.printWriter().use { writer ->
                writer.write(exampleGoHandler(it, module))
            }
        }

    }



}

}

