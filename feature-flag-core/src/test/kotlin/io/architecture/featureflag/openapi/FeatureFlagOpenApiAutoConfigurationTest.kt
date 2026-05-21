package io.architecture.featureflag.openapi

import io.architecture.featureflag.core.FeatureFlagClient
import io.micrometer.core.instrument.MeterRegistry
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.springdoc.core.customizers.OperationCustomizer
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

class FeatureFlagOpenApiAutoConfigurationTest {

    private val contextRunner = ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(FeatureFlagOpenApiAutoConfiguration::class.java))

    @Test
    fun `should register FeatureFlagOperationCustomizer when hi enabled true`() {
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
            .withPropertyValues("feature-gate.hi.enabled=true")
            .run { context ->
                assert(context.getBeansOfType(OperationCustomizer::class.java).isNotEmpty())
            }
    }

    @Test
    fun `should not register any OpenAPI bean when hi enabled false`() {
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
                assert(context.getBeansOfType(OperationCustomizer::class.java).isEmpty())
            }
    }
}
