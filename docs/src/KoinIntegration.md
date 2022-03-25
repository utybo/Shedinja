# Koin Integration

Shedinja allows you to embed Shedinja modules within [Koin v2/v3](https://insert-koin.io/) applications. It is compatible both ways: you can inject components from Koin into Shedinja and vice-versa.

!> This functionality abuses internal `@PublishedApi` mechanisms and is experimental.

## Dependency

You will first need to add a dependency on the `shedinja-koin-v2` or `shedinja-koin-v3` module depending of which versino of Koin you want to integrate with. Make sure you also [have the `shedinja` dependency](/GettingStarted.md#adding-shedinja) as well as [Koin's own dependencies](https://insert-koin.io/docs/setup/v2). Shedinja only requires the `koin-core` module for the integration to work.


### Koin 2

Relevant functions and classes are provided in the `guru.zoroark.shedinja.koin2` package.

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

### Koin 3

Relevant functions and classes are provided in the `guru.zoroark.shedinja.koin2` package.

<!-- tabs:start -->

#### **Gradle Groovy DSL**

```groovy
dependencies {
    implementation 'guru.zoroark.shedinja:shedinja-koin-v3:VERSION'
}
```

#### **Gradle Kotlin DSL**

```kotlin
dependencies {
    implementation("guru.zoroark.shedinja:shedinja-koin-v3:VERSION")
}
```

#### **Maven**

```xml
<dependency>
    <groupId>guru.zoroark.shedinja</groupId>
    <artifactId>shedinja-koin-v3</artifactId>
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

!> It is not currently possible to add Koin modules to Shedinja environments. If you wish to migrate from Koin to Shedinja, you should do it by progressively turning your Koin modules into Shedinja modules and, once all of your modules have become Shedinja modules, use Shedinja's [environment builders](UsingShedinja.md#environment) and [test facilities](Testing.md).
