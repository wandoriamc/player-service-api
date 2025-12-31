import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar


plugins {
    id("java")
    id("com.gradleup.shadow") version "9.0.0"
    id("me.qoomon.git-versioning") version "6.4.4"
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

rootProject.version = version
extra["VERSION_WITHOUT_PATCH"] = rootProject.version.toString().withoutPatch()

subprojects {
    apply(plugin = "java-library")
    apply(plugin = "com.gradleup.shadow")
    apply(plugin = "me.qoomon.git-versioning")
    group = "it.einjojo.playerapi"
    version = "1.5.0"
    gitVersioning.apply {
        refs {
            tag("v(?<version>.*)") {
                version = if (name == "api") {
                    "\${ref.version.major}.\${ref.version.minor}"
                } else {
                    "\${ref.version}"
                }
            }
        }
        rev {
            version = if (name == "api") {
                "\${version.major}.\${version.minor}-\${commit.short}"
            } else {
                "\${version}-\${commit.short}"
            }
        }
    }

    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
    }




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

