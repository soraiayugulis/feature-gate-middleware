dependencies {
    implementation("org.springframework:spring-context:6.1.6")
    implementation("org.springframework:spring-aop:6.1.6")
    implementation("org.aspectj:aspectjweaver:1.9.21")
    implementation("jakarta.validation:jakarta.validation-api:3.0.2")

    implementation("com.configcat:configcat-java-client:9.0.0")

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testImplementation("org.assertj:assertj-core:3.25.3")
    testImplementation("io.mockk:mockk:1.13.10")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            groupId = "io.architecture"
            artifactId = "feature-flag-core"
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
