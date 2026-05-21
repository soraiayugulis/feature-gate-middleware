# Library Feature Gate Middleware

## What is it?
Spring Boot middleware for feature flags with typed context, AOP, metrics, and OpenAPI.

## Hexagonal Architecture
- **Domain Port**: `FeatureFlagClient` (interface)
- **Adapter**: `ConfigCatFeatureFlagClient` (ConfigCat SDK)
- **Application Service**: `FeatureFlagExecutor` (fail-safe)
- **AOP Aspects**: `WatchFeatureFlagAspect` (metrics), `HiFeatureFlagAspect` (inventory)
- **Auto-Configuration**: `FeatureGateAutoConfiguration` (Spring Boot 3.x)

## Key Features
- Typed context with identifier, email, country, custom attributes
- `@WatchFeatureFlag`: automatic Micrometer metrics
- `@HiFeatureFlag`: flag inventory at startup
- OpenAPI integration: Swagger UI shows endpoints with flags
- Zero-config auto-configuration with conditionals

## Development
- Spec-Driven Development (SDD) via step2step.md
- Rigorous TDD (RED → GREEN → REFACTOR) with MockK/JUnit 5
- AI-augmented development (Cascade/Windsurf)
- 9 incremental phases, each with its own PR
- Global rules: TDD quality gates, Kotlin Spring Expert patterns

## Development Methodology (AI-Augmented)

### Rules
- **Global Rules**: Never commit directly to main, always create new branches from updated main
- **Cascade Response Spec**: Mandatory header structure (Persona, Objective, Quality, Suggestions, Questions)
- **Test-Drive Gate**: Strict TDD enforcement with JUnit 5/MockK, Arrange-Act-Assert pattern, Testcontainers for integration tests
- **Kotlin Spring Expert**: Idiomatic Kotlin over Java-Style, constructor injection, null-safety, Kotlin DSL for Gradle

### Skills
- **kotlin-spring-dev**: Expert guidance for Spring applications in Kotlin
- **tdd-expert**: Test-Driven Development discipline for new features and complex bug fixes

### Workflows
- **new-feature-sdd**: Implement features following Spec-Driven Development process
- **prepare-pr**: Prepare pull request for review
- **pr-create**: Create pull request for review
- **pre-commit**: Prepare all commits to user review
- **review**: Review code changes for bugs, security issues, and improvements

### Persona
- **Default**: Principal Software Engineer with AI-augmented development experience
- Follows best practices, proposes effective solutions, focuses on quality, simplicity, and maintainability

### Objectives
- Every response includes: Persona, Objective, Quality assessment, Suggestions, Questions
- Ensures structured, high-quality development with clear communication

## Tech Stack
- Kotlin, Spring Boot 3.2.5, Gradle 8.5 (Kotlin DSL)
- ConfigCat SDK 9.0.0, Micrometer 1.12.5
- SpringDoc OpenAPI 2.3.0, AspectJ 1.9.21
- Testcontainers for integration tests
- GitHub Packages for publishing

## Deliverables
- 74 tests, 0 failures
- Published to GitHub Packages (io.featuregate:feature-gate-middleware:0.1.0-SNAPSHOT)

## Trajectory
The entire development trajectory, from the creation of the specification to the final implementation, is documented in the [Cascade Trajectory](spec-driven-development/feature-flag-cascade-trajectory.md) document.
