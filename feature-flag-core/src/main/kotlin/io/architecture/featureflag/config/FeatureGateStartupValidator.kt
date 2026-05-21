package io.architecture.featureflag.config

import io.architecture.featureflag.aop.HiFeatureFlag
import io.architecture.featureflag.aop.WatchFeatureFlag
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationContext

/**
 * Startup validator for annotation hygiene and SDK key guard.
 *
 * Throws [IllegalStateException] on hard violations (EX-05, EX-07).
 * Emits WARN on soft violations (EX-01, EX-06).
 *
 * Designed to be called at application startup — either as an
 * [org.springframework.context.ApplicationListener] or invoked directly in tests.
 */
class FeatureGateStartupValidator(
    private val properties: FeatureGateProperties,
    private val applicationContext: ApplicationContext,
    private val logger: Logger = LoggerFactory.getLogger(FeatureGateStartupValidator::class.java)
) {

    fun validate() {
        validateSdkKey()
        validateAnnotations()
    }

    private fun validateSdkKey() {
        val sdkKey = properties.configcat.sdkKey
        check(sdkKey.isNotBlank() && sdkKey != "YOUR_SDK_KEY") {
            "[FeatureGate] Invalid sdk-key: must not be blank or placeholder 'YOUR_SDK_KEY'. " +
                "Set 'feature-gate.configcat.sdk-key' in your application properties. (EX-05)"
        }
    }

    private fun validateAnnotations() {
        val beanNames = applicationContext.getBeanDefinitionNames()

        for (beanName in beanNames) {
            val bean = runCatching { applicationContext.getBean(beanName) }.getOrNull() ?: continue
            val beanClass = bean::class.java

            for (method in beanClass.declaredMethods) {
                val watch = method.getAnnotation(WatchFeatureFlag::class.java)
                val hi = method.getAnnotation(HiFeatureFlag::class.java)

                // EX-07: detect @WatchFeatureFlag on suspend fun
                if (watch != null) {
                    val hasContinuationParam = method.parameterTypes.any {
                        it.name == "kotlin.coroutines.Continuation"
                    }
                    check(!hasContinuationParam) {
                        "[FeatureGate] @WatchFeatureFlag on suspend fun '${method.name}' in '${beanClass.simpleName}' " +
                            "is not supported. AOP proxies do not intercept coroutine continuations. " +
                            "Use FeatureFlagExecutor.execute() manually instead. (EX-07)"
                    }
                }

                // EX-06: @WatchFeatureFlag flagKey not in @HiFeatureFlag.flagKeys
                if (watch != null && hi != null) {
                    if (watch.flagKey !in hi.flagKeys) {
                        logger.warn(
                            "[FeatureGate] @WatchFeatureFlag(flagKey='${watch.flagKey}') on " +
                                "'${beanClass.simpleName}.${method.name}' is not listed in " +
                                "@HiFeatureFlag.flagKeys=${hi.flagKeys.toList()}. (EX-06)"
                        )
                    }
                }

                // EX-01: blank flagKey in @WatchFeatureFlag
                if (watch != null && watch.flagKey.isBlank()) {
                    logger.warn(
                        "[FeatureGate] @WatchFeatureFlag with blank flagKey on " +
                            "'${beanClass.simpleName}.${method.name}'. (EX-01)"
                    )
                }

                // EX-01: blank flagKeys entries in @HiFeatureFlag
                if (hi != null && hi.flagKeys.any { it.isBlank() }) {
                    logger.warn(
                        "[FeatureGate] @HiFeatureFlag with blank entry in flagKeys on " +
                            "'${beanClass.simpleName}.${method.name}'. (EX-01)"
                    )
                }
            }
        }
    }
}
