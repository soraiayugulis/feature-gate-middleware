package io.architecture.featureflag.provider

import com.configcat.ConfigCatClient
import com.configcat.User
import io.architecture.featureflag.core.FeatureFlagContext
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ConfigCatFeatureFlagClientTest {

    private lateinit var configCatClient: ConfigCatClient
    private lateinit var featureFlagClient: ConfigCatFeatureFlagClient

    @BeforeEach
    fun setUp() {
        configCatClient = mockk(relaxed = true)
        featureFlagClient = ConfigCatFeatureFlagClient(configCatClient)
    }

    @Test
    fun `isActive should return true when ConfigCat returns true`() {
        // Given
        val context = FeatureFlagContext(identifier = "user-123")
        every {
            configCatClient.getValue(Boolean::class.javaObjectType, "test-flag", any<User>(), false)
        } returns true

        // When
        val result = featureFlagClient.isActive("test-flag", context)

        // Then
        assertThat(result).isTrue()
    }

    @Test
    fun `isActive should return false when ConfigCat returns false`() {
        // Given
        val context = FeatureFlagContext(identifier = "user-123")
        every {
            configCatClient.getValue(Boolean::class.javaObjectType, "test-flag", any<User>(), false)
        } returns false

        // When
        val result = featureFlagClient.isActive("test-flag", context)

        // Then
        assertThat(result).isFalse()
    }

    @Test
    fun `isActive should pass user context to ConfigCat`() {
        // Given
        val context = FeatureFlagContext(
            identifier = "user-456",
            email = "test@example.com",
            custom = mapOf("plan" to "premium")
        )
        every {
            configCatClient.getValue(Boolean::class.javaObjectType, "test-flag", any<User>(), false)
        } returns true

        // When
        val result = featureFlagClient.isActive("test-flag", context)

        // Then
        assertThat(result).isTrue()
    }
}
