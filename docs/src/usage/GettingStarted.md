# Getting Started

Shedinja is made of two libraries: a main library and a test library.

- The main library (`guru.zoroark.shedinja:shedinja`) provides the framework that will be used in your main application.
- The test library (`guru.zoroark.shedinja:shedinja-test`) provides tools for easily testing your Shedinja-powered application and checking the modules you created for them.

## Adding Shedinja

Shedinja can be used in your project by adding it as a dependency in Gradle or Maven.

Replace `VERSION` with the version you want. You can choose any version from the [packages available on the GitLab repository](https://gitlab.com/utybo/packages/-/packages?search[]=guru%2Fzoroark%2Fshedinja).

<!-- tabs:start -->

### **Gradle Groovy DSL**

```groovy
repositories {
    maven { url 'https://gitlab.com/api/v4/projects/29365238/packages/maven' }
}

dependencies {
    implementation 'guru.zoroark.shedinja:shedinja:VERSION'
    testImplementation 'guru.zoroark.shedinja:shedinja-test:VERSION'
}
```

#### Additional dependencies

The following features require you to add additional dependencies:

* [Services extension](extensions/Services.md)
```groovy
implementation 'guru.zoroark.shedinja:shedinja-services:VERSION'
```

### **Gradle Kotlin DSL**

```kotlin
repositories {
    maven {
        url = uri("https://gitlab.com/api/v4/projects/29365238/packages/maven")
    }
}

dependencies {
    implementation("guru.zoroark.shedinja:shedinja:VERSION")
    testImplementation("guru.zoroark.shedinja:shedinja-test:VERSION")
}
```

#### Additional dependencies

The following features require you to add additional dependencies:

* [Services extension](extensions/Services.md)
```kotlin
implementation("guru.zoroark.shedinja:shedinja-services:VERSION")
```

### **Maven**

```xml
<!-- Add this to your repositories -->
<repository>
    <id>utybo-github-com-Servine</id>
    <url>https://gitlab.com/api/v4/projects/29365238/packages/maven</url>
</repository>

<!-- Add these to your dependencies -->
<dependency>
    <groupId>guru.zoroark.shedinja</groupId>
    <artifactId>shedinja</artifactId>
    <version>VERSION</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>guru.zoroark.shedinja</groupId>
    <artifactId>shedinja</artifactId>
    <version>VERSION</version>
</dependency>
```
#### Additional dependencies

The following features require you to add additional dependencies:

* [Services extension](extensions/Services.md)
```xml
<dependency>
    <groupId>guru.zoroark.shedinja</groupId>
    <artifactId>shedinja-services</artifactId>
    <version>VERSION</version>
</dependency>
```

<!-- tabs:end -->

## Introduction to dependency injection.

Let's imagine that we are making a cooking server where a Chef wants to cook a cake. In order to make a cake, the Chef will need a cupboard, which in turn has access to an egg provider, a flour provider and some yeast, as well as kitchen tools such as an oven and a cake mold.

We can create the following classes to model our problem:

```kotlin
class Chef {
    fun makeSomeCake() {
        // ???
    }
}

class Cupboard {
    fun get(ingredientKind: IngredientKind): String {
        // ???
    }
}

enum class IngredientKind { Egg, Flour, Yeast }

class EggProvider {
    fun getEgg(): String {
        return "an egg"
    }
}

class FlourProvider {
    fun getFlour(): String {
        return "some flour"
    }
}

class YeastProvider {
    fun getYeast(): String {
        return "some yeast"
    }
}

class Oven {
    fun useOven(cookWhat: String, temperatureCelsius: Int, timeMinutes: Int) {
        println("Cooking $cookWhat at $temperatureCelsius°C for $time minutes")
    }
}

class CakeMold {
    fun useCakeMold(vararg ingredients: String): String {
        return "Cake mold with " + ingredients.joinToString()
    }
}
```

The problem now is to "link" these classes together: the chef will need access to the cupboard, the oven and the cake mold, and the cupboard will need access to the egg, flour and yeast providers.

This is where injection comes in. Thanks to Shedinja, we can request the cupboard, the oven and the cake mold from the environment, and the chef will be able to access them without having to know about the objects directly (i.e. without passing them through the constructor).

Let's modify our first two classes to use dependency injection:

```kotlin
class Chef(scope: InjectionScope) {
    private val cupboard: Cupboard by scope()
    private val oven: Oven by scope()
    private val cakeMold: CakeMold by scope()

    fun makeSomeCake() {
        val egg = cupboard.get(IngredientKind.Egg)
        val flour = cupboard.get(IngredientKind.Flour)
        val yeast = cupboard.get(IngredientKind.Yeast)

        val filledCakeMold = cakeMold.useCakeMold(egg, flour, yeast)
        oven.useOven(filledCakeMold, 180, 25)
        println("Cake has been baked successfully!")
    }
}

class Cupboard(scope: InjectionScope) {
    private val eggProvider: EggProvider by scope()
    private val flourProvider: FlourProvider by scope()
    private val yeastProvider: YeastProvider by scope()

    fun get(ingredient: IngredientKind): String {
        return when(ingredient) {
            Ingredient.Egg -> eggProvider.getEgg()
            Ingredient.Flour -> flourProvider.getFlour()
            Ingredient.Yeast -> yeastProvider.getYeast()
        }
    }
}
```

Here, we added two things:

* A `scope: InjectionScope` parameter to our classes' constructor. This scope provides information on how to retrieve the objects that the classes need upon injection (i.e. upon using `by scope()`).
* Our dependencies, expressed as `by scope()` [delegated properties](https://kotlinlang.org/docs/delegated-properties.html), which are now retrieved from the environment.

Now that our classes know how to request things, it's time to make the environment in which all these objects will live. In Shedinja, this is done by using the `shedinja` function.

```kotlin
val environment = shedinja {
    put { Chef(scope) }
    put { Cupboard(scope) }

    put { EggProvider() }
    put { FlourProvider() }
    put { YeastProvider() }

    put { Oven() }
    put { CakeMold() }
}
```

This creates an environment with all of the objects we provide within the `shedinja` block. Each `put` call adds an object to the environment.

* Classes with constructors that do not require a scope (in our case, everything but the chef and the cubpoard) can be created with a regular constructor call.
* Classes that require a scope (in our case, the chef and the cubpoard) can be created using the `scope` variable that is automatically available within the block of a `put` call.

## Going further

Our basic example has already shown you 80% of the job when using Shedinja, but there are a few things we can do to make our life easier.

### Simpler `put` calls

You may notice that our `put` calls are somewhat repetitive -- creating a new lambda every time is not paraticularily fun.

Shedinja provides a shortcut for cases where the constructor of the component we'd like to inject does not take any argument *or* only takes an injection scope. We can just reference the constructor of a class `Foo` by using `::Foo`:

```kotlin
val environment = shedinja {
    put(::Chef)
    put(::Cupboard)

    put(::EggProvider)
    put(::FlourProvider)
    put(::YeastProvider)

    put(::Oven)
    put(::CakeMold)
}
```

### Modularizing

Shedinja provides a way to modularize our code. We can create a module that contains all the objects we need to create a specific "facet" of our overall environment. The semantics of what a module is up to you: in Shedinja's eyes, a module is just a bunch of component definitions.

In our example, we can split our environment into two modules: one for the chef and his kitchen tools and one for the cupboard and its content.

```kotlin
val chefModule = shedinjaModule {
    put(::Chef)
    put(::Oven)
    put(::CakeMold)
}

val cupboardModule = shedinjaModule {
    put(::Cupboard)
    put(::EggProvider)
    put(::FlourProvider)
    put(::YeastProvider)
}

val environment = shedinja {
    put(chefModule)
    put(cupboardModule)
}
```

### Testing support

Shedinja provides tools to aid the creation of test in environments: see our [testing documentation](/Testing) for more information.
