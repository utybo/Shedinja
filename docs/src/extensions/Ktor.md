# Ktor

Shedinja provides integrations with Ktor in the form of the `shedinja-ktor` extension. This extension is completely optional, and you can use Shedinja and Ktor without it, but it makes developing Shedinja-backed Ktor applications easier.

## Getting started

First, you will need the `shedinja-ktor` dependency (see [the general Getting Started page](/usage/GettingStarted.md) for more information).

Next, you will need to enable both the Ktor extension and the Shedinja extension.

```kotlin
val env = shedinja {
    useServices()
    useKtor()
}
```

Now that we have enabled the Ktor extension, we can start our server, which is just a regular [service](extensions/Services.md).

```kotlin
env.services.startAll()
```

Similarly, we can stop our server with:

```kotlin
env.services.stopAll()
```

Read [the services documentation](extensions/Services.md) for more information about services.

## Special components

The Ktor extension uses three kinds of components to do its job. These components all inherit from Ktor extension classes and do some things automatically.

Note that you will still need to register your components using the usual `put()` functions.

### KtorApplication

The primary class that will host the actual Ktor application needs to inherit from `KtorApplication`.

This class is responsible for:

- Providing the very basic configuration of the application. This is done by overriding the `settings` property.
- Installing essential Ktor features required by your Ktor modules. This is done by overriding the `Application.setup()` function.

Here is an example:

```kotlin
class MyApplication(scope: InjectionScope) : KtorApplication(scope) {
    override val settings get() = KtorApplicationSettings(Netty, port = 8080)

    override fun Application.setup() {
        install(ContentNegotiation) {
            jackson()
        }
    }
}

val env = shedinja {
    useServices()
    useKtor()

    put(::MyApplication)
}
``` 

!> Do not forget the `get()` after `val settings`, otherwise you will be performing an [unsafe injection](../ShedinjaCheck.md#safe-injection-only) in case you use some Shedinja component in your settings (e.g. some component that provides the actual port).

### KtorModule

A Ktor module (not to be confused with a [Shedinja module](/usage/Modules.md)) is a class that inherits from `KtorModule`. This is a module in the Ktor sense: an extension function on top of `Application` that adds features, routes, etc.

```kotlin
// As with any regular Shedinja component, the 'scope: InjectionScope' parameter
// is optional if you do not need it.
class MyModule(scope: InjectionScope) : KtorModule() {
    override fun Application.installModule() {
        install(...) {
            ...
        }

        routing {
            ...
        }
    }
}

val env = shedinja {
    useServices()
    useKtor()

    put(::MyModule)
}
```

### KtorController

Ktor controllers are identical to Ktor modules but are more convenient for adding routes and eliminate the need to explicitly call `routing { }` in the module.

```kotlin
// As with any regular Shedinja component, the 'scope: InjectionScope' parameter
// is optional if you do not need it.
class MyController(scope: InjectionScope) : KtorController() {
    override fun Routing.installController() {
        get("/hello") {
            call.respondText("Hello World!")
        }
    }
}

val env = shedinja {
    useServices()
    useKtor()

    put(::MyController)
}
```

## Installation order

In case you need to control the order in which your components are installed, you can pass a priority integer to `KtorController` and `KtorModule` constructors.

```kotlin
class MyController(scope: InjectionScope) : KtorController(10) {
    ...
}

class MyModule(scope: InjectionScope) : KtorModule(200) {
    ...
}
```

By default, modules and controllers have a priority of 100. Modules and controllers are installed in the order of their priority, from largest to smallest. Note that controllers are just regular modules and are installed at the same time as modules.

Controllers and modules that have the same priority are installed in an arbitrary order. Make sure you use priorities if you need a specific order, even if the default order works for you.

## Multiple Ktor apps

`shedinja-ktor` supports hosting multiple independent Ktor applications under the same environment.

In order to differentiate between applications (e.g. which controllers are installed where), you can give your applications a name and tell your controllers and modules to install themselves only onto applications of a specific name:

```kotlin
const val AppOneName = "app-one"
const val AppTwoName = "app-two"

class AppOne(scope: InjectionScope) : KtorApplication(scope, AppOneName) {
    ...
}

class AppTwo(scope: InjectionScope) : KtorApplication(scope, AppTwoName) {
    ...
}

class ModuleA : KtorModule(restrictToAppName = AppOneName) {
    // This module is only installed on AppOne
    ...
}

class ModuleB : KtorModule(restrictToAppName = AppTwoName) {
    // This module is only installed on AppOne
    ...
}
```

If you do not specify a name for your application, it gets the name `null` by default. `null` is a valid name and has no specific meaning (other than that it is the default name).
