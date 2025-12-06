package com.respiroc.greg.fullstackdeveloperproject.testutil

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.Assumptions
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.DockerClientFactory

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class AbstractIntegrationTest {

    companion object {
        @JvmStatic
        @BeforeAll
        fun checkDocker() {
            // Gracefully skip the entire test class when Docker is unavailable
            Assumptions.assumeTrue(
                try { DockerClientFactory.instance().isDockerAvailable() } catch (ex: Throwable) { false },
                "Docker is not available; skipping integration tests"
            )
            // Ensure the shared container is started once per JVM
            SharedPostgresContainer.instance
        }

        @JvmStatic
        @DynamicPropertySource
        fun postgresProps(registry: DynamicPropertyRegistry) {
            val container = SharedPostgresContainer.instance
            registry.add("spring.datasource.url") { container.jdbcUrl }
            registry.add("spring.datasource.username") { container.username }
            registry.add("spring.datasource.password") { container.password }
        }
    }
}