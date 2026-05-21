package io.architecture.featureflag.aop

import io.architecture.featureflag.core.FeatureFlagClient
import io.micrometer.core.instrument.MeterRegistry
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.reflect.MethodSignature
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class WatchFeatureFlagAspectMetricsTest {

    private lateinit var featureFlagClient: FeatureFlagClient
    private lateinit var meterRegistry: MeterRegistry
    private lateinit var aspect: WatchFeatureFlagAspect
    private lateinit var joinPoint: ProceedingJoinPoint
    private lateinit var methodSignature: MethodSignature

    @BeforeEach
    fun setUp() {
        featureFlagClient = mockk()
        meterRegistry = mockk(relaxed = true)
        aspect = WatchFeatureFlagAspect(featureFlagClient, meterRegistry)
        joinPoint = mockk()
        methodSignature = mockk()

        every { joinPoint.signature } returns methodSignature
        every { methodSignature.name } returns "testMethod"
        every { joinPoint.target } returns this
    }

    @Test
    fun `should record evaluation counter with ENABLED status when flag is active`() {
        // Given
        val annotation = mockk<WatchFeatureFlag>()
        every { annotation.flagKey } returns "checkout-v2"
        every { featureFlagClient.isActive("checkout-v2", null) } returns true
        every { joinPoint.proceed() } returns "result"

        // When
        aspect.aroundMethod(joinPoint, annotation)

        // Then
        verify { meterRegistry.counter("feature_flag.evaluation.total", "ff.key", "checkout-v2", "ff.status", "ENABLED") }
    }

    @Test
    fun `should record evaluation counter with DISABLED status when flag is inactive`() {
        // Given
        val annotation = mockk<WatchFeatureFlag>()
        every { annotation.flagKey } returns "checkout-v2"
        every { featureFlagClient.isActive("checkout-v2", null) } returns false
        every { joinPoint.proceed() } returns "result"

        // When
        aspect.aroundMethod(joinPoint, annotation)

        // Then
        verify { meterRegistry.counter("feature_flag.evaluation.total", "ff.key", "checkout-v2", "ff.status", "DISABLED") }
    }

    @Test
    fun `should record duration timer after method completes`() {
        // Given
        val annotation = mockk<WatchFeatureFlag>()
        every { annotation.flagKey } returns "test-flag"
        every { featureFlagClient.isActive("test-flag", null) } returns true
        every { joinPoint.proceed() } returns "result"

        // When
        aspect.aroundMethod(joinPoint, annotation)

        // Then
        verify { meterRegistry.timer("feature_flag.evaluation.duration", "ff.key", "test-flag") }
    }

    @Test
    fun `should record error counter when intercepted method throws`() {
        // Given
        val annotation = mockk<WatchFeatureFlag>()
        every { annotation.flagKey } returns "test-flag"
        every { featureFlagClient.isActive("test-flag", null) } returns true
        every { joinPoint.proceed() } throws RuntimeException("Test exception")

        // When / Then
        try {
            aspect.aroundMethod(joinPoint, annotation)
        } catch (e: RuntimeException) {
            // Expected
        }

        verify { meterRegistry.counter("feature_flag.evaluation.errors", "ff.key", "test-flag", "ff.error_type", "RuntimeException") }
    }
}
