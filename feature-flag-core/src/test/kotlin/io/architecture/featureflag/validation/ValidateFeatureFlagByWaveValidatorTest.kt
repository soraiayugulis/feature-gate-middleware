package io.architecture.featureflag.validation

import io.architecture.featureflag.core.FeatureFlagClient
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import jakarta.validation.ConstraintValidatorContext
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ValidateFeatureFlagByWaveValidatorTest {

    private lateinit var featureFlagClient: FeatureFlagClient
    private lateinit var validator: ValidateFeatureFlagByWaveValidator
    private lateinit var context: ConstraintValidatorContext

    @BeforeEach
    fun setUp() {
        featureFlagClient = mockk()
        validator = ValidateFeatureFlagByWaveValidator(featureFlagClient)
        context = mockk(relaxed = true)

        val annotation = mockk<ValidateFeatureFlagByWave>()
        every { annotation.flagKey } returns "beta-feature"
        every { annotation.contextIdField } returns "userId"

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
    fun `isValid should return true when user is in active wave`() {
        // Given
        every { featureFlagClient.isActive("beta-feature", any()) } returns true

        // When
        val result = validator.isValid("access-key", context)

        // Then
        assert(result)
        verify { featureFlagClient.isActive("beta-feature", any()) }
    }

    @Test
    fun `isValid should return false when user is not in active wave`() {
        // Given
        every { featureFlagClient.isActive("beta-feature", any()) } returns false

        // When
        val result = validator.isValid("access-key", context)

        // Then
        assert(!result)
        verify { featureFlagClient.isActive("beta-feature", any()) }
    }
}
