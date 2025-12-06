package com.respiroc.greg.fullstackdeveloperproject.repository

import com.respiroc.greg.fullstackdeveloperproject.model.Product
import com.respiroc.greg.fullstackdeveloperproject.testutil.AbstractIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate

class ProductRepositoryTest : AbstractIntegrationTest() {

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
    fun `save and findById`() {
        val saved = productRepository.save(Product(title = "T-Shirt", vendor = "Acme", productType = "Clothes"))
        val found = productRepository.findById(saved.id)
        assertThat(found).isNotNull
        assertThat(found!!.title).isEqualTo("T-Shirt")
        assertThat(found.vendor).isEqualTo("Acme")
        assertThat(found.productType).isEqualTo("Clothes")
    }

    @Test
    fun `findAll ordered by id`() {
        val p1 = productRepository.save(Product(title = "A", vendor = "V1"))
        val p2 = productRepository.save(Product(title = "B", vendor = "V2"))
        val p3 = productRepository.save(Product(title = "C", vendor = "V3"))

        val all = productRepository.findAll()
        assertThat(all.map { it.id }).containsExactly(p1.id, p2.id, p3.id)
    }

    @Test
    fun `update and delete`() {
        val saved = productRepository.save(Product(title = "Old", vendor = "Ven", productType = null))
        val updatedCount = productRepository.update(saved.id, "New", "Ven2", "TypeX")
        assertThat(updatedCount).isEqualTo(1)

        val after = productRepository.findById(saved.id)
        assertThat(after!!.title).isEqualTo("New")
        assertThat(after.vendor).isEqualTo("Ven2")
        assertThat(after.productType).isEqualTo("TypeX")

        val del = productRepository.deleteById(saved.id)
        assertThat(del).isEqualTo(1)
        val missing = productRepository.findById(saved.id)
        assertThat(missing).isNull()
    }
}
