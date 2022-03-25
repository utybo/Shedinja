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
import guru.zoroark.shedinja.extensions.injectors.component
import guru.zoroark.shedinja.extensions.injectors.inject
import guru.zoroark.shedinja.extensions.injectors.invoke
import guru.zoroark.shedinja.extensions.injectors.withQualifier
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

    class ExternalStringHolder(val theString: String)

    class ExternalStringUser(val holder: ExternalStringHolder, val otherString: String) {
        fun makeString() = "${holder.theString} $otherString!"
    }

    class ExternalB(private val b: String) {
        val info = "b = $b"
    }

    class ExternalBContainer(val b: ExternalB) {
        fun makeString() = "I contain ${b.info}"
    }

    interface SomeService {
        fun prependFiveSpaces(str: String): String
    }

    class SomeServiceImpl : SomeService {
        override fun prependFiveSpaces(str: String): String = "     $str"
    }

    class ExternalC(private val someService: SomeService) {
        fun prependEightSpaces(str: String): String = someService.prependFiveSpaces("   $str")
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
    fun `Usage with external injected in external`() {
        val env = shedinja {
            put(named("the_b")) { "B" }
            putExternal(
                ::ExternalBContainer,
                0 inject external
            )
            putExternal(
                ::ExternalB,
                0 inject component withQualifier named("the_b")
            )
        }
    }

    @Test
    fun `Usage with specifying qualifiers on external component's dependencies`() {
        val env = shedinja {
            put(named("world")) { "World" }
            put(named("le monde")) { "le monde " }
            put { listOf('H', 'e', 'l', 'l', 'o') }
            putExternal(
                ::ExternalThing,
                0 inject component withQualifier named("world")
            )
            putExternal(
                named("fr"),
                ::ExternalThing,
                0 inject component withQualifier named("le monde")
            )
        }
        val externalThingEnglish = env.getExternal<ExternalThing>()
        assertEquals("Hello World!", externalThingEnglish.makeString())

        val externalThingFrench = env.getExternal<ExternalThing>(named("fr"))
        assertEquals("Hello le monde !", externalThingFrench.makeString())
    }

    @Test
    fun `Multiple parameters and complex usage`() {
        val env = shedinja {
            put(named("hello str")) { "Hello" }
            put(named("world str")) { "World" }
            putExternal(
                named("hello obj"),
                ::ExternalStringHolder,
                0 inject component withQualifier named("hello str")
            )
            putExternal(
                named("hello world"),
                ::ExternalStringUser,
                0 inject external withQualifier named("hello obj"),
                1 inject component withQualifier named("world str")
            )
        }
        val helloWorld = env.getExternal<ExternalStringUser>(named("hello world"))
        assertEquals("Hello World!", helloWorld.makeString())
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

    @Test
    fun `Inject exeternal component parameter with subtype`() {
        val env = shedinja {
            putExternal(
                ::ExternalC,
                0 inject component(SomeServiceImpl::class)
            )
            put(::SomeServiceImpl)
        }
        val c = env.getExternal<ExternalC>()
        assertEquals("        hello there", c.prependEightSpaces("hello there"))
    }
}
