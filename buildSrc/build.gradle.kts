plugins {
    `kotlin-dsl`
}

repositories {
    mavenLocal()
    mavenCentral()
    gradlePluginPortal()
    maven("https://repo.papermc.io/repository/maven-public/")
}

fun convertPlugin(plugin: Provider<PluginDependency>): String {
    val id = plugin.get().pluginId
    return "$id:$id.gradle.plugin:${plugin.get().version}"
}

dependencies {
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
    implementation(convertPlugin(libs.plugins.indragit))
    implementation(convertPlugin(libs.plugins.lombok))
    implementation(convertPlugin(libs.plugins.plugin.yml.paper))
    implementation(convertPlugin(libs.plugins.shadow))
}
