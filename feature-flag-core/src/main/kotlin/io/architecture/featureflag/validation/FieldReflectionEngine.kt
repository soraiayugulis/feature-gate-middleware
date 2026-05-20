package io.architecture.featureflag.validation

import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties

/**
 * Engine for extracting field values from DTOs via reflection.
 * Used to dynamically read values from fields specified in validation annotations.
 */
object FieldReflectionEngine {

    /**
     * Extracts the value of a field from an object using reflection.
     *
     * @param obj The object to extract the field value from
     * @param fieldName The name of the field to extract
     * @return The field value as String, or null if not found or null
     */
    fun extractFieldValue(obj: Any, fieldName: String): String? {
        return try {
            val kClass = obj::class
            val property = kClass.memberProperties.find { it.name == fieldName }
            property?.getter?.call(obj)?.toString()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Caches reflection metadata for performance optimization.
     * Key: Class name, Value: Map of field names to property accessors
     * Thread-safe using ConcurrentHashMap for concurrent access.
     */
    private val reflectionCache = java.util.concurrent.ConcurrentHashMap<String, Map<String, kotlin.reflect.KProperty1<Any, *>>>()

    /**
     * Extracts field value with caching for improved performance.
     *
     * @param obj The object to extract the field value from
     * @param fieldName The name of the field to extract
     * @return The field value as String, or null if not found or null
     */
    fun extractFieldValueCached(obj: Any, fieldName: String): String? {
        val className = obj::class.qualifiedName ?: return extractFieldValue(obj, fieldName)
        
        val properties = reflectionCache.getOrPut(className) {
            @Suppress("UNCHECKED_CAST")
            obj::class.memberProperties.associateBy { it.name } as Map<String, kotlin.reflect.KProperty1<Any, *>>
        }
        
        return try {
            properties[fieldName]?.getter?.call(obj)?.toString()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Clears the reflection cache. Useful for testing or memory management.
     */
    fun clearCache() {
        reflectionCache.clear()
    }
}
