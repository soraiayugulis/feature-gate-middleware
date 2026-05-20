package io.architecture.featureflag.logging

import org.slf4j.MDC

/**
 * Utility for managing MDC (Mapped Diagnostic Context) entries for feature flag operations.
 * Ensures consistent logging context across all feature flag mechanisms.
 */
object FeatureFlagMdc {

    /**
     * Sets the feature flag key in MDC.
     */
    fun setFlagKey(flagKey: String) {
        MDC.put("ff_key", flagKey)
    }

    /**
     * Sets the context/user identifier in MDC.
     */
    fun setContextId(contextId: String) {
        MDC.put("ff_context_id", contextId)
    }

    /**
     * Sets the mechanism name in MDC (e.g., "ValidateIfFeatureFlagActive", "WatchFeatureFlag").
     */
    fun setMechanism(mechanism: String) {
        MDC.put("ff_mechanism", mechanism)
    }

    /**
     * Sets the feature flag evaluation result in MDC.
     */
    fun setResult(result: Boolean) {
        MDC.put("ff_result", result.toString())
    }

    /**
     * Sets the method signature for AOP context.
     */
    fun setMethod(method: String) {
        MDC.put("ff_method", method)
    }

    /**
     * Clears all feature flag related MDC entries.
     * Should be called in finally blocks to prevent thread contamination.
     */
    fun clear() {
        MDC.remove("ff_key")
        MDC.remove("ff_context_id")
        MDC.remove("ff_mechanism")
        MDC.remove("ff_result")
        MDC.remove("ff_method")
    }

    /**
     * Executes a block with MDC context and automatically clears it afterwards.
     *
     * @param flagKey The feature flag key
     * @param mechanism The mechanism name
     * @param contextId The user/context identifier
     * @param block The code block to execute
     * @return The result of the block execution
     */
    inline fun <T> withContext(
        flagKey: String,
        mechanism: String,
        contextId: String = "anonymous",
        block: () -> T
    ): T {
        setFlagKey(flagKey)
        setMechanism(mechanism)
        setContextId(contextId)

        return try {
            block()
        } finally {
            clear()
        }
    }
}
