package com.authsamples.basicmobileapp.plumbing.utilities

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

private typealias SuccessCallback = () -> Unit
private typealias ErrorCallback = (ex: Throwable) -> Unit

/*
 * Used when multiple UI fragments attempt an action that needs to be synchronised
 */
class ConcurrentActionHandler {

    // A concurrent collection of callbacks
    private val callbacks = ArrayList(
        ArrayList<Pair<SuccessCallback, ErrorCallback>>().toList()
    )

    // A lock object for multiple statements
    private val lock = Object()

    /*
     * Run the supplied action the first time only and return a promise to the caller
     */
    suspend fun execute(action: suspend () -> Unit) {

        return suspendCoroutine { continuation ->

            val onSuccess = {
                continuation.resume(Unit)
            }

            val onError = { exception: Throwable ->
                continuation.resumeWithException(exception)
            }

            // Add the callback to the collection, in a thread safe manner
            synchronized (this.lock) {
                this.callbacks.add(Pair(onSuccess, onError))
            }

            // Perform the action for the first caller only
            if (this.callbacks.count() == 1) {

                val that = this@ConcurrentActionHandler
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        // Do the work
                        action()

                        // Resolve all promises with the same success result
                        synchronized (that.lock) {
                            that.callbacks.forEach {
                                it.first()
                            }

                            that.callbacks.clear()
                        }

                    } catch (ex: Throwable) {

                        // Resolve all promises with the same error
                        synchronized (that.lock) {
                            that.callbacks.forEach {
                                it.second(ex)
                            }

                            that.callbacks.clear()
                        }
                    }
                }
            }
        }
    }
}