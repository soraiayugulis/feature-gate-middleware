package io.architecture.featureflag.validation

/**
 * Thread-local holder for storing the object being validated.
 * Allows validators to access the root bean and extract field values for context.
 */
object ValidationContextHolder {
    private val currentObject = ThreadLocal<Any>()

    /**
     * Sets the current object being validated.
     */
    fun set(obj: Any) {
        currentObject.set(obj)
    }

    /**
     * Gets the current object being validated.
     */
    fun get(): Any? = currentObject.get()

    /**
     * Clears the current object from the thread-local.
     */
    fun clear() {
        currentObject.remove()
    }

    /**
     * Executes a block with the given object in context, clearing afterwards.
     */
    inline fun <T> withContext(obj: Any, block: () -> T): T {
        set(obj)
        return try {
            block()
        } finally {
            clear()
        }
    }
}
