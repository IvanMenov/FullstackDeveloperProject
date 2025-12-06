package com.respiroc.greg.fullstackdeveloperproject.model

data class Product(
	val id: Long = 0,
	val title: String,
	val vendor: String,
	val productType: String? = null,
    val productVariants : List<ProductVariant>  = emptyList()
)
