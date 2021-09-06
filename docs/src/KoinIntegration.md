# Koin Integration

Shedinja allows you to embed Shedinja modules within [Koin v2](https://insert-koin.io/) application. It is compatible both ways: you can inject components from Koin into Shedinja and vice-versa.

!> Koin v3 is not supported. **This functionality abuses internal `@PublishedApi` mechanisms and is experimental.**

## Dependency

You will first need to add a dependency on the `shedinja-koin-v2` module. Make sure you also [have the `shedinja` dependency](/GettingStarted.md#adding-shedinja) as well as [Koin's dependencies](https://insert-koin.io/docs/setup/v2). Shedinja only requires the `koin-core` module for the integration to work. 

<!-- tabs:start -->

#### **Gradle Groovy DSL**

```groovy
dependencies {
    implementation 'guru.zoroark.shedinja:shedinja-koin-v2:VERSION'
}
```

#### **Gradle Kotlin DSL**

```kotlin
dependencies {
    implementation("guru.zoroark.shedinja:shedinja-koin-v2:VERSION")
}
```

#### **Maven**

```xml
<dependency>
    <groupId>guru.zoroark.shedinja</groupId>
    <artifactId>shedinja-koin-v2</artifactId>
    <version>VERSION</version>
</dependency>
```

<!-- tabs:end -->


## Shedinja modules in Koin apps

Create your modules as you would with regular Koin/Shedinja modules. Within your `KoinApplication` block, add your Shedinja modules using the `shedinjaModules` function:

```kotlin
val moduleFromShedinja = shedindjaModule {
    // ...
}

val moduleFromKoin = module {
    // ...
}

val koinApp = startKoin {
    modules(moduleFromKoin)
    
    shedinjaModules(moduleFromShedinja)
}
```
