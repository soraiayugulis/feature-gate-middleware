# Feature Gate Middleware — Step-by-Step Implementation Plan

> **Spec reference**: `new-path.md`
> **Status**: DONE — updated each implementation cycle. Code is not authorized.
> **Main Skills**: `kotlin-spring-dev` and `cascade-repsonse-spec`
> **TDD discipline**: every task follows Red → Green → Refactor. No production code before a failing test.

---

## Package Structure

```
io.featuregate
├── core/
│   ├── FeatureFlagClient.kt          # domain port interface
│   ├── FeatureFlagContext.kt          # data class — ConfigCat User Object mapping
│   └── FeatureFlagExecutor.kt         # functional executor
├── annotation/
│   ├── WatchFeatureFlag.kt            # @WatchFeatureFlag annotation
│   └── HiFeatureFlag.kt               # @HiFeatureFlag annotation
├── spring/
│   ├── aspect/
│   │   └── WatchFeatureFlagAspect.kt  # Spring AOP @Around advice
│   ├── listener/
│   │   └── HiFeatureFlagStartupListener.kt  # ApplicationListener<ContextRefreshedEvent>
│   ├── config/
│   │   ├── FeatureGateProperties.kt   # @ConfigurationProperties root
│   │   └── FeatureGateAutoConfiguration.kt
│   └── validation/
│       └── FeatureGateStartupValidator.kt   # blank key + sdk-key + EX-06 checks
└── adapter/
    └── configcat/
        └── ConfigCatFeatureFlagClient.kt    # default adapter
```

---

## Tech Stack

| Concern            | Choice                          | Rationale                                              |
|--------------------|---------------------------------|--------------------------------------------------------|
| Language           | Kotlin + JVM 21                 | Idiomatic lambdas, data classes, null safety           |
| Build              | Gradle (Kotlin DSL)             | Single module, `api` vs `implementation` split         |
| Test framework     | JUnit 5 + MockK                 | Kotlin-idiomatic mocking                               |
| Spring             | Spring Boot 3.x (optional)      | `compileOnly` — core has zero Spring dependency        |
| Metrics            | Micrometer (`compileOnly`)      | Facade — backend-agnostic                              |
| Feature flag SDK   | ConfigCat (`compileOnly`)       | Default adapter; replaceable via `FeatureFlagClient`   |
| Logging            | SLF4J + MDC                     | Structured, framework-agnostic                         |

---

## Phase 1 — Domain Core (no Spring, no SDK)

**Goal**: Establish the pure domain contracts. Zero framework dependencies. Everything else builds on this.

### Tasks

#### T1.1 — `FeatureFlagContext` data class
- **Subtasks**:
  - Define `data class FeatureFlagContext(val identifier: String, val email: String? = null, val country: String? = null, val custom: Map<String, Any>? = null)`
  - Validate: `identifier` is not blank (secondary constructor guard or `init` block)
- **TDD (Red first)**:
  - `should throw when identifier is blank`
  - `should create context with only identifier`
  - `should create context with all fields`
- **Commit**: `feat(core): add FeatureFlagContext data class aligned with ConfigCat User Object`

#### T1.2 — `FeatureFlagClient` interface (domain port)
- **Subtasks**:
  - Define `interface FeatureFlagClient { fun isActive(flagKey: String, context: FeatureFlagContext?): Boolean }`
  - KDoc: contract, null context = global evaluation
- **TDD**: no production logic to test — this is a contract. Test via fake in T1.3.
- **Commit**: `feat(core): add FeatureFlagClient domain port interface`

#### T1.3 — `FeatureFlagExecutor`
- **Subtasks**:
  - Implement `execute<T>(flag, context?, onActive, onDisable)` with:
    - `require(flag.isNotBlank())` guard
    - fail-safe catch → `onDisable` fallback
    - nested catch if `onDisable` also throws → log + re-throw
  - Context-free overload: `execute<T>(flag, onActive, onDisable)` delegates to main with `context = null`
  - **`suspend` overload**: `suspend fun <T> executeSuspend(flag, context?, onActive: suspend () -> T, onDisable: suspend () -> T)` — same logic, same guards, same fail-safe, but lambdas are `suspend`. This is the only supported path for coroutine consumers. See [Coroutines note] below.
