package com.authguidance.basicmobileapp.plumbing.utilities

import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

// Internal types
private typealias SuccessCallback = () -> Unit
private typealias ErrorCallback = (ex: Throwable) -> Unit

/*
 * Used when multiple UI fragments attempt an action that needs to be synchronised
 */
class ConcurrentActionHandler {

    // A flag to record whether the work is in progress
    @Volatile
    private var actionInProgress = false

    // The collection of callbacks
    private val callbacks = ArrayList<Pair<SuccessCallback, ErrorCallback>>()

    // An object to synchronise access to the collection
    private val lock = Object()

    /*
     * Start the concurrent action
     */
    fun start(): Boolean {

        if (this.actionInProgress) {
            return false
        }

        this.actionInProgress = true
        return true
    }

    /*
     * The first UI fragment does a refresh action and other UI fragments wait on a continuation
     */
    suspend fun createContinuation() {

        return suspendCoroutine { continuation ->

            // Define callbacks through which to return the result
            val onSuccess = {
                println("GJA: continuation success")
                continuation.resume(Unit)
            }

            val onError = { exception: Throwable ->
                println("GJA: continuation error")
                continuation.resumeWithException(exception)
            }

            synchronized(this.lock) {
                this.callbacks.add(Pair(onSuccess, onError))
            }

            println("GJA: created callbacks")
        }
    }

    /*
     * When the first UI fragment has completed successfully, return the same result to all other fragments
     */
    fun resume() {

        synchronized (this.lock) {
            this.callbacks.forEach {
                println("GJA: success callback")
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

        synchronized (this.lock) {
            this.callbacks.forEach {
                println("GJA: failure callback")
                it.second(ex);
            }

            this.callbacks.clear()
            this.actionInProgress = false
        }
    }
}