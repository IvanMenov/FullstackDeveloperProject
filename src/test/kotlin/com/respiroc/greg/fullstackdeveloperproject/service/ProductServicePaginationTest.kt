package com.respiroc.greg.fullstackdeveloperproject.service

import com.respiroc.greg.fullstackdeveloperproject.model.Product
import com.respiroc.greg.fullstackdeveloperproject.repository.ProductRepository
import com.respiroc.greg.fullstackdeveloperproject.testutil.AbstractIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.CacheManager
import org.springframework.jdbc.core.JdbcTemplate

class ProductServicePaginationTest : AbstractIntegrationTest() {

    @Autowired
    lateinit var productService: ProductService

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
    fun `page size is capped at 20 and navigation flags are correct`() {
        seedProducts(70) // 3 pages with size 20 + 1 page with size 10

        val p0 = productService.findPage(0, 100)
        assertThat(p0.size).isEqualTo(20)
        assertThat(p0.page).isEqualTo(0)
        assertThat(p0.totalItems).isEqualTo(70)
        assertThat(p0.totalPages).isEqualTo(4)
        assertThat(p0.hasPrev).isFalse()
        assertThat(p0.hasNext).isTrue()
        assertThat(p0.items).hasSize(20)
        assertThat(p0.items.first().title).isEqualTo("P0001")
        assertThat(p0.items.last().title).isEqualTo("P0020")

        val p1 = productService.findPage(1, 20)
        assertThat(p1.size).isEqualTo(20)
        assertThat(p1.page).isEqualTo(1)
        assertThat(p1.hasPrev).isTrue()
        assertThat(p1.hasNext).isTrue()
        assertThat(p1.items).hasSize(20)
        assertThat(p1.items.first().title).isEqualTo("P0021")
        assertThat(p1.items.last().title).isEqualTo("P0040")

        val p2 = productService.findPage(2, 20)
        assertThat(p2.size).isEqualTo(20)
        assertThat(p2.page).isEqualTo(2)
        assertThat(p2.hasPrev).isTrue()
        assertThat(p2.hasNext).isTrue()
        assertThat(p2.items).hasSize(20)
        assertThat(p2.items.first().title).isEqualTo("P0041")
        assertThat(p2.items.last().title).isEqualTo("P0060")

        val p3 = productService.findPage(3, 20)
        assertThat(p3.size).isEqualTo(20)
        assertThat(p3.page).isEqualTo(3)
        assertThat(p3.hasPrev).isTrue()
        assertThat(p3.hasNext).isFalse()
        assertThat(p3.items).hasSize(10)
        assertThat(p3.items.first().title).isEqualTo("P0061")
        assertThat(p3.items.last().title).isEqualTo("P0070")
    }

    @Test
    fun `page index is clamped within range`() {
        seedProducts(10) // totalPages = 1 when size=35

        val neg = productService.findPage(-5, 35)
        assertThat(neg.page).isEqualTo(0)
        assertThat(neg.hasPrev).isFalse()
        assertThat(neg.hasNext).isFalse()
        assertThat(neg.totalPages).isEqualTo(1)
        assertThat(neg.items).hasSize(10)

        val overflow = productService.findPage(999, 35)
        assertThat(overflow.page).isEqualTo(0) // only one page exists
        assertThat(overflow.totalPages).isEqualTo(1)
        assertThat(overflow.items).hasSize(10)
    }
}