- **TDD (Red first)**:
  - `should execute onActive when flag is enabled`
  - `should execute onDisable when flag is disabled`
  - `should fall back to onDisable when client throws`
  - `should propagate exception when onDisable also throws`
  - `should throw IllegalArgumentException when flag key is blank`
  - `should execute without context (global flag)`
  - `should execute suspend onActive when flag is enabled`
  - `should execute suspend onDisable when flag is disabled`
  - `should fall back to suspend onDisable when client throws inside coroutine`
- **Test doubles**: `FakeFeatureFlagClient` (simple map-based stub — no mocking framework needed). Suspend tests use `runTest { }` from `kotlinx-coroutines-test`.
- **Commits**:
  - `feat(core): implement FeatureFlagExecutor with fail-safe error handling`
  - `feat(core): add executeSuspend overload for Kotlin coroutines consumers`

> **[Coroutines note] — Why `@WatchFeatureFlag` cannot support `suspend fun`**:
> Kotlin compiles `suspend fun` to a Java method with an extra `Continuation<T>` parameter and return type `Any?`. Spring AOP proxies operate at the Java bytecode level — they see `Any?` as the return type and `COROUTINE_SUSPENDED` as the immediate return value of `proceed()`. This means:
> 1. **Timer stops prematurely** — `proceed()` returns before the coroutine resumes; duration measurement is wrong.
> 2. **Return value replacement crashes** — injecting a result where `Continuation` machinery is expected causes a runtime error.
> Solutions requiring AspectJ LTW (`-javaagent`), compiler plugins, or `kotlinx-coroutines-reactor` bridges all add disproportionate complexity for this library's scope.
> **Resolution**: `@WatchFeatureFlag` is Spring MVC only (documented limitation). Coroutine consumers use `featureFlagExecutor.executeSuspend(...)`, which provides identical observability (same metrics, same MDC log, same fail-safe) with no overhead.

---

## Phase 2 — Annotations (pure Kotlin, no runtime logic)

**Goal**: Define the annotation contracts. Retention, targets, and parameters. No aspect yet.

### Tasks

#### T2.1 — `@WatchFeatureFlag`
- **Subtasks**:
  - `@Target(AnnotationTarget.FUNCTION)` — method-level only
  - `@Retention(AnnotationRetention.RUNTIME)` — needed for AOP proxy introspection
  - Parameter: `flagKey: String`
- **TDD**: annotation existence and retention are verified via reflection in a simple meta-test
- **Commit**: `feat(annotation): add @WatchFeatureFlag annotation`

#### T2.2 — `@HiFeatureFlag`
- **Subtasks**:
  - `@Target(AnnotationTarget.FUNCTION)`
  - `@Retention(AnnotationRetention.RUNTIME)`
  - Parameters: `flagKeys: Array<String>`, `description: String`
  - `flagKeys` must be non-empty array (validated at startup, not annotation level)
- **TDD**: meta-test — annotation is readable via reflection, `flagKeys` is an array
- **Commit**: `feat(annotation): add @HiFeatureFlag annotation with flagKeys array`

---

## Phase 3 — ConfigCat Adapter

**Goal**: Implement the default `FeatureFlagClient` adapter backed by ConfigCat SDK.

### Tasks

#### T3.1 — `ConfigCatFeatureFlagClient`
- **Subtasks**:
  - Implement `FeatureFlagClient` interface
  - Map `FeatureFlagContext` → `ConfigCatUser`: `identifier`, `email`, `country`, `custom`
  - `context = null` → pass no user object (global flag)
  - Use `autoPoll` mode; interval from `FeatureGateProperties.configcat.pollIntervalSeconds`
  - Log `WARN` at construction if initial fetch fails (SDK startup failure — EX-04)
- **TDD (Red first)**:
  - `should return true when ConfigCat SDK evaluates flag as active`
  - `should return false when ConfigCat SDK evaluates flag as inactive`
  - `should map FeatureFlagContext fields to ConfigCatUser correctly`
  - `should return false when context is null (global flag)`
  - `should return false and not throw when SDK throws`
