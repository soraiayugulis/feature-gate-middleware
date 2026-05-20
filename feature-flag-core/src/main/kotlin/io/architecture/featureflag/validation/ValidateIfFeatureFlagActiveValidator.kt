package io.architecture.featureflag.validation

import io.architecture.featureflag.core.FeatureFlagClient
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import org.slf4j.LoggerFactory
import org.slf4j.MDC

/**
 * Validator for the @ValidateIfFeatureFlagActive constraint.
 * Evaluates if the annotated field should be validated based on feature flag state.
 */
class ValidateIfFeatureFlagActiveValidator(
    private val featureFlagClient: FeatureFlagClient
) : ConstraintValidator<ValidateIfFeatureFlagActive, Any> {

    companion object {
        private val logger = LoggerFactory.getLogger(ValidateIfFeatureFlagActiveValidator::class.java)
    }

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

        // Set MDC context for structured logging
        MDC.put("ff_key", flagKey)
        MDC.put("ff_mechanism", "ValidateIfFeatureFlagActive")

        return try {
            // Extract userId from root bean using contextIdField
            val rootBean = ValidationContextHolder.get()
            val userId = if (rootBean != null) {
                FieldReflectionEngine.extractFieldValueCached(rootBean, contextIdField) ?: "anonymous"
            } else {
                "anonymous"
            }
            MDC.put("ff_context_id", userId)

            // Check if feature flag is active
            val isFlagActive = featureFlagClient.isActive(flagKey, userId)
            MDC.put("ff_result", isFlagActive.toString())

            logger.info("Feature Flag evaluated for field validation")

            // If flag is active, the field value is valid
            isFlagActive
        } finally {
            // Clean MDC to prevent thread contamination
            MDC.remove("ff_key")
            MDC.remove("ff_context_id")
            MDC.remove("ff_result")
            MDC.remove("ff_mechanism")
        }
    }
}
