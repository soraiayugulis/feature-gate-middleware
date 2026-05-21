package io.architecture.featureflag.config

import io.architecture.featureflag.aop.WatchFeatureFlagAspect
import io.architecture.featureflag.aop.HiFeatureFlagStartupListener
import io.architecture.featureflag.core.FeatureFlagClient
import io.micrometer.core.instrument.MeterRegistry
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

class FeatureGateAutoConfigurationTest {

    private val contextRunner = ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(FeatureGateAutoConfiguration::class.java))

    @Test
    fun `should register WatchFeatureFlagAspect only when watch enabled true and Micrometer on classpath`() {
        // Given
        @Configuration
        class CustomConfig {
            @Bean
            fun customFeatureFlagClient(): FeatureFlagClient = mockk()

            @Bean
            fun meterRegistry(): MeterRegistry = mockk()
        }

        // When / Then
        contextRunner
            .withUserConfiguration(CustomConfig::class.java)
            .withPropertyValues("feature-gate.watch.enabled=true")
            .run { context ->
                assert(context.getBeansOfType(WatchFeatureFlagAspect::class.java).isNotEmpty())
            }
    }

    @Test
    fun `should not register WatchFeatureFlagAspect when watch enabled false`() {
        // Given
        @Configuration
        class CustomConfig {
            @Bean
            fun customFeatureFlagClient(): FeatureFlagClient = mockk()
        }

        // When / Then
        contextRunner
            .withUserConfiguration(CustomConfig::class.java)
            .withPropertyValues("feature-gate.watch.enabled=false")
            .run { context ->
                assert(context.getBeansOfType(WatchFeatureFlagAspect::class.java).isEmpty())
            }
    }

    @Test
    fun `should register HiFeatureFlagStartupListener only when hi enabled true`() {
        // Given
        @Configuration
        class CustomConfig {
            @Bean
            fun customFeatureFlagClient(): FeatureFlagClient = mockk()
        }

        // When / Then
        contextRunner
            .withUserConfiguration(CustomConfig::class.java)
            .withPropertyValues("feature-gate.hi.enabled=true")
            .run { context ->
                assert(context.getBeansOfType(HiFeatureFlagStartupListener::class.java).isNotEmpty())
            }
    }

    @Test
    fun `should not register HiFeatureFlagStartupListener when hi enabled false`() {
        // Given
        @Configuration
        class CustomConfig {
            @Bean
            fun customFeatureFlagClient(): FeatureFlagClient = mockk()
        }

        // When / Then
        contextRunner
            .withUserConfiguration(CustomConfig::class.java)
            .withPropertyValues("feature-gate.hi.enabled=false")
            .run { context ->
                assert(context.getBeansOfType(HiFeatureFlagStartupListener::class.java).isEmpty())
            }
    }
}