- **Test doubles**: MockK mock of `ConfigCatClient`
- **Commit**: `feat(adapter): implement ConfigCatFeatureFlagClient with autoPoll and context mapping`

#### T3.2 — `FeatureGateProperties`
- **Subtasks**:
  - `@ConfigurationProperties("feature-gate")`
  - Structure:
    ```
    watch.enabled: Boolean = false
    hi.enabled: Boolean = false
    hi.logOnStartup: Boolean = false
    configcat.sdkKey: String = ""
    configcat.pollIntervalSeconds: Int = 60
    ```
- **TDD**: `@SpringBootTest` slice — properties bind correctly from `application.yml`
- **Commit**: `feat(config): add FeatureGateProperties configuration properties`

#### T3.3 — `FeatureGateStartupValidator`
- **Subtasks**:
  - `ApplicationListener<ContextRefreshedEvent>`
  - Checks (all `IllegalStateException` on failure):
    - `sdkKey` is not blank and not `"YOUR_SDK_KEY"` — EX-05
    - Any method annotated with `@WatchFeatureFlag` whose Java-compiled signature contains a `Continuation` parameter — EX-07 (detect `suspend fun` misuse)
  - Checks (all `WARN` on violation):
    - Any `@WatchFeatureFlag(flagKey)` not contained in co-located `@HiFeatureFlag.flagKeys` — EX-06
    - Any `@WatchFeatureFlag` or `@HiFeatureFlag` with blank `flagKey` / `flagKeys` entries — EX-01
- **TDD (Red first)**:
  - `should throw IllegalStateException when sdk-key is blank`
  - `should throw IllegalStateException when sdk-key is placeholder`
  - `should throw IllegalStateException when @WatchFeatureFlag is placed on a suspend fun`
  - `should emit WARN when @Watch flagKey not in @Hi flagKeys on same method`
  - `should pass validation when @Watch flagKey is contained in @Hi flagKeys`
  - `should emit WARN when @HiFeatureFlag flagKeys contains blank entry`
- **Commit**: `feat(spring): add startup validator for annotation hygiene and sdk-key guard`

---

## Phase 4 — Spring AOP Aspect (`@WatchFeatureFlag`)

**Goal**: Implement the `@Around` advice that instruments annotated methods with metrics and structured logs.

### Tasks

#### T4.1 — `WatchFeatureFlagAspect`
- **Subtasks**:
  - `@Aspect @Component`; conditional on `feature-gate.watch.enabled=true`
  - `@Around` pointcut: `@annotation(io.featuregate.annotation.WatchFeatureFlag)`
  - Per invocation:
    1. Read `flagKey` from annotation
    2. Evaluate flag via `FeatureFlagClient` (context is NOT available in the aspect — aspect evaluates global status only; per-user targeting must use `FeatureFlagExecutor` directly)
    3. Record `feature_flag.evaluation.total` Counter with tags `ff.key`, `ff.status`
    4. Start Timer
    5. Proceed with `joinPoint.proceed()`
    6. Stop Timer → record `feature_flag.evaluation.duration`
    7. On exception → record `feature_flag.evaluation.errors` Counter with `ff.key`, `ff.error_type`
    8. MDC: set `ff.key`, `ff.status`, `ff.method` before proceed; remove in `finally`
    9. Emit structured log (INFO) after proceed
  - `when watch.enabled=false`: aspect bean not registered (`@ConditionalOnProperty`)
- **TDD (Red first)**:
  - `should record evaluation counter with ENABLED status when flag is active`
  - `should record evaluation counter with DISABLED status when flag is inactive`
  - `should record duration timer after method completes`
  - `should record error counter when intercepted method throws`
  - `should emit structured log with ff.key, ff.status, ff.method, ff.duration_ms`
  - `should clean MDC after method completes (even on exception)`
  - `should not register aspect bean when watch.enabled=false`
- **Test doubles**: MockK for `FeatureFlagClient`, MockK for `MeterRegistry` / `Counter` / `Timer`
- **Commit**: `feat(spring): implement WatchFeatureFlagAspect with Micrometer metrics and MDC logging`

