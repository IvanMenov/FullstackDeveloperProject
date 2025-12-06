package com.respiroc.greg.fullstackdeveloperproject.testutil

import org.testcontainers.containers.PostgreSQLContainer

/**
 * A singleton PostgreSQL Testcontainers instance shared across the whole JVM test run.
 *
 * Using a shared container prevents Hikari from holding onto connections to a
 * stopped container when the Spring test context gets reused across classes.
 */
object SharedPostgresContainer {
    val instance: PostgreSQLContainer<*> by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        PostgreSQLContainer("postgres:16-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
            .apply { start() }
    }
}