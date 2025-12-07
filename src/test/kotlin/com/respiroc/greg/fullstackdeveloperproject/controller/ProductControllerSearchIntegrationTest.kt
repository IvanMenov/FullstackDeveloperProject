package com.respiroc.greg.fullstackdeveloperproject.controller

import com.respiroc.greg.fullstackdeveloperproject.model.Product
import com.respiroc.greg.fullstackdeveloperproject.repository.ProductRepository
import com.respiroc.greg.fullstackdeveloperproject.testutil.AbstractIntegrationTest
import org.hamcrest.Matchers
import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@AutoConfigureMockMvc
class ProductControllerSearchIntegrationTest : AbstractIntegrationTest() {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var productRepository: ProductRepository

    @Autowired
    lateinit var jdbcTemplate: JdbcTemplate

    @BeforeEach
    fun clean() {
        jdbcTemplate.execute("DELETE FROM product_variants")
        jdbcTemplate.execute("DELETE FROM products")
    }

    private fun seed() {
        productRepository.save(Product(title = "Alpha Tee", vendor = "Acme", productType = "Clothes"))
        productRepository.save(Product(title = "alpha hat", vendor = "BrandCo", productType = "Hats"))
        productRepository.save(Product(title = "Bravo Mug", vendor = "KitchenPro", productType = "Mugs"))
    }

    @Test
    fun `GET products table with q filters rows and keeps q in pagination`() {
        seed()
        mockMvc.perform(get("/products/table").param("q", "alpha"))
            .andExpect(status().isOk)
            .andExpect(content().string(containsString("Alpha Tee")))
            .andExpect(content().string(containsString("alpha hat")))
            .andExpect(content().string(Matchers.not(containsString("Bravo Mug"))))
            // Pagination buttons should preserve q
            .andExpect(content().string(containsString("/products/table?page=1&amp;q=alpha")))
    }

    @Test
    fun `GET products table with q that yields no results shows search empty state`() {
        seed()
        mockMvc.perform(get("/products/table").param("q", "zzz"))
            .andExpect(status().isOk)
            .andExpect(content().string(containsString("No products match")))
            .andExpect(content().string(containsString("zzz")))
    }
}
