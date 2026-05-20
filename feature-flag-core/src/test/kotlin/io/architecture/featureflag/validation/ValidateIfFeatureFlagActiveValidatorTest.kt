package io.architecture.featureflag.validation

import io.architecture.featureflag.core.FeatureFlagClient
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import jakarta.validation.ConstraintValidatorContext
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ValidateIfFeatureFlagActiveValidatorTest {

    private lateinit var featureFlagClient: FeatureFlagClient
    private lateinit var validator: ValidateIfFeatureFlagActiveValidator
    private lateinit var context: ConstraintValidatorContext

    @BeforeEach
    fun setUp() {
        featureFlagClient = mockk()
        validator = ValidateIfFeatureFlagActiveValidator(featureFlagClient)
        context = mockk(relaxed = true)

        val annotation = mockk<ValidateIfFeatureFlagActive>()
        every { annotation.flagKey } returns "test-flag"
        every { annotation.contextIdField } returns "email"

        validator.initialize(annotation)
    }

    @Test
    fun `isValid should return true when value is null`() {
        // Given: null value

        // When
        val result = validator.isValid(null, context)

        // Then
        assert(result)
    }

    @Test
    fun `isValid should return true when feature flag is active`() {
        // Given
        every { featureFlagClient.isActive("test-flag", any()) } returns true

        // When
        val result = validator.isValid("some-value", context)

        // Then
        assert(result)
        verify { featureFlagClient.isActive("test-flag", any()) }
    }

    @Test
    fun `isValid should return false when feature flag is inactive`() {
        // Given
        every { featureFlagClient.isActive("test-flag", any()) } returns false

        // When
        val result = validator.isValid("some-value", context)

        // Then
        assert(!result)
        verify { featureFlagClient.isActive("test-flag", any()) }
    }
}
