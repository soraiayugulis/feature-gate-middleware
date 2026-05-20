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

    private lateinit var flagKey: String
    private lateinit var contextIdField: String

    override fun initialize(constraintAnnotation: ValidateIfFeatureFlagActive) {
        this.flagKey = constraintAnnotation.flagKey
        this.contextIdField = constraintAnnotation.contextIdField
    }

    override fun isValid(value: Any?, context: ConstraintValidatorContext): Boolean {
        // Field-level validation: if value is null, it's considered valid
        // (use @NotNull for mandatory validation)
        if (value == null) {
            return true
        }

        // TODO: Access root bean to extract contextIdField value
        // This requires Hibernate Validator specific API or cross-field validation approach
        // For now, placeholder implementation
        
        // Check if feature flag is active (using empty attributes for now)
        val isFlagActive = featureFlagClient.isActive(flagKey, "anonymous")

        // If flag is active, the field value is valid
        // If flag is inactive and field has value, it's invalid
        return isFlagActive
    }
}
