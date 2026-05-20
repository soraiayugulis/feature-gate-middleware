package io.architecture.featureflag.aop

import io.architecture.featureflag.core.FeatureFlagClient
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.reflect.MethodSignature
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class WatchFeatureFlagAspectTest {

    private lateinit var featureFlagClient: FeatureFlagClient
    private lateinit var aspect: WatchFeatureFlagAspect
    private lateinit var joinPoint: ProceedingJoinPoint
    private lateinit var methodSignature: MethodSignature

    @BeforeEach
    fun setUp() {
        featureFlagClient = mockk()
        aspect = WatchFeatureFlagAspect(featureFlagClient)
        joinPoint = mockk()
        methodSignature = mockk()

        every { joinPoint.signature } returns methodSignature
        every { methodSignature.name } returns "testMethod"
        every { joinPoint.target } returns this
    }

    @Test
    fun `aroundMethod should proceed with join point when flag is active`() {
        // Given
        val annotation = mockk<WatchFeatureFlag>()
        every { annotation.flagKey } returns "test-flag"
        every { featureFlagClient.isActive("test-flag", any()) } returns true
        every { joinPoint.proceed() } returns "result"

        // When
        val result = aspect.aroundMethod(joinPoint, annotation)

        // Then
        assert(result == "result")
        verify { joinPoint.proceed() }
        verify { featureFlagClient.isActive("test-flag", any()) }
    }

    @Test
    fun `aroundMethod should proceed with join point when flag is inactive`() {
        // Given
        val annotation = mockk<WatchFeatureFlag>()
        every { annotation.flagKey } returns "test-flag"
        every { featureFlagClient.isActive("test-flag", any()) } returns false
        every { joinPoint.proceed() } returns "result"

        // When
        val result = aspect.aroundMethod(joinPoint, annotation)

        // Then
        assert(result == "result")
        verify { joinPoint.proceed() }
    }

    @Test
    fun `aroundMethod should rethrow exception from join point`() {
        // Given
        val annotation = mockk<WatchFeatureFlag>()
        every { annotation.flagKey } returns "test-flag"
        every { featureFlagClient.isActive("test-flag", any()) } returns true
        every { joinPoint.proceed() } throws RuntimeException("Test exception")

        // When / Then
        try {
            aspect.aroundMethod(joinPoint, annotation)
            assert(false) { "Should have thrown exception" }
        } catch (e: RuntimeException) {
            assert(e.message == "Test exception")
        }
    }
}
