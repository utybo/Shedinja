package guru.zoroark.shedinja.koin2

import guru.zoroark.shedinja.dsl.put
import guru.zoroark.shedinja.dsl.shedinjaModule
import guru.zoroark.shedinja.environment.InjectionScope
import guru.zoroark.shedinja.environment.invoke
import org.junit.jupiter.api.Test
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.qualifier.named
import org.koin.dsl.module
import kotlin.test.assertEquals

@OptIn(KoinApiExtension::class)
class TestKoinIntegration {

    interface IdentityRetriever {
        fun koinGetAIdentity(): String
        fun koinGetA2Identity(): String
        fun koinComponentAIdentity(): String
        fun koinComponentA2Identity(): String
        fun shedinjaAIdentity(): String
        fun shedinjaA2Identity(): String
    }

    class KoinGetA(str: String = "") {
        val identity = "I am KoinGetA$str"
    }

    class KoinGetB(
        private val koinGetA: KoinGetA,
        private val koinGetA2: KoinGetA,
        private val koinComponentA: KoinComponentA,
        private val koinComponentA2: KoinComponentA,
        private val shedinjaA: ShedinjaA,
        private val shedinjaA2: ShedinjaA
    ) : IdentityRetriever {
        override fun koinGetAIdentity() = koinGetA.identity + " via KoinGetB"
        override fun koinGetA2Identity() = koinGetA2.identity + " via KoinGetB"
        override fun koinComponentAIdentity() = koinComponentA.identity + " via KoinGetB"
        override fun koinComponentA2Identity() = koinComponentA2.identity + " via KoinGetB"
        override fun shedinjaAIdentity() = shedinjaA.identity + " via KoinGetB"
        override fun shedinjaA2Identity() = shedinjaA2.identity + " via KoinGetB"
    }

    class KoinComponentA(str: String = "") : KoinComponent {
        val identity = "I am KoinComponentA$str"
    }

    class KoinComponentB : KoinComponent, IdentityRetriever {
        private val koinGetA: KoinGetA by inject()
        private val koinGetA2: KoinGetA by inject(named("GA2"))
        private val koinComponentA: KoinComponentA by inject()
        private val koinComponentA2: KoinComponentA by inject(named("CA2"))
        private val shedinjaA: ShedinjaA by inject()
        private val shedinjaA2: ShedinjaA by inject(named("SA2"))

        override fun koinGetAIdentity() = koinGetA.identity + " via KoinComponentB"
        override fun koinGetA2Identity() = koinGetA2.identity + " via KoinComponentB"
        override fun koinComponentAIdentity() = koinComponentA.identity + " via KoinComponentB"
        override fun koinComponentA2Identity() = koinComponentA2.identity + " via KoinComponentB"
        override fun shedinjaAIdentity() = shedinjaA.identity + " via KoinComponentB"
        override fun shedinjaA2Identity() = shedinjaA2.identity + " via KoinComponentB"
    }

    class ShedinjaA(str: String = "") {
        val identity = "I am ShedinjaA$str"
    }

    class ShedinjaB(scope: InjectionScope) : IdentityRetriever {
        private val koinGetA: KoinGetA by scope()
        private val koinGetA2: KoinGetA by scope(guru.zoroark.shedinja.environment.named("GA2"))
        private val koinComponentA: KoinComponentA by scope()
        private val koinComponentA2: KoinComponentA by scope(guru.zoroark.shedinja.environment.named("CA2"))
        private val shedinjaA: ShedinjaA by scope()
        private val shedinjaA2: ShedinjaA by scope(guru.zoroark.shedinja.environment.named("SA2"))

        override fun koinGetAIdentity() = koinGetA.identity + " via ShedinjaB"
        override fun koinGetA2Identity() = koinGetA2.identity + " via ShedinjaB"
        override fun koinComponentAIdentity() = koinComponentA.identity + " via ShedinjaB"
        override fun koinComponentA2Identity() = koinComponentA2.identity + " via ShedinjaB"
        override fun shedinjaAIdentity() = shedinjaA.identity + " via ShedinjaB"
        override fun shedinjaA2Identity() = shedinjaA2.identity + " via ShedinjaB"
    }

    @Test
    fun `Test shedinja integration`() {
        val shedinja = shedinjaModule {
            put { ShedinjaA() }
            put(guru.zoroark.shedinja.environment.named("SA2")) { ShedinjaA("2") }
            put(::ShedinjaB)
        }

        val koinGet = module {
            single { KoinGetA() }
            single(named("GA2")) { KoinGetA("2") }
            single { KoinGetB(get(), get(named("GA2")), get(), get(named("CA2")), get(), get(named("SA2"))) }
        }

        val koinComponent = module {
            single { KoinComponentA() }
            single(named("CA2")) { KoinComponentA("2") }
            single { KoinComponentB() }
        }

        val koin = startKoin {
            modules(koinGet, koinComponent)

            shedinjaModules(shedinja)
        }.koin

        val retrievers = mapOf(
            "KoinGetB" to koin.get<KoinGetB>(),
            "KoinComponentB" to koin.get<KoinComponentB>(),
            "ShedinjaB" to koin.get<ShedinjaB>()
        )

        for ((name, obj) in retrievers) {
            assertEquals("I am KoinGetA via $name", obj.koinGetAIdentity())
            assertEquals("I am KoinGetA2 via $name", obj.koinGetA2Identity())
            assertEquals("I am KoinComponentA via $name", obj.koinComponentAIdentity())
            assertEquals("I am KoinComponentA2 via $name", obj.koinComponentA2Identity())
            assertEquals("I am ShedinjaA via $name", obj.shedinjaAIdentity())
            assertEquals("I am ShedinjaA2 via $name", obj.shedinjaA2Identity())
        }

        stopKoin()
    }
}
