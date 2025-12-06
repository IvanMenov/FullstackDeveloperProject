package com.respiroc.greg.fullstackdeveloperproject

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
@EnableCaching
class FullstackDeveloperProjectApplication

fun main(args: Array<String>) {
	runApplication<FullstackDeveloperProjectApplication>(*args)
}


