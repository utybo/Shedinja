package guru.zoroark.shedinja.test

import guru.zoroark.shedinja.dsl.ContextBuilderDsl
import guru.zoroark.shedinja.dsl.ShedinjaDsl
import guru.zoroark.shedinja.dsl.put
import guru.zoroark.shedinja.dsl.shedinja
import guru.zoroark.shedinja.dsl.shedinjaModule
import guru.zoroark.shedinja.environment.Identifier
import guru.zoroark.shedinja.environment.InjectableModule
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.jvmErasure

/**
 * Base class for testing Shedinja-applications.
 *
 * The basic idea behind this class is to provide testing facilities around a single "subject". Let's say we wanted to
 * test a service and its interactivity with a repository. Our subject here would be the service.
 *
 * The class itself provides a wrapper around an [UnsafeMutableEnvironment] that is well-suited for test scenarios.
 *
 * Here is what a typical use of this class could look like, with the MockK library for mocking our repository.
 *
 * ```kotlin
 * // Main code
 * interface Repository {
 *     fun storeThis(text: String)
 * }
 *
 * class Service(scope: InjectionScope) {
 *     private val repository: Repository by scope()
 *
 *     fun incomingText(text: String) {
 *         // ...
 *         repository.storeThis(text)
 *         // ...
 *     }
 * }
 *
 * // Test code
 * class TestService : ShedinjaBaseTest<Service>(::Service) {
 *     @Test
 *     fun `Accepts incoming text properly`() = test {
 *         put<Repository> {
 *             mockk { every { storeThis("hello") } just runs }
 *         }
 *
 *         subject.incomingText("hello")
 *
 *         verify { get<Repository>().storeThis("hello") }
 *     }
 * }
 * ```
 *
 * The class defines the environment using a *base module* that usually only contains the test subject's entry, but may
 * also contain additional dependencies as you see fit. Refer to the different constructors for more information.
 *
 * @constructor This constructor takes a pre-built base module and the class of the subject and uses them as a base.
 * @param subjectClass The class of the subject, used for [subject] to work properly.
 * @param baseModule The base module as an [InjectableModule] instance.
 */
open class ShedinjaBaseTest<S : Any>(
    private val subjectClass: KClass<S>,
    private val baseModule: InjectableModule
) {
    /**
     * This constructor takes a module builder and the subject's class, builds the module then uses that as a base.
     *
     * This is equivalent to calling `shedinjaModule` on the builder and running the primary constructor instead.
     *
     * For example:
     *
     * ```
     * class TestService : ShedinjaBaseTest<Service>(
     *     Service::class, { put(::Service) }
     * ) {
     *     // ...
     * }
     * ```
     *
     * @param subjectClass The class of the subject, used for [subject] to work properly.
     * @param baseModuleBuilder The base module as a builder, just like the body of a [shedinjaModule] call.
     */
    constructor(subjectClass: KClass<S>, baseModuleBuilder: ContextBuilderDsl.() -> Unit) : this(
        subjectClass, shedinjaModule("<base test module>", baseModuleBuilder)
    )

    /**
     * Shortcut for cases where you want to create a single-component module. For example, this:
     *
     * ```
     * class TestService : ShedinjaBaseTest<Service>(::Service)
     * ```
     *
     * Is equivalent to this:
     *
     * ```
     * class TestService : ShedinjaBaseTest<Service>(Service::class, { put(::Service) })
     * ```
     */
    @Suppress("UNCHECKED_CAST")
    constructor(constructor: KFunction<S>) : this(
        constructor.returnType.jvmErasure as KClass<S>,
        { put(constructor.returnType.jvmErasure as KClass<S>, constructor) }
    )

    /**
     * Create a new environment from this instance's base module (and an optional [additionalBuilder]) and execute
     * the [block] within it.
     *
     * See the example [on this class][ShedinjaBaseTest]
     */
    @ShedinjaDsl
    fun <T> test(
        additionalBuilder: ContextBuilderDsl.() -> Unit = {},
        block: UnsafeMutableEnvironment.() -> T
    ): T {
        val env = shedinja(UnsafeMutableEnvironment) {
            put(baseModule)
            additionalBuilder()
        }
        return with(env) { block() }
    }

    /**
     * Returns the subject of this test class within this environment.
     */
    val UnsafeMutableEnvironment.subject: S
        get() = this.get(Identifier(subjectClass))
}
