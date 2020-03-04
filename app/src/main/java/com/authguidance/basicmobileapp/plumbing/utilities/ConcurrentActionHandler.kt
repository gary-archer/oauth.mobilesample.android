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

    /*
     * Run the supplied action once and return a continuation while in progress
     */
    suspend fun execute(action: suspend () -> Unit): Unit {

        println("GJA: refresh token for UI fragment")

        // Create a continuation through which to return the result
        val response: Unit = suspendCoroutine { continuation ->

            val onSuccess = {
                println("GJA: continuation success")
                continuation.resume(Unit)
            }

            val onError = { exception: Throwable ->
                println("GJA: continuation error")
                continuation.resumeWithException(exception)
            }

            println("GJA: adding callback")
            this.callbacks.add(Pair(onSuccess, onError))
            println("GJA: added callback")
        }

        println("GJA: suspend coroutine created")
        if (this.actionInProgress) {
            println("GJA: already in progress")
        }

        // Only do the work for the first UI fragment that calls us
        if (!this.actionInProgress) {
            this.actionInProgress = true

            println("GJA: refresh token for first UI fragment")

            try {

                // Do the work
                action()
                println("GJA: refresh token completed")

                // On success resolve all continuations
                this.callbacks.forEach{
                    println("GJA: success callback")
                    it.first()
                }

            } catch (ex: Throwable) {

                // On success reject all continuations
                this.callbacks.forEach{
                    println("GJA: failure callback")
                    it.second(ex);
                }
            }

            // Reset once complete
            this.callbacks.clear()
            this.actionInProgress = false;
        }

        // Return the continuation
        return response;
    }
}