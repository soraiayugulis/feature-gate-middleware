package io.architecture.featureflag.service

import io.architecture.featureflag.core.FeatureFlagClient
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class FeatureFlagExecutorTest {

    private lateinit var featureFlagClient: FeatureFlagClient
    private lateinit var executor: FeatureFlagExecutor

    @BeforeEach
    fun setUp() {
        featureFlagClient = mockk()
        executor = FeatureFlagExecutor(featureFlagClient)
    }

    @Test
    fun `execute should run onActive block when flag is active`() {
        // Given
        every { featureFlagClient.isActive("test-flag", "user-123") } returns true

        // When
        val result = executor.execute("test-flag", "user-123") {
            onActive { "active-result" }
            onDisable { "disable-result" }
        }

        // Then
        assert(result == "active-result")
        verify { featureFlagClient.isActive("test-flag", "user-123") }
    }

    @Test
    fun `execute should run onDisable block when flag is inactive`() {
        // Given
        every { featureFlagClient.isActive("test-flag", "user-123") } returns false

        // When
        val result = executor.execute("test-flag", "user-123") {
            onActive { "active-result" }
            onDisable { "disable-result" }
        }

        // Then
        assert(result == "disable-result")
        verify { featureFlagClient.isActive("test-flag", "user-123") }
    }

    @Test
    fun `execute should propagate exceptions from onActive block`() {
        // Given
        every { featureFlagClient.isActive("test-flag", "user-123") } returns true

        // When / Then
        assertThrows<RuntimeException> {
            executor.execute("test-flag", "user-123") {
                onActive { throw RuntimeException("Active error") }
                onDisable { "disable-result" }
            }
        }
    }

    @Test
    fun `execute should propagate exceptions from onDisable block`() {
        // Given
        every { featureFlagClient.isActive("test-flag", "user-123") } returns false

        // When / Then
        assertThrows<RuntimeException> {
            executor.execute("test-flag", "user-123") {
                onActive { "active-result" }
                onDisable { throw RuntimeException("Disable error") }
            }
        }
    }

    @Test
    fun `execute should return null when onActive is not defined and flag is active`() {
        // Given
        every { featureFlagClient.isActive("test-flag", "user-123") } returns true

        // When
        val result = executor.execute<String?>("test-flag", "user-123") {
            onDisable { "disable-result" }
        }

        // Then
        assert(result == null)
    }

    @Test
    fun `execute should return null when onDisable is not defined and flag is inactive`() {
        // Given
        every { featureFlagClient.isActive("test-flag", "user-123") } returns false

        // When
        val result = executor.execute<String?>("test-flag", "user-123") {
            onActive { "active-result" }
        }

        // Then
        assert(result == null)
    }

    @Test
    fun `execute should allow only onActive handler`() {
        // Given
        every { featureFlagClient.isActive("test-flag", "user-123") } returns true

        // When
        val result = executor.execute<String?>("test-flag", "user-123") {
            onActive { "active-result" }
        }

        // Then
        assert(result == "active-result")
    }

    @Test
    fun `execute should allow only onDisable handler`() {
        // Given
        every { featureFlagClient.isActive("test-flag", "user-123") } returns false

        // When
        val result = executor.execute<String?>("test-flag", "user-123") {
            onDisable { "disable-result" }
        }

        // Then
        assert(result == "disable-result")
    }

}
