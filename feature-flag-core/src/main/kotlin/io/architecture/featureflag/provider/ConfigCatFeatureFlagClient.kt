package io.architecture.featureflag.provider

import com.configcat.ConfigCatClient
import com.configcat.User
import io.architecture.featureflag.core.FeatureFlagClient
import io.architecture.featureflag.core.FeatureFlagContext
import org.slf4j.LoggerFactory

/**
 * ConfigCat implementation of the FeatureFlagClient interface.
 * Acts as an adapter between the abstraction and the ConfigCat SDK.
 *
 * Evaluation is always local (in-memory cache via autoPoll).
 * On any SDK exception: logs WARN and returns false (fail-safe to disable branch).
 */
class ConfigCatFeatureFlagClient(
    private val configCatClient: ConfigCatClient
) : FeatureFlagClient {

    companion object {
        private val logger = LoggerFactory.getLogger(ConfigCatFeatureFlagClient::class.java)
    }

    override fun isActive(flagKey: String, context: FeatureFlagContext?): Boolean {
        return try {
            val user = context?.let(::mapToConfigCatUser)
            configCatClient.getValue(Boolean::class.javaObjectType, flagKey, user, false)
        } catch (e: Exception) {
            logger.warn("[FeatureGate] SDK evaluation failed for flag='$flagKey', defaulting to false", e)
            false
        }
    }

    private fun mapToConfigCatUser(context: FeatureFlagContext): User {
        val custom = context.custom.mapValues { it.value.toString() }
        return User.newBuilder()
            .email(context.email)
            .country(context.country)
            .custom(custom)
            .build(context.identifier)
    }
}
