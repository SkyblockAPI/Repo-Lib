pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.kikugie.dev/snapshots")
        maven("https://maven.fabricmc.net/")
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
    id("dev.kikugie.stonecutter") version "0.7.10"
}

rootProject.name = "repo-lib"


buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("com.google.code.gson:gson:2.13.1")
    }
}

include("exporter")
include("minecraft")
include("core")


val versions = listOf("1.21.11", "1.21.10", "1.21.9", "1.21.8", "1.21.7", "1.21.6", "1.21.5")

stonecutter {
    create(project(":minecraft")) {
        versions(versions)
        vcsVersion = versions.first()
    }
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files(rootProject.projectDir.resolve("version-catalogues/libs.versions.toml")))
        }

        versions.forEach {
            val name = it.replace(".", "")
            println("creating version catalogue libs$name")
            create("libs$name") {
                from(
                    files(
                        rootProject.projectDir.resolve("version-catalogues/${it.replace(".", "_")}.versions.toml")
                    )
                )
            }
        }
    }
}