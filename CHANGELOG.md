# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/), and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased] (0.0.4)

**Warning:** This release contains breaking changes with the previous release (noted as **BREAKS API** or **BREAKS ABI**).

### Added

- `shedinja` (core)
    - Added the ability to inject meta-environment components into regular components via the `scope.meta` property.
    - Added the ability to combine qualifiers to create a `MultiQualifier`.
    - Added optional injections (`scope.optional()`).
- All
    - (Internal) Added `jacoco` plugin for code coverage. This does not impact builds depending on Shedinja with CodeCov on the repository.
    - (Internal) Drastically improved code coverage across the entire code base.
    - (Internal) Added binary compatibility validator.

### Changed

- All
    - All modules now use `ShedinjaException` subclasses for reporting exceptions.
- `shedinja`
    - Split `Qualifier` classes and functions into multiple files (**BREAKS ABI**).
    - Removed the `factory from scope` syntax and use `scope.factory()` instead (**BREAKS API**).

### Removed

- `shedinja` (core)
    - Removed `BuildResult<T>` and related symbols. They were never used anywhere other than to signal a successful build, making them useless.

## [0.0.3] - 2022-03-12

### Added

- `shedinja` (core)
    - Added a base for the extension system with:
        - Installable extensions
        - Extensible injection environments with meta-environments
        - Declaration tags
        - Declaration processors
    - Added a reified `getOrNull()` function, similar to the existing `get()` function, e.g. `env.getOrNull<MyComponent>()`
- `shedinja-test`
    - Added the `noUnused` check.
    - Added the `safeInjection` check.
- `shedinja-services` ✨ **New module** ✨
    - Added the Shedinja Services extension with basic functionality.

### Changed

- `shedinja` (core)
    - All `put` DSL functions now return the declaration they created (this is required for the declaration tagging feature to work).
    - `MixedImmutableEnvironment` is now an extensible injection environment.
    - By default, the `shedinja` DSL creates an extensible injection environment.
- `shedinja-test`
    - Made the `DependencyTrackingInjectionEnvironment` class public: feel free to use it to analyze component dependencies!

## [0.0.2] - 2021-09-12

### Added

- Added Koin v2 and v3 integration.
- Added factories injection extension.

## [0.0.1] - 2021-09-05

### Added

- Added basic functionality and test facilities

[Unreleased]: https://github.com/utybo/Shedinja/compare/v0.0.3..main
[0.0.3]: https://github.com/utybo/Shedinja/compare/v0.0.3..v0.0.2
[0.0.2]: https://github.com/utybo/Shedinja/compare/v0.0.2..v0.0.1
[0.0.1]: https://github.com/utybo/Shedinja/releases/tag/v0.0.1
