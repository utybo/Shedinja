package guru.zoroark.shedinja.koin2

import guru.zoroark.shedinja.dsl.put
import guru.zoroark.shedinja.dsl.shedinjaModule
import guru.zoroark.shedinja.environment.InjectableModule
import guru.zoroark.shedinja.environment.InjectionScope
import guru.zoroark.shedinja.environment.invoke
import org.junit.jupiter.api.Test
import org.koin.core.KoinApplication
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import kotlin.test.assertEquals


@OptIn(KoinApiExtension::class)
class TestKoinIntegration {

    interface IdentityRetriever {
        fun koinGetAIdentity(): String
        fun koinComponentAIdentity(): String
        fun shedinjaAIdentity(): String
    }

    class KoinGetA {
        val identity = "I am KoinGetA"
    }

    class KoinGetB(
        private val koinGetA: KoinGetA,
        private val koinComponentA: KoinComponentA,
        private val shedinjaA: ShedinjaA
    ) : IdentityRetriever {
        override fun koinGetAIdentity() = koinGetA.identity + " via KoinGetB"
        override fun koinComponentAIdentity() = koinComponentA.identity + " via KoinGetB"
        override fun shedinjaAIdentity() = shedinjaA.identity + " via KoinGetB"
    }

    class KoinComponentA : KoinComponent {
        val identity = "I am KoinComponentA"
    }

    class KoinComponentB: KoinComponent, IdentityRetriever {
        private val koinGetA: KoinGetA by inject()
        private val koinComponentA: KoinComponentA by inject()
        private val shedinjaA: ShedinjaA by inject()

        override fun koinGetAIdentity() = koinGetA.identity + " via KoinComponentB"
        override fun koinComponentAIdentity() = koinComponentA.identity + " via KoinComponentB"
        override fun shedinjaAIdentity() = shedinjaA.identity + " via KoinComponentB"
    }

    class ShedinjaA {
        val identity = "I am ShedinjaA"
    }

    class ShedinjaB(scope: InjectionScope): IdentityRetriever {
        private val koinGetA: KoinGetA by scope()
        private val koinComponentA: KoinComponentA by scope()
        private val shedinjaA: ShedinjaA by scope()

        override fun koinGetAIdentity() = koinGetA.identity + " via ShedinjaB"
        override fun koinComponentAIdentity() = koinComponentA.identity + " via ShedinjaB"
        override fun shedinjaAIdentity() = shedinjaA.identity + " via ShedinjaB"
    }

    @Test
    fun `Test shedinja integration`() {
        val shedinja = shedinjaModule {
            put(::ShedinjaA)
            put(::ShedinjaB)
        }

        val koinGet = module {
            single { KoinGetA() }
            single { KoinGetB(get(), get(), get()) }
        }

        val koinComponent = module {
            single { KoinComponentA() }
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
            assertEquals("I am KoinComponentA via $name", obj.koinComponentAIdentity())
            assertEquals("I am ShedinjaA via $name", obj.shedinjaAIdentity())
        }

        stopKoin()
    }
}
