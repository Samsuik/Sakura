pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenLocal()
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
}

rootProject.name = "sakura"

include("sakura-api", "sakura-server")

include(":slime-api")
include(":slime-core")

include(":slime-loaders")
project(":slime-loaders").projectDir = file("AdvancedSlimePaper-mc-1.21.4/loaders")

include(":slime-loaders:file-loader")
project(":slime-loaders:file-loader").projectDir = file("AdvancedSlimePaper-mc-1.21.4/loaders/file-loader")

include(":slime-loaders:api-loader")
project(":slime-loaders:api-loader").projectDir = file("AdvancedSlimePaper-mc-1.21.4/loaders/api-loader")

include(":slime-loaders:mongo-loader")
project(":slime-loaders:mongo-loader").projectDir = file("AdvancedSlimePaper-mc-1.21.4/loaders/mongo-loader")

include(":slime-loaders:mysql-loader")
project(":slime-loaders:mysql-loader").projectDir = file("AdvancedSlimePaper-mc-1.21.4/loaders/mysql-loader")

include(":slime-loaders:redis-loader")
project(":slime-loaders:redis-loader").projectDir = file("AdvancedSlimePaper-mc-1.21.4/loaders/redis-loader")

include(":slime-plugin")
project(":slime-plugin").projectDir = file("AdvancedSlimePaper-mc-1.21.4/plugin")

include(":slime-importer")
project(":slime-importer").projectDir = file("AdvancedSlimePaper-mc-1.21.4/importer")
