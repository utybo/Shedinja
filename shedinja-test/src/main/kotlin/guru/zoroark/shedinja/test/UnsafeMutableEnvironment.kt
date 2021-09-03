package guru.zoroark.shedinja.test

import guru.zoroark.shedinja.dsl.ContextBuilderDsl
import guru.zoroark.shedinja.dsl.ShedinjaDsl
import guru.zoroark.shedinja.dsl.put
import guru.zoroark.shedinja.environment.Declaration
import guru.zoroark.shedinja.environment.EnvironmentBasedScope
import guru.zoroark.shedinja.environment.EnvironmentContext
import guru.zoroark.shedinja.environment.Identifier
import guru.zoroark.shedinja.environment.InjectionEnvironment
import guru.zoroark.shedinja.environment.InjectionEnvironmentKind
import guru.zoroark.shedinja.environment.Injector
import guru.zoroark.shedinja.environment.ScopedContext
import guru.zoroark.shedinja.environment.ensureInstance
import kotlin.reflect.KProperty

/**
 * A fully mutable environment implementation with very little safety.
 *
 * This class **should not be used in main code** and only within test code that typically requires more flexibility
 * and the ability to add elements on the fly.
 *
 * You should use the class within a [ShedinjaBaseTest], although it is not a requirement.
 *
 * ### Mutability
 *
 * You can mutate this environment (i.e. add components) by using any [put] function you are already used to. You can
 * `put` components and modules.
 *
 * ### Characteristics
 *
 * - **Eager object creation**. Objects are created upon construction of this environment.
 * - **Active object injection**. Objects are re-injected at every use.
 * - **NI/Mutable**. Objects can be replaced, injection methods will not always return the same thing.
 */
class UnsafeMutableEnvironment(baseContext: EnvironmentContext) : InjectionEnvironment, ContextBuilderDsl {
    companion object : InjectionEnvironmentKind<UnsafeMutableEnvironment> {
        override fun build(context: EnvironmentContext) = UnsafeMutableEnvironment(context)
    }

    private inner class UMEInjector<T : Any>(
        private val identifier: Identifier<T>,
        private val onInjection: (T) -> Unit
    ) : Injector<T> {
        override fun getValue(thisRef: Any?, property: KProperty<*>): T {
            return get(identifier).also(onInjection)
        }
    }

    private val components = baseContext.declarations.mapValues { (_, decl) ->
        decl.supplier(ScopedContext(EnvironmentBasedScope(this)))
    }.toMutableMap()

    override fun <T : Any> getOrNull(identifier: Identifier<T>): T? =
        components[identifier]?.let { ensureInstance(identifier.kclass, it) }

    override fun <T : Any> createInjector(identifier: Identifier<T>, onInjection: (T) -> Unit): Injector<T> =
        UMEInjector(identifier, onInjection)

    override fun <T : Any> put(declaration: Declaration<T>) {
        components[declaration.identifier] =
            declaration.supplier(ScopedContext(EnvironmentBasedScope(this)))
    }

    @ShedinjaDsl
    inline fun <reified T : Any> T.alsoPut(): T =
        also { put { this@alsoPut } }
}
