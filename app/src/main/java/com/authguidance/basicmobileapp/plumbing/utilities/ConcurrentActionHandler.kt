package com.authguidance.basicmobileapp.plumbing.utilities

import java.util.concurrent.CopyOnWriteArrayList
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

// Internal types
private typealias SuccessCallback = () -> Unit
private typealias ErrorCallback = (ex: Throwable) -> Unit

/*
 * Used when multiple UI fragments attempt an action that needs to be synchronised
 * We have to write the copde a little differently to our React / SwiftUI samples to avoid this compiler error:
 *   'Suspension functions can only be called within coroutine body'
 */
class ConcurrentActionHandler {

    // A flag to record whether the work is in progress
    @Volatile
    private var actionInProgress = false

    // A concurrent collection of callbacks
    private val callbacks = CopyOnWriteArrayList(
        ArrayList<Pair<SuccessCallback, ErrorCallback>>().toList()
    )

    // A lock object for multiple statements
    private val lock = Object()

    /*
     * Start the concurrent action once only
     */
    fun start(): Boolean {

        synchronized(this.lock) {
            if (!this.actionInProgress) {
                this.actionInProgress = true
                return true
            }
        }

        return false
    }

    /*
     * The first UI fragment does a refresh action and other UI fragments wait on a continuation
     */
    suspend fun addContinuation() {

        return suspendCoroutine { continuation ->

            val onSuccess = {
                continuation.resume(Unit)
            }

            val onError = { exception: Throwable ->
                continuation.resumeWithException(exception)
            }

            this.callbacks.add(Pair(onSuccess, onError))
        }
    }

    /*
     * When the first UI fragment has completed successfully, return the same result to all other fragments
     */
    fun resume() {

        synchronized(this.lock) {
            this.callbacks.forEach {
                it.first()
            }

            this.callbacks.clear()
            this.actionInProgress = false
        }
    }

    /*
     * When the first UI fragment has failed, return the same error to all other fragments
     */
    fun resumeWithException(ex: Throwable) {

        synchronized(this.lock) {
            this.callbacks.forEach {
                it.second(ex)
            }

            this.callbacks.clear()
            this.actionInProgress = false
        }
    }
}
