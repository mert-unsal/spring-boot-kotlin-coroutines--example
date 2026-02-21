package com.example.demo.config

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Runtime manager that controls whether coroutine-based (async) execution is used
 * across the application.
 *
 * When [enabled] is `true`, service operations run as suspending coroutines and may
 * leverage delays, parallel execution, or structured concurrency.
 * When [enabled] is `false`, operations are executed in a simplified synchronous path
 * without coroutine overhead, useful for comparison and debugging.
 */
@Component
class CoroutineManager {

    private val log = LoggerFactory.getLogger(CoroutineManager::class.java)

    private val _enabled = AtomicBoolean(true)

    val isEnabled: Boolean
        get() = _enabled.get()

    fun enable() {
        _enabled.set(true)
        log.info("Coroutines ENABLED")
    }

    fun disable() {
        _enabled.set(false)
        log.info("Coroutines DISABLED")
    }

    fun status(): CoroutineStatus = CoroutineStatus(enabled = _enabled.get())
}

data class CoroutineStatus(val enabled: Boolean)
