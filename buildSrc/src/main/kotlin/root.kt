import net.fabricmc.loom.api.LoomGradleExtensionAPI
import org.gradle.api.Project
import org.gradle.api.artifacts.ExternalModuleDependencyBundle
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionConstraint
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.attributes.AttributeDisambiguationRule
import org.gradle.api.attributes.MultipleCandidatesDetails
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import org.gradle.language.jvm.tasks.ProcessResources
import org.gradle.plugin.use.PluginDependency
import java.nio.file.Path
import java.util.*
import javax.inject.Inject
import kotlin.io.path.exists

data class ForwardingVersionCatalog(
    val catalogs: List<VersionCatalog>,
) {
    constructor(vararg catalogs: VersionCatalog) : this(listOf(*catalogs))

    private fun <T> first(name: String, lookup: VersionCatalog.(String) -> Optional<T>): T {
        return catalogs.firstNotNullOf { it.lookup(name).orElse(null) }
    }

    val libraries: ForwardingProperty<Provider<MinimalExternalModuleDependency>> =
        ForwardingProperty(this, VersionCatalog::findLibrary)
    val bundles: ForwardingProperty<Provider<ExternalModuleDependencyBundle>> =
        ForwardingProperty(this, VersionCatalog::findBundle)
    val plugins: ForwardingProperty<Provider<PluginDependency>> = ForwardingProperty(this, VersionCatalog::findPlugin)
    val versions: ForwardingProperty<VersionConstraint> = ForwardingProperty(this, VersionCatalog::findVersion)

    fun library(name: String): Provider<MinimalExternalModuleDependency> = first(name, VersionCatalog::findLibrary)
    fun bundle(name: String): Provider<ExternalModuleDependencyBundle> = first(name, VersionCatalog::findBundle)
    fun plugin(name: String): Provider<PluginDependency> = first(name, VersionCatalog::findPlugin)
    fun version(name: String): VersionConstraint = first(name, VersionCatalog::findVersion)

    operator fun get(name: String): Provider<MinimalExternalModuleDependency> = library(name)

    data class ForwardingProperty<T>(
        val parent: ForwardingVersionCatalog,
        val lookup: VersionCatalog.(String) -> Optional<T>,
    ) {
        operator fun get(name: String): T = parent.first(name, lookup)
        fun getOrFallback(
            name: String,
            fallbackName: String,
        ) = runCatching { this[name] }.getOrElse { this[fallbackName] }
    }
}

internal val entries: MutableMap<Project, ForwardingVersionCatalog> = mutableMapOf()
internal val accessWideners: MutableMap<Project, Path> = mutableMapOf()
val Project.versionedCatalog get() = entries[this] ?: ForwardingVersionCatalog()

data class ClocheDisambiguationRule @Inject constructor(val version: String) : AttributeDisambiguationRule<String> {
    override fun execute(canidate: MultipleCandidatesDetails<String>) {
        canidate.closestMatch(version)
    }
}

fun DependencyHandler.includeImplementation(dep: Any) {
    add("include", dep)
    add("modImplementation", dep)
}

fun Project.accessWidener(path: RegularFile) {
    if (!path.asFile.toPath().exists()) return
    this.extensions.getByType<LoomGradleExtensionAPI>().accessWidenerPath.set(path)

    tasks.withType<ProcessResources>().configureEach {
        with(copySpec {
            from(path)
        })
    }
}