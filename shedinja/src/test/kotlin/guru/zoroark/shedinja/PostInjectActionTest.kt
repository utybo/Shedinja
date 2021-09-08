package guru.zoroark.shedinja

import guru.zoroark.shedinja.dsl.ContextBuilderDsl
import guru.zoroark.shedinja.dsl.ShedinjaDsl
import guru.zoroark.shedinja.dsl.put
import guru.zoroark.shedinja.dsl.shedinja
import guru.zoroark.shedinja.dsl.shedinjaModule
import guru.zoroark.shedinja.environment.InjectionScope
import guru.zoroark.shedinja.environment.Qualifier
import guru.zoroark.shedinja.environment.get
import guru.zoroark.shedinja.environment.invoke
import org.junit.jupiter.api.Test
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.test.assertEquals

// TODO Put the interesting stuff in main, not in tests

fun interface InjectableFactory<T : Any> {
    fun make(requestor: Any): T
}

class PostInjectActionTest {
    interface WhatIsYourA {
        val name: String
        fun whatIsYourA(): String
    }

    interface WhatIsYourLogger {
        val loggerName: String
        fun whatIsYourLogger(): String
    }

    class A(who: String) {
        val identity = "I am $who's A"
    }

    class Logger(origin: String) {
        val identity = "I am $origin's Logger"
    }

    class B(scope: InjectionScope) : WhatIsYourA, WhatIsYourLogger {
        override val name = "B"
        override val loggerName = "log.B"
        private val a: A by factory from scope
        private val logger: Logger by factory from scope

        override fun whatIsYourA(): String = "My A is ${a.identity}"
        override fun whatIsYourLogger(): String = "My Logger is ${logger.identity}"
    }

    class C(scope: InjectionScope) : WhatIsYourA, WhatIsYourLogger {
        override val name = "C"
        override val loggerName = "log.C"
        private val a: A by factory from scope
        private val logger: Logger by factory from scope

        override fun whatIsYourA(): String = "My A is ${a.identity}"
        override fun whatIsYourLogger(): String = "My Logger is ${logger.identity}"
    }

    class D(scope: InjectionScope) : WhatIsYourA {
        override val name = "D"
        private val a: A by factory from scope

        override fun whatIsYourA(): String = "My A is ${a.identity}"
    }

    class E(scope: InjectionScope) : WhatIsYourLogger {
        override val loggerName  = "log.E"
        private val logger: Logger by factory from scope

        override fun whatIsYourLogger(): String = "My Logger is ${logger.identity}"
    }

    @Test
    fun `Test factory system`() {
        val module = shedinjaModule {
            putFactory { requestor ->
                A((requestor as WhatIsYourA).name)
            }
            put(::B)
            put(::C)
            put(::D)
        }
        val env = shedinja { put(module) }

        mapOf(
            env.get<B>() to "My A is I am B's A",
            env.get<C>() to "My A is I am C's A",
            env.get<D>() to "My A is I am D's A"
        ).forEach { (k, v) ->
            assertEquals(k.whatIsYourA(), v)
        }
    }

    @Test
    fun `Test factory with double stuff system`() {
        val module = shedinjaModule {
            putFactory { requestor ->
                A((requestor as WhatIsYourA).name)
            }
            putFactory { requestor ->
                Logger((requestor as WhatIsYourLogger).loggerName)
            }

            put(::B)
            put(::C)
            put(::D)
            put(::E)
        }
        val env = shedinja { put(module) }

        mapOf(
            env.get<B>() to "My A is I am B's A",
            env.get<C>() to "My A is I am C's A",
            env.get<D>() to "My A is I am D's A"
        ).forEach { (k, v) ->
            assertEquals(k.whatIsYourA(), v)
        }

        mapOf(
            env.get<B>() to "My Logger is I am log.B's Logger",
            env.get<C>() to "My Logger is I am log.C's Logger",
            env.get<E>() to "My Logger is I am log.E's Logger"
        ).forEach { (k, v) ->
            assertEquals(k.whatIsYourLogger(), v)
        }
    }
}

// Environment management

data class InjectableFactoryOutputTypeQualifier(val outputs: KClass<*>) : Qualifier {
    override fun toString(): String = "Factory with output $outputs"
}

@ShedinjaDsl
fun outputs(output: KClass<*>) = InjectableFactoryOutputTypeQualifier(output)

// Creation in module

@ShedinjaDsl
inline fun <reified T : Any> ContextBuilderDsl.putFactory(crossinline block: (Any) -> T) {
    put(outputs(T::class)) { InjectableFactory { block(it) } }
}

// Injection DSL

class FactoryDsl(val ofObject: Any)

@ShedinjaDsl
val Any.factory
    get() = FactoryDsl(this)

@ShedinjaDsl
inline infix fun <R, reified T : Any> FactoryDsl.from(scope: InjectionScope): ReadOnlyProperty<R, T> =
    scope<InjectableFactory<T>>(outputs(T::class)) wrapIn { it.make(ofObject) }

// Utilities

infix fun <T, V, R> ReadOnlyProperty<T, V>.wrapIn(mapper: (V) -> R): WrappedReadOnlyProperty<T, V, R> =
    WrappedReadOnlyProperty(this, mapper)

class WrappedReadOnlyProperty<T, V, R>(
    private val original: ReadOnlyProperty<T, V>,
    private val mapper: (V) -> R
) : ReadOnlyProperty<T, R> {
    override fun getValue(thisRef: T, property: KProperty<*>): R =
        mapper(original.getValue(thisRef, property))
}
