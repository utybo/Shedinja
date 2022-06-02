# SHEDINJA IS DEPRECATED

Shedinja has been moved to the [Tegral project](https://github.com/utybo/Tegral) under the name "Tegral DI". See [here](https://tegral.zoroark.guru/docs/core/di) for more information on Tegral DI.

This repository is no longer maintained as a result.

*Previous README below.*

.

.

.

# [![Shedinja](https://img.pokemondb.net/sprites/black-white/anim/normal/shedinja.gif)](http://pokemondb.net/pokedex/shedinja) Shedinja

[![Documentation](https://img.shields.io/badge/Documentation-Click%20Here!-b5945a?style=for-the-badge)](https://shedinja.zoroark.guru) [![Packages](https://img.shields.io/github/v/release/utybo/Shedinja?label=packages&logo=gitlab&style=for-the-badge)](https://gitlab.com/utybo/packages/-/packages?search[]=guru%2Fzoroark%2Fshedinja) [![MIT license](https://img.shields.io/badge/License-MIT-blue?style=for-the-badge)](LICENSE.md) [![GitHub Workflow Status](https://img.shields.io/github/workflow/status/utybo/Shedinja/Tests?logo=github&style=for-the-badge)](https://github.com/utybo/Shedinja/actions/workflows/tests.yaml) [![Codecov](https://img.shields.io/codecov/c/github/utybo/Shedinja?style=for-the-badge&logo=codecov)](https://codecov.io/gh/utybo/Shedinja)

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

