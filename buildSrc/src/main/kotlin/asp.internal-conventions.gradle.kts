import net.kyori.indra.git.IndraGitExtension
import org.gradle.kotlin.dsl.the

plugins {
    id("net.kyori.indra.git")
}

version = "${rootProject.providers.gradleProperty("version").get()}-${the<IndraGitExtension>().commit()?.name ?: "SNAPSHOT"}"
