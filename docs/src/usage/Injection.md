# Injection

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

!> Unless otherwise noted, meta-injections are ignored in [Shedinja checks](ShedinjaCheck.md) and will not raise errors.
