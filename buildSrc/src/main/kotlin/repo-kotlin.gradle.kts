import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.expand
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.kotlin
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import org.gradle.language.jvm.tasks.ProcessResources
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.ByteArrayOutputStream

plugins {
    id("com.google.devtools.ksp")
    kotlin("jvm")
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions.jvmTarget.set(JvmTarget.JVM_21)
    compilerOptions.optIn.add("kotlin.time.ExperimentalTime")
    compilerOptions.freeCompilerArgs.add("-Xnullability-annotations=@org.jspecify.annotations:warn")
    compilerOptions.freeCompilerArgs.add("-Xwhen-guards")
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(21)
}

extensions.getByType<JavaPluginExtension>().apply {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
    toolchain.languageVersion = JavaLanguageVersion.of(21)
    withSourcesJar()
}

val gitRef = tasks.register<Exec>("gitRef") {
    outputs.upToDateWhen { false }
    standardOutput = ByteArrayOutputStream()
    commandLine("git", "rev-parse", "HEAD")
}

val gitBranch = tasks.register<Exec>("getBranch") {
    outputs.upToDateWhen { false }
    standardOutput = ByteArrayOutputStream()
    commandLine("git", "rev-parse", "--abbrev-ref", "HEAD")
}


tasks.withType<ProcessResources>() {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    dependsOn(gitRef, gitBranch)
    mustRunAfter(gitRef, gitBranch)

    outputs.upToDateWhen { false }

    filesMatching("build_info.json") {
        expand(
            "branch" to gitBranch.map { it.standardOutput.toString().substringBefore("\n") }.get(),
            "ref" to gitRef.map { it.standardOutput.toString().substringBefore("\n") }.get(),
            "build_time" to provider { System.currentTimeMillis() }.get(),
        )
    }
}
