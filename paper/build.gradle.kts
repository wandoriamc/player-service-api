plugins {
    id("java")
    id("de.eldoria.plugin-yml.paper") version "0.7.1"
    id("xyz.jpenilla.run-paper") version "2.3.1"
}


dependencies {
    implementation(project(":core"))
    compileOnly("io.papermc.paper:paper-api:1.21.8-R0.1-SNAPSHOT")
    compileOnly("org.jetbrains:annotations:24.0.0")
    paperLibrary("io.lettuce:lettuce-core:6.8.1.RELEASE")
}

paper {
    name = "PlayerApi"
    main = "it.einjojo.playerapi.PaperPlayerApiProviderPlugin"
    authors = listOf("EinJOJO")
    description = "Player Service Api"
    apiVersion = "1.21"
    generateLibrariesJson = true
    loader = "it.einjojo.playerapi.PluginLibrariesLoader"

}

tasks {
    runServer {
        minecraftVersion("1.21.4")
    }
}