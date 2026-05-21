package io.architecture.featureflag.aop

import io.architecture.featureflag.config.FeatureGateProperties
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.slf4j.Logger
import org.springframework.context.ApplicationContext

class HiFeatureFlagStartupListenerTest {

    @Test
    fun `should complete without error when log-on-startup=true`() {
        // Given
        val props = FeatureGateProperties(
            hi = FeatureGateProperties.HiProperties(enabled = true, logOnStartup = true)
        )
        val logger = mockk<Logger>(relaxed = true)
        val ctx = mockk<ApplicationContext>(relaxed = true)
        every { ctx.getBeansOfType(Any::class.java) } returns emptyMap()

        val listener = HiFeatureFlagStartupListener(props, ctx, logger)

        // When / Then - should not throw
        listener.onApplicationEvent(mockk())
    }

    @Test
    fun `should not emit log when log-on-startup=false`() {
        // Given
        val props = FeatureGateProperties(
            hi = FeatureGateProperties.HiProperties(enabled = true, logOnStartup = false)
        )
        val logger = mockk<Logger>(relaxed = true)
        val ctx = mockk<ApplicationContext>(relaxed = true)

        class TestBean {
            @HiFeatureFlag(flagKeys = ["checkout-v2"], description = "Checkout v2 feature")
            fun endpoint() {}
        }

        every { ctx.getBeansOfType(Any::class.java) } returns mapOf("testBean" to TestBean())

        val listener = HiFeatureFlagStartupListener(props, ctx, logger)

        // When
        listener.onApplicationEvent(mockk())

        // Then
        verify(exactly = 0) { logger.info(any<String>(), any()) }
    }
}
