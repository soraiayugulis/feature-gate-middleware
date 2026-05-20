package io.architecture.featureflag.validation

import io.architecture.featureflag.core.FeatureFlagClient
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import org.slf4j.LoggerFactory
import org.slf4j.MDC

/**
 * Validator for the @ValidateFeatureFlagByWave constraint.
 * Evaluates if the user is part of the active canary/percentage rollout wave.
 */
class ValidateFeatureFlagByWaveValidator(
    private val featureFlagClient: FeatureFlagClient
) : ConstraintValidator<ValidateFeatureFlagByWave, Any> {

    companion object {
        private val logger = LoggerFactory.getLogger(ValidateFeatureFlagByWaveValidator::class.java)
    }

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

        // Set MDC context for structured logging
        MDC.put("ff_key", flagKey)
        MDC.put("ff_mechanism", "ValidateFeatureFlagByWave")

        return try {
            // TODO: Access root bean to extract contextIdField value for wave evaluation
            val userId = "anonymous"
            MDC.put("ff_context_id", userId)

            // The ConfigCat SDK handles percentage rollout automatically based on userId
            val isInActiveWave = featureFlagClient.isActive(flagKey, userId)
            MDC.put("ff_result", isInActiveWave.toString())

            logger.info("Feature Flag evaluated for canary/wave validation")

            // If user is in active wave, field is valid
            isInActiveWave
        } finally {
            // Clean MDC to prevent thread contamination
            MDC.remove("ff_key")
            MDC.remove("ff_context_id")
            MDC.remove("ff_result")
            MDC.remove("ff_mechanism")
        }
    }
}
