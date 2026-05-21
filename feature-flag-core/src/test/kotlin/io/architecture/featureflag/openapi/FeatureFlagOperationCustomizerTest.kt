package io.architecture.featureflag.openapi

import io.architecture.featureflag.aop.HiFeatureFlag
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.springdoc.core.customizers.OperationCustomizer
import org.springframework.web.method.HandlerMethod
import io.swagger.v3.oas.models.Operation

class FeatureFlagOperationCustomizerTest {

    class TestController {
        @HiFeatureFlag(flagKeys = ["checkout-v2"], description = "Checkout v2 feature")
        fun annotatedMethod() {}

        @HiFeatureFlag(flagKeys = ["checkout-v2", "pix-routing"], description = "Payment features")
        fun multiFlagMethod() {}

        @HiFeatureFlag(flagKeys = ["checkout-v2"], description = "Checkout v2")
        fun descriptionMethod() {}

        fun nonAnnotatedMethod() {}
    }

    @Test
    fun `should add feature-flag tag when method has @HiFeatureFlag`() {
        // Given
        val customizer = FeatureFlagOperationCustomizer()
        val operation = Operation()
        val handlerMethod = mockk<HandlerMethod>()
        val method = TestController::class.java.getMethod("annotatedMethod")
        
        every { handlerMethod.method } returns method

        // When
        val result = customizer.customize(operation, handlerMethod)

        // Then
        assert(result.tags.contains("feature-flag"))
    }

    @Test
    fun `should inject x-feature-flags extension with one entry per flagKey`() {
        // Given
        val customizer = FeatureFlagOperationCustomizer()
        val operation = Operation()
        val handlerMethod = mockk<HandlerMethod>()
        val method = TestController::class.java.getMethod("multiFlagMethod")
        
        every { handlerMethod.method } returns method

        // When
        val result = customizer.customize(operation, handlerMethod)

        // Then
        val extensions = result.extensions
        assert(extensions.containsKey("x-feature-flags"))
        val flags = extensions["x-feature-flags"] as List<Map<String, String>>
        assert(flags.size == 2)
        assert(flags[0]["key"] == "checkout-v2")
        assert(flags[1]["key"] == "pix-routing")
    }

    @Test
    fun `should append description suffix without overwriting existing description`() {
        // Given
        val customizer = FeatureFlagOperationCustomizer()
        val operation = Operation().description("Existing description")
        val handlerMethod = mockk<HandlerMethod>()
        val method = TestController::class.java.getMethod("descriptionMethod")
        
        every { handlerMethod.method } returns method

        // When
        val result = customizer.customize(operation, handlerMethod)

        // Then
        assert(result.description!!.contains("Existing description"))
        assert(result.description!!.contains("⚑ This endpoint's behavior may vary based on active feature flags."))
    }

    @Test
    fun `should return operation unmodified when @HiFeatureFlag is absent`() {
        // Given
        val customizer = FeatureFlagOperationCustomizer()
        val operation = Operation()
        val handlerMethod = mockk<HandlerMethod>()
        val method = TestController::class.java.getMethod("nonAnnotatedMethod")
        
        every { handlerMethod.method } returns method

        // When
        val result = customizer.customize(operation, handlerMethod)

        // Then
        assert(result === operation) // Same object reference
        assert(result.tags?.contains("feature-flag") != true)
    }

    @Test
    fun `should handle null existing description gracefully`() {
        // Given
        val customizer = FeatureFlagOperationCustomizer()
        val operation = Operation().description(null)
        val handlerMethod = mockk<HandlerMethod>()
        val method = TestController::class.java.getMethod("descriptionMethod")
        
        every { handlerMethod.method } returns method

        // When
        val result = customizer.customize(operation, handlerMethod)

        // Then
        assert(result.description!!.contains("⚑ This endpoint's behavior may vary based on active feature flags."))
    }
}
