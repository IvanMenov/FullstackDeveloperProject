package com.respiroc.greg.fullstackdeveloperproject.testutil

import com.redis.testcontainers.RedisContainer


object SharedRedisContainer {
    val instance: RedisContainer by lazy(LazyThreadSafetyMode.SYNCHRONIZED){
        RedisContainer("redis:7.2")
            .withExposedPorts(6379)
            .apply { start() }
    }


}