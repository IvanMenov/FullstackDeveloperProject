package com.respiroc.greg.fullstackdeveloperproject.repository

import com.respiroc.greg.fullstackdeveloperproject.model.Product
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository

@Repository
class ProductRepository(
	private val jdbcClient: JdbcClient
) {

    fun save(product: Product): Product {
        val productId: Long = jdbcClient
            .sql(
                """
                INSERT INTO products(title, vendor, product_type)
                VALUES (:title, :vendor, :productType)
                RETURNING id
                """
            )
            .param("title", product.title)
            .param("vendor", product.vendor)
            .param("productType", product.productType)
            .query(Long::class.java)
            .single()

        return product.copy(id = productId)
    }

    fun findAll(): List<Product> {
        return jdbcClient.sql("SELECT id, title, vendor, product_type FROM products ORDER BY id LIMIT 50")
            .query { rs, _ ->
                Product(
                    id = rs.getLong("id"),
                    title = rs.getString("title"),
                    vendor = rs.getString("vendor"),
                    productType = rs.getString("product_type"),
                    productVariants = emptyList()
                )
            }
            .list()
    }

    fun findPage(offset: Int, size: Int): List<Product> {
        return jdbcClient.sql("SELECT id, title, vendor, product_type FROM products ORDER BY id LIMIT :size OFFSET :offset")
            .param("size", size)
            .param("offset", offset)
            .query { rs, _ ->
                Product(
                    id = rs.getLong("id"),
                    title = rs.getString("title"),
                    vendor = rs.getString("vendor"),
                    productType = rs.getString("product_type"),
                    productVariants = emptyList()
                )
            }
            .list()
    }

    fun findPageFiltered(offset: Int, size: Int, q: String): List<Product> {
        val like = "%$q%"
        return jdbcClient.sql(
            """
            SELECT id, title, vendor, product_type
            FROM products
            WHERE title ILIKE :like
            ORDER BY id
            LIMIT :size OFFSET :offset
            """.trimIndent()
        )
            .param("like", like)
            .param("size", size)
            .param("offset", offset)
            .query { rs, _ ->
                Product(
                    id = rs.getLong("id"),
                    title = rs.getString("title"),
                    vendor = rs.getString("vendor"),
                    productType = rs.getString("product_type"),
                    productVariants = emptyList()
                )
            }
            .list()
    }

    fun findById(id: Long): Product? {
        return jdbcClient.sql("SELECT id, title, vendor, product_type FROM products WHERE id = ?")
            .param(id)
            .query { rs, _ ->
                Product(
                    id = rs.getLong("id"),
                    title = rs.getString("title"),
                    vendor = rs.getString("vendor"),
                    productType = rs.getString("product_type"),
                    productVariants = emptyList()
                )
            }
            .optional()
            .orElse(null)
    }

    fun update(id: Long, title: String, vendor: String, productType: String?): Int {
        return jdbcClient.sql(
            """
            UPDATE products
            SET title = :title, vendor = :vendor, product_type = :productType
            WHERE id = :id
            """.trimIndent()
        )
            .param("title", title)
            .param("vendor", vendor)
            .param("productType", productType)
            .param("id", id)
            .update()
    }

    fun deleteById(id: Long): Int {
        return jdbcClient.sql("DELETE FROM products WHERE id = ?")
            .param(id)
            .update()
    }

	fun deleteAll() {
		jdbcClient.sql("DELETE FROM product_variants").update()
	}
	
	fun count(): Long {
		return jdbcClient.sql("SELECT COUNT(id) FROM products")
			.query(Long::class.java)
			.single()
	}

    fun countFiltered(q: String): Long {
        val like = "%$q%"
        return jdbcClient.sql(
            """
            SELECT COUNT(id)
            FROM products
            WHERE title ILIKE :like
            """.trimIndent()
        )
            .param("like", like)
            .query(Long::class.java)
            .single()
    }
}
