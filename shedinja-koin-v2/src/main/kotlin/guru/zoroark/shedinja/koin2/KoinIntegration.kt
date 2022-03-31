package guru.zoroark.shedinja.koin2

import guru.zoroark.shedinja.InvalidDeclarationException
import guru.zoroark.shedinja.NotExtensibleException
import guru.zoroark.shedinja.dsl.ShedinjaDsl
import guru.zoroark.shedinja.environment.EmptyQualifier
import guru.zoroark.shedinja.environment.Identifier
import guru.zoroark.shedinja.environment.InjectableModule
import guru.zoroark.shedinja.environment.InjectionScope
import guru.zoroark.shedinja.environment.Injector
import guru.zoroark.shedinja.environment.MetalessInjectionScope
import guru.zoroark.shedinja.environment.NameQualifier
import guru.zoroark.shedinja.environment.ScopedContext
import org.koin.core.KoinApplication
import org.koin.core.definition.BeanDefinition
import org.koin.core.definition.Definitions
import org.koin.core.module.Module
import org.koin.core.qualifier.Qualifier
import org.koin.core.qualifier.StringQualifier
import org.koin.dsl.module
import kotlin.reflect.KProperty

/**
 * Adds the given Shedinja modules to this Koin application. This process is done transparently for you: you can inject
 * components from Koin modules into Shedinja modules and vice versa.
 *
 * See [toKoinModule] for more information.
 */
@ShedinjaDsl
fun KoinApplication.shedinjaModules(vararg modules: InjectableModule) {
    modules(modules.map { it.toKoinModule(this@shedinjaModules) })
}

/**
 * Turns this Shedinja module into a module that is uses the given Koin application for processing cross-framework
 * injections.
 *
 * Internally, Shedinja injects the components created in this module into the module (thus making Shedinja components
 * injectable from Koin) and supplies an [InjectionScope] that is backed by the given Koin application object to the
 * Shedinja object suppliers (thus making Koin components injectable from Koin).
 *
 * **NOTE:** This feature abuses the fact that `internal` properties annotated with `@PublishedApi` are effectively
 * public and can be accessed via reflection without any use of `setAccessible`. Such properties are byte-code public,
 * although the compiler will raise an error, thus making reflection needed.
 */
fun InjectableModule.toKoinModule(app: KoinApplication): Module = module {
    val definitions = this.reflectiveAccessTo<Module, HashSet<BeanDefinition<*>>>("getDefinitions")
    val moduleRootScope = this.reflectiveAccessTo<Module, Qualifier>("getRootScope")
    declarations.forEach { declaration ->
        definitions.add(
            Definitions.createSingle(
                declaration.identifier.kclass,
                declaration.identifier.qualifier.toKoinQualifier(),
                { declaration.supplier(ScopedContext(KoinApplicationBackedScope(app))) },
                makeOptions(false),
                emptyList(),
                moduleRootScope
            )
        )
    }
}

/**
 * Interface for Shedinja qualifier that provides a way of translating them to Koin v2 qualifiers.
 *
 * Note that:
 *
 * - This should only be implemented for custom qualifiers in applications that require the Shedinja-Koin
 * - Internally, [EmptyQualifier] and [NameQualifier] can already be converted to Koin's qualifiers.
 * - There is a naming conflict between `guru.zoroark.shedinja.environment.Qualifier` and
 * `org.koin.core.qualifier.Qualifier` which may cause issues. Consider using
 * [the `as` keyword](https://kotlinlang.org/docs/packages.html#imports) when importing these classes for solving this
 * conflict.
 */
interface KoinCompatibleQualifier : guru.zoroark.shedinja.environment.Qualifier {
    /**
     * Convert this qualifier to a Koin qualifier.
     */
    fun toKoinQualifier(): Qualifier
}

private fun guru.zoroark.shedinja.environment.Qualifier.toKoinQualifier(): Qualifier? =
    when (this) {
        is KoinCompatibleQualifier -> toKoinQualifier()
        EmptyQualifier -> null
        is NameQualifier -> StringQualifier(name)
        else -> throw InvalidDeclarationException(
            """
            The following Shedinja qualifier cannot be converted to Koin's qualifier type: ${this.javaClass.name}.
            You can resolve this by making ${this.javaClass.name} implement the 'KoinCompatibleQualifier` interface.
            """.trimIndent()
        )
    }

private class KoinApplicationBackedScope(private val app: KoinApplication) : InjectionScope {
    override fun <T : Any> inject(what: Identifier<T>): Injector<T> =
        KoinApplicatedBackedInjector(what, app)

    override val meta: MetalessInjectionScope
        get() = throw NotExtensibleException(
            "Injections from meta environments are not supported in Koin applications."
        )
}

private class KoinApplicatedBackedInjector<T : Any>(
    private val identifier: Identifier<T>,
    private val app: KoinApplication
) :
    Injector<T> {
    private val value by lazy {
        app.koin.get(identifier.kclass, identifier.qualifier.toKoinQualifier())
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): T = value
}

private inline fun <reified R, reified T> R.reflectiveAccessTo(propertyName: String): T {
    return R::class.java.getMethod(propertyName).invoke(this) as T
}
