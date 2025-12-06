package com.respiroc.greg.fullstackdeveloperproject.service

import com.respiroc.greg.fullstackdeveloperproject.model.Product
import com.respiroc.greg.fullstackdeveloperproject.repository.ProductRepository
import com.respiroc.greg.fullstackdeveloperproject.testutil.AbstractIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate

class ProductServiceSearchTest : AbstractIntegrationTest() {

    @Autowired
    lateinit var productService: ProductService

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
    fun `findPage with query filters results and computes pagination`() {
        seed()
        val res = productService.findPage(page = 0, requestedSize = 35, query = "alpha")
        assertThat(res.totalItems).isEqualTo(2)
        assertThat(res.totalPages).isEqualTo(1)
        assertThat(res.items).hasSize(2)
        assertThat(res.items.map { it.title }).allMatch { it.contains("Alpha", ignoreCase = true) }
        assertThat(res.hasPrev).isFalse()
        assertThat(res.hasNext).isFalse()
    }

    @Test
    fun `blank query falls back to unfiltered`() {
        seed()
        val res = productService.findPage(page = 0, requestedSize = 35, query = "   ")
        assertThat(res.totalItems).isEqualTo(3)
        assertThat(res.items).hasSize(3)
    }
}
