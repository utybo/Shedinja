# Modules

Shedinja provides a module system that allows you to define your environment in multiple, smaller bits. For example:

```kotlin
class UserRepository
class UserService(scope: InjectionScope) {
    private val repository: UserRepository by scope()
}
class UserController(scope: InjectionScope) {
    private val service: UserService by scope()
}

class MessageRepository
class MessageService(scope: InjectionScope) {
    private val repository: MessageRepository by scope()
}
class MessageController(scope: InjectionScope) {
    private val service: MessageService by scope()
}

val userModule = shedinjaModule {
    put(::UserRepository)
    put(::UserService)
    put(::UserController)
}

val messageModule = shedinjaModule {
    put(::MessageRepository)
    put(::MessageService)
    put(::MessageController)
}

val environment = shedinja {
    put(userModule)
    put(messageModule)
}
```
