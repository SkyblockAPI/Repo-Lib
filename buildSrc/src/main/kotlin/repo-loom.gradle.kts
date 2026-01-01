import net.fabricmc.loom.api.LoomGradleExtensionAPI
import net.fabricmc.loom.task.ValidateAccessWidenerTask

plugins {
    idea
    id("fabric-loom")
    id("repo-kotlin")
}

repositories {
    fun scopedMaven(url: String, vararg paths: String) =
        maven(url) { content { paths.forEach(::includeGroupAndSubgroups) } }

    scopedMaven("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1", "me.djtheredstoner")
    scopedMaven("https://repo.hypixel.net/repository/Hypixel", "net.hypixel")
    scopedMaven("https://maven.parchmentmc.org/", "org.parchmentmc")
    scopedMaven("https://api.modrinth.com/maven", "maven.modrinth")
    scopedMaven("https://maven.teamresourceful.com/repository/maven-public/", "tech.thatgravyboat", "me.owdding")
    scopedMaven("https://maven.nucleoid.xyz/", "eu.pb4")
    mavenCentral()
}

extensions.getByType<LoomGradleExtensionAPI>().apply {
    runConfigs["client"].apply {
        ideConfigGenerated(true)
    }
}

tasks.withType<ValidateAccessWidenerTask> { enabled = false }