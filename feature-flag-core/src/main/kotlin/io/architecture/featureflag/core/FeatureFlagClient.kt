package io.architecture.featureflag.core

/**
 * Core abstraction for feature flag evaluation.
 * Implementations of this interface provide the actual mechanism
 * for checking if a feature flag is active for a given user context.
 */
interface FeatureFlagClient {

    /**
     * Evaluates if a feature flag is active for the given context.
     *
     * @param flagKey The unique identifier of the feature flag
     * @param context Optional targeting context (identifier, email, country, custom attributes).
     *   Pass null for global on/off flags that require no per-user targeting.
     * @return true if the flag is active for this context, false otherwise
     */
    fun isActive(flagKey: String, context: FeatureFlagContext?): Boolean
}
