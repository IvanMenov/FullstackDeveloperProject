package com.respiroc.greg.fullstackdeveloperproject.testutil

import org.testcontainers.containers.PostgreSQLContainer

object SharedPostgresContainer {
    val instance: PostgreSQLContainer<*> by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        PostgreSQLContainer("postgres:16-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
            .apply { start() }
    }
}