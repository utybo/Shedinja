# Shedinja

> Dependency injection doesn't have to be a mess

Inspired by [Koin](https://insert-koin.io), Shedinja is a simple, safe and easy-to-use dependency injection library that features:

* Lazily injection and object creation, the way it's meant to be
* Strongly scoped elements. Two different instances of your app in a single Java process? No problem.
* Easily testable components. Life is too short to fight with your DI library. 

## Getting Started

Using Shedinja is simple:

* Define your injectable elements.
* Define your modules. Each module is just a set of elements.
* Create an environment.
* Profit!

```kotlin
class Repository(scope: SComponent) : SComponent by scope

class Service(scope: SComponent) : SComponent by scope {
    private val repo: Repository by inject()
}

data class Configuration(val someUrl: String) : SComponent by scopeless

val appModule = module {
    put { Repository(s) }
    put { Service(s) }
    put { Configuration("https://example.com") }
}

val env = shedinja(appModule)
```

