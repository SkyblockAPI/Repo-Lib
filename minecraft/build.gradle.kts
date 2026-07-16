@file:Suppress("UnstableApiUsage")

plugins {
    `repo-loom`
    `versioned-catalogues`
}

dependencies {
    minecraft(versionedCatalog["minecraft"])
}

accessWidener {
    rootProject.project(":minecraft").file("repo-lib.accesswidener")
}

val archiveName = "RepoLibMinecraft"

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

