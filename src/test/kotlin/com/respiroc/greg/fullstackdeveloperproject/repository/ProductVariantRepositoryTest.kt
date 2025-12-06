package com.respiroc.greg.fullstackdeveloperproject.repository

import com.respiroc.greg.fullstackdeveloperproject.model.Product
import com.respiroc.greg.fullstackdeveloperproject.model.ProductVariant
import com.respiroc.greg.fullstackdeveloperproject.testutil.AbstractIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import java.math.BigDecimal

class ProductVariantRepositoryTest : AbstractIntegrationTest() {

    @Autowired
    lateinit var productRepository: ProductRepository

    @Autowired
    lateinit var productVariantRepository: ProductVariantRepository

    @Autowired
    lateinit var jdbcTemplate: JdbcTemplate

    private fun newProduct(title: String = "Base", vendor: String = "Vendor"): Long {
        return productRepository.save(Product(title = title, vendor = vendor)).id
    }

    @BeforeEach
    fun clean() {
        jdbcTemplate.execute("DELETE FROM product_variants")
        jdbcTemplate.execute("DELETE FROM products")
    }

    @Test
    fun `saveOne and getAllVariantsForProductId`() {
        val pid = newProduct("Shirt", "Acme")
        val id1 = productVariantRepository.saveOne(
            ProductVariant(colorOption = "Red", sizeOption = "M", price = BigDecimal("19.99"), available = true, productId = pid),
            pid
        )
        val id2 = productVariantRepository.saveOne(
            ProductVariant(colorOption = "Blue", sizeOption = null, price = BigDecimal("17.50"), available = false, productId = pid),
            pid
        )

        val all = productVariantRepository.getAllVariantsForProductId(pid)
        assertThat(all).hasSize(2)
        assertThat(all.map { it.id }).containsExactly(id1, id2)
        assertThat(all[0].colorOption).isEqualTo("Red")
        assertThat(all[1].available).isFalse()
    }

    @Test
    fun `update and deletes`() {
        val pid = newProduct()
        val id = productVariantRepository.saveOne(
            ProductVariant(colorOption = "Green", sizeOption = "L", price = BigDecimal("25.00"), available = true, productId = pid),
            pid
        )
        var one = productVariantRepository.findById(id)!!
        val updated = one.copy(colorOption = "Dark Green", available = false)
        val cnt = productVariantRepository.update(updated)
        assertThat(cnt).isEqualTo(1)
        one = productVariantRepository.findById(id)!!
        assertThat(one.colorOption).isEqualTo("Dark Green")
        assertThat(one.available).isFalse()

        // deleteById
        val del1 = productVariantRepository.deleteById(id)
        assertThat(del1).isEqualTo(1)
        assertThat(productVariantRepository.findById(id)).isNull()

        // deleteByProductId on another product with 2 variants
        val pid2 = newProduct("P2", "V2")
        productVariantRepository.saveOne(ProductVariant(colorOption = "Black", sizeOption = null, price = BigDecimal("10.00"), available = true, productId = pid2), pid2)
        productVariantRepository.saveOne(ProductVariant(colorOption = "White", sizeOption = null, price = BigDecimal("11.00"), available = true, productId = pid2), pid2)
        val del2 = productVariantRepository.deleteByProductId(pid2)
        assertThat(del2).isEqualTo(2)
        assertThat(productVariantRepository.getAllVariantsForProductId(pid2)).isEmpty()
    }
}
