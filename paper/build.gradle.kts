plugins {
    id("java")
    id("de.eldoria.plugin-yml.paper") version "0.7.1"
}


dependencies {
    implementation(project(":core"))
    compileOnly("io.papermc.paper:paper-api:1.21.8-R0.1-SNAPSHOT")
    paperLibrary("io.lettuce:lettuce-core:6.8.1.RELEASE")
}

paper {
    name = "Abilities"
    main = "it.einjojo.playerapi.PaperPlayerApiProviderPlugin"
    authors = listOf("EinJOJO")
    description = "Abilities"
    apiVersion = "1.21"
    generateLibrariesJson = true
    loader = "it.einjojo.playerapi.PluginLibrariesLoader"
}
