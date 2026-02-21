package com.example.demo.controller

import com.example.demo.config.CoroutineManager
import com.example.demo.config.CoroutineStatus
import com.example.demo.model.Product
import com.example.demo.service.ProductService
import jakarta.validation.Valid
import kotlinx.coroutines.flow.Flow
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

/**
 * REST controller exposing CRUD endpoints for [Product] resources.
 *
 * All handler methods are `suspend` functions, allowing Spring WebFlux to
 * bridge the coroutine execution model transparently.
 *
 * Additionally, `/coroutines` endpoints allow runtime control of coroutine behaviour.
 */
@RestController
@RequestMapping("/api")
class ProductController(
    private val productService: ProductService,
    private val coroutineManager: CoroutineManager
) {

    // ── Product CRUD ───────────────────────────────────────────────────────────

    @GetMapping("/products")
    suspend fun getAllProducts(): Flow<Product> =
        productService.findAll()

    @GetMapping("/products/{id}")
    suspend fun getProduct(@PathVariable id: Long): ResponseEntity<Product> {
        val product = productService.findById(id)
        return if (product != null) ResponseEntity.ok(product)
        else ResponseEntity.notFound().build()
    }

    @PostMapping("/products")
    @ResponseStatus(HttpStatus.CREATED)
    suspend fun createProduct(@Valid @RequestBody product: Product): Product =
        productService.create(product)

    @PutMapping("/products/{id}")
    suspend fun updateProduct(
        @PathVariable id: Long,
        @Valid @RequestBody product: Product
    ): ResponseEntity<Product> {
        val updated = productService.update(id, product)
        return if (updated != null) ResponseEntity.ok(updated)
        else ResponseEntity.notFound().build()
    }

    @DeleteMapping("/products/{id}")
    suspend fun deleteProduct(@PathVariable id: Long): ResponseEntity<Void> {
        val deleted = productService.delete(id)
        return if (deleted) ResponseEntity.noContent().build()
        else ResponseEntity.notFound().build()
    }

    // ── Coroutine management ───────────────────────────────────────────────────

    /** Returns the current coroutine execution mode. */
    @GetMapping("/coroutines")
    fun getCoroutineStatus(): CoroutineStatus = coroutineManager.status()

    /** Enables coroutine-based async execution. */
    @PostMapping("/coroutines/enable")
    fun enableCoroutines(): CoroutineStatus {
        coroutineManager.enable()
        return coroutineManager.status()
    }

    /** Disables coroutine-based async execution (synchronous path). */
    @PostMapping("/coroutines/disable")
    fun disableCoroutines(): CoroutineStatus {
        coroutineManager.disable()
        return coroutineManager.status()
    }
}
