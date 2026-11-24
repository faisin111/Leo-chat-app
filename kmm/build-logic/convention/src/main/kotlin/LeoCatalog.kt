import org.gradle.api.Project
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.getByType

/** `libs` version catalog for convention plugins. */
internal val Project.catalog: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

internal fun Project.lib(alias: String): Provider<MinimalExternalModuleDependency> =
    catalog.findLibrary(alias).orElseThrow { IllegalArgumentException("Missing catalog library '$alias'") }

/** `:feature:chat` → `com.leochat.feature.chat`. */
internal val Project.leoNamespace: String
    get() = "com.leochat" + path.replace(':', '.').replace('-', '_')
