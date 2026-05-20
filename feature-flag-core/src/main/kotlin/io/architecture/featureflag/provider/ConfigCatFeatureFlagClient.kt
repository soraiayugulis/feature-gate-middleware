package io.architecture.featureflag.provider

import com.configcat.ConfigCatClient
import com.configcat.User
import io.architecture.featureflag.core.FeatureFlagClient

/**
 * ConfigCat implementation of the FeatureFlagClient interface.
 * Acts as an adapter between the abstraction and the ConfigCat SDK.
 */
class ConfigCatFeatureFlagClient(
    private val configCatClient: ConfigCatClient
) : FeatureFlagClient {

    override fun isActive(flagKey: String, userId: String, attributes: Map<String, Any>): Boolean {
        val user = createUser(userId, attributes)
        return configCatClient.getValue(Boolean::class.javaObjectType, flagKey, user, false)
    }

    private fun createUser(userId: String, attributes: Map<String, Any>): User {
        val customAttributes = attributes.mapValues { it.value.toString() }
        return User.newBuilder()
            .custom(customAttributes)
            .build(userId)
    }
}
