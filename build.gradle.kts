plugins {
    java
    id("maven-publish")
}

version = "1.1.0"
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

    "fabricImplementation"("net.fabricmc:fabric-loader:0.15.0")

    "neoforgeImplementation"("net.neoforged.fancymodloader:loader:3.0.13")
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