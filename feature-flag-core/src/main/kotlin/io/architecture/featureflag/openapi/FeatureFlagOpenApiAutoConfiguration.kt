package io.architecture.featureflag.openapi

import org.springdoc.core.customizers.OperationCustomizer
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean

/**
 * Auto-configuration for OpenAPI/Swagger integration.
 *
 * Only registered when:
 * - SpringDoc (OperationCustomizer) is on the classpath
 * - feature-gate.hi.enabled=true
 *
 * Rationale: Separate class prevents NoClassDefFoundError when SpringDoc is absent.
 * The inner class is never loaded if the outer @ConditionalOnClass fails.
 */
@AutoConfiguration
@ConditionalOnClass(OperationCustomizer::class)
@ConditionalOnProperty(prefix = "feature-gate.hi", name = ["enabled"], havingValue = "true", matchIfMissing = false)
class FeatureFlagOpenApiAutoConfiguration {

    @Bean
    fun featureFlagOperationCustomizer(): FeatureFlagOperationCustomizer {
        return FeatureFlagOperationCustomizer()
    }

    // TODO: SwaggerUiConfigParameters customizer pending API investigation
    // Consumers can manually configure the JS plugin via swagger-ui config
}