---

## Phase 5 — `@HiFeatureFlag` Runtime Behavior

**Goal**: Startup inventory log when `hi.enabled=true` and `hi.log-on-startup=true`.

### Tasks

#### T5.1 — `HiFeatureFlagStartupListener`
- **Subtasks**:
  - `ApplicationListener<ContextRefreshedEvent>`; conditional on `hi.enabled=true`
  - Scan all beans in `ApplicationContext` via `context.getBeansOfType(Any::class.java)`
  - Collect methods annotated with `@HiFeatureFlag` via reflection
  - When `hi.log-on-startup=true`: emit single structured INFO log with array of `{ key, description, method }`
  - Each entry in `flagKeys` array → separate element in the log array
  - Deduplication: same flag key appearing on multiple methods → one entry per (key, method) pair
- **TDD (Red first)**:
  - `should emit startup log listing all @HiFeatureFlag methods when log-on-startup=true`
  - `should emit one log entry per flagKey per method`
  - `should not emit log when log-on-startup=false`
  - `should not register listener when hi.enabled=false`
- **Commit**: `feat(spring): add HiFeatureFlagStartupListener with structured startup inventory log`

---

## Phase 6 — Auto-Configuration & Integration Tests

**Goal**: Wire everything together via Spring Boot auto-configuration; verify end-to-end.

### Tasks

#### T6.1 — `FeatureGateAutoConfiguration`
- **Subtasks**:
  - `@AutoConfiguration`
  - Register `ConfigCatFeatureFlagClient` bean: `@ConditionalOnMissingBean(FeatureFlagClient::class)` — consumer can override
  - Register `FeatureFlagExecutor` bean
  - Register `WatchFeatureFlagAspect`: `@ConditionalOnProperty("feature-gate.watch.enabled", havingValue="true")` + `@ConditionalOnClass(MeterRegistry::class)`
  - Register `HiFeatureFlagStartupListener`: `@ConditionalOnProperty("feature-gate.hi.enabled", havingValue="true")`
  - Register `FeatureGateStartupValidator`
  - `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` entry
- **TDD**:
  - `should register ConfigCatFeatureFlagClient when no custom bean is present`
  - `should not register ConfigCatFeatureFlagClient when consumer provides a FeatureFlagClient bean`
  - `should register WatchFeatureFlagAspect only when watch.enabled=true and Micrometer on classpath`
  - `should register HiFeatureFlagStartupListener only when hi.enabled=true`
- **Commit**: `feat(spring): add FeatureGateAutoConfiguration with conditional bean registration`

#### T6.2 — Integration test: full Spring Boot context
- **Subtasks**:
  - `@SpringBootTest` with a minimal consumer application context
  - Inject `FeatureFlagExecutor`, call with a fake `FeatureFlagClient` bean
  - Verify `@WatchFeatureFlag` aspect fires and metrics are recorded
  - Verify startup log emitted with `@HiFeatureFlag` inventory
  - Verify startup validator rejects blank `sdk-key`
- **Commit**: `test(integration): add end-to-end Spring Boot context integration tests`

---

## Phase 7 — OpenAPI / Swagger Integration

**Goal**: Add `FeatureFlagOperationCustomizer` so Swagger UI surfaces which endpoints are flag-gated. Fully conditional on SpringDoc being on the consumer's classpath — zero impact otherwise.

**Package**: `io.featuregate.spring.openapi`

### Tasks

#### T7.1 — `FeatureFlagOperationCustomizer`
- **Subtasks**:
  - Implement `OperationCustomizer` interface from `springdoc-openapi-starter-webmvc-ui`
  - In `customize(operation, handlerMethod)`: reflect on `handlerMethod.method` to find `@HiFeatureFlag`
  - If present:
    - Add tag `"feature-flag"` to the operation tags list
    - Build `x-feature-flags` extension as a `List<Map<String, String>>` — one entry per key in `flagKeys`
    - Append `"⚑ This endpoint's behavior may vary based on active feature flags."` to operation description (null-safe)
  - If `@HiFeatureFlag` absent: return operation unmodified
