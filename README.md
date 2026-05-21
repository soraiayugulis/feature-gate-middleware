# Feature Gate Middleware

by @_sysout

A Spring Boot middleware library for feature flag evaluation with typed targeting context, AOP instrumentation, metrics, and OpenAPI integration.

## Features

- **Typed Targeting Context**: Structured `FeatureFlagContext` with identifier, email, country, and custom attributes
- **AOP Instrumentation**: `@WatchFeatureFlag` for automatic aspect-oriented metrics and logging
- **Startup Inventory**: `@HiFeatureFlag` annotation with startup logging of all flag-gated methods
- **Metrics Integration**: Micrometer Counters and Timers for flag evaluation metrics
- **OpenAPI/Swagger**: Automatic documentation of flag-gated endpoints in Swagger UI
- **Spring Boot Auto-Configuration**: Zero-configuration setup with conditional bean registration
- **ConfigCat Integration**: Built-in ConfigCat SDK client (or provide custom `FeatureFlagClient`)

## Gradle Dependency

```kotlin
dependencies {
    implementation("io.architecture:feature-flag-core:1.0.0")
}
```

## Configuration

Add to your `application.yml`:

```yaml
feature-gate:
  # ConfigCat SDK configuration (required)
  configcat:
    sdk-key: ${CONFIGCAT_SDK_KEY}  # Your ConfigCat SDK key

  # WatchFeatureFlag aspect (optional, requires Micrometer)
  watch:
    enabled: true  # Enable/disable @WatchFeatureFlag metrics

  # HiFeatureFlag startup inventory (optional)
  hi:
    enabled: true  # Enable/disable @HiFeatureFlag startup logging
```

## Usage Examples

### 1. Using FeatureFlagExecutor Directly

```kotlin
@Service
class CheckoutService(
    private val featureFlagExecutor: FeatureFlagExecutor
) {
    fun checkout(): String {
        val context = FeatureFlagContext(
            identifier = "user123",
            email = "user@example.com",
            country = "BR",
            custom = mapOf("wave" to "early-adopter")
        )

        return featureFlagExecutor.execute("checkout-v2", context) {
            // This block runs if the flag is enabled
            "checkout-v2"
        } ?: "checkout-v1" // Fallback when flag is disabled
    }
}
```

### 2. Using @WatchFeatureFlag (AOP)

```kotlin
@Service
class PaymentService {

    @WatchFeatureFlag(flagKeys = ["pix-routing"], description = "Pix payment routing")
    fun processPayment(): PaymentResult {
        // Automatically wrapped with metrics and logging
        // Counter: feature.flag.evaluation.total{flag=pix-routing}
        // Timer: feature.flag.evaluation.duration{flag=pix-routing}
        // Error Counter: feature.flag.evaluation.errors{flag=pix-routing}
        return PaymentResult.success()
    }
}
```

### 3. Using @HiFeatureFlag (Startup Inventory)

```kotlin
@RestController
@RequestMapping("/api/v1")
class ProductController {

    @HiFeatureFlag(
        flagKeys = ["new-product-catalog"],
        description = "New product catalog UI"
    )
    @GetMapping("/products")
    fun getProducts(): List<Product> {
        // This method will be logged at startup:
        // INFO - Feature Flag Inventory: [{"key":"new-product-catalog","description":"New product catalog UI","method":"ProductController.getProducts"}]
        return productService.findAll()
    }
}
```

### 4. Custom FeatureFlagClient

If you don't use ConfigCat, provide your own `FeatureFlagClient` bean:

```kotlin
@Configuration
class FeatureFlagConfiguration {
    @Bean
    fun customFeatureFlagClient(): FeatureFlagClient {
        return object : FeatureFlagClient {
            override fun isActive(flagKey: String, context: FeatureFlagContext?): Boolean {
                // Your custom flag evaluation logic
                return true
            }
        }
    }
}
```

## OpenAPI/Swagger Integration

When `feature-gate.hi.enabled=true` and SpringDoc OpenAPI is on the classpath, the library automatically enhances Swagger UI:

- **Feature Flag Tag**: Endpoints with `@HiFeatureFlag` are tagged with "feature-flag"
- **x-feature-flags Extension**: Each operation includes a list of flag keys and descriptions
- **Description Suffix**: "⚑ This endpoint's behavior may vary based on active feature flags." is appended

Example Swagger UI output:
```json
{
  "operationId": "getProducts",
  "tags": ["feature-flag", "products"],
  "description": "Get all products ⚑ This endpoint's behavior may vary based on active feature flags.",
  "x-feature-flags": [
    {
      "key": "new-product-catalog",
      "description": "New product catalog UI"
    }
  ]
}
```

## Known Limitations

### 1. AOP Proxy Bypass
Internal method calls within the same class bypass Spring AOP proxies. This means `@WatchFeatureFlag` and `@HiFeatureFlag` annotations will not trigger when methods are called internally:

```kotlin
@Service
class MyService {
    @WatchFeatureFlag(flagKeys = ["my-flag"], description = "My feature")
    fun publicMethod() {
        // Metrics will be recorded when called externally
    }

    fun callingMethod() {
        publicMethod() // ⚠️ Metrics will NOT be recorded - AOP bypass
    }
}
```

**Workaround**: Call the method via `self` proxy injected by `@Autowired`.

### 2. suspend fun on @WatchFeatureFlag
The `@WatchFeatureFlag` aspect does not support `suspend` functions. Using it on suspend functions will trigger a startup validation warning:

```kotlin
@WatchFeatureFlag(flagKeys = ["my-flag"], description = "My feature")
suspend fun mySuspendFunction() { // ⚠️ Not supported - metrics will not be recorded
    // ...
}
```

**Workaround**: Use `FeatureFlagExecutor` directly within suspend functions.

### 3. MDC + Virtual Threads
MDC (Mapped Diagnostic Context) is not propagated with virtual threads. If you use virtual threads, MDC context set in the aspect will not be visible in downstream code.

**Workaround**: Use structured logging with explicit context parameters instead of relying on MDC.

## License

Apache License 2.0
