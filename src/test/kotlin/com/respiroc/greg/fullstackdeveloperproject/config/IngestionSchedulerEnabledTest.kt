package com.respiroc.greg.fullstackdeveloperproject.config

import com.respiroc.greg.fullstackdeveloperproject.service.ProductService
import com.respiroc.greg.fullstackdeveloperproject.testutil.AbstractIntegrationTest
import org.junit.jupiter.api.Test
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.bean.override.mockito.MockitoBean

@TestPropertySource(properties = [
    "app.ingestion.enabled=true",
    // keep scheduling infrastructure disabled so @Scheduled won't actually schedule
    "spring.task.scheduling.enabled=false"
])
class IngestionSchedulerEnabledTest : AbstractIntegrationTest() {

    @MockitoBean
    lateinit var productService: ProductService

    @Autowired(required = false)
    lateinit var ingestionScheduler: IngestionScheduler

    @Test
    fun `scheduler bean exists and triggers service method`() {
        // Bean should be created when app.ingestion.enabled=true
        assert(this::ingestionScheduler.isInitialized) { "IngestionScheduler should be present" }

        verify(productService).fetchAndSaveProducts()
    }
}
