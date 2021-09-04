# [![Shedinja](https://img.pokemondb.net/sprites/black-white/anim/normal/shedinja.gif)](http://pokemondb.net/pokedex/shedinja) Shedinja

> Dependency injection doesn't have to be a mess

Inspired by [Koin](https://insert-koin.io), Shedinja is a simple, safe and easy-to-use dependency injection library with extremely flexible internals.

> ⚡ This framework is experimental and under heavy development. As such, it should not be considered stable and should not be used in production environments.

## Getting Started

Using Shedinja is simple:

* Add a `scope: InjectionScope` parameter to your class.
  * This is only required if you would like to inject components into your class. If your class does not require injection, such a parameter is not necessary.
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

