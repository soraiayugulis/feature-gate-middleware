package io.architecture.featureflag.annotation

import io.architecture.featureflag.aop.WatchFeatureFlag
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import kotlin.reflect.KClass

class WatchFeatureFlagAnnotationTest {

    @Test
    fun `should have RUNTIME retention`() {
        // Arrange
        val retention = WatchFeatureFlag::class.annotations
            .filterIsInstance<Retention>()
            .firstOrNull()

        // Act / Assert
        assertThat(retention).isNotNull
        assertThat(retention!!.value).isEqualTo(AnnotationRetention.RUNTIME)
    }

    @Test
    fun `should target FUNCTION only`() {
        // Arrange
        val target = WatchFeatureFlag::class.annotations
            .filterIsInstance<Target>()
            .firstOrNull()

        // Act / Assert
        assertThat(target).isNotNull
        assertThat(target!!.allowedTargets.toList()).containsExactly(AnnotationTarget.FUNCTION)
        assertThat(target.allowedTargets.toList()).doesNotContain(AnnotationTarget.CLASS)
    }

    @Test
    fun `should be readable via reflection on annotated method`() {
        // Arrange
        class Subject {
            @WatchFeatureFlag(flagKey = "checkout-v2")
            fun annotatedMethod() {}
        }

        // Act
        val method = Subject::class.java.getMethod("annotatedMethod")
        val annotation = method.getAnnotation(WatchFeatureFlag::class.java)

        // Assert
        assertThat(annotation).isNotNull
        assertThat(annotation.flagKey).isEqualTo("checkout-v2")
    }

    @Test
    fun `flagKey should be accessible from annotation instance`() {
        // Arrange
        class Subject {
            @WatchFeatureFlag(flagKey = "my-feature-flag")
            fun method() {}
        }

        // Act
        val annotation = Subject::class.java
            .getMethod("method")
            .getAnnotation(WatchFeatureFlag::class.java)

        // Assert
        assertThat(annotation.flagKey).isEqualTo("my-feature-flag")
    }
}
