package io.architecture.featureflag.annotation

import io.architecture.featureflag.aop.HiFeatureFlag
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class HiFeatureFlagAnnotationTest {

    @Test
    fun `should have RUNTIME retention`() {
        // Arrange
        val retention = HiFeatureFlag::class.annotations
            .filterIsInstance<Retention>()
            .firstOrNull()

        // Act / Assert
        assertThat(retention).isNotNull
        assertThat(retention!!.value).isEqualTo(AnnotationRetention.RUNTIME)
    }

    @Test
    fun `should target FUNCTION only`() {
        // Arrange
        val target = HiFeatureFlag::class.annotations
            .filterIsInstance<Target>()
            .firstOrNull()

        // Act / Assert
        assertThat(target).isNotNull
        assertThat(target!!.allowedTargets).containsExactly(AnnotationTarget.FUNCTION)
        assertThat(target.allowedTargets).doesNotContain(AnnotationTarget.CLASS)
    }

    @Test
    fun `should be readable via reflection on annotated method`() {
        // Arrange
        class Subject {
            @HiFeatureFlag(flagKeys = ["checkout-v2", "pix-routing"], description = "New checkout flow")
            fun annotatedMethod() {}
        }

        // Act
        val method = Subject::class.java.getMethod("annotatedMethod")
        val annotation = method.getAnnotation(HiFeatureFlag::class.java)

        // Assert
        assertThat(annotation).isNotNull
        assertThat(annotation.flagKeys.toList()).containsExactly("checkout-v2", "pix-routing")
        assertThat(annotation.description).isEqualTo("New checkout flow")
    }

    @Test
    fun `flagKeys should be an array supporting multiple keys`() {
        // Arrange
        class Subject {
            @HiFeatureFlag(flagKeys = ["flag-a", "flag-b", "flag-c"], description = "Multi-flag endpoint")
            fun method() {}
        }

        // Act
        val annotation = Subject::class.java
            .getMethod("method")
            .getAnnotation(HiFeatureFlag::class.java)

        // Assert
        assertThat(annotation.flagKeys).hasSize(3)
        assertThat(annotation.flagKeys).containsExactly("flag-a", "flag-b", "flag-c")
    }

    @Test
    fun `should support single flagKey in array`() {
        // Arrange
        class Subject {
            @HiFeatureFlag(flagKeys = ["single-flag"], description = "Single flag endpoint")
            fun method() {}
        }

        // Act
        val annotation = Subject::class.java
            .getMethod("method")
            .getAnnotation(HiFeatureFlag::class.java)

        // Assert
        assertThat(annotation.flagKeys).hasSize(1)
        assertThat(annotation.flagKeys[0]).isEqualTo("single-flag")
    }

    @Test
    fun `description should be accessible from annotation instance`() {
        // Arrange
        class Subject {
            @HiFeatureFlag(flagKeys = ["feat-x"], description = "Feature X description")
            fun method() {}
        }

        // Act
        val annotation = Subject::class.java
            .getMethod("method")
            .getAnnotation(HiFeatureFlag::class.java)

        // Assert
        assertThat(annotation.description).isEqualTo("Feature X description")
    }
}
