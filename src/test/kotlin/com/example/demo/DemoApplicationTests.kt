package com.example.demo

import com.example.demo.config.CoroutineManager
import com.example.demo.model.Product
import com.example.demo.repository.ProductRepository
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.returnResult
import java.math.BigDecimal

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DemoApplicationTests {

    @Autowired
    lateinit var webTestClient: WebTestClient

    @Autowired
    lateinit var productRepository: ProductRepository

    @Autowired
    lateinit var coroutineManager: CoroutineManager

    @AfterEach
    fun cleanup() = runBlocking {
        productRepository.deleteAll()
        coroutineManager.enable()
    }

    // ── Product CRUD tests ─────────────────────────────────────────────────────

    @Test
    fun `create product returns 201`() {
        webTestClient.post().uri("/api/products")
            .bodyValue(mapOf("name" to "Tablet", "description" to "Android tablet", "price" to "299.99"))
            .exchange()
            .expectStatus().isCreated
            .expectBody()
            .jsonPath("$.id").isNotEmpty
            .jsonPath("$.name").isEqualTo("Tablet")
    }

    @Test
    fun `get all products returns list`() = runBlocking {
        productRepository.save(Product(name = "Monitor", price = BigDecimal("399.99")))

        webTestClient.get().uri("/api/products")
            .exchange()
            .expectStatus().isOk
            .returnResult<Product>().responseBody
            .collectList()
            .block()
            .let { products ->
                assertNotNull(products)
                assertTrue(products!!.isNotEmpty())
            }
    }

    @Test
    fun `get product by id returns product`() = runBlocking {
        val saved = productRepository.save(Product(name = "Camera", price = BigDecimal("599.99")))

        webTestClient.get().uri("/api/products/${saved.id}")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.name").isEqualTo("Camera")
    }

    @Test
    fun `get product by id returns 404 when not found`() {
        webTestClient.get().uri("/api/products/99999")
            .exchange()
            .expectStatus().isNotFound
    }

    @Test
    fun `update product returns updated product`() = runBlocking {
        val saved = productRepository.save(Product(name = "OldName", price = BigDecimal("100.00")))

        webTestClient.put().uri("/api/products/${saved.id}")
            .bodyValue(mapOf("name" to "NewName", "price" to "150.00"))
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.name").isEqualTo("NewName")
    }

    @Test
    fun `delete product returns 204`() = runBlocking {
        val saved = productRepository.save(Product(name = "Headphones", price = BigDecimal("75.00")))

        webTestClient.delete().uri("/api/products/${saved.id}")
            .exchange()
            .expectStatus().isNoContent

        assertNull(productRepository.findById(saved.id!!))
    }

    @Test
    fun `delete non-existent product returns 404`() {
        webTestClient.delete().uri("/api/products/99999")
            .exchange()
            .expectStatus().isNotFound
    }

    // ── Coroutine management tests ─────────────────────────────────────────────

    @Test
    fun `get coroutine status returns enabled by default`() {
        webTestClient.get().uri("/api/coroutines")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.enabled").isEqualTo(true)
    }

    @Test
    fun `disable coroutines returns disabled status`() {
        webTestClient.post().uri("/api/coroutines/disable")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.enabled").isEqualTo(false)

        assertEquals(false, coroutineManager.isEnabled)
    }

    @Test
    fun `enable coroutines returns enabled status`() {
        coroutineManager.disable()

        webTestClient.post().uri("/api/coroutines/enable")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.enabled").isEqualTo(true)

        assertEquals(true, coroutineManager.isEnabled)
    }

    @Test
    fun `crud works when coroutines disabled`() = runBlocking {
        coroutineManager.disable()

        val response = webTestClient.post().uri("/api/products")
            .bodyValue(mapOf("name" to "Webcam", "price" to "79.99"))
            .exchange()
            .expectStatus().isCreated
            .expectBody(Product::class.java)
            .returnResult()

        val created = response.responseBody
        assertNotNull(created)
        assertNotNull(created!!.id)

        val all = productRepository.findAll().toList()
        assertTrue(all.any { it.name == "Webcam" })
    }
}
