package guru.zoroark.shedinja

import guru.zoroark.shedinja.dsl.put
import guru.zoroark.shedinja.dsl.shedinja
import guru.zoroark.shedinja.environment.InjectionScope
import guru.zoroark.shedinja.environment.get
import guru.zoroark.shedinja.environment.named
import guru.zoroark.shedinja.extensions.external.external
import guru.zoroark.shedinja.extensions.external.from
import guru.zoroark.shedinja.extensions.external.getExternal
import guru.zoroark.shedinja.extensions.external.putExternal
import kotlin.test.Test
import kotlin.test.assertEquals

class ExternalExtensionTest {
    class ExternalThing(
        private val someValue: String,
        private val someList: List<Char>
    ) {
        fun makeString() = "${someList.joinToString(separator = "")} $someValue!"
    }

    class OtherExternalThing(
        private val someValue: String,
        private val someList: List<Char>
    ) {
        fun makeString() = "${someList.joinToString(separator = "")} $someValue!"
    }

    class RegularComponent(scope: InjectionScope) {
        private val externalThingRaw: ExternalThing by external from scope
        private val externalThingNamed: ExternalThing by external(named("ext")) from scope

        fun makeStringViaRegular() = externalThingRaw.makeString()
        fun makeStringViaNamed() = externalThingNamed.makeString()
    }

    @Test
    fun `Unqualified usage`() {
        val env = shedinja {
            putExternal(::ExternalThing)
            put { "World" }
            put { listOf('H', 'e', 'l', 'l', 'o') }
        }

        val externalThing = env.getExternal<ExternalThing>()
        assertEquals("Hello World!", externalThing.makeString())
    }

    @Test
    fun `Usage with named`() {
        val env = shedinja {
            putExternal(named("ext1"), ::ExternalThing)
            putExternal(named("ext2"), ::ExternalThing)
            put { "World" }
            put { listOf('H', 'e', 'l', 'l', 'o') }
        }

        val externalThing1 = env.getExternal<ExternalThing>(named("ext1"))
        assertEquals("Hello World!", externalThing1.makeString())
        val externalThing2 = env.getExternal<ExternalThing>(named("ext2"))
        assertEquals("Hello World!", externalThing2.makeString())
    }

    @Test
    fun `Usage with truly different types`() {
        val env = shedinja {
            putExternal(::ExternalThing)
            putExternal(::OtherExternalThing)
            put { "World" }
            put { listOf('H', 'e', 'l', 'l', 'o') }
        }

        val externalThing1 = env.getExternal<ExternalThing>()
        assertEquals("Hello World!", externalThing1.makeString())
        val externalThing2 = env.getExternal<OtherExternalThing>()
        assertEquals("Hello World!", externalThing2.makeString())
    }

    @Test
    fun `Usage with specifying qualifiers on external component's dependencies`() {
        val env = shedinja {
            put(named("world")) { "World" }
            put(named("le monde")) { "le monde " }
            put { listOf('H', 'e', 'l', 'l', 'o') }
            putExternal(::ExternalThing, mapOf(0 to named("world")))
            putExternal(named("fr"), ::ExternalThing, mapOf(0 to named("le monde")))
        }
        val externalThingEnglish = env.getExternal<ExternalThing>()
        assertEquals("Hello World!", externalThingEnglish.makeString())

        val externalThingFrench = env.getExternal<ExternalThing>(named("fr"))
        assertEquals("Hello le monde !", externalThingFrench.makeString())
    }

    @Test
    fun `Injecting external component in regular component`() {
        val env = shedinja {
            putExternal(::ExternalThing)
            putExternal(named("ext"), ::ExternalThing)
            put(::RegularComponent)
            put { listOf('H', 'e', 'l', 'l', 'o') }
            put { "World" }
        }
        val regular = env.get<RegularComponent>()
        assertEquals("Hello World!", regular.makeStringViaRegular())
        assertEquals("Hello World!", regular.makeStringViaNamed())
    }
}
