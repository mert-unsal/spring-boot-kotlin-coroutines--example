package com.example.demo.service

import com.example.demo.config.CoroutineManager
import com.example.demo.model.Product
import com.example.demo.repository.ProductRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

/**
 * Service layer for [Product] CRUD operations.
 *
 * When [CoroutineManager.isEnabled] is `true`, operations run on [Dispatchers.IO]
 * with an artificial delay to simulate real-world async I/O and to make coroutine
 * behaviour observable.
 *
 * When coroutines are disabled, operations still use the coroutine-based repository
 * but skip the extra async context switch and simulated delay — demonstrating the
 * difference between the two modes.
 */
@Service
class ProductService(
    private val productRepository: ProductRepository,
    private val coroutineManager: CoroutineManager
) {

    private val log = LoggerFactory.getLogger(ProductService::class.java)

    suspend fun findAll(): Flow<Product> {
        log.info("findAll – coroutines enabled: ${coroutineManager.isEnabled}")
        return if (coroutineManager.isEnabled) {
            withContext(Dispatchers.IO) {
                productRepository.findAll()
                    .onEach { delay(10) } // simulate async I/O per item
            }
        } else {
            productRepository.findAll()
        }
    }

    suspend fun findById(id: Long): Product? {
        log.info("findById($id) – coroutines enabled: ${coroutineManager.isEnabled}")
        return if (coroutineManager.isEnabled) {
            withContext(Dispatchers.IO) {
                delay(20)
                productRepository.findById(id)
            }
        } else {
            productRepository.findById(id)
        }
    }

    suspend fun create(product: Product): Product {
        log.info("create – coroutines enabled: ${coroutineManager.isEnabled}")
        return if (coroutineManager.isEnabled) {
            withContext(Dispatchers.IO) {
                delay(20)
                productRepository.save(product)
            }
        } else {
            productRepository.save(product)
        }
    }

    suspend fun update(id: Long, product: Product): Product? {
        log.info("update($id) – coroutines enabled: ${coroutineManager.isEnabled}")
        val existing = productRepository.findById(id) ?: return null
        val updated = existing.copy(
            name = product.name,
            description = product.description,
            price = product.price
        )
        return if (coroutineManager.isEnabled) {
            withContext(Dispatchers.IO) {
                delay(20)
                productRepository.save(updated)
            }
        } else {
            productRepository.save(updated)
        }
    }

    suspend fun delete(id: Long): Boolean {
        log.info("delete($id) – coroutines enabled: ${coroutineManager.isEnabled}")
        if (!productRepository.existsById(id)) return false
        return if (coroutineManager.isEnabled) {
            withContext(Dispatchers.IO) {
                delay(20)
                productRepository.deleteById(id)
                true
            }
        } else {
            productRepository.deleteById(id)
            true
        }
    }
}
