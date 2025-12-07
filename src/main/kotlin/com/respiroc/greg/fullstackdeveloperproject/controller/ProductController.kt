package com.respiroc.greg.fullstackdeveloperproject.controller

import com.respiroc.greg.fullstackdeveloperproject.service.ProductService
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import jakarta.servlet.http.HttpServletResponse
import java.math.BigDecimal

@Controller
class ProductController(
    private val productService: ProductService
) {

    @GetMapping("/")
    fun root(): String {
        return "redirect:/products"
    }

    @GetMapping("/products")
    fun index(model: Model): String {
        return "index"
    }

    @GetMapping("/products/table")
    fun getProductTable(
        model: Model,
        @RequestParam(name = "page", required = false, defaultValue = "0") page: Int,
        @RequestParam(name = "q", required = false) q: String?
    ): String {
        val result = productService.findPage(page, 35, q)
        model.addAttribute("products", result.items)
        model.addAttribute("page", result.page)
        model.addAttribute("size", result.size)
        model.addAttribute("totalPages", result.totalPages)
        model.addAttribute("hasPrev", result.hasPrev)
        model.addAttribute("hasNext", result.hasNext)
        model.addAttribute("query", q?.trim() ?: "")
        return "fragments/product-table :: table"
    }

    @GetMapping("/products/{id}/variants")
    fun getProductVariants(@PathVariable("id") id: Long, model: Model): String {
        val variants = productService.findVariantsByProductId(id)
        model.addAttribute("variants", variants)
        model.addAttribute("productId", id)
        return "fragments/product-variants :: variants"
    }

    @PostMapping("/products")
    fun addProduct(
        @RequestParam title: String,
        @RequestParam vendor: String,
        @RequestParam productType: String,
        model: Model,
        response: HttpServletResponse
    ): String {
        return try {
            productService.createProduct(title, vendor, productType)
            // Notify client via HTMX event
            response.setHeader("HX-Trigger", "{\"productAdded\": { \"message\": \"Product created successfully\" }}")
            // Return the updated table (reset to first page)
            getProductTable(model, 0, null)
        } catch (ex: Exception) {
            response.status = 400
            response.setHeader("HX-Reswap", "none")
            val message = (ex.message ?: "Failed to create product")
            response.setHeader("HX-Trigger", "{\"productAddFailed\": { \"message\": \"" + message.replace("\"", "'") + "\" }}")
            "fragments/product-table :: table"
        }
    }

    @GetMapping("/products/{id}/edit")
    fun editProduct(@PathVariable("id") id: Long, model: Model): String {
        val product = productService.findProductById(id)
        model.addAttribute("product", product)
        return "fragments/product-edit-form :: form"
    }

    @PostMapping("/products/{id}/update")
    fun updateProduct(
        @PathVariable("id") id: Long,
        @RequestParam title: String,
        @RequestParam vendor: String,
        @RequestParam(required = false) productType: String?,
        model: Model,
        response: HttpServletResponse
    ): String {
        return try {
            productService.updateProduct(id, title, vendor, productType)
            response.setHeader("HX-Trigger", "{\"productUpdated\": { \"message\": \"Product updated successfully\" }}")
            getProductTable(model, 0, null)
        } catch (ex: Exception) {
            response.status = 400
            response.setHeader("HX-Reswap", "none")
            val message = (ex.message ?: "Failed to update product")
            response.setHeader("HX-Trigger", "{\"productUpdateFailed\": { \"message\": \"" + message.replace("\"", "'") + "\" }}")
            "fragments/product-table :: table"
        }
    }

    @PostMapping("/products/{id}/delete")
    fun deleteProduct(
        @PathVariable("id") id: Long,
        model: Model
    ): String {
        productService.deleteProduct(id)
        return getProductTable(model, 0, null)
    }

    // --- Variant CRUD ---
    @GetMapping("/products/{productId}/variants/new")
    fun newVariant(@PathVariable("productId") productId: Long, model: Model): String {
        model.addAttribute("mode", "create")
        model.addAttribute("productId", productId)
        return "fragments/variant-form :: form"
    }

    @GetMapping("/products/{productId}/variants/{variantId}/edit")
    fun editVariant(
        @PathVariable("productId") productId: Long,
        @PathVariable("variantId") variantId: Long,
        model: Model
    ): String {
        val variant = productService.findVariantById(variantId)
        model.addAttribute("mode", "edit")
        model.addAttribute("variant", variant)
        model.addAttribute("productId", productId)
        return "fragments/variant-form :: form"
    }

    @PostMapping("/products/{productId}/variants")
    fun createVariant(
        @PathVariable("productId") productId: Long,
        @RequestParam("color") color: String,
        @RequestParam("size", required = false) size: String?,
        @RequestParam("price") price: BigDecimal,
        @RequestParam(value = "available", required = false, defaultValue = "false") available: Boolean,
        model: Model,
        response: HttpServletResponse
    ): String {
        return try {
            productService.createVariant(productId, color, size, price, available)
            response.setHeader("HX-Trigger", "{\"variantCreated\": { \"message\": \"Variant created successfully\" }}")
            // Return refreshed variants list
            val variants = productService.findVariantsByProductId(productId)
            model.addAttribute("variants", variants)
            model.addAttribute("productId", productId)
            "fragments/product-variants :: variants"
        } catch (ex: Exception) {
            response.status = 400
            response.setHeader("HX-Reswap", "none")
            val message = (ex.message ?: "Failed to create variant")
            response.setHeader("HX-Trigger", "{\"variantCreateFailed\": { \"message\": \"" + message.replace("\"", "'") + "\" }}")
            "fragments/product-variants :: variants"
        }
    }

    @PostMapping("/products/{productId}/variants/{variantId}/update")
    fun updateVariant(
        @PathVariable("productId") productId: Long,
        @PathVariable("variantId") variantId: Long,
        @RequestParam("color") color: String,
        @RequestParam("size", required = false) size: String?,
        @RequestParam("price") price: BigDecimal,
        @RequestParam(value = "available", required = false, defaultValue = "false") available: Boolean,
        model: Model,
        response: HttpServletResponse
    ): String {
        return try {
            productService.updateVariant(variantId, color, size, price, available)
            response.setHeader("HX-Trigger", "{\"variantUpdated\": { \"message\": \"Variant updated successfully\" }}")
            val variants = productService.findVariantsByProductId(productId)
            model.addAttribute("variants", variants)
            model.addAttribute("productId", productId)
            "fragments/product-variants :: variants"
        } catch (ex: Exception) {
            response.status = 400
            response.setHeader("HX-Reswap", "none")
            val message = (ex.message ?: "Failed to update variant")
            response.setHeader("HX-Trigger", "{\"variantUpdateFailed\": { \"message\": \"" + message.replace("\"", "'") + "\" }}")
            "fragments/product-variants :: variants"
        }
    }

    @PostMapping("/products/{productId}/variants/{variantId}/delete")
    fun deleteVariant(
        @PathVariable("productId") productId: Long,
        @PathVariable("variantId") variantId: Long,
        model: Model
    ): String {
        productService.deleteVariant(variantId)
        // reload variants for the product and return the fragment
        val variants = productService.findVariantsByProductId(productId)
        model.addAttribute("variants", variants)
        model.addAttribute("productId", productId)
        return "fragments/product-variants :: variants"
    }
}