- **TDD (Red first)**:
  - `should add feature-flag tag when method has @HiFeatureFlag`
  - `should inject x-feature-flags extension with one entry per flagKey`
  - `should append description suffix without overwriting existing description`
  - `should return operation unmodified when @HiFeatureFlag is absent`
  - `should handle null existing description gracefully`
- **Test doubles**: MockK for `HandlerMethod`; real `Operation` object from swagger-models
- **Commit**: `feat(openapi): implement FeatureFlagOperationCustomizer injecting x-feature-flags extension`

#### T7.2 — Swagger UI badge plugin (static JS)
- **Subtasks**:
  - Create `src/main/resources/META-INF/resources/feature-gate/swagger-plugin.js`
  - Plugin reads `x-feature-flags` array on each operation block and renders a `<span>` badge per key
  - Register via `SwaggerUiConfigParameters.addPluginUrl("/feature-gate/swagger-plugin.js")` in auto-configuration
  - JS must be self-contained — no external CDN, no `npm` build step
- **TDD**: Verified manually in Swagger UI; no unit test for JS (out of scope for library tests)
- **Commit**: `feat(openapi): add Swagger UI badge plugin for feature-flag extensions`

#### T7.3 — `FeatureFlagOpenApiAutoConfiguration`
- **Subtasks**:
  - Separate `@AutoConfiguration` class — **not** merged into `FeatureGateAutoConfiguration`
  - Conditions:
    - `@ConditionalOnClass(OperationCustomizer::class)` — SpringDoc present
    - `@ConditionalOnProperty("feature-gate.hi.enabled", havingValue = "true")` — HiFF enabled
  - Registers: `FeatureFlagOperationCustomizer` bean + `SwaggerUiConfigParameters` customizer for JS plugin
  - Add entry to `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`
  - **Rationale for separate class**: prevents `NoClassDefFoundError` on `OperationCustomizer` when SpringDoc is absent — the inner class is never loaded if the outer `@ConditionalOnClass` fails
- **TDD (Red first)**:
  - `should register FeatureFlagOperationCustomizer when SpringDoc and hi.enabled=true`
  - `should not register any OpenAPI bean when SpringDoc is absent`
  - `should not register any OpenAPI bean when hi.enabled=false`
- **Commit**: `feat(openapi): add FeatureFlagOpenApiAutoConfiguration with conditional registration`

---

## Phase 8 — README & Consumer Documentation

**Goal**: Document how to add and configure the library. Iterative — updated alongside code.

### Tasks

#### T8.1 — README
- **Subtasks**:
  - Gradle dependency snippet
  - Minimal `application.yml` with all `feature-gate.*` properties
  - Usage examples: `@WatchFeatureFlag`, `@HiFeatureFlag`, `FeatureFlagExecutor`, `executeSuspend`
  - OpenAPI section: what to expect in Swagger UI when `hi.enabled=true` and SpringDoc present
  - Known limitations section: AOP proxy bypass, `suspend fun` on `@WatchFeatureFlag` unsupported, MDC + virtual threads caveat
- **Commit**: `docs: add README with usage guide, configuration reference, and known limitations`

---

## Phase 9 — Publishing (GitHub Packages)

**Goal**: Make the library consumable as a versioned artifact from GitHub Packages.

### Tasks

#### T9.1 — Gradle publishing configuration
- **Subtasks**:
  - Apply `maven-publish` Gradle plugin
  - Configure `MavenPublication` with:
    - `groupId`: `io.featuregate` (or chosen group)
    - `artifactId`: `feature-gate-middleware`
    - `version`: driven by git tag via `gradle-git-version` plugin or `VERSION` file
  - Generate sources JAR (`withSourcesJar()`)
  - Generate Javadoc/KDoc JAR (`withJavadocJar()`)
  - Configure POM: `name`, `description`, `url`, `licenses (Apache 2.0)`, `developers`, `scm`
  - Repository target: `https://maven.pkg.github.com/OWNER/REPO`
  - Credentials via `GITHUB_ACTOR` / `GITHUB_TOKEN` env vars (available natively in GitHub Actions — never hardcoded)
