# Shedinja :id=shedinja-main

Welcome to Shedinja's documentation website!

Shedinja is an easy-to-use dependency injection framework for Kotlin, mainly inspired by [Koin](https://insert-koin.io) with the objective of being more flexible and safer.

- Shedinja components are easy to test
- Environments can be ran entirely in parallel, both during tests and at runtime.
- You can automatically test your Shedinja modules for incoherences, cyclic dependencies, completeness and much more.
- Shedinja does not use code generation and is a pure Kotlin/JVM library.


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
