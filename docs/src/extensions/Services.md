# Services

?> This extension is experimental.

*This is an installable extension that is only compatible with extensible environments.*

The services extension provides an easy way to "start and stop all components". The exact meaning of "starting components" is up to you: it could be connecting to a database, starting a web server, or anything else.

It is somewhat equivalent to [hosted services on ASP.NET Core](https://docs.microsoft.com/en-us/aspnet/core/fundamentals/host/hosted-services).

## Usage

Note that this extension is not included in the base release of Shedinja due to its use of coroutines. You need to add `guru.zoroark.shedinja:shedinja-services` in your dependencies: see [Getting Started](GettingStarted.md) for more information.

### Installation

This extension needs to be installed with `useServices()`, like so:

```kotlin
val env = shedinja {
    useServices()

    // ...
}
```

### Creating services

TODO

### Starting and stopping services

TODO

### Statistics

TODO

### Excluding services

TODO