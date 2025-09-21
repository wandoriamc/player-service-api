import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar




plugins {
    id("java")
    id("com.gradleup.shadow") version "9.0.0"
}

subprojects {

    group = "it.einjojo.playerapi"
    version = "1.1.0"

    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
    }

    apply(plugin = "java-library")
    apply(plugin = "com.gradleup.shadow")

    java {
        toolchain.languageVersion.set(JavaLanguageVersion.of(21))
    }
    tasks.withType<JavaCompile> {
        options.release.set(21)
        options.encoding = "UTF-8"
    }
    tasks.named("assemble") {
        dependsOn(tasks.named("shadowJar"))
    }

    if (project.name != "api" && project.name != "playerapi") {
        tasks.named("shadowJar", ShadowJar::class) {
            destinationDirectory.set(rootProject.layout.buildDirectory.dir("libs"))
            relocate("io.grpc", "it.einjojo.playerapi.libs.grpc")
            mergeServiceFiles()
        }
    }
}


