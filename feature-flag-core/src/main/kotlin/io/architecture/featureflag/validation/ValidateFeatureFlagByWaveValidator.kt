package io.architecture.featureflag.validation

import io.architecture.featureflag.core.FeatureFlagClient
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext

/**
 * Validator for the @ValidateFeatureFlagByWave constraint.
 * Evaluates if the user is part of the active canary/percentage rollout wave.
 */
class ValidateFeatureFlagByWaveValidator(
    private val featureFlagClient: FeatureFlagClient
) : ConstraintValidator<ValidateFeatureFlagByWave, Any> {

    private lateinit var flagKey: String
    private lateinit var contextIdField: String

    override fun initialize(constraintAnnotation: ValidateFeatureFlagByWave) {
        this.flagKey = constraintAnnotation.flagKey
        this.contextIdField = constraintAnnotation.contextIdField
    }

    override fun isValid(value: Any?, context: ConstraintValidatorContext): Boolean {
        // Field-level validation: if value is null, it's considered valid
        if (value == null) {
            return true
        }

        // TODO: Access root bean to extract contextIdField value for wave evaluation
        // For now, placeholder implementation using anonymous user

        // The ConfigCat SDK handles percentage rollout automatically based on userId
        val isInActiveWave = featureFlagClient.isActive(flagKey, "anonymous")

        // If user is in active wave, field is valid
        // If user is not in active wave and field has value, it's invalid
        return isInActiveWave
    }
}
