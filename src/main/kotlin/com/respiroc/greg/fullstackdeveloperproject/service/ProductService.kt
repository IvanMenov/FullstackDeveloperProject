package com.respiroc.greg.fullstackdeveloperproject.service

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.ObjectMapper
import com.respiroc.greg.fullstackdeveloperproject.model.Product
import com.respiroc.greg.fullstackdeveloperproject.model.ProductVariant
import com.respiroc.greg.fullstackdeveloperproject.repository.ProductRepository
import com.respiroc.greg.fullstackdeveloperproject.repository.ProductVariantRepository
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.cache.annotation.Caching
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.core.io.Resource
import org.springframework.transaction.annotation.Transactional
import java.io.InputStream
import java.math.BigDecimal
import kotlin.String

@Service
class ProductService(
    private val productRepository: ProductRepository,
    private val productVariantRepository: ProductVariantRepository,
    private val restTemplate: RestTemplate,
    private val objectMapper: ObjectMapper
) {

    data class PageResult<T>(
        val items: List<T>,
        val page: Int,
        val size: Int,
        val totalItems: Long,
        val totalPages: Int,
        val hasPrev: Boolean,
        val hasNext: Boolean
    )

    private val logger = LoggerFactory.getLogger(ProductService::class.java)
    private val jsonFactory = JsonFactory(objectMapper)

    fun findAll(): List<Product> {
        return productRepository.findAll()
    }

    fun findPage(page: Int, requestedSize: Int = 35, query: String? = null): PageResult<Product> {
        val size = requestedSize.coerceIn(1, 35)
        val q = query?.trim().orEmpty()
        val filtered = q.isNotEmpty()
        val total = if (filtered) productRepository.countFiltered(q) else productRepository.count()
        if (total == 0L) {
            return PageResult(emptyList(), 0, size, 0, 0, hasPrev = false, hasNext = false)
        }
        val totalPages = ((total + size - 1) / size).toInt()
        val safePage = page.coerceIn(0, totalPages - 1)
        val offset = safePage * size
        val items = if (filtered) productRepository.findPageFiltered(offset, size, q) else productRepository.findPage(offset, size)
        val hasPrev = safePage > 0
        val hasNext = safePage < totalPages - 1
        return PageResult(items, safePage, size, total, totalPages, hasPrev, hasNext)
    }

    @Cacheable(value = ["products"], key = "#id")
    fun findProductById(id: Long): Product? = productRepository.findById(id)

    @Caching(evict = [
        CacheEvict(value = ["products"], key = "#id"),
        CacheEvict(value = ["productPages"], allEntries = true)
    ])
    fun updateProduct(id: Long, title: String, vendor: String, productType: String?): Product {
        require(title.isNotBlank()) { "Title must not be empty" }
        require(vendor.isNotBlank()) { "Vendor must not be empty" }
        val updated = productRepository.update(id, title, vendor, productType)
        if (updated <= 0) throw IllegalArgumentException("Product not found: $id")
        return productRepository.findById(id) ?: throw IllegalStateException("Failed to load updated product")
    }

    @Cacheable(value = ["productVariants"], key = "#productId")
    fun findVariantsByProductId(productId: Long): List<ProductVariant> {
        return productVariantRepository.getAllVariantsForProductId(productId)
    }

    @Cacheable(value = ["productVariants"], key = "'variant:' + #id")
    fun findVariantById(id: Long): ProductVariant? = productVariantRepository.findById(id)

    @CacheEvict(value = ["productVariants"], key = "#productId")
    fun createVariant(
        productId: Long,
        colorOption: String,
        sizeOption: String?,
        price: BigDecimal,
        available: Boolean
    ): ProductVariant {
        require(colorOption.isNotBlank()) { "Color must not be empty" }
        require(price >= BigDecimal.ZERO) { "Price must be >= 0" }
        val id = productVariantRepository.saveOne(
            ProductVariant(
                colorOption = colorOption,
                sizeOption = sizeOption,
                price = price,
                available = available,
                productId = productId
            ),
            productId
        )
        return productVariantRepository.findById(id)!!
    }

    @CacheEvict(value = ["productVariants"], allEntries = true)
    fun updateVariant(
        variantId: Long,
        colorOption: String,
        sizeOption: String?,
        price: BigDecimal,
        available: Boolean
    ): ProductVariant {
        require(colorOption.isNotBlank()) { "Color must not be empty" }
        require(price >= BigDecimal.ZERO) { "Price must be >= 0" }
        val existing = productVariantRepository.findById(variantId)
            ?: throw IllegalArgumentException("Variant not found: $variantId")
        val updated = existing.copy(
            colorOption = colorOption,
            sizeOption = sizeOption,
            price = price,
            available = available
        )
        val count = productVariantRepository.update(updated)
        if (count <= 0) throw IllegalStateException("Failed to update variant $variantId")
        return productVariantRepository.findById(variantId)!!
    }

    @Caching(evict = [
        CacheEvict(value = ["products"], key = "#productId"),
        CacheEvict(value = ["productVariants", "productPages"], allEntries = true)
    ])
    @Transactional
    fun deleteProduct(productId: Long) {
        // Rely on ON DELETE CASCADE for variants, but ensure explicit variant cleanup just in case
        try {
            productVariantRepository.deleteByProductId(productId)
        } catch (e: Exception) {
            logger.warn("Variant cleanup failed (may be safe due to cascade): ${e.message}")
        }
        productRepository.deleteById(productId)
    }

    @CacheEvict(value = ["productVariants"], allEntries = true)
    fun deleteVariant(variantId: Long) {
        productVariantRepository.deleteById(variantId)
    }

    @CacheEvict(value = ["productPages"], allEntries = true)
    fun createProduct(
        title: String,
        vendor: String,
        productType: String,
    ): Product {
        logger.info("Creating new product: $title")

        val product = Product(
            title = title,
            vendor = vendor,
            productType = productType
        )

        return productRepository.save(product)
    }

    fun fetchAndSaveProducts() {
        logger.info("Starting product fetch job")

        try {
            val url = "https://famme.no/products.json"
            val response = restTemplate.getForEntity(url, Resource::class.java)
            val resource = response.body ?: return
            val inputStream = resource.inputStream

            // Parse only first 50 products using streaming
            val products = parseJsonFromStream(inputStream, 50)
            logger.info("Fetched ${products.size} products from API")
            saveProductsAndVariants(products)

        } catch (e: Exception) {
            logger.error("Error fetching products from API: ${e.message}", e)
        }
    }

    @Transactional
    fun saveProductsAndVariants(products: List<Product>) {
        var savedCount = 0
        var variantCount = 0

        products.forEach { product ->
            try {
                val savedProduct = productRepository.save(product)
                savedCount++
                if (product.productVariants.isNotEmpty()) {
                    variantCount = productVariantRepository.save(product.productVariants, savedProduct.id)
                }
            } catch (e: Exception) {
                logger.error("Error saving products / variants :${e.message}", e)
            }
        }

        logger.info("Successfully saved $savedCount products and $variantCount variants")
    }

    private fun parseJsonFromStream(inputStream: InputStream, limit: Int): List<Product> {
        val products = mutableListOf<Product>()
        val parser = jsonFactory.createParser(inputStream)

        try {
            // Find the "products" array
            while (parser.nextToken() != JsonToken.END_OBJECT) {
                val fieldName = parser.currentName()
                if (fieldName == "products" && parser.nextToken() == JsonToken.START_ARRAY) {
                    // Parse products until we reach the limit or end of array
                    while (parser.nextToken() != JsonToken.END_ARRAY && products.size < limit) {
                        if (parser.currentToken == JsonToken.START_OBJECT) {
                            val product = parseProduct(parser)
                            product?.let { products.add(it) }
                        }
                    }
                    // Stop parsing once we have enough products
                    break
                }
            }
        } finally {
            parser.close()
            inputStream.close()
        }

        return products
    }

    private fun parseProduct(parser: JsonParser): Product? {
        var title: String? = null
        var vendor: String? = null
        var productType: String? = null
        var variants: List<ProductVariant>? = null

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            when (parser.currentName()) {
                "title" -> {
                    parser.nextToken()
                    title = parser.text
                }

                "vendor" -> {
                    parser.nextToken()
                    vendor = parser.text
                }

                "product_type" -> {
                    parser.nextToken()
                    productType = parser.text
                }

                "variants" -> {
                    parser.nextToken()
                    if (parser.currentToken == JsonToken.START_ARRAY) {
                        variants = mutableListOf()
                        while (parser.nextToken() != JsonToken.END_ARRAY) {
                            if (parser.currentToken == JsonToken.START_OBJECT) {
                                parseVariant(parser)?.let { variants.add(it) }
                            }
                        }
                    }
                }

                else -> {
                    // Skip unknown fields
                    parser.nextToken()
                    parser.skipChildren()
                }
            }
        }

        return if (vendor != null && title != null && variants != null) {
            Product(title = title, vendor = vendor, productType = productType, productVariants = variants)
        } else {
            null
        }
    }

    private fun parseVariant(parser: JsonParser): ProductVariant? {
        var price: BigDecimal? = null
        var available: Boolean? = null
        var colorOption: String? = null
        var sizeOption: String? = null
        var option2: String? = null
        var option3: String? = null

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            when (parser.currentName()) {
                "price" -> {
                    parser.nextToken()
                    price = parser.text.toBigDecimal()
                }

                "available" -> {
                    parser.nextToken()
                    available = parser.valueAsBoolean
                }

                "option1" -> {
                    parser.nextToken()
                    colorOption = parser.text
                }

                "option2" -> {
                    parser.nextToken()
                    option2 = parser.text
                }

                "option3" -> {
                    parser.nextToken()
                    option3 = parser.text
                }

                else -> {
                    // Skip unknown fields
                    parser.nextToken()
                    parser.skipChildren()
                }
            }
        }

        return if (price != null && available != null && colorOption != null) {
            sizeOption = getValidSizeOption(option2, option3)
            ProductVariant(colorOption = colorOption, sizeOption = sizeOption, price = price, available = available)
        } else {
            null
        }
    }

    private fun getValidSizeOption(option2: String?, option3: String?): String {
        if(option3 != null && option3.isNotBlank() && option3 != "null") {
            return option3
        }
        if(option2 != null && option2.isNotBlank() && option2 != "null") {
            return option2
        }
        return "N/A"
    }
}

