package io.architecture.featureflag.service

import io.architecture.featureflag.core.FeatureFlagClient
import io.architecture.featureflag.logging.FeatureFlagMdc
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * Executor component for feature flag conditional execution.
 * Provides a DSL-style API for executing code based on feature flag state.
 *
 * Example usage:
 * ```kotlin
 * featureFlagExecutor.execute("novo-checkout-flow", userId) {
 *     onActive { processNewCheckout(order) }
 *     onDisable { processLegacyCheckout(order) }
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
     * @param userId The user identifier for flag evaluation
     * @param block The execution block defining onActive and onDisable handlers
     * @return The result of the executed block
     */
    fun <T> execute(
        flagKey: String,
        userId: String,
        block: ExecutionBlock<T>.() -> Unit
    ): T {
        val executionBlock = ExecutionBlock<T>().apply(block)

        return FeatureFlagMdc.withContext(flagKey, "FeatureFlagExecutor", userId) {
            val isActive = featureFlagClient.isActive(flagKey, userId)
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
         * Executes the onActive block or throws if not defined.
         */
        fun executeOnActive(): T {
            return onActive?.invoke()
                ?: throw IllegalStateException("onActive handler not defined")
        }

        /**
         * Executes the onDisable block or returns null if not defined.
         */
        fun executeOnDisable(): T {
            return onDisable?.invoke()
                ?: throw IllegalStateException("onDisable handler not defined")
        }
    }
}
