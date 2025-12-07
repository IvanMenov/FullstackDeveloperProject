package com.respiroc.greg.fullstackdeveloperproject.config

import com.respiroc.greg.fullstackdeveloperproject.testutil.AbstractIntegrationTest
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.NoSuchBeanDefinitionException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext

class IngestionSchedulerDisabledTest : AbstractIntegrationTest() {

    @Autowired
    lateinit var applicationContext: ApplicationContext

    @Test
    fun `IngestionScheduler bean is absent when app_ingestion_enabled_false`() {
        // application-test.properties sets app.ingestion.enabled=false
        assertThatThrownBy {
            applicationContext.getBean(IngestionScheduler::class.java)
        }.isInstanceOf(NoSuchBeanDefinitionException::class.java)
    }
}
