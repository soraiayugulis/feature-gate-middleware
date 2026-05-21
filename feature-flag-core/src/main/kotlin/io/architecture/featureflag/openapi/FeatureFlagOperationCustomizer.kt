package io.architecture.featureflag.openapi

import io.architecture.featureflag.aop.HiFeatureFlag
import org.springdoc.core.customizers.OperationCustomizer
import org.springframework.web.method.HandlerMethod
import io.swagger.v3.oas.models.Operation

/**
 * OperationCustomizer that injects feature flag metadata into Swagger/OpenAPI documentation.
 *
 * For methods annotated with @HiFeatureFlag:
 * - Adds "feature-flag" tag to the operation
 * - Adds "x-feature-flags" extension with flag keys and descriptions
 * - Appends a description suffix indicating the endpoint behavior may vary based on feature flags
 */
class FeatureFlagOperationCustomizer : OperationCustomizer {

    override fun customize(operation: Operation, handlerMethod: HandlerMethod): Operation {
        val hiAnnotation = handlerMethod.method.getAnnotation(HiFeatureFlag::class.java) ?: return operation

        // Add feature-flag tag
        operation.addTagsItem("feature-flag")

        // Build x-feature-flags extension
        val flags = hiAnnotation.flagKeys.map { flagKey ->
            mapOf(
                "key" to flagKey,
                "description" to hiAnnotation.description
            )
        }
        operation.addExtension("x-feature-flags", flags)

        // Append description suffix (null-safe)
        val suffix = " ⚑ This endpoint's behavior may vary based on active feature flags."
        val currentDescription = operation.description
        operation.description = if (currentDescription.isNullOrBlank()) {
            suffix.trim()
        } else {
            "$currentDescription$suffix"
        }

        return operation
    }
}
