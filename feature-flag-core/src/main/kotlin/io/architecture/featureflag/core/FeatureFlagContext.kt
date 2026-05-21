package io.architecture.featureflag.core

/**
 * Typed targeting context for feature flag evaluation.
 *
 * Direct mapping of the ConfigCat User Object — all fields mirror the
 * ConfigCat SDK's targeting data model. Supports provider portability:
 * adapters for other providers map these fields to their own user model.
 *
 * @param identifier Required. The unique ID of the user/session/device.
 *   Used as the seed for percentage-based rollouts.
 * @param email Optional. Enables email-based targeting rules on the dashboard.
 * @param country Optional. ISO country string for geo-based targeting.
 * @param custom Optional. Any application-specific attributes (plan, role, etc.).
 */
data class FeatureFlagContext(
    val identifier: String,
    val email: String? = null,
    val country: String? = null,
    val custom: Map<String, Any> = emptyMap()
)
