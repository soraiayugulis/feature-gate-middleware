package io.architecture.featureflag.aop

/**
 * Annotation to document feature flag flows at the entry point of an endpoint.
 * Acts as living documentation: declares which feature flags influence the behavior
 * of the annotated method and describes the intent of the flagged flow.
 *
 * Every key listed in [flagKeys] represents a feature flag that may alter the behavior
 * of this endpoint. All flags monitored via [@WatchFeatureFlag][WatchFeatureFlag] on the
 * same method must be present in this array — enforced at startup by the validator.
 *
 * Example usage:
 * ```kotlin
 * @HiFeatureFlag(
 *     flagKeys = ["checkout-v2", "pix-routing"],
 *     description = "New checkout flow with PIX priority routing"
 * )
 * @WatchFeatureFlag(flagKey = "checkout-v2")
 * fun processCheckout(): ResponseEntity<CheckoutResponse>
 * ```
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class HiFeatureFlag(
    /**
     * The feature flag keys that influence the behavior of the annotated endpoint.
     * Supports multiple keys when more than one flag gates the flow.
     * Must be non-empty — validated at startup.
     */
    val flagKeys: Array<String>,

    /**
     * Human-readable description of the feature flag flow.
     * Injected into OpenAPI/Swagger documentation when SpringDoc is present.
     */
    val description: String
)
