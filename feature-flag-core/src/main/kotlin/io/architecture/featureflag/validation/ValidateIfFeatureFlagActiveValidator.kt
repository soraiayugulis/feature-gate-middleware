package io.architecture.featureflag.validation

import io.architecture.featureflag.core.FeatureFlagClient
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext

/**
 * Validator for the @ValidateIfFeatureFlagActive constraint.
 * Evaluates if the annotated field should be validated based on feature flag state.
 */
class ValidateIfFeatureFlagActiveValidator(
    private val featureFlagClient: FeatureFlagClient
) : ConstraintValidator<ValidateIfFeatureFlagActive, Any> {

    override fun initialize(constraintAnnotation: ValidateIfFeatureFlagActive) {
        // Initialization if needed
    }

    override fun isValid(value: Any?, context: ConstraintValidatorContext): Boolean {
        // Field-level validation: if value is null, it's considered valid
        // (use @NotNull for mandatory validation)
        if (value == null) {
            return true
        }

        // TODO: Implement reflection-based extraction of contextIdField from parent DTO
        // TODO: Call featureFlagClient.isActive() to check flag status
        // TODO: If flag is active, field is valid; if inactive, field should be null

        // Placeholder implementation - always valid for now
        return true
    }
}
