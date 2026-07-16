@file:Suppress("UnstableApiUsage")

plugins {
    id("repo-loom")
}

dependencies {
    minecraft("com.mojang:minecraft:26.2")
}

accessWidener {
    rootProject.file("src/repoExporter.accesswidener")
}

ksp {
    arg("meowdding.project_name", "RepoExporter")
    arg("meowdding.package", "tech.thatgravyboat.repo.exporter.generated")
}

val archiveName = "RepoExporter"

base {
    archivesName.set("$archiveName-${archivesName.get()}")
}

idea {
    module {
        isDownloadJavadoc = true
        isDownloadSources = true

        excludeDirs.add(file("run"))
    }
}

