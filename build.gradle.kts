import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar


plugins {
    id("java")
    id("com.gradleup.shadow") version "9.0.0"
}


fun String.withoutPatch(): String {
    val parts = this.split('-', limit = 2)
    val base = parts[0]
    val suffix = if (parts.size > 1) "-${parts[1]}" else ""
    val nums = base.split('.')
    val major = nums.getOrNull(0) ?: "0"
    val minor = nums.getOrNull(1) ?: "0"
    return "$major.$minor$suffix"
}

val rootVer = (findProperty("GITHUB_VERSION") as String?) ?: "1.5.0-DEV"
rootProject.version = rootVer
extra["VERSION_WITHOUT_PATCH"] = rootVer.withoutPatch()

subprojects {
    group = "it.einjojo.playerapi"
    version = if (name == "api") {
        rootProject.extra["VERSION_WITHOUT_PATCH"] as String
    } else {
        rootProject.version
    }


    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
    }


    apply(plugin = "java-library")
    apply(plugin = "com.gradleup.shadow")

    dependencies {
        compileOnly("org.jetbrains:annotations:26.0.2-1")
    }

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

    if (project.name == "api" || project.name == "playerapi") {
        tasks.named("shadowJar", ShadowJar::class) {
            enabled = false;
        }
    } else {
        tasks.named("shadowJar", ShadowJar::class) {
            destinationDirectory.set(rootProject.layout.buildDirectory.dir("libs"))
            archiveClassifier.set("")
            archiveBaseName.set("playerapi-${project.name}")
            relocate("io.grpc", "it.einjojo.playerapi.libs.grpc")
            relocate("com.google.protobuf", "it.einjojo.playerapi.libs.protobuf")
            relocate("io.lettuce", "it.einjojo.playerapi.libs.lettuce")
            mergeServiceFiles()
        }
    }
}
tasks {
    shadowJar {
        enabled = false;
    }
    jar {
        enabled = false;
    }
}

