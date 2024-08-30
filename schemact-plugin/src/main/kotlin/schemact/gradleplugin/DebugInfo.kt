package schemact.gradleplugin

import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

object DebugInfo {
    fun printProjectInfo(project: Project) {
        val sourceSets = project.extensions.getByType(KotlinJvmProjectExtension::class.java).sourceSets
        // val sourceSet = sourceSets.create("schemactgen")
        sourceSets.forEach {
            it.kotlin.srcDirs.forEach {
                println(" name:${it.name}   srcDir: ${it.absolutePath} ")
            }
            it.kotlin.forEach {
                println("   source:   ${it.absolutePath}")
            }
        }
        val jSourceSets = project.extensions.getByType(JavaPluginExtension::class.java).sourceSets
        jSourceSets.forEach {
            println("j source set ${it.name} output: ${it.output.dirs.map { it.absolutePath }.joinToString (",")}")
        }

        project.configurations.forEach {
            println("config : ${it.name} ${it.javaClass}")
        }

        project.configurations.getByName("runtimeClasspath").resolve().forEach {
            println("   runtimeClasspath artifact: ${it.name} $it")
        }

        project.configurations.getByName("kotlinCompilerClasspath").resolve().forEach {
            println("   kotlinCompilerClasspath artifact: ${it.name} $it")
        }
        project.tasks.forEach {
            println("   task : ${it.name} $it")
        }
    }
}