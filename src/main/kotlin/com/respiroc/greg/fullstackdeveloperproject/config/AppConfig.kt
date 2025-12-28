package com.respiroc.greg.fullstackdeveloperproject.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.CacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.RedisPassword
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer
import org.springframework.web.client.RestTemplate
import java.time.Duration

@Configuration
class AppConfig {
	
	@Bean
	fun restTemplate(): RestTemplate {
		return RestTemplate()
	}

	@Bean
	fun redisConnectionFactory(
		@Value("\${spring.data.redis.host:localhost}") host: String,
		@Value("\${spring.data.redis.port:6379}") port: Int,
		@Value("\${spring.data.redis.password:}") password: String
	): RedisConnectionFactory {
		val redisConfig = RedisStandaloneConfiguration()
		redisConfig.hostName = host
		redisConfig.port = port
		if (password.isNotBlank()) {
			redisConfig.password = RedisPassword.of(password)
		}
		return LettuceConnectionFactory(redisConfig)
	}

	@Bean
	fun redisObjectMapper(): ObjectMapper {
		return ObjectMapper().apply {
			registerModule(KotlinModule.Builder().build())
		}
	}

	@Bean
	fun cacheManager(redisConnectionFactory: RedisConnectionFactory, redisObjectMapper: ObjectMapper): CacheManager {
		// Create ObjectMapper with Kotlin support and type information using PROPERTY format
		// This ensures proper serialization/deserialization of generic types like PageResult<T>
		val cacheObjectMapper = ObjectMapper().apply {
			registerModule(KotlinModule.Builder().build())
			// Enable default typing with PROPERTY format (adds @class property, not array wrapper)
			activateDefaultTyping(
				LaissezFaireSubTypeValidator.instance,
				ObjectMapper.DefaultTyping.NON_FINAL,
				com.fasterxml.jackson.annotation.JsonTypeInfo.As.PROPERTY
			)
		}
		val serializer = GenericJackson2JsonRedisSerializer(cacheObjectMapper)
		val stringSerializer = StringRedisSerializer()
		
		val cacheConfig = RedisCacheConfiguration.defaultCacheConfig()
			.entryTtl(Duration.ofHours(1))
			.serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(stringSerializer))
			.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer))
			.disableCachingNullValues()

		return RedisCacheManager.builder(redisConnectionFactory)
			.cacheDefaults(cacheConfig)
			.build()
	}
}

