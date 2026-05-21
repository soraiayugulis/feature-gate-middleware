package io.architecture.featureflag.aop

import io.architecture.featureflag.config.FeatureGateProperties
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationListener
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.stereotype.Component

/**
 * Startup listener that emits an inventory log of all @HiFeatureFlag annotated methods
 * when hi.enabled=true and hi.log-on-startup=true.
 *
 * Only registered when feature-gate.hi.enabled=true.
 */
@Component
@ConditionalOnProperty(prefix = "feature-gate.hi", name = ["enabled"], havingValue = "true", matchIfMissing = false)
class HiFeatureFlagStartupListener(
    private val properties: FeatureGateProperties,
    private val applicationContext: ApplicationContext,
    private val logger: Logger = LoggerFactory.getLogger(HiFeatureFlagStartupListener::class.java)
) : ApplicationListener<ContextRefreshedEvent> {

    override fun onApplicationEvent(event: ContextRefreshedEvent) {
        if (!properties.hi.logOnStartup) {
            return
        }

        val inventory = mutableListOf<Map<String, Any>>()

        val beans = applicationContext.getBeansOfType(Any::class.java)
        for (bean in beans.values) {
            val beanClass = bean::class.java

            for (method in beanClass.declaredMethods) {
                val hiAnnotation = method.getAnnotation(HiFeatureFlag::class.java) ?: continue

                for (flagKey in hiAnnotation.flagKeys) {
                    inventory.add(
                        mapOf(
                            "key" to flagKey,
                            "description" to hiAnnotation.description,
                            "method" to "${beanClass.simpleName}.${method.name}"
                        )
                    )
                }
            }
        }

        if (inventory.isNotEmpty()) {
            logger.info("[FeatureGate] @HiFeatureFlag startup inventory: {}", inventory)
        }
    }
}
