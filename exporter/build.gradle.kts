@file:Suppress("UnstableApiUsage")

plugins {
    id("repo-loom")
}

dependencies {
    minecraft("com.mojang:minecraft:1.21.11")
    mappings(loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-1.21.9:2025.10.05@zip")
    })
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

