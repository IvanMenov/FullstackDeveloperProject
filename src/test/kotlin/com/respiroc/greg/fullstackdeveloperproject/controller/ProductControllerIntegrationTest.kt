package com.respiroc.greg.fullstackdeveloperproject.controller

import com.respiroc.greg.fullstackdeveloperproject.model.Product
import com.respiroc.greg.fullstackdeveloperproject.repository.ProductRepository
import com.respiroc.greg.fullstackdeveloperproject.testutil.AbstractIntegrationTest
import org.hamcrest.Matchers
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@AutoConfigureMockMvc
class ProductControllerIntegrationTest : AbstractIntegrationTest() {

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


    @Test
    fun `POST create product adds row and returns updated table`() {
        mockMvc.perform(
            post("/products")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("title", "Beta Cap")
                .param("vendor", "BrandCo")
                .param("productType", "Hats")
        )
            .andExpect(status().isOk)
           //
        mockMvc.perform(get("/products/table"))
            .andExpect(status().isOk)
            .andExpect(content().string(Matchers.containsString("<table")))
            .andExpect(content().string(Matchers.containsString("<td>Beta Cap</td>")))
            .andExpect(content().string(Matchers.containsString("<td>BrandCo</td>")))
            .andExpect(content().string(Matchers.containsString("<td>Hats</td>")))
    }

    @Test
    fun `Variants flow - create and list`() {
        val product = productRepository.save(Product(title = "Base", vendor = "Vendor"))

        mockMvc.perform(
            post("/products/${product.id}/variants")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("color", "Red")
                .param("size", "M")
                .param("price", "19.99")
                .param("available", "true")
        )
            .andExpect(status().isOk)
            .andExpect(content().string(Matchers.containsString("Red")))

        mockMvc.perform(get("/products/${product.id}/variants"))
            .andExpect(status().isOk)
            .andExpect(content().string(Matchers.containsString("Red")))
            .andExpect(content().string(Matchers.containsString("M")))
    }
}
