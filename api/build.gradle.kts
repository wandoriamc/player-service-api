plugins {
    id("maven-publish")
    signing
}


java {
    withSourcesJar()
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifactId = "playerapi"
            pom {
                name.set("playerapi")
                description.set("manage players")
                url.set("https://einjojo.it/")


                // Füge Entwicklerinformationen hinzu
                developers {
                    developer {
                        id.set("einjojo")
                        name.set("Johannes")
                        email.set("johannes@einjojo.it")
                    }
                }
            }
        }
    }
    repositories {
        maven {
            val snapshotsUri = uri("https://repo.einjojo.it/snapshots")
            val releasesUri = uri("https://repo.einjojo.it/snapshots")
            url = if (project.hasProperty("release")) releasesUri else snapshotsUri;
            credentials {
                username = System.getenv("REPO_USERNAME")
                password = System.getenv("REPO_PASSWORD")
            }
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }
}
signing {
    sign(publishing.publications["mavenJava"])
}
