package io.architecture.featureflag.core

/**
 * Core abstraction for feature flag evaluation.
 * Implementations of this interface provide the actual mechanism
 * for checking if a feature flag is active for a given user context.
 */
interface FeatureFlagClient {

    /**
     * Evaluates if a feature flag is active for the given user context.
     *
     * @param flagKey The unique identifier of the feature flag
     * @param userId The unique identifier of the user (used for targeting and percentage rollouts)
     * @param attributes Optional map of custom attributes for targeting rules
     * @return true if the flag is active for this user context, false otherwise
     */
    fun isActive(flagKey: String, userId: String, attributes: Map<String, Any> = emptyMap()): Boolean
}
