# [![Shedinja](https://img.pokemondb.net/sprites/black-white/anim/normal/shedinja.gif)](http://pokemondb.net/pokedex/shedinja) Shedinja

> Dependency injection doesn't have to be a mess

Inspired by [Koin](https://insert-koin.io), Shedinja is a simple, safe and easy-to-use dependency injection library that is easy to use and has extremely flexible internals.

## Getting Started

Using Shedinja is simple:

* Add a `scope: InjectionScope` parameter to your constructor.
* Define your injectable elements, either one-by-one or grouped in modules.
* Create an environment.
* Profit!

```kotlin
class Repository {
    // ...
}

class Service(scope: InjectionScope) {
    private val repo: Repository by scope()
    // ...
}

data class Configuration(val someUrl: String)

val appModule = module {
    put(::Repository)
    put(::Service)
    put { Configuration("https://example.com") }
}

val env = shedinja { put(appModule) }
```

