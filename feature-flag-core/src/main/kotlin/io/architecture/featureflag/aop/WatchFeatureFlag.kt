package io.architecture.featureflag.aop

/**
 * Annotation to enable feature flag monitoring on methods or classes.
 * When applied, the aspect will log method execution with feature flag context.
 *
 * Example usage on method:
 * ```kotlin
 * @WatchFeatureFlag("novo-checkout-flow")
 * fun processCheckout(order: Order) {
 *     // Method implementation
 * }
 * ```
 *
 * Example usage on class:
 * ```kotlin
 * @WatchFeatureFlag("beta-api-v2")
 * class BetaController {
 *     // All methods will be monitored
 * }
 * ```
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class WatchFeatureFlag(
    /**
     * The feature flag key to evaluate and log during method execution.
     */
    val flagKey: String
)
