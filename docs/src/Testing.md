# Testing Shedinja code

Shedinja provides easy-to-use testing facilities for applications that use it as a dependency injection framework. This comes in two parts:

- A base test class that provides a number of useful methods for dynamically injecting components on the fly.
- A check system that allows you to test the completeness/correctness of your Shedinja modules. This requires using [modules](/UsingShedinja#modules) throughout your application.

## Base test class

Shedinja provides a class that can be used by your test classes to automatically create very flexible test environments. Let's imagine we want to test our `UserRegistrationService` class, as follows:

```kotlin
class RegistrationException(message: String) : Exception(message)

class UserRegistrationService(scope: InjectionScope) {
    private val userRepository: UserRepository by scope()
    private val permissionsService: PermissionsService by scope()

    fun registerUser(registration: UserRegistrationData) {
        when {
            !registration.isRegistrationValid() ->
                throw RegistrationException("Registration is invalid")
            !permissionsService.isUserAllowedToRegister(registration) ->
                throw RegistrationException("User is not allowed to register")
            else ->
                userRepository.createFrom(registration)
        }
    }
}
```

Let's say we want to test the `registerUser` function. We can create a test class like this:

```kotlin
class UserRegistrationServiceTest : ShedinjaBaseTest<UserRegistrationService>(
    UserRegistrationService::class,
    shedinjaModule {
        put { UserRegistrationService() }
    }
) {
    // ...
}
```

The `ShedinjaBaseTest` class is based around the idea of a "test subject" -- that is, a type that we are trying to test. In this case, this is the `UserRegistrationService` class. We need to provide:

- The `KClass` for this service.
- A base module that will be used to create the test subject and any additional objects you will always need in the environment within your tests. This one can be initialized in a few ways:
    - By directly providing a module (either by using one that you already made before *or* by creating a new one via `shedinjaModule`).
    - By providing a lambda that will create the module. This basically means passing the lambda you'd use with `shedinjaModule`, but without the `shedinjaModule` that goes with it.
    ```kotlin
    class UserRegistrationServiceTest : ShedinjaBaseTest<UserRegistrationService>(
       UserRegistrationService::class, { put { UserRegistrationService() } }
    ) {
        // ...
    }
    ```
    - By providing a constructor. This is useful if you only need the test subject within your test, and that subject's constructor is simple, similiar to using `put { ... }` versus `put(::...)`. The class is automatically extracted from the function's signature. Note that this approach is more bug-prone and less flexible than the above ones and is therefor not recommended.
    ```kotlin
    class UserRegistrationServiceTest : ShedinjaBaseTest<UserRegistrationService>(
       ::UserRegistrationService
    ) {
        // ...
    }
    ```

### Writing tests

Once you have set up your base module, you can use the `test` function to automatically create an injection environment.

```kotlin
class UserRegistrationServiceTest : ShedinjaBaseTest<UserRegistrationService>(
    UserRegistrationService::class,
    shedinjaModule {
        put { UserRegistrationService() }
    }
) {
    @Test
    fun `Test fails if registration is invalid`() = test {
        val registration = mockk<UserRegistrationData> {
            every { isRegistrationValid() } returns false
        }
        put { registration }
        assertThrows<RegistrationException> {
            subject.registerUser(registration)
        }
        verify { registration.isRegistrationValid() }
    }
}
```

In this example, we're using [MockK](https://mockk.io) to create a mocked user registration data object where `isRegistrationValid` always returns false. Then, we add it to the injection environment (using `put`). We then call `registerUser` and assert that it throws a `RegistrationException`, as wrong user registrations should trigger this kind of exception. We then use MockK's `verify` function to ensure that the `isRegistrationValid` function was called.

You have access to all the usual environment methods (`get` and `createInjector`) and all DSL component creation methods (the `put` function family) within the `test` block. `subject` is a shortcut for `get`ting the test subject: in this example, it's equivalent to `get<UserRegistrationService>()`.

Since the pattern of "mock (or create something) and put it in the environment" is common, you can save a line by using `.alsoPut()` instead of a separate `put` call. This is equivalent to calling `.also { put(it) }`, but uses a nicer format.

```kotlin
class UserRegistrationServiceTest : ShedinjaBaseTest<UserRegistrationService>(
    UserRegistrationService::class,
    shedinjaModule {
        put { UserRegistrationService() }
    }
) {
    @Test
    fun `Registration fails if data is invalid`() = test {
        val registration = mockk<UserRegistrationData> {
            every { isRegistrationValid() } returns false
        }.alsoPut()
        assertThrows<RegistrationException> {
            subject.registerUser(registration)
        }
        verify { registration.isRegistrationValid() }
    }
}
```

You can also create your own extension function that does all of this for you, for example:

```kotlin
inline fun <reified T> UnsafeMutableEnvironment.putMockk(block: T.() -> Unit): T =
        mockk(block = block).alsoPut()
```

?> You may be wondering: why doesn't Shedinja include a `putMockk` function? This is because Shedinja can work with any mocking library of your choosing -- we do not want to force you to use a particular one, and including a `putMockk` function within our testing library would always include MockK in your builds, even if you use something else. You are better off writing your own handy extension function that corresponds exactly to what you need.

We can then continue on with this pattern and mock the behaviors we need for the three cases in our `registerUser` function.

```kotlin
inline fun <reified T> UnsafeMutableEnvironment.putMockk(block: T.() -> Unit): T =
        mockk(block = block).alsoPut()

@Test
fun `Registration fails if data is invalid`() = test {
    val registration = putMockk<UserRegistrationData> {
        every { isRegistrationValid() } returns false
    }
    assertThrows<RegistrationException> {
        subject.registerUser(registration)
    }
    verify { registration.isRegistrationValid() }
}

@Test
fun `Test fails if registration is invalid`() = test {
    val registration = putMockk<UserRegistrationData> {
        every { isRegistrationValid() } returns true
    }

    val perms = putMockk<PermissionsService> {
        every { isUserAllowedToRegister(registration) } returns false
    }

    assertThrows<RegistrationException> {
        subject.registerUser(registration)
    }
    verify {
        registration.isRegistrationValid()
        perms.isUserAllowedToRegister(registration)
    }
}

@Test
fun `Registration succeeds and calls repository`() = test {
    val registration = putMockk<UserRegistrationData> {
        every { isRegistrationValid() } returns true
    }
    val perms = putMockk<PermissionsService> {
        every { isUserAllowedToRegister(registration) } returns true
    }
    val repo = putMockk<UserRepository> {
        every { registerUser(registration) } just runs
    }
    assertDoesNotThrow {
        subject.registerUser(registration)
    }
    verify {
        registration.isRegistrationValid()
        perms.isUserAllowedToRegister(registration)
        repo.registerUser(registration)
    }
}
```

### Common patterns

One possible use is to mock an object that is so commonly used as a dependency in your test subject within the base module. Using MockK, you can then add more behavior to this mock on the fly, within tests where such behavior matters.

```kotlin
class UserRegistrationServiceTest : ShedinjaBaseTest<UserRegistrationService>(
    UserRegistrationService::class, {
        put { UserRegistrationService() }
        put { mockk<UserRegistrationData>() }
    }
) {
    @Test
    fun `Registration fails if data is invalid`() = test {
        val registration = get<UserRegistrationData>().apply {
            every { isRegistrationValid() } returns false
        }
        assertThrows<RegistrationException> {
            subject.registerUser(registration)
        }
        verify { registration.isRegistrationValid() }
    }
}
```

### Running tests in parallel

Shedinja's environments are strongly scoped: that is, they live independently of each other without any global state. This means that, as long as your own classes do not rely on a kind of "global state", they can be ran in parallel.

## Check system

Shedinja provides a check system for your modules that allow you to verify some of its properties.

Add a test anywhere within your test sources (preferably in its own class for clarity) with the following format:

```kotlin
@Test
fun `Shedinja module checks`() = shedinjaCheck {
    modules(...)

    // ...
}
```

You will need to pass your application's modules to the `modules(...)`, and add any checks you want to perform below them.

When a check fails, it throws an exception with a message that describes the problem, hereby making the test fail.

In order to use this feature "idiomatically", you should

- Define your modules in your main code in a public way. Note that a module itself does not instantiate its components: it only describes *how* to instantiate them. The environment created when calling `shedinja` is the one responsible for that.
- Use a single test for all of the checks.

### Completeness check

Add this check by putting `+complete` after the modules declaration(s).

Checks that the module set is complete, meaning that no dependency requirement is missing. For example, considering the following classes:

```kotlin
class A
class B(scope: InjectionScope) {
    val a: A by scope()
}
class C(scope: InjectionScope) {
    val b: B by scope()
    val a: A by scope()
}
```

This is correct:

```kotlin
val module = shedinjaModule {
    put(::A)
    put(::B)
    put(::C)
}

shedinjaCheck {
    modules(module)

    +complete
}
```

But this is not, as `C` has a dependency on `B`, which is missing in the module:

```kotlin
val module = shedinjaModule {
    put(::A)
    put(::C)
}

shedinjaCheck {
    modules(module)

    +complete
}
```

When this check fails, Shedinja will tell you exactly which dependency is missing and which components requested it:

```kotlin
class A

class B(scope: InjectionScope) {
    private val a: A by scope()
    private val d: D by scope()
}

class C(scope: InjectionScope) {
    private val b: B by scope()
    private val d: D by scope()
    private val e: E by scope()
}

class D
class E

val module = shedinjaModule {
    put(::A)
    put(::B)
    put(::C)
}

shedinjaCheck {
    modules(module)

    +complete
}
```

```
Some dependencies were not found. Make sure they are present within your module definitions.
--> org.example.shedinja.D (<no qualifier>) not found
    Requested by:
    --> org.example.shedinja.B (<no qualifier>)
    --> org.example.shedinja.C (<no qualifier>)
--> org.example.shedinja.E (<no qualifier>) not found
    Requested by:
    --> org.example.shedinja.C (<no qualifier>)
```

Note that the `complete` check does not verify if modules are complete on their own. It verifies that the entire module set is complete, meaning that, despite `B` being missing from `module1`, the fact that it can still be found in `module2` makes this system complete:

```kotlin
val module1 = shedinjaModule {
    put(::A)
    put(::C)
}

val module2 = shedinjaModule {
    put(::B)
}

shedinjaCheck {
    modules(module1, module2)

    +complete
}
```

### No cycles check

Add this check by putting `+noCycle` after the modules declaration(s).

Checks that the module set does not contain any cyclic dependencies. A cyclic dependency situation is when a class `Foo` depends on itself, either directly (`Foo` depends on `Foo` by injecting itself) or indirectly (`Foo` depends on `Bar` and `Bar` depends on `Foo`).

While cyclic dependency situations are generally handled correctly by Shedinja, they can mean that you have a problem in your design somewhere, especially in cases of layered architecture.

Here is an example of a cyclic dependency that this check would report. A depends on B, which depends on A, hereby creating a cycle:

```kotlin
class A(scope: InjectionScope) {
    private val b: B by scope()
}

class B(scope: InjectionScope) {
    private val a: A by scope()
}

val module = shedinjaModule {
    put(::A)
    put(::B)
}

shedinjaCheck {
    modules(module)

    +noCycle
}
```

```
Cyclic dependency found:
    org.example.shedinja.A (<no qualifier>)
--> org.example.shedinja.B (<no qualifier>)
--> org.example.shedinja.A (<no qualifier>)
Note: --> represents an injection (i.e. A --> B means 'A is injected in B').
```

And here is a more complicated one:

```kotlin
class C(scope: InjectionScope) {
    private val d: D by scope()
}

class D(scope: InjectionScope) {
    private val e: E by scope()
}

class E(scope: InjectionScope) {
    private val f: F by scope()
    private val c: C by scope()
}

class F(scope: InjectionScope) {
    private val c: C by scope()
}

val module = shedinjaModule {
    put(::C)
    put(::D)
    put(::E)
    put(::F)
}

shedinjaCheck {
    modules(module)

    +noCycle
}
```

```
Cyclic dependency found:
    org.example.shedinja.C (<no qualifier>)
--> org.example.shedinja.D (<no qualifier>)
--> org.example.shedinja.E (<no qualifier>)
--> org.example.shedinja.F (<no qualifier>)
--> org.example.shedinja.C (<no qualifier>)
Note: --> represents an injection (i.e. A --> B means 'A is injected in B').
```