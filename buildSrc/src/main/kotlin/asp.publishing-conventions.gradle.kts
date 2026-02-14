import com.infernalsuite.asp.conventions.PublishConfiguration.Companion.publishConfiguration

plugins {
    `maven-publish`
}

val publishConfiguration = publishConfiguration()

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "${project.group}"
            artifactId = project.name
            version = "${project.version}"

            from(components["java"])

            pom {
                name.set(publishConfiguration.name)
                description.set(publishConfiguration.description)
            }
        }
    }
}
