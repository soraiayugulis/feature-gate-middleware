package io.architecture.featureflag.aop

import io.architecture.featureflag.core.FeatureFlagClient
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

/**
 * Aspect that intercepts methods annotated with @WatchFeatureFlag.
 * Evaluates the feature flag, records Micrometer metrics, and logs execution with structured context.
 *
 * Only registered when:
 * - `feature-gate.watch.enabled=true`
 * - `MeterRegistry` is on classpath (Micrometer available)
 */
@Aspect
@Component
@ConditionalOnProperty(prefix = "feature-gate.watch", name = ["enabled"], havingValue = "true", matchIfMissing = false)
@ConditionalOnClass(MeterRegistry::class)
class WatchFeatureFlagAspect(
    private val featureFlagClient: FeatureFlagClient,
    private val meterRegistry: MeterRegistry
) {

    companion object {
        private val logger = LoggerFactory.getLogger(WatchFeatureFlagAspect::class.java)
    }

    /**
     * Intercepts method execution for @WatchFeatureFlag annotated methods.
     * Records metrics, sets MDC context, and logs execution with feature flag evaluation.
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

        // Evaluate feature flag with no user context — aspect has no request context at this level
        val isFlagActive = featureFlagClient.isActive(flagKey, null)
        val status = if (isFlagActive) "ENABLED" else "DISABLED"
        MDC.put("ff_result", status)

        // Record evaluation counter
        meterRegistry.counter("feature_flag.evaluation.total", "ff.key", flagKey, "ff.status", status).increment()

        logger.info("Method entry - feature flag evaluated")

        return try {
            // Start timer and proceed
            val sample = Timer.start(meterRegistry)
            val result = joinPoint.proceed()
            val durationMs = sample.stop(meterRegistry.timer("feature_flag.evaluation.duration", "ff.key", flagKey))
            MDC.put("ff_duration_ms", durationMs.toString())

            logger.info("Method exit - execution completed")
            result
        } catch (e: Throwable) {
            // Record error counter
            meterRegistry.counter(
                "feature_flag.evaluation.errors",
                "ff.key", flagKey,
                "ff.error_type", e.javaClass.simpleName
            ).increment()

            logger.error("Method exit - exception thrown: ${e.message}")
            throw e
        } finally {
            // Clean MDC to prevent thread contamination
            MDC.remove("ff_key")
            MDC.remove("ff_context_id")
            MDC.remove("ff_result")
            MDC.remove("ff_mechanism")
            MDC.remove("ff_method")
            MDC.remove("ff_duration_ms")
        }
    }
}
