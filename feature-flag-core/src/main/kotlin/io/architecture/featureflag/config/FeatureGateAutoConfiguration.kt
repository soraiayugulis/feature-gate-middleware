package io.architecture.featureflag.config

import io.architecture.featureflag.aop.HiFeatureFlagStartupListener
import io.architecture.featureflag.aop.WatchFeatureFlagAspect
import io.architecture.featureflag.core.FeatureFlagClient
import io.architecture.featureflag.provider.ConfigCatFeatureFlagClient
import io.architecture.featureflag.service.FeatureFlagExecutor
import com.configcat.ConfigCatClient
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean

/**
 * Auto-configuration for the feature-gate-middleware library.
 *
 * Registers beans conditionally based on configuration and classpath:
 * - ConfigCatFeatureFlagClient: when no custom FeatureFlagClient bean is present (TODO: pending SDK API investigation)
 * - FeatureFlagExecutor: always registered
 * - WatchFeatureFlagAspect: when watch.enabled=true and Micrometer is on classpath
 * - HiFeatureFlagStartupListener: when hi.enabled=true
 * - FeatureGateStartupValidator: always registered
 * - FeatureGateProperties: always registered
 *
 * Note: Consumers must provide a FeatureFlagClient bean (ConfigCat or custom) for now.
 * ConfigCat auto-registration pending SDK API investigation.
 */
@AutoConfiguration
@EnableConfigurationProperties(FeatureGateProperties::class)
class FeatureGateAutoConfiguration {

    // TODO: ConfigCatClient SDK API investigation needed
    // @Bean
    // @ConditionalOnMissingBean(FeatureFlagClient::class)
    // fun configCatFeatureFlagClient(properties: FeatureGateProperties): FeatureFlagClient { ... }

    @Bean
    fun featureFlagExecutor(featureFlagClient: FeatureFlagClient): FeatureFlagExecutor {
        return FeatureFlagExecutor(featureFlagClient)
    }

    @Bean
    @ConditionalOnProperty(prefix = "feature-gate.watch", name = ["enabled"], havingValue = "true", matchIfMissing = false)
    @ConditionalOnClass(MeterRegistry::class)
    fun watchFeatureFlagAspect(
        featureFlagClient: FeatureFlagClient,
        meterRegistry: MeterRegistry
    ): WatchFeatureFlagAspect {
        return WatchFeatureFlagAspect(featureFlagClient, meterRegistry)
    }

    @Bean
    @ConditionalOnProperty(prefix = "feature-gate.hi", name = ["enabled"], havingValue = "true", matchIfMissing = false)
    fun hiFeatureFlagStartupListener(
        properties: FeatureGateProperties,
        applicationContext: org.springframework.context.ApplicationContext
    ): HiFeatureFlagStartupListener {
        return HiFeatureFlagStartupListener(properties, applicationContext)
    }

    @Bean
    fun featureGateStartupValidator(
        properties: FeatureGateProperties,
        applicationContext: org.springframework.context.ApplicationContext
    ): FeatureGateStartupValidator {
        return FeatureGateStartupValidator(properties, applicationContext)
    }
}
