dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("../version-catalogues/libs.versions.toml"))
        }
    }
}

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.fabricmc.net")
    }
}

rootProject.name = "buildSrc"
