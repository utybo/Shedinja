# External Components

?> This extension is experimental. Some functions internally use Kotlin's [`typeOf` function](), which is itself experimental.

*This is a pure extension that is compatible with all environments.*

Sometimes, you may want to add classes that were not built with Shedinja in mind to your injection environment. While this is fine for classes that do not rely on anything from the injection environment (e.g. a simple `String`), this becomes a problem when you want to inject things from the Shedinja environment *into* the external class.

The External Components extension (or External extension for short) allows you to add such components to your environment.

## Usage

External components are added using the `putExternal` family of functions.

```kotlin
val env = shedinja {
    // Simple definition (with reified typing)
    putExternal(::MyExternalComponent)
    // Definition with a qualifier (with reified typing)
    putExternal(named("My component"), ::MyExternalComponent)

    // Simple definition (without reified typing)
    putExternal(MyExternalComponent::class, ::MyExternalComponent)
    // Definition with a qualifier (without reified typing)
    putExternal(MyExternalComponent::class, named("My component"), ::MyExternalComponent)
}
```

You can retrieve such components from the environment using the `getExternal` and `getExternalOrNull` functions.


### Customizing injections

By default, injections performed in the constructor given to the putExternal function are made of a simple identifier with the type required by the constructor and no qualifier.

