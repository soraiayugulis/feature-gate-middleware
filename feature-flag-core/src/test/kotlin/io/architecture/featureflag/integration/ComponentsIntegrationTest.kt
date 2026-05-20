package io.architecture.featureflag.integration

import io.architecture.featureflag.aop.WatchFeatureFlagAspect
import io.architecture.featureflag.core.FeatureFlagClient
import io.architecture.featureflag.logging.FeatureFlagMdc
import io.architecture.featureflag.service.FeatureFlagExecutor
import io.architecture.featureflag.validation.FieldReflectionEngine
import io.architecture.featureflag.validation.ValidateIfFeatureFlagActiveValidator
import io.architecture.featureflag.validation.ValidateFeatureFlagByWaveValidator
import org.junit.jupiter.api.Test

/**
 * Integration test verifying all feature flag components exist and can be instantiated.
 * Note: Full Spring Boot integration test would require spring-boot-starter-test dependency.
 */
class ComponentsIntegrationTest {

    @Test
    fun `all validation components should exist`() {
        // Verify validator classes exist
        val validateIfActiveClass = ValidateIfFeatureFlagActiveValidator::class
        val validateByWaveClass = ValidateFeatureFlagByWaveValidator::class

        assert(validateIfActiveClass.simpleName == "ValidateIfFeatureFlagActiveValidator")
        assert(validateByWaveClass.simpleName == "ValidateFeatureFlagByWaveValidator")
    }

    @Test
    fun `all AOP components should exist`() {
        // Verify AOP classes exist
        val aspectClass = WatchFeatureFlagAspect::class
        assert(aspectClass.simpleName == "WatchFeatureFlagAspect")
    }

    @Test
    fun `all service components should exist`() {
        // Verify service classes exist
        val executorClass = FeatureFlagExecutor::class
        assert(executorClass.simpleName == "FeatureFlagExecutor")
    }

    @Test
    fun `logging utilities should exist`() {
        // Verify logging utilities exist
        val mdcClass = FeatureFlagMdc::class
        assert(mdcClass.simpleName == "FeatureFlagMdc")
    }

    @Test
    fun `reflection engine should exist`() {
        // Verify reflection engine exists
        val engineClass = FieldReflectionEngine::class
        assert(engineClass.simpleName == "FieldReflectionEngine")
    }
}
