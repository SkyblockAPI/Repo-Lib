import com.google.gson.JsonParser
import java.net.URI
import java.nio.file.StandardOpenOption
import kotlin.io.path.*

plugins {
    java
    id("maven-publish")
}

version = "1.6.0"
group = "tech.thatgravyboat.repo-lib"

repositories {
    mavenCentral()
    maven("https://maven.fabricmc.net/")
    maven("https://repo.spongepowered.org/maven")
    maven("https://maven.neoforged.net/releases")
}

val fabric: SourceSet by sourceSets.creating {
    compileClasspath += sourceSets.main.get().output
}

val neoforge: SourceSet by sourceSets.creating {
    compileClasspath += sourceSets.main.get().output
}

dependencies {
    implementation("org.jetbrains:annotations:24.1.0")
    implementation("com.google.code.gson:gson:2.10")

    "fabricImplementation"("net.fabricmc:fabric-loader:0.15.0") { isTransitive = false}

    "neoforgeImplementation"("net.neoforged.fancymodloader:loader:3.0.13") { isTransitive = false}
}

tasks.jar {
    from(fabric.output)
    from(neoforge.output)
}

tasks.register<Jar>("sourcesJar") {
    group = "build"
    archiveClassifier.set("sources")
    sourceSets.map { it.allSource }.forEach {
        from(it)
    }
}

val baseUrl = "https://raw.githubusercontent.com/SkyblockAPI/Repo/refs/heads/main/cloudflare"

val downloadRepo = tasks.create("downloadRepo") {
    val outDir = layout.buildDirectory.dir("backup_repo")
    val outDirPath = outDir.get().asFile.toPath().resolve("backup")
    outputs.dir(outDir)
    outputs.upToDateWhen { false }

    fun download(path: String): String = URI.create("$baseUrl/$path").toURL().readText()

    fun getRepoPaths(): List<String> {
        val json = JsonParser.parseString(project.file("repo.json").readText()).asJsonObject
        return listOf(
            json.getAsJsonArray("static").map { it.asString },
            json.getAsJsonArray("versioned").flatMap {
                json.getAsJsonArray("versions").map { version ->
                    "${version.asString}/${it.asString}"
                }
            }
        ).flatten()
    }

    doFirst {
        logger.info("Downloading backup repo!")

        getRepoPaths().forEach { constant ->
            val file = outDirPath.resolve(constant)
            val content = download(constant)
            if (file.parent.notExists()) {
                file.parent.createDirectories()
            }
            file.writeText(
                content,
                Charsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
            )
        }
    }
}

sourceSets.main.configure {
    resources.srcDir(downloadRepo.outputs)
}

tasks.build.configure {
    this.dependsOn(downloadRepo);
    this.mustRunAfter(downloadRepo)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = "repo-lib"
            from(components["java"])

            pom {
                name.set("Repo-Lib")
                url.set("https://github.com/SkyblockAPI/Repo-Lib")

                scm {
                    connection.set("git:https://github.com/SkyblockAPI/Repo-Lib.git")
                    developerConnection.set("git:https://github.com/SkyblockAPI/Repo-Lib.git")
                    url.set("https://github.com/SkyblockAPI/Repo-Lib")
                }
            }
        }
    }
    repositories {
        maven {
            setUrl("https://maven.teamresourceful.com/repository/thatgravyboat/")
            credentials {
                username = System.getenv("MAVEN_USER") ?: providers.gradleProperty("maven_username").orNull
                password = System.getenv("MAVEN_PASS") ?: providers.gradleProperty("maven_password").orNull
            }
        }
    }
}