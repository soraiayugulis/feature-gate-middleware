package io.architecture.featureflag.config

import io.architecture.featureflag.aop.HiFeatureFlag
import io.architecture.featureflag.aop.WatchFeatureFlag
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.slf4j.Logger
import org.springframework.context.ApplicationContext

class FeatureGateStartupValidatorTest {

    private fun buildValidator(
        sdkKey: String = "valid-sdk-key",
        beans: Map<String, Any> = emptyMap(),
        logger: Logger? = null
    ): FeatureGateStartupValidator {
        val props = FeatureGateProperties(
            configcat = FeatureGateProperties.ConfigCatProperties(sdkKey = sdkKey)
        )
        val ctx = mockk<ApplicationContext>(relaxed = true)
        every { ctx.getBeanDefinitionNames() } returns beans.keys.toTypedArray()
        beans.forEach { (name, bean) ->
            every { ctx.getBean(name) } returns bean
        }
        return if (logger != null) {
            FeatureGateStartupValidator(props, ctx, logger)
        } else {
            FeatureGateStartupValidator(props, ctx)
        }
    }

    @Test
    fun `should throw IllegalStateException when sdk-key is blank`() {
        // Arrange
        val validator = buildValidator(sdkKey = "")

        // Act / Assert
        assertThatThrownBy { validator.validate() }
            .isInstanceOf(IllegalStateException::class.java)
            .hasMessageContaining("sdk-key")
    }

    @Test
    fun `should throw IllegalStateException when sdk-key is placeholder`() {
        // Arrange
        val validator = buildValidator(sdkKey = "YOUR_SDK_KEY")

        // Act / Assert
        assertThatThrownBy { validator.validate() }
            .isInstanceOf(IllegalStateException::class.java)
            .hasMessageContaining("sdk-key")
    }

    @Test
    fun `should throw IllegalStateException when WatchFeatureFlag is placed on a suspend fun`() {
        // Arrange — suspend fun compiles to a method with Continuation parameter
        class SuspendSubject {
            @WatchFeatureFlag(flagKey = "my-flag")
            suspend fun suspendMethod() {}
        }
        val validator = buildValidator(beans = mapOf("subject" to SuspendSubject()))

        // Act / Assert
        assertThatThrownBy { validator.validate() }
            .isInstanceOf(IllegalStateException::class.java)
            .hasMessageContaining("suspend")
    }

    @Test
    fun `should emit WARN when WatchFeatureFlag flagKey not in HiFeatureFlag flagKeys on same method`() {
        // Arrange
        class MismatchSubject {
            @HiFeatureFlag(flagKeys = ["other-flag"], description = "desc")
            @WatchFeatureFlag(flagKey = "checkout-v2")
            fun endpoint() {}
        }
        val warnLogger = mockk<Logger>(relaxed = true)
        val validator = buildValidator(
            beans = mapOf("subject" to MismatchSubject()),
            logger = warnLogger
        )

        // Act
        validator.validate()

        // Assert
        verify { warnLogger.warn(any<String>()) }
    }

    @Test
    fun `should pass validation when WatchFeatureFlag flagKey is contained in HiFeatureFlag flagKeys`() {
        // Arrange
        class ValidSubject {
            @HiFeatureFlag(flagKeys = ["checkout-v2", "pix-routing"], description = "desc")
            @WatchFeatureFlag(flagKey = "checkout-v2")
            fun endpoint() {}
        }
        val validator = buildValidator(beans = mapOf("subject" to ValidSubject()))

        // Act / Assert — should not throw
        validator.validate()
    }

    @Test
    fun `should emit WARN when HiFeatureFlag flagKeys contains blank entry`() {
        // Arrange
        class BlankKeySubject {
            @HiFeatureFlag(flagKeys = ["", "valid-flag"], description = "desc")
            fun endpoint() {}
        }
        val warnLogger = mockk<Logger>(relaxed = true)
        val validator = buildValidator(
            beans = mapOf("subject" to BlankKeySubject()),
            logger = warnLogger
        )

        // Act
        validator.validate()

        // Assert
        verify { warnLogger.warn(any<String>()) }
    }
}
