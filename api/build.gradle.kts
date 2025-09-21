plugins {
    id("maven-publish")
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
            pom {
                name.set("playerapi")
                description.set("manage players")
                url.set("https://einjojo.it/work/springx")


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
            name = "einjojoReleases"
            url = uri("https://repo.einjojo.it/releases")
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