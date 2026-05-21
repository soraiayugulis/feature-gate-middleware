# Feature Gate Middleware - Apresentação Técnica

## O que é?
Spring Boot middleware para feature flags com contexto tipado, AOP, métricas e OpenAPI.

## Arquitetura Hexagonal
- **Domain Port**: `FeatureFlagClient` (interface)
- **Adapter**: `ConfigCatFeatureFlagClient` (ConfigCat SDK)
- **Application Service**: `FeatureFlagExecutor` (fail-safe)
- **AOP Aspects**: `WatchFeatureFlagAspect` (métricas), `HiFeatureFlagAspect` (inventory)
- **Auto-Configuration**: `FeatureGateAutoConfiguration` (Spring Boot 3.x)

## Principais Features
- Contexto tipado com identifier, email, country, custom attributes
- `@WatchFeatureFlag`: métricas Micrometer automáticas
- `@HiFeatureFlag`: inventory de flags no startup
- OpenAPI integration: Swagger UI mostra endpoints com flags
- Auto-configuration zero-config com condicionais

## Desenvolvimento
- Spec-Driven Development (SDD) via step2step.md
- TDD rigoroso (RED → GREEN → REFACTOR) com MockK/JUnit 5
- AI-augmented development (Cascade/Windsurf)
- 9 fases incrementais, cada um com PR próprio
- Global rules: TDD quality gates, Kotlin Spring Expert patterns

## Tech Stack
- Kotlin, Spring Boot 3.2.5, Gradle 8.5 (Kotlin DSL)
- ConfigCat SDK 9.0.0, Micrometer 1.12.5
- SpringDoc OpenAPI 2.3.0, AspectJ 1.9.21
- Testcontainers para testes de integração
- GitHub Packages para publicação

## Entregáveis
- 74 testes, 0 falhas
- Publicado em GitHub Packages (io.featuregate:feature-gate-middleware:0.1.0-SNAPSHOT)
- README completo com exemplos e limitações conhecidas
