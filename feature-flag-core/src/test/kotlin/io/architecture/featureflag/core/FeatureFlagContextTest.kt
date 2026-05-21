package io.architecture.featureflag.core

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class FeatureFlagContextTest {

    @Test
    fun `should create context with identifier only`() {
        // Arrange / Act
        val context = FeatureFlagContext(identifier = "user-123")

        // Assert
        assertThat(context.identifier).isEqualTo("user-123")
        assertThat(context.email).isNull()
        assertThat(context.country).isNull()
        assertThat(context.custom).isEmpty()
    }

    @Test
    fun `should create context with all fields`() {
        // Arrange / Act
        val context = FeatureFlagContext(
            identifier = "user-456",
            email = "user@example.com",
            country = "BR",
            custom = mapOf("plan" to "premium", "role" to "admin")
        )

        // Assert
        assertThat(context.identifier).isEqualTo("user-456")
        assertThat(context.email).isEqualTo("user@example.com")
        assertThat(context.country).isEqualTo("BR")
        assertThat(context.custom).containsEntry("plan", "premium")
        assertThat(context.custom).containsEntry("role", "admin")
    }

    @Test
    fun `should support equality via data class`() {
        // Arrange
        val a = FeatureFlagContext(identifier = "user-1", email = "a@b.com")
        val b = FeatureFlagContext(identifier = "user-1", email = "a@b.com")

        // Act / Assert
        assertThat(a).isEqualTo(b)
        assertThat(a.hashCode()).isEqualTo(b.hashCode())
    }

    @Test
    fun `should support copy via data class`() {
        // Arrange
        val original = FeatureFlagContext(identifier = "user-1", country = "US")

        // Act
        val copy = original.copy(country = "BR")

        // Assert
        assertThat(copy.identifier).isEqualTo("user-1")
        assertThat(copy.country).isEqualTo("BR")
        assertThat(original.country).isEqualTo("US")
    }

    @Test
    fun `custom defaults to empty map when not provided`() {
        // Arrange / Act
        val context = FeatureFlagContext(identifier = "user-1")

        // Assert
        assertThat(context.custom).isNotNull
        assertThat(context.custom).isEmpty()
    }
}
