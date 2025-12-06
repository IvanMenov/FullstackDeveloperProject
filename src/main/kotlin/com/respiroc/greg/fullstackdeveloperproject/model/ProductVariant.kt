package com.respiroc.greg.fullstackdeveloperproject.model

import java.math.BigDecimal


data class ProductVariant(
    val id: Long = 0,
    val colorOption: String,
    val sizeOption: String? = null,
    val price: BigDecimal,
    val available: Boolean = true,
    val productId: Long? = null
)
