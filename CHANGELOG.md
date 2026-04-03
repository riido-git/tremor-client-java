# Changelog

All notable changes to this project will be documented in this file.

The format is based on Keep a Changelog, and this project follows Semantic Versioning.

## [Unreleased]

## [0.1.0] - 2026-04-03

### Added

- Initial Java 8 Tremor client with OkHttp transport and Gson serialization.
- Builder-based `TremorClient` API for synchronous reporting with `report(...)`.
- Throwable capture with `TremorCaptureOptions` for tags, custom data, user, environment, breadcrumbs, and grouping metadata.
- Explicit event reporting with `TremorEvent`, `TremorError`, `TremorStackFrame`, `TremorBreadcrumb`, `TremorEnvironment`, `TremorClientInfo`, and `TremorUser`.
- Asynchronous reporting with `reportAsync(...)` returning `CompletableFuture<String>`.
- Best-effort suppression helpers with `reportAndSuppress(...)`.
- Support for both library-owned and caller-provided `OkHttpClient` instances.
- Builder support for `baseUrl(String)` and `baseUrl(URL)`.
- Builder timeout configuration for library-owned transport: connect, read, write, and call timeout.
- Immutable throwable-capture defaults through `TremorClientDefaults`.
- MockWebServer-based HTTP behavior tests covering request shape, ownership, malformed responses, async behavior, and validation failures.
- Maven packaging for sources and Javadocs, plus Maven Central metadata.
