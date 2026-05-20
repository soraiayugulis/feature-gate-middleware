package io.architecture.featureflag.validation

import jakarta.validation.Constraint
import jakarta.validation.Payload
import kotlin.reflect.KClass

/**
 * Validates that a field passes through a canary/percentage rollout wave.
 * The validation is determined by whether the user's context ID falls within
 * the active percentage of the feature flag rollout.
 *
 * Example usage:
 * ```kotlin
 * data class BetaAccessDto(
 *     val userId: String,
 *     @field:ValidateFeatureFlagByWave(
 *         flagKey = "acesso-funcionalidade-beta",
 *         contextIdField = "userId"
 *     )
 *     val betaFeatureAccessKey: String?
 * )
 * ```
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = arrayOf(ValidateFeatureFlagByWaveValidator::class))
annotation class ValidateFeatureFlagByWave(
    /**
     * The feature flag key for the canary/percentage rollout.
     */
    val flagKey: String,

    /**
     * The name of the field containing the user identifier for wave evaluation.
     */
    val contextIdField: String,

    /**
     * The validation error message template.
     */
    val message: String = "Field is not valid for this user's rollout wave",

    /**
     * The validation groups to which this constraint belongs.
     */
    val groups: Array<KClass<*>> = [],

    /**
     * The payload associated with this constraint.
     */
    val payload: Array<KClass<out Payload>> = []
)
