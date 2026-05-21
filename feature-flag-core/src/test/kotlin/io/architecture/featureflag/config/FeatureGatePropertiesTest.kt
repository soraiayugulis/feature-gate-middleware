package io.architecture.featureflag.config

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class FeatureGatePropertiesTest {

    @Test
    fun `should have correct defaults`() {
        // Arrange / Act
        val props = FeatureGateProperties()

        // Assert
        assertThat(props.watch.enabled).isFalse()
        assertThat(props.hi.enabled).isFalse()
        assertThat(props.hi.logOnStartup).isFalse()
        assertThat(props.configcat.sdkKey).isEmpty()
        assertThat(props.configcat.pollIntervalSeconds).isEqualTo(60)
    }

    @Test
    fun `should allow overriding watch enabled`() {
        // Arrange / Act
        val props = FeatureGateProperties(
            watch = FeatureGateProperties.WatchProperties(enabled = true)
        )

        // Assert
        assertThat(props.watch.enabled).isTrue()
    }

    @Test
    fun `should allow overriding hi properties`() {
        // Arrange / Act
        val props = FeatureGateProperties(
            hi = FeatureGateProperties.HiProperties(enabled = true, logOnStartup = true)
        )

        // Assert
        assertThat(props.hi.enabled).isTrue()
        assertThat(props.hi.logOnStartup).isTrue()
    }

    @Test
    fun `should allow overriding configcat properties`() {
        // Arrange / Act
        val props = FeatureGateProperties(
            configcat = FeatureGateProperties.ConfigCatProperties(
                sdkKey = "my-sdk-key",
                pollIntervalSeconds = 30
            )
        )

        // Assert
        assertThat(props.configcat.sdkKey).isEqualTo("my-sdk-key")
        assertThat(props.configcat.pollIntervalSeconds).isEqualTo(30)
    }

    @Test
    fun `should support equality via data class`() {
        // Arrange
        val a = FeatureGateProperties()
        val b = FeatureGateProperties()

        // Assert
        assertThat(a).isEqualTo(b)
    }
}
