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

    fun UserRegistrationData.isRegistrationValid(registration: UserRegistrationData): Boolean =
        /* ... */

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
    - By providing a constructor. This is useful if you only need the test subject within your test, and that subject's constructor is simple, similiar to using `put { ... }` versus `put(::...)`.

<!-- TODO actually show test writing -->

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

Available checks are:

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

But this is not, as `C` has a depdency on `B`, which is missing in the module:

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

<!-- TODO document cyclic dependency check -->