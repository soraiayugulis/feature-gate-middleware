package io.architecture.featureflag.service

import io.architecture.featureflag.core.FeatureFlagClient
import io.architecture.featureflag.core.FeatureFlagContext
import io.architecture.featureflag.logging.FeatureFlagMdc
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * Executor component for feature flag conditional execution.
 * Provides a DSL-style API for executing code based on feature flag state.
 *
 * Handlers are optional - if not defined, null is returned for that branch.
 *
 * Example usage:
 * ```kotlin
 * // Both handlers defined
 * featureFlagExecutor.execute("novo-checkout-flow", userId) {
 *     onActive { processNewCheckout(order) }
 *     onDisable { processLegacyCheckout(order) }
 * }
 *
 * // Only active handler (returns null when disabled)
 * featureFlagExecutor.execute<String?>("beta-feature", userId) {
 *     onActive { "beta-result" }
 * }
 *
 * // Only disable handler (returns null when active)
 * featureFlagExecutor.execute<String?>("legacy-mode", userId) {
 *     onDisable { "legacy-result" }
 * }
 * ```
 */
@Component
class FeatureFlagExecutor(
    private val featureFlagClient: FeatureFlagClient
) {

    companion object {
        private val logger = LoggerFactory.getLogger(FeatureFlagExecutor::class.java)
    }

    /**
     * Executes code based on feature flag state.
     *
     * @param flagKey The feature flag key to evaluate
     * @param context Optional targeting context. Pass null for global on/off flags.
     * @param block The execution block defining onActive and onDisable handlers
     * @return The result of the executed block
     */
    fun <T> execute(
        flagKey: String,
        context: FeatureFlagContext? = null,
        block: ExecutionBlock<T>.() -> Unit
    ): T {
        val executionBlock = ExecutionBlock<T>().apply(block)
        val contextId = context?.identifier ?: "global"

        return FeatureFlagMdc.withContext(flagKey, "FeatureFlagExecutor", contextId) {
            val isActive = featureFlagClient.isActive(flagKey, context)
            FeatureFlagMdc.setResult(isActive)

            logger.info("FeatureFlagExecutor evaluated flag for user")

            if (isActive) {
                executionBlock.executeOnActive()
            } else {
                executionBlock.executeOnDisable()
            }
        }
    }

    /**
     * Execution block DSL for defining active and disable handlers.
     */
    class ExecutionBlock<T> {
        private var onActive: (() -> T)? = null
        private var onDisable: (() -> T)? = null

        /**
         * Defines the code to execute when the feature flag is active.
         */
        fun onActive(block: () -> T) {
            this.onActive = block
        }

        /**
         * Defines the code to execute when the feature flag is disabled.
         */
        fun onDisable(block: () -> T) {
            this.onDisable = block
        }

        /**
         * Executes the onActive block or returns null if not defined.
         */
        @Suppress("UNCHECKED_CAST")
        fun executeOnActive(): T {
            return onActive?.invoke() ?: null as T
        }

        /**
         * Executes the onDisable block or returns null if not defined.
         */
        @Suppress("UNCHECKED_CAST")
        fun executeOnDisable(): T {
            return onDisable?.invoke() ?: null as T
        }
    }
}
