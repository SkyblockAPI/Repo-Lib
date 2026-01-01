plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    kotlin("plugin.serialization") version "2.2.0"
}

repositories {
    gradlePluginPortal()
    maven("https://maven.fabricmc.net")
    maven("https://maven.teamresourceful.com/repository/maven-public/")
}

dependencies {
    //implementation(libs.meowdding.resources)
    //implementation(libs.gson)
    implementation(plugin(libs.plugins.ksp))
    implementation(plugin(libs.plugins.loom))
    implementation(plugin(libs.plugins.kotlin.jvm))
    implementation("net.peanuuutz.tomlkt:tomlkt:0.5.0")
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1")
    //implementation(libs.guava)
}

fun plugin(plugin: Provider<PluginDependency>): Provider<String> =
    plugin.map { "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}" }