# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/), and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

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
