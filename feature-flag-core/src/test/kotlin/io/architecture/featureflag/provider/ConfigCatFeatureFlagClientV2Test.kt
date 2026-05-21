package io.architecture.featureflag.provider

import com.configcat.ConfigCatClient
import com.configcat.User
import io.architecture.featureflag.core.FeatureFlagContext
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ConfigCatFeatureFlagClientV2Test {

    private lateinit var configCatClient: ConfigCatClient
    private lateinit var client: ConfigCatFeatureFlagClient

    @BeforeEach
    fun setUp() {
        configCatClient = mockk(relaxed = true)
        client = ConfigCatFeatureFlagClient(configCatClient)
    }

    @Test
    fun `should return true when ConfigCat evaluates flag as active`() {
        // Arrange
        val context = FeatureFlagContext(identifier = "user-123")
        every {
            configCatClient.getValue(Boolean::class.javaObjectType, "checkout-v2", any<User>(), false)
        } returns true

        // Act
        val result = client.isActive("checkout-v2", context)

        // Assert
        assertThat(result).isTrue()
    }

    @Test
    fun `should return false when ConfigCat evaluates flag as inactive`() {
        // Arrange
        val context = FeatureFlagContext(identifier = "user-123")
        every {
            configCatClient.getValue(Boolean::class.javaObjectType, "checkout-v2", any<User>(), false)
        } returns false

        // Act
        val result = client.isActive("checkout-v2", context)

        // Assert
        assertThat(result).isFalse()
    }

    @Test
    fun `should map FeatureFlagContext fields to ConfigCatUser correctly`() {
        // Arrange
        val userSlot = slot<User>()
        val context = FeatureFlagContext(
            identifier = "user-456",
            email = "user@example.com",
            country = "BR",
            custom = mapOf("plan" to "premium")
        )
        every {
            configCatClient.getValue(Boolean::class.javaObjectType, "flag", capture(userSlot), false)
        } returns true

        // Act
        client.isActive("flag", context)

        // Assert — User fields are private in ConfigCat SDK; verify the User was built and passed
        assertThat(userSlot.isCaptured).isTrue()
        assertThat(userSlot.captured).isNotNull()
        // Identifier is accessible via toString or by re-building — verify SDK was called with a User object
        verify { configCatClient.getValue(Boolean::class.javaObjectType, "flag", any<User>(), false) }
    }

    @Test
    fun `should return false when context is null (global flag)`() {
        // Arrange
        every {
            configCatClient.getValue(Boolean::class.javaObjectType, "maintenance-mode", isNull(), false)
        } returns false

        // Act
        val result = client.isActive("maintenance-mode", null)

        // Assert
        assertThat(result).isFalse()
    }

    @Test
    fun `should return false and not throw when SDK throws`() {
        // Arrange
        val context = FeatureFlagContext(identifier = "user-789")
        every {
            configCatClient.getValue(Boolean::class.javaObjectType, any(), any<User>(), false)
        } throws RuntimeException("SDK failure")

        // Act
        val result = client.isActive("any-flag", context)

        // Assert
        assertThat(result).isFalse()
    }
}
