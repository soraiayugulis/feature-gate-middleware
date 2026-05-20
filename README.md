# Feature Gate Middleware

by @_sysout

A Kotlin library for feature flag validation and conditional execution with Spring Boot integration.

## Features

- **Field-Level Validation**: Jakarta Bean Validation constraints for feature flag-based field validation
- **Method-Level AOP**: Aspect-oriented monitoring with structured logging
- **DSL Executor**: Functional API for conditional code execution
- **Structured Logging**: SLF4J MDC integration for observability
- **ConfigCat Integration**: Ready-to-use adapter for ConfigCat feature flags

## Installation

```kotlin
dependencies {
    implementation("io.architecture:feature-flag-core:1.0.0")
}
```

## Quick Start

### 1. Field Validation

```kotlin
data class UserRegistrationDto(
    val email: String,
    @field:ValidateIfFeatureFlagActive(
        flagKey = "require-verified-document",
        contextIdField = "email"
    )
    val taxId: String?
)
```

### 2. Method Monitoring

```kotlin
@WatchFeatureFlag("new-checkout-flow")
fun processCheckout(order: Order) {
    // Method implementation
}
```

### 3. DSL Executor

```kotlin
@Autowired
lateinit var executor: FeatureFlagExecutor

fun processOrder(order: Order, userId: String): String {
    return executor.execute("new-checkout", userId) {
        onActive { newCheckoutService.process(order) }
        onDisable { legacyCheckoutService.process(order) }
    }
}
```

## Configuration

```yaml
feature-flag:
  configcat:
    sdk-key: ${CONFIGCAT_SDK_KEY}
    polling-mode: auto-poll
    poll-interval-seconds: 60
```

## Annotations Reference

| Annotation | Target | Purpose |
|------------|--------|---------|
| `@ValidateIfFeatureFlagActive` | Field | Validates field only when flag is active |
| `@ValidateFeatureFlagByWave` | Field | Canary/percentage rollout validation |
| `@WatchFeatureFlag` | Method/Class | Method execution monitoring |

## Logging

Structured logs with MDC context:
```
ff_key: new-checkout-flow
ff_context_id: user-123
ff_result: true
ff_mechanism: FeatureFlagExecutor
```

## License

MIT License - see LICENSE file for details.

