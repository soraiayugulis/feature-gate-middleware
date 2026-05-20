package io.architecture.featureflag.provider

import com.configcat.ConfigCatClient
import com.configcat.User
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ConfigCatFeatureFlagClientTest {

    private lateinit var configCatClient: ConfigCatClient
    private lateinit var featureFlagClient: ConfigCatFeatureFlagClient

    @BeforeEach
    fun setUp() {
        configCatClient = mockk()
        featureFlagClient = ConfigCatFeatureFlagClient(configCatClient)
    }

    @Test
    fun `isActive should return true when ConfigCat returns true`() {
        // Given
        val flagKey = "test-flag"
        val userId = "user-123"
        val attributes = emptyMap<String, Any>()

        every { 
            configCatClient.getValue(Boolean::class.javaObjectType, flagKey, any<User>(), false) 
        } returns true

        // When
        val result = featureFlagClient.isActive(flagKey, userId, attributes)

        // Then
        assertThat(result).isTrue()
    }

    @Test
    fun `isActive should return false when ConfigCat returns false`() {
        // Given
        val flagKey = "test-flag"
        val userId = "user-123"
        val attributes = emptyMap<String, Any>()

        every { 
            configCatClient.getValue(Boolean::class.javaObjectType, flagKey, any<User>(), false) 
        } returns false

        // When
        val result = featureFlagClient.isActive(flagKey, userId, attributes)

        // Then
        assertThat(result).isFalse()
    }

    @Test
    fun `isActive should pass user context to ConfigCat`() {
        // Given
        val flagKey = "test-flag"
        val userId = "user-456"
        val attributes = mapOf("email" to "test@example.com", "plan" to "premium")

        every { 
            configCatClient.getValue(Boolean::class.javaObjectType, flagKey, any<User>(), false) 
        } returns true

        // When
        val result = featureFlagClient.isActive(flagKey, userId, attributes)

        // Then
        assertThat(result).isTrue()
    }
}
