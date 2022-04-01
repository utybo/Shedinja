# Building Shedinja

The Shedinja project is built using a tool named [Gradle](https://gradle.org/). You will need a recent Java Development
Kit (JDK for short) on your system: you do not need to install Gradle.

You can grab a JDK over at [Adoptium](https://adoptium.net).

## Using gradle

Most tasks will require you to use Gradle. You can run Gradle tasks by using the wrapper scripts (`gradlew` for
macOS/Linux or `gradlew.bat` for Windows).

```sh
$ ./gradlew
```

## Building Shedinja

Shedinja can be built using the `build` task, like so:

```sh
$ ./gradlew build
```

This will run all the tests, code analysis tools and produce JAR files that you can then ingest. 

?> We recommend using [our pre-built JAR files](/usage/GettingStarted.md#adding-shedinja) instead of compiling Shedinja yourself.

Shedinja is organized in modules. Each module provides either specific functionality (like Koin integration) or can be 
more complex (like the `shedinja` module, which provides most of the core classes).

## Building the documentation

The documentation website uses custom Gradle tasks. There are two scenarios:

* Building the website
* Serving the website for previewing it while editing.

For the first case, you can just run:

```sh
$ ./gradlew buildDocs
```

For the second case, you will need two separate terminals.

```sh
# In the first terminal
$ ./gradlew -t buildDocs
# In the second terminal
$ ./gradlew serveDocs
```
