# Shedinja :id=shedinja-main

Welcome to Shedinja's documentation website!

Shedinja is an easy-to-use dependency injection framework for Kotlin.

```kotlin
// A simple repository/service/controller sample using Shedinja

class Repository {
    // ...
}

class Service(scope: InjectionScope) {
    private val repository: Repository by scope()

    // ...
}

class Controller(scope: InjectionScope) {
    private val service: Service by scope()

    // ...
}

val module = shedinjaModule {
    put(::Repository)
    put(::Service)
    put(::Controller)
}
```
