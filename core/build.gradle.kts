import com.google.protobuf.gradle.id

plugins {
    id("java-library")
    id("com.google.protobuf") version "0.9.4"
}



dependencies {
    api(project(":api"))
    runtimeOnly("io.grpc:grpc-netty-shaded:1.74.0")
    api("io.grpc:grpc-protobuf:1.74.0")
    api("io.grpc:grpc-stub:1.74.0")
    compileOnly("org.apache.tomcat:annotations-api:6.0.53")
    compileOnly("com.google.code.gson:gson:2.11.0") // provided by either paper or velocity
    compileOnly("io.lettuce:lettuce-core:6.8.1.RELEASE")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.25.5"
    }
    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.74.0"
        }
    }
    generateProtoTasks {
        all().forEach {
            it.plugins {
                id("grpc") {
                    option("@generated=omit")
                }
            }
        }
    }
}


tasks.test {
    useJUnitPlatform()
}