package com.respiroc.greg.fullstackdeveloperproject.config

import com.respiroc.greg.fullstackdeveloperproject.service.ProductService
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

/**
 * Schedules product ingestion when app.ingestion.enabled=true (default).
 *
 * Toggle off per environment or in tests via:
 *  - application-<profile>.properties: app.ingestion.enabled=false
 *  - or globally disable all schedules: spring.task.scheduling.enabled=false
 */
@Component
@ConditionalOnProperty(prefix = "app.ingestion", name = ["enabled"], havingValue = "true", matchIfMissing = false)
class IngestionScheduler(
    private val productService: ProductService
) {
    private val logger = LoggerFactory.getLogger(IngestionScheduler::class.java)

    @Scheduled(initialDelay = 0, fixedRate = 3_600_000) // Run immediately at startup, then every hour
    fun scheduledFetchAndSaveProducts() {
        logger.info("[IngestionScheduler] Triggering scheduled fetchAndSaveProducts")
        productService.fetchAndSaveProducts()
    }
}
