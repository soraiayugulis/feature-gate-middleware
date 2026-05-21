plugins {
    `java`
    `maven-publish`
}

group = "io.featuregate"
version = "0.1.0-SNAPSHOT"

dependencies {
    implementation("org.springframework:spring-context:6.1.6")
    implementation("org.springframework:spring-aop:6.1.6")
    implementation("org.aspectj:aspectjweaver:1.9.21")
    implementation("jakarta.validation:jakarta.validation-api:3.0.2")
    implementation("org.slf4j:slf4j-api:2.0.13")

    implementation("com.configcat:configcat-java-client:9.0.0")
    implementation("io.micrometer:micrometer-core:1.12.5")

    compileOnly("org.springframework.boot:spring-boot:3.2.5")
    compileOnly("org.springframework.boot:spring-boot-autoconfigure:3.2.5")
    compileOnly("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0")

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testImplementation("org.assertj:assertj-core:3.25.3")
    testImplementation("io.mockk:mockk:1.13.10")
    testImplementation("org.springframework.boot:spring-boot:3.2.5")
    testImplementation("org.springframework.boot:spring-boot-test:3.2.5")
    testImplementation("org.springframework.boot:spring-boot-autoconfigure:3.2.5")
    testImplementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            artifactId = "feature-gate-middleware"
            
            // Generate sources JAR
            artifact(tasks.named("sourcesJar"))
            
            // Generate Javadoc JAR
            artifact(tasks.named("javadocJar"))
            
            // POM metadata
            pom {
                name.set("Feature Gate Middleware")
                description.set("Spring Boot middleware library for feature flag evaluation with typed targeting context, AOP instrumentation, metrics, and OpenAPI integration")
                url.set("https://github.com/soraiayugulis/feature-gate-middleware")
                
                licenses {
                    license {
                        name.set("Apache License, Version 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0")
                    }
                }
                
                developers {
                    developer {
                        id.set("_sysout")
                        name.set("_sysout")
                        email.set("")
                    }
                }
                
                scm {
                    connection.set("scm:git:git://github.com/soraiayugulis/feature-gate-middleware.git")
                    developerConnection.set("scm:git:ssh://github.com:soraiayugulis/feature-gate-middleware.git")
                    url.set("https://github.com/soraiayugulis/feature-gate-middleware")
                }
            }
        }
    }
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/soraiayugulis/feature-gate-middleware")
            credentials {
                username = System.getenv("GITHUB_ACTOR") ?: ""
                password = System.getenv("GITHUB_TOKEN") ?: ""
            }
        }
    }
}

// Tasks for sources and Javadoc
tasks.register<Jar>("sourcesJar") {
    dependsOn(tasks.classes)
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

tasks.register<Jar>("javadocJar") {
    dependsOn(tasks.javadoc)
    archiveClassifier.set("javadoc")
    from(tasks.javadoc.get().destinationDir)
}
