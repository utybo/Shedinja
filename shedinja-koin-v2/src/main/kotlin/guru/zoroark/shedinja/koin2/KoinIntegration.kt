package guru.zoroark.shedinja.koin2

import guru.zoroark.shedinja.dsl.ShedinjaDsl
import guru.zoroark.shedinja.environment.Declaration
import guru.zoroark.shedinja.environment.Identifier
import guru.zoroark.shedinja.environment.InjectableModule
import guru.zoroark.shedinja.environment.InjectionScope
import guru.zoroark.shedinja.environment.Injector
import guru.zoroark.shedinja.environment.ScopedContext
import org.koin.core.KoinApplication
import org.koin.core.definition.BeanDefinition
import org.koin.core.definition.Definitions
import org.koin.core.module.Module
import org.koin.core.qualifier.Qualifier
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
    declarations.forEach {
        it.applyDeclarationTo(this@module, app)
    }
}

private fun <T : Any> Declaration<T>.applyDeclarationTo(module: Module, app: KoinApplication) {
    val definitions = module.reflectiveAccessTo<Module, HashSet<BeanDefinition<*>>>("getDefinitions")
    val moduleRootScope = module.reflectiveAccessTo<Module, Qualifier>("getRootScope")
    definitions.add(
        Definitions.createSingle(
            identifier.kclass,
            null, // TODO translate string qualifiers
            { supplier(ScopedContext(KoinApplicationBackedScope(app))) },
            module.makeOptions(false),
            emptyList(),
            moduleRootScope
        )
    )
}

private class KoinApplicationBackedScope(private val app: KoinApplication) : InjectionScope {
    override fun <T : Any> inject(what: Identifier<T>): Injector<T> =
        KoinApplicatedBackedInjector(what, app)
}

private class KoinApplicatedBackedInjector<T : Any>(private val identifier: Identifier<T>, private val app: KoinApplication) :
    Injector<T> {
    private val value by lazy {
        app.koin.get(identifier.kclass)
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): T = value
}

private inline fun <reified R, reified T> R.reflectiveAccessTo(propertyName: String): T {
    return R::class.java.getMethod(propertyName).invoke(this) as T
}