- **Commit**: `build: add maven-publish configuration targeting GitHub Packages`

#### T9.2 — CI/CD publish pipeline (GitHub Actions)
- **Subtasks**:
  - Trigger: push to tag matching `v*.*.*`
  - Steps: checkout → setup JDK 21 → `./gradlew test` → `./gradlew publishAllPublicationsToGitHubPackagesRepository`
  - Required secrets: `GITHUB_TOKEN` (automatically available in Actions — no manual secret setup needed)
  - Separate workflow job for `SNAPSHOT` builds on `main`: publish `0.1.0-SNAPSHOT` on every merge to `main`
- **Commit**: `ci: add publish workflow for GitHub Packages on version tag and main branch`

#### T9.3 — Version strategy
- **Subtasks**:
  - Semantic versioning: `MAJOR.MINOR.PATCH` — start at `0.1.0` (pre-stable API)
  - Promote to `1.0.0` after first production adoption and API freeze
  - `SNAPSHOT` builds: `0.1.0-SNAPSHOT` published on every merge to `main`
  - Release builds: published on `v*.*.*` tag push
- **Commit**: `build: add version strategy and SNAPSHOT configuration`

---

## Commit Sequence Summary

| Phase | Commit                                                                               |
|-------|--------------------------------------------------------------------------------------|
| 1     | `feat(core): add FeatureFlagContext data class`                                      |
| 1     | `feat(core): add FeatureFlagClient domain port interface`                            |
| 1     | `feat(core): implement FeatureFlagExecutor with fail-safe error handling`            |
| 2     | `feat(annotation): add @WatchFeatureFlag annotation`                                 |
| 2     | `feat(annotation): add @HiFeatureFlag annotation with flagKeys array`                |
| 3     | `feat(adapter): implement ConfigCatFeatureFlagClient`                                |
| 3     | `feat(config): add FeatureGateProperties`                                            |
| 3     | `feat(spring): add startup validator for annotation hygiene and sdk-key guard`       |
| 4     | `feat(spring): implement WatchFeatureFlagAspect`                                     |
| 5     | `feat(spring): add HiFeatureFlagStartupListener`                                     |
| 6     | `feat(spring): add FeatureGateAutoConfiguration`                                     |
| 6     | `test(integration): add end-to-end Spring Boot context integration tests`            |
| 7     | `feat(openapi): implement FeatureFlagOperationCustomizer`                            |
| 7     | `feat(openapi): add Swagger UI badge plugin`                                         |
| 7     | `feat(openapi): add FeatureFlagOpenApiAutoConfiguration`                             |
| 8     | `docs: add README`                                                                   |
| 9     | `build: add maven-publish configuration targeting GitHub Packages`                   |
| 9     | `ci: add publish workflow for GitHub Packages on version tag and main branch`        |
| 9     | `build: add version strategy and SNAPSHOT configuration`                             |

---

## Open for Next Iteration

- [x] OpenAPI / Swagger integration — resolved as Phase 7 (`FeatureFlagOperationCustomizer` + badge plugin + `FeatureFlagOpenApiAutoConfiguration`).
- [x] Publish to GitHub Packages — resolved as Phase 9 (Gradle `maven-publish` + GitHub Actions workflow). GitHub Packages is the sole target.
- [x] Kotlin coroutines (`suspend fun`) — resolved via `executeSuspend()` overload in `FeatureFlagExecutor` (T1.3). `@WatchFeatureFlag` remains Spring MVC only — documented limitation.
- [x] MDC context propagation for WebFlux / virtual threads — documented as unsupported. MDC is thread-local; it does not propagate across coroutine suspensions or virtual thread handoffs without explicit configuration by the consumer. This library does not provide MDC bridging. Consumers using WebFlux or Project Loom are responsible for configuring their own MDC propagation (e.g., `MDCContext` from `kotlinx-coroutines-slf4j`, or a `ThreadLocal`-aware executor). Documented in the README known limitations section (T8.1).

---

*Iterative working document — updated each implementation cycle.*
