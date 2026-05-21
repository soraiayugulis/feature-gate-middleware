# Library Feature Gate Middleware

A Spring Boot middleware library for feature flag evaluation with typed targeting context, AOP instrumentation, metrics, and OpenAPI integration.

## Documentation

- [Presentation](docs/presentation.md) — Architecture, features, and development methodology
- [Step by Step Spec-Driven Development](docs/spec-driven-development/step2step.md) — Detailed specification and implementation phases
- [Project Specification](docs/spec-driven-development/new-path.md) — Defined scope and value proposition

## Quick Start

### Gradle Dependency

```kotlin
dependencies {
    implementation("io.featuregate:feature-gate-middleware:0.1.0-SNAPSHOT")
}
```

### Configuration

Add to your `application.yml`:

```yaml
feature-gate:
  configcat:
    sdk-key: ${CONFIGCAT_SDK_KEY}
  watch:
    enabled: true
  hi:
    enabled: true
```

### Basic Usage

```kotlin
// Direct execution
featureFlagExecutor.execute("my-flag", context) {
    // enabled path
} ?: fallback()

// AOP instrumentation
@WatchFeatureFlag(flagKeys = ["my-flag"], description = "My feature")
fun myMethod() {
    // automatically instrumented with metrics
}

// Startup inventory
@HiFeatureFlag(flagKeys = ["my-flag"], description = "My feature")
fun myEndpoint() {
    // logged at startup
}
```

## Features

- Typed targeting context with identifier, email, country, custom attributes
- `@WatchFeatureFlag` for automatic Micrometer metrics
- `@HiFeatureFlag` for startup flag inventory
- OpenAPI/Swagger integration for flag-gated endpoints
- Spring Boot auto-configuration with conditional bean registration
- ConfigCat SDK integration (or custom `FeatureFlagClient`)

## Development

Built with Spec-Driven Development (SDD) and AI-augmented practices. See [Technical Presentation](docs/presentation.md) for methodology details.

---

by @_sysout
