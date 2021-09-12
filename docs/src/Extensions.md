# Extensions

Shedinja's core APIs are designed to be quite simple yet flexible. In addition to those, Shedinja also provides more advanced features in the form of extension functions that only use Shedinja's public APIs.

Extensions are all located within the `guru.zoroark.shedinja.extensions` package.

## Injection Factories

?> This extension is experimental.

Many frameworks allow you to either inject a *singleton* (where a single object instance is injected) or a *factory* (where any component that depends on the factory gets its own instance of the object). Factories are very useful for objects like loggers.

Shedinja factories are an extension of Shedinja's system -- in fact, they are implemented entirely with public APIs from Shedinja's core system!

You can create factories using the `putFactory` method within your environment builder or within a module. Factories are injected using the `factory from scope` syntax.

!> Make sure you do not use `by scope()` to retrieve an object that is supposed to be created by a factory!

```kotlin
class Logger {
    fun logInfo(message: String) = println("INFO: $message")
    fun logWarn(message: String) = println("WARN: $message")
}

class ServiceA(scope: InjectionScope) {
    private val logger by factory from scope

    fun doSomething() {
        logger.logInfo("Doing something in A...")
    }
}

class ServiceB(scope: InjectionScope) {
    private val logger by factory from scope
    private val a by scope()

    fun doSomething() {
        a.doSomething()
        logger.logInfo("Doing something in B...")
    }
}

val environment = shedinja {
    putFactory { Logger() }

    put(::ServiceA)
    put(::ServiceB)
}

environment.get<ServiceA>().doSomething()
// INFO: Doing something in A...
// INFO: Doing something in B...
```

### Creating objects based on requester

It is possible to create objects based on which component is requesting them. This is useful for giving a logger a name, for example:

```kotlin
class Logger(private val name: String) {
    fun logInfo(message: String) = println("($name) INFO: $message")
    fun logWarn(message: String) = println("($name) WARN: $message")
}

// ServiceA and ServiceB are the same as before

val environment = shedinja {
    putFactory { requester -> Logger(requester::class.qualifiedName ?: "<anon>") }

    put(::ServiceA)
    put(::ServiceB)
}

environment.get<ServiceA>().doSomething()
// (org.example.ServiceA) INFO: Doing something in A...
// (org.example.ServiceB) INFO: Doing something in B...
```

You can go even further with annotations:

```kotlin
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class LoggerName(val name: String)

private val KClass<*>.loggerName: String
    get() = findAnnotation<LoggerName>()?.name ?: qualifiedName ?: "<anon>"

class Logger(private val name: String) {
    fun logInfo(message: String) = println("($name) INFO: $message")
    fun logWarn(message: String) = println("($name) WARN: $message")
}

@LoggerName("Custom logger name!")
class ServiceA(scope: InjectionScope) {
    private val logger by factory from scope

    fun doSomething() {
        logger.logInfo("Doing something in A...")
    }
}

class ServiceB(scope: InjectionScope) {
    private val logger by factory from scope
    private val a by scope()

    fun doSomething() {
        a.doSomething()
        logger.logInfo("Doing something in B...")
    }
}

val environment = shedinja {
    putFactory { requester -> Logger(requester::class.loggerName) }

    put(::ServiceA)
    put(::ServiceB)
}

environment.get<ServiceA>().doSomething()
// (Custom logger name!) INFO: Doing something in A...
// (org.example.ServiceB) INFO: Doing something in B...
```

### How do factories work?

In a nutshell, *factories* are injected in the environment, and this factory gets invoked when the object is requested. The factory is injected in the environment as a regular singleton.

Factories are objects which implement the functional interface `InjectableFactory<T>` (the Kotlin equivalent for SAMs in Java). Its `make(requester: Any): T` function is invoked when an object is requested.

Factories are injected with an additional qualifier. Because of its generic typing, only including the class would lead to components with the same identifier, which would be `InjectableFactory::class` without qualifiers. In order to avoid this, an `InjectableFactoryOutputTypeQualifer` is used as a qualifier to differentiate between factories. The `outputs` function can also be used as an alias for this qualifier.

The `putFactory` method is in charge of the entire injection process: creating the factory with the correct qualifier and injecting it within a simple `put` call.

On the injection side, the only change is that a wrapper is put around the `scope()` call: instead of the actual type being requested, the corresponding factory is requested when using `factory from scope`. It is then wrapped using the `WrappedReadOnlyProperty` class which executes the factory's `make` method and wrapped *again* within a `SynchronizedLazyPropertyWrapper`. As such, the factory's method is only called once and only when necessary.