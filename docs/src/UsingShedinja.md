# Using Shedinja

?> **New to Shedinja or dependency injection in general?** Check out our [getting started guide](/GettingStarted)!

Shedinja is a dependency injection framework. As such, its job is to provide a way for components to depend on one another and provide an environment where such components can live.

Shedinja does not rely on code generation and is a pure Kotlin library. All components that live within Shedinja maintain a reference to their scope -- it is impossible for components to live in a global state. As such, it is possible for entire instances of your application to live multiple times in parallel within the same process, or run tests in parallel.

## Injection

Injections are performed via an `InjectionScope` object, which provides a delegation mechanism for properties. This is the main recommended way of getting your dependencies:

- Add a `scope: InjectionScope` parameter to your class' primary constructor. You do not need to store this object as a property: it only needs to be present for initializing your other scopes.

- Use the `invoke` operator on this scope to create delegated properties.

For example:

```kotlin
class RepositoryA
class RepositoryB

class Service(scope: InjectionScope) {
    private val repository: RepositoryA by scope()
    private val otherRepository: RepositoryB by scope()
}

class Controller(scope: InjectionScope) {
    private val service: Service by scope()
}
```

If you need to use [qualifiers](#qualifiers), you can add the qualifier as an argument to the `scope` call:

```kotlin
class AuthService(scope: InjectionScope) {
    private val bannedUsers: List<String> by scope(named("banned"))
    private val adminUsers: List<String> by scope(named("admin"))
}
```

### Meta-environment injections

?> Meta-environment injections require being in an extensible environment. See [here](extensions/Introduction.md#meta-environment) for more information.

Some extensions inject components within the meta-environments. These components can be useful to get in your own components (outside of the meta-environment). For example, a typical use case may be a Ktor application that has a special shutdown endpoint that you wish to use to trigger a `stopAll` call on the [services extension](extensions/Services.md).

You can do this via the `meta` property on the `scope`, e.g.

```kotlin
class SomeEndpoint(scope: InjectionScope) {
    private val services: ServicesManager by scope.meta()

    fun Application.install() {
        routing {
            get("/_example/shutdown") {
                services.stopAll()
                call.respond("OK")
            }
        }
    }
}
```

Note that `meta` actually just returns an injection scope bound to the meta-environment: you can use any scope operation on `.meta` as you would on `scope` (except that you cannot call `.meta.meta` as meta-environments cannot have meta-environments of their own).

## Environment

An environment is a manager for multiple components that can inject one another. They are created using the `shedinja` DSL function, onto which you can call `put` functions to add components:

```kotlin
val environment = shedinja {
    // There are multiple possible styles for adding components:

    // 1. Using a lambda
    put { RepositoryA() }
    put { Controller(scope) }

    // 2. Using a constructor reference
    put(::RepositoryB)
    put(::Service)
}
```

Note that these all use reified types, but `KClass` alternatives are available if the types are not known at compile-time:

```kotlin
val environment = shedinja {
    // 1. Using a lamda
    put(RepositoryA::class) { RepositoryA() }
    put(Controller::class) { Controller(scope) }

    // 2. Using a constructor reference
    put(RepositoryB::class, ::RepositoryB)
    put(Service::class, ::Service)
}
```

You may also add [qualifiers](#qualifiers) to your components:

```kotlin
val environment = shedinja {
    // A. With reified types
    // 1. Using a lambda
    put(named("my repo")) { RepositoryA() }
    put(named("my controller")) { Controller(scope) }

    // 2. Using a constructor reference
    put(named("another repo"), ::RepositoryB)
    put(named("my service"), ::Service)

    // B. With class objects
    // 1. Using a lambda
    put(RepositoryA::class, named("my repo")) { RepositoryA() }
    put(Controller::class, named("my controller")) { Controller(scope) }

    // 2. Using a constructor reference
    put(RepositoryB::class, named("another repo"), ::RepositoryB)
    put(Service::class, named("my service"), ::Service)
}
```

Once your environment is created, you may retrieve objects from it directly, e.g. to start a server or a service somewhere:

```kotlin
val environment = shedinja {
    // ...
}

environment.get<Service>().start()
// Or, using a qualifier
environment.get<Service>(named("my service")).start()
```

## Modules

Shedinja provides a module system that allows you to define your environment in multiple, smaller bits. For example:

```kotlin
class UserRepository
class UserService(scope: InjectionScope) {
    private val repository: UserRepository by scope()
}
class UserController(scope: InjectionScope) {
    private val service: UserService by scope()
}

class MessageRepository
class MessageService(scope: InjectionScope) {
    private val repository: MessageRepository by scope()
}
class MessageController(scope: InjectionScope) {
    private val service: MessageService by scope()
}

val userModule = shedinjaModule {
    put(::UserRepository)
    put(::UserService)
    put(::UserController)
}

val messageModule = shedinjaModule {
    put(::MessageRepository)
    put(::MessageService)
    put(::MessageController)
}

val environment = shedinja {
    put(userModule)
    put(messageModule)
}
```

## Qualifiers

Components must be uniquely identified. By default, components are identified by their type. However, you can add qualifiers to your components to distinguish them if they have the same type.

For example, you can inject two lists within your environment: a list of banned users and a list of admin users:

```kotlin
class BanRepository {
    fun ban(user: String) {
        // ...
    }
}

class BanService(scope: InjectionScope) {
    private val permaBannedUsers: List<String> by scope(named("banned"))
    private val adminUsers: List<String> by scope(named("admin"))

    private val banRepo: BanRepository by scope()

    fun ban(userToBan: String, requester: String) {
        if (requester in adminUsers && requester !in permaBannedUsers) {
            banRepo.ban(userToBan)
        } else {
            error("Not enough permissions!")
        }
    }
}

val environment = shedinja {
    put(::BanRepository)
    put(::BanService)

    put(named("banned")) { listOf("banned1", "banned2") }
    put(named("admin")) { listOf("admin1", "admin2") }
}

environment.get<BanService>().ban("banned3", "admin1")
```
