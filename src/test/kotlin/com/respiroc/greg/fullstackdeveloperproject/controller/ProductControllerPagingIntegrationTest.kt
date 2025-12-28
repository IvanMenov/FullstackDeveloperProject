package com.respiroc.greg.fullstackdeveloperproject.controller

import com.respiroc.greg.fullstackdeveloperproject.model.Product
import com.respiroc.greg.fullstackdeveloperproject.repository.ProductRepository
import com.respiroc.greg.fullstackdeveloperproject.testutil.AbstractIntegrationTest
import org.hamcrest.Matchers
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.cache.CacheManager
import org.springframework.http.MediaType
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@AutoConfigureMockMvc
class ProductControllerPagingIntegrationTest : AbstractIntegrationTest() {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var productRepository: ProductRepository

    @Autowired
    lateinit var jdbcTemplate: JdbcTemplate

    @Autowired
    lateinit var cacheManager: CacheManager

    @BeforeEach
    fun clean() {
        jdbcTemplate.execute("DELETE FROM product_variants")
        jdbcTemplate.execute("DELETE FROM products")
        // Clear cache to prevent test interference
        cacheManager.getCache("productPages")?.clear()
    }

    private fun seedProducts(n: Int) {
        for (i in 1..n) {
            val title = "P" + i.toString().padStart(4, '0')
            productRepository.save(Product(title = title, vendor = "V", productType = null))
        }
    }

    @Test
    fun `GET products table page 0 shows first 35 items, correct controls and labels`() {
        seedProducts(40)

        mockMvc.perform(get("/products/table").contentType(MediaType.TEXT_HTML).param("page", "0"))
            .andExpect(status().isOk)
            // Page indicator and size label
            .andExpect(content().string(Matchers.containsString("Page 1 of 2")))
            // Contains first page items
            .andExpect(content().string(Matchers.containsString("P0001")))
            .andExpect(content().string(Matchers.containsString("P0035")))
            // Does not contain next page first item
            .andExpect(content().string(Matchers.not(Matchers.containsString("P0036"))))
            // Prev should be disabled (button present) and Next enabled
            .andExpect(content().string(Matchers.containsString("Prev")))
            .andExpect(content().string(Matchers.containsString("disabled")))
            .andExpect(content().string(Matchers.containsString("Next")))
    }

    @Test
    fun `GET products table page 1 shows remaining items, correct controls and labels`() {
        seedProducts(40)

        mockMvc.perform(get("/products/table").contentType(MediaType.TEXT_HTML).param("page", "1"))
            .andExpect(status().isOk)
            // Page indicator and size label
            .andExpect(content().string(Matchers.containsString("Page 2 of 2")))
            // Contains items 36..40
            .andExpect(content().string(Matchers.containsString("P0036")))
            .andExpect(content().string(Matchers.containsString("P0040")))
            // Does not contain first page first item
            .andExpect(content().string(Matchers.not(Matchers.containsString("P0001"))))
            // Next should be disabled on last page
            .andExpect(content().string(Matchers.containsString("Next")))
            .andExpect(content().string(Matchers.containsString("disabled")))
    }
}
