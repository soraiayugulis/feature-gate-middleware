package io.architecture.featureflag.validation

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class FieldReflectionEngineTest {

    data class TestDto(
        val email: String,
        val name: String,
        val age: Int?
    )

    @BeforeEach
    fun setUp() {
        FieldReflectionEngine.clearCache()
    }

    @Test
    fun `extractFieldValue should return string value from field`() {
        // Given
        val dto = TestDto("test@example.com", "John", 25)

        // When
        val result = FieldReflectionEngine.extractFieldValue(dto, "email")

        // Then
        assert(result == "test@example.com")
    }

    @Test
    fun `extractFieldValue should return null for non-existent field`() {
        // Given
        val dto = TestDto("test@example.com", "John", 25)

        // When
        val result = FieldReflectionEngine.extractFieldValue(dto, "nonExistent")

        // Then
        assert(result == null)
    }

    @Test
    fun `extractFieldValue should convert non-string values to string`() {
        // Given
        val dto = TestDto("test@example.com", "John", 25)

        // When
        val result = FieldReflectionEngine.extractFieldValue(dto, "age")

        // Then
        assert(result == "25")
    }

    @Test
    fun `extractFieldValueCached should cache reflection metadata`() {
        // Given
        val dto = TestDto("test@example.com", "John", 25)

        // When - first call
        val result1 = FieldReflectionEngine.extractFieldValueCached(dto, "email")

        // When - second call (should use cache)
        val result2 = FieldReflectionEngine.extractFieldValueCached(dto, "name")

        // Then
        assert(result1 == "test@example.com")
        assert(result2 == "John")
    }

    @Test
    fun `clearCache should remove all cached metadata`() {
        // Given
        val dto = TestDto("test@example.com", "John", 25)
        FieldReflectionEngine.extractFieldValueCached(dto, "email")

        // When
        FieldReflectionEngine.clearCache()

        // Then - should still work after cache clear
        val result = FieldReflectionEngine.extractFieldValueCached(dto, "email")
        assert(result == "test@example.com")
    }
}
