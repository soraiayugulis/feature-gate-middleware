package io.architecture.featureflag.config

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Configuration properties for the feature-gate-middleware library.
 *
 * Bind via:
 * ```yaml
 * feature-gate:
 *   watch:
 *     enabled: true
 *   hi:
 *     enabled: true
 *     log-on-startup: true
 *   configcat:
 *     sdk-key: "YOUR_SDK_KEY"
 *     poll-interval-seconds: 60
 * ```
 */
@ConfigurationProperties(prefix = "feature-gate")
data class FeatureGateProperties(
    val watch: WatchProperties = WatchProperties(),
    val hi: HiProperties = HiProperties(),
    val configcat: ConfigCatProperties = ConfigCatProperties()
) {
    data class WatchProperties(
        val enabled: Boolean = false
    )

    data class HiProperties(
        val enabled: Boolean = false,
        val logOnStartup: Boolean = false
    )

    data class ConfigCatProperties(
        val sdkKey: String = "",
        val pollIntervalSeconds: Int = 60
    )
}
