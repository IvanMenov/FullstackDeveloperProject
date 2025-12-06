package com.respiroc.greg.fullstackdeveloperproject.repository

import com.respiroc.greg.fullstackdeveloperproject.model.Product
import com.respiroc.greg.fullstackdeveloperproject.model.ProductVariant
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository

@Repository
class ProductVariantRepository(private val jdbcClient: JdbcClient) {

    fun save(productVariants: List<ProductVariant>, productId: Long): Int {
        val sql = """
        INSERT INTO product_variants
        (product_id, color_option, size_option, price, available)
        VALUES (:productId, :colorOption, :sizeOption, :price, :available)
        """.trimIndent()

        var total = 0
        for (v in productVariants) {
            val updated = jdbcClient.sql(sql)
                .param("productId", productId)
                .param("colorOption", v.colorOption)
                .param("sizeOption", v.sizeOption)
                .param("price", v.price)
                .param("available", v.available)
                .update()
            total += updated
        }
        return total
    }

    fun saveOne(variant: ProductVariant, productId: Long): Long {
        return jdbcClient.sql(
            """
            INSERT INTO product_variants (product_id, color_option, size_option, price, available)
            VALUES (:productId, :colorOption, :sizeOption, :price, :available)
            RETURNING id
            """.trimIndent()
        )
            .param("productId", productId)
            .param("colorOption", variant.colorOption)
            .param("sizeOption", variant.sizeOption)
            .param("price", variant.price)
            .param("available", variant.available)
            .query(Long::class.java)
            .single()
    }


    fun getAllVariantsForProductId(productId: Long): List<ProductVariant> {
        return jdbcClient.sql(
            """
                SELECT pv.color_option, pv.size_option, pv.available, pv.price, pv.id, pv.product_id
                FROM product_variants pv
                WHERE pv.product_id = :productId
                order by pv.id
            """.trimIndent()
        )
            .param("productId", productId)
            .query(ProductVariant::class.java)
            .list()
    }

    fun findById(id: Long): ProductVariant? {
        return jdbcClient.sql(
            """
            SELECT id, product_id, color_option, size_option, price, available
            FROM product_variants WHERE id = ?
            """.trimIndent()
        )
            .param(id)
            .query(ProductVariant::class.java)
            .optional()
            .orElse(null)
    }

    fun update(variant: ProductVariant): Int {
        return jdbcClient.sql(
            """
            UPDATE product_variants
            SET color_option = :colorOption, size_option = :sizeOption, price = :price, available = :available
            WHERE id = :id
            """.trimIndent()
        )
            .param("colorOption", variant.colorOption)
            .param("sizeOption", variant.sizeOption)
            .param("price", variant.price)
            .param("available", variant.available)
            .param("id", variant.id)
            .update()
    }

    fun deleteById(id: Long): Int {
        return jdbcClient.sql("DELETE FROM product_variants WHERE id = ?")
            .param(id)
            .update()
    }

    fun deleteByProductId(productId: Long): Int {
        return jdbcClient.sql("DELETE FROM product_variants WHERE product_id = ?")
            .param(productId)
            .update()
    }

    fun deleteAll() {
        jdbcClient.sql("DELETE FROM product_variants").update()
    }
    fun count(): Long {
        return jdbcClient.sql("SELECT COUNT(id) FROM product_variants")
            .query(Long::class.java)
            .single()
    }
}