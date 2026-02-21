package com.example.demo.repository

import com.example.demo.model.Product
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

/**
 * Reactive repository for [Product] using Kotlin coroutine-based API.
 *
 * [CoroutineCrudRepository] exposes `suspend` functions instead of Reactor Mono/Flux,
 * making it a first-class citizen in coroutine-based applications.
 */
interface ProductRepository : CoroutineCrudRepository<Product, Long>
