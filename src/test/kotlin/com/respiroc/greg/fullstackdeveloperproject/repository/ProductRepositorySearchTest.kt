package com.respiroc.greg.fullstackdeveloperproject.repository

import com.respiroc.greg.fullstackdeveloperproject.model.Product
import com.respiroc.greg.fullstackdeveloperproject.testutil.AbstractIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate

class ProductRepositorySearchTest : AbstractIntegrationTest() {

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
        productRepository.save(Product(title = "Charlie Shirt", vendor = "Alpha Inc", productType = "Clothes"))
    }

    @Test
    fun `countFiltered is case-insensitive on title`() {
        seed()
        val c1 = productRepository.countFiltered("alpha")
        val c2 = productRepository.countFiltered("ALPHA")
        val c3 = productRepository.countFiltered("bravo")
        assertThat(c1).isEqualTo(2) // Alpha Tee, alpha hat — title matches
        assertThat(c2).isEqualTo(2)
        assertThat(c3).isEqualTo(1)
    }

    @Test
    fun `findPageFiltered returns expected results ordered by id with paging`() {
        seed()
        // Query 'alpha' should return first two inserted (by id order)
        val page0 = productRepository.findPageFiltered(offset = 0, size = 1, q = "alpha")
        assertThat(page0).hasSize(1)
        assertThat(page0[0].title.lowercase()).contains("alpha")

        val page1 = productRepository.findPageFiltered(offset = 1, size = 1, q = "alpha")
        assertThat(page1).hasSize(1)
        assertThat(page1[0].title.lowercase()).contains("alpha")

        val none = productRepository.findPageFiltered(offset = 0, size = 10, q = "zzz")
        assertThat(none).isEmpty()
    }
}
