# Services

?> This extension is experimental.

*This is an installable extension that is only compatible with extensible environments.*

The services extension provides an easy way to "start and stop all components". The exact meaning of "starting components" is up to you: it could be connecting to a database, starting a web server, or anything else.

It is somewhat equivalent to [hosted services on ASP.NET Core](https://docs.microsoft.com/en-us/aspnet/core/fundamentals/host/hosted-services). Note that Shedinja services are not autostarted: you need to call `env.services.startAll()` manually.

## Usage

Note that this extension is not included in the base release of Shedinja due to its use of coroutines. You need to add `guru.zoroark.shedinja:shedinja-services` in your dependencies: see the [Getting Started](/usage/GettingStarted.md) page for more information.

### Installation

This extension needs to be installed with `useServices()`, like so:

```kotlin
val env = shedinja {
    useServices()

    // ...
}
```

### Creating services

Services are regular components that implement `ShedinjaService` or `SuspendShedinjaService`.

* You should use `ShedinjaService` if your start/stop functions are blocking.
* You should use `SuspendShedinjaService` if your start/stop functions are suspending (or can be made suspending via the use of Kotlin coroutines facilities).

Here is an example using a (fictional) database system:

```kotlin
class DatabaseService(scope: InjectionScope) : ShedinjaService /* or SuspendShedinjaService */ {
    private val db by scope<DatabaseConfiguration>() wrapIn { Database(it) }

    override fun start() { // or override suspend fun start() for SuspendShedinjaService
        db.connect()
    }

    override fun stop() { // or override suspend fun stop() for SuspendShedinjaService
        db.disconnect()
    }
}
```

You can then `put` this component like any other component:

```kotlin
val env = shedinja {
    useServices()

    put(::DatabaseService)
}
```

### Starting and stopping services

Once your environment is created, you can then call `.services.startAll()` and `.services.stopAll()` to start and stop all services.


```kotlin
val env = shedinja {
    useServices()

    put(::DatabaseService)
}

env.services.startAll()
// ...
env.services.stopAll()
```

Note that `startAll` and `stopAll` are suspending functions. If you are not using coroutines within your application at all, you may want to wrap the `startAll`/`stopAll` within a `runBlocking { ... }` block.

```kotlin
runBlocking { env.services.startAll() }
// ...
runBlocking { env.services.stopAll() }
```

### Statistics

The Shedinja services extension provides information on how much time each service took to start/stop. There are two ways to access this information:

#### Message handler

The `startAll` and `stopAll` functions optionally take a lambda. This lambda takes a `String` and is used by the extension to send messages when services are done starting/stopping.

By default, this lambda is a no-op. You can supply your own lambda or function to this parameter. For example, using a regular `println` call:

```kotlin
env.services.startAll(::println)
env.services.stopAll(::println)
```

#### Return value

`startAll` and `stopAll` return a map of identifiers to the time it took to start the component with this identifier. You can use this map to process the statistics in any way you like.

### Excluding services

You may exclude services from being started/stopped by tagging their `put` statement with `noService`, `noServiceStart` or `noServiceStop`.

```kotlin
val env = shedinja {
    useServices()

    put(::DoNotStartMe) with noServiceStart
    put(::DoNotStopMe) with noServiceStop
    put(::DoNotStartNorStopMe) with noService
}
```
