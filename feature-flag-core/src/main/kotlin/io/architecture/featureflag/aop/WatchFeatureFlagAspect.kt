package io.architecture.featureflag.aop

import io.architecture.featureflag.core.FeatureFlagClient
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.stereotype.Component

/**
 * Aspect that intercepts methods annotated with @WatchFeatureFlag.
 * Evaluates the feature flag and logs execution with structured context.
 */
@Aspect
@Component
class WatchFeatureFlagAspect(
    private val featureFlagClient: FeatureFlagClient
) {

    companion object {
        private val logger = LoggerFactory.getLogger(WatchFeatureFlagAspect::class.java)
    }

    /**
     * Intercepts method execution for @WatchFeatureFlag annotated methods.
     * Logs entry and exit with feature flag evaluation context.
     */
    @Around("@annotation(watchFeatureFlag)")
    fun aroundMethod(joinPoint: ProceedingJoinPoint, watchFeatureFlag: WatchFeatureFlag): Any? {
        val flagKey = watchFeatureFlag.flagKey
        val methodName = joinPoint.signature.name
        val className = joinPoint.target::class.simpleName

        // Set MDC context for structured logging
        MDC.put("ff_key", flagKey)
        MDC.put("ff_mechanism", "WatchFeatureFlag")
        MDC.put("ff_method", "$className.$methodName")

        // Evaluate feature flag (anonymous user for now)
        val isFlagActive = featureFlagClient.isActive(flagKey, "anonymous")
        MDC.put("ff_result", isFlagActive.toString())

        return try {
            // Execute the actual method
            val result = joinPoint.proceed()
            logger.info("Feature Flag method execution completed")
            result
        } catch (e: Throwable) {
            logger.error("Feature Flag method execution failed: ${e.message}")
            throw e
        } finally {
            // Clean MDC to prevent thread contamination
            MDC.remove("ff_key")
            MDC.remove("ff_context_id")
            MDC.remove("ff_result")
            MDC.remove("ff_mechanism")
            MDC.remove("ff_method")
        }
    }
}
