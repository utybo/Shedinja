package guru.zoroark.shedinja.test.check

import guru.zoroark.shedinja.InternalErrorException
import guru.zoroark.shedinja.dsl.ShedinjaDsl
import guru.zoroark.shedinja.dsl.put
import guru.zoroark.shedinja.dsl.shedinja
import guru.zoroark.shedinja.environment.EnvironmentBasedScope
import guru.zoroark.shedinja.environment.EnvironmentContext
import guru.zoroark.shedinja.environment.Identifier
import guru.zoroark.shedinja.environment.InjectionEnvironment
import guru.zoroark.shedinja.environment.InjectionEnvironmentKind
import guru.zoroark.shedinja.environment.Injector
import guru.zoroark.shedinja.environment.ScopedContext
import java.lang.reflect.InvocationTargetException
import kotlin.reflect.KProperty

private class CrashOnUseEnvironment(context: EnvironmentContext) : InjectionEnvironment {
    override fun <T : Any> getOrNull(identifier: Identifier<T>): T? {
        throw ShedinjaCheckException("getOrNull is not implemented and cannot be used during 'safeInjection' checks.")
    }

    companion object : InjectionEnvironmentKind<CrashOnUseEnvironment> {
        override fun build(context: EnvironmentContext): CrashOnUseEnvironment =
            CrashOnUseEnvironment(context)
    }

    private lateinit var currentInstantiation: Identifier<*>

    init {
        context.declarations.forEach { (i, v) ->
            currentInstantiation = i
            v.supplier(ScopedContext(EnvironmentBasedScope(this)))
        }
    }

    private fun generateErrorMessage(to: String): String =
        """
        'safeInjection' check failed.
        The following injection is done during the instantiation of $currentInstantiation:
            $currentInstantiation
        --> $to

        You *must not* actually perform injections during the instantiation of objects.
        If you need to do something on an object provided by an environment before storing it as a property, use ^
        'wrapIn' instead. See the documentation on the 'safeInjection' check for more details.
        """.trimIndent().replace("^\n", "")

    private inner class TrapInjector<T : Any>(private val target: Identifier<T>) : Injector<T> {
        override fun getValue(thisRef: Any?, property: KProperty<*>): T {
            throw ShedinjaCheckException(generateErrorMessage(target.toString()))
        }
    }

    override fun <T : Any> createInjector(identifier: Identifier<T>, onInjection: (T) -> Unit): Injector<T> {
        return TrapInjector(identifier)
    }
}

/**
 * Check that verifies no injection is actually performed during the instantiation of components.
 */
@ShedinjaDsl
val safeInjection = IndividualCheck { modules ->
    try {
        shedinja(CrashOnUseEnvironment) {
            modules.forEach { put(it) }
        }
    } catch (ex: InvocationTargetException) {
        val original = ex.getUpperCause<ShedinjaCheckException>()
        throw original ?: throw InternalErrorException("Unexpected error in 'safeInjection' check", ex)
    }
}

private inline fun <reified T : Throwable> InvocationTargetException.getUpperCause(): T? {
    var upper = cause
    while (upper != null && upper !is T) {
        upper = upper.cause
    }
    return if (upper == null) null else upper as T
}
