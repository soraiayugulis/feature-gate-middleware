package io.architecture.featureflag.validation

import jakarta.validation.Constraint
import jakarta.validation.Payload
import kotlin.reflect.KClass

/**
 * Validates that a field is only valid (not null/empty) when the specified feature flag is active
 * for the user context derived from another field in the DTO.
 *
 * Example usage:
 * ```kotlin
 * data class UserRegistrationDto(
 *     val email: String,
 *     @field:ValidateIfFeatureFlagActive(
 *         flagKey = "exigir-documento-validado",
 *         contextIdField = "email"
 *     )
 *     val taxId: String?
 * )
 * ```
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = arrayOf(ValidateIfFeatureFlagActiveValidator::class))
annotation class ValidateIfFeatureFlagActive(
    /**
     * The feature flag key to evaluate.
     */
    val flagKey: String,

    /**
     * The name of the field in the same DTO that contains the user identifier
     * used for feature flag evaluation context.
     */
    val contextIdField: String,

    /**
     * The validation error message template.
     */
    val message: String = "Field is not valid when feature flag is inactive",

    /**
     * The validation groups to which this constraint belongs.
     */
    val groups: Array<KClass<*>> = [],

    /**
     * The payload associated with this constraint.
     */
    val payload: Array<KClass<out Payload>> = []
)
