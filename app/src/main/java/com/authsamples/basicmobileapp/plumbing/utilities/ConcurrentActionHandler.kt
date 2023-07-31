package com.authsamples.basicmobileapp.plumbing.utilities

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/*
 * Used when multiple UI fragments attempt an action that needs to be synchronised
 */
class ConcurrentActionHandler {

    // Queue all requests in an array of continuations
    private val callbacks = ArrayList(
        ArrayList<Continuation<Unit>>().toList()
    )

    // A lock object for multiple statements
    private val lock = Object()

    /*
     * Run the supplied action the first time only and return a continuation to the caller
     */
    suspend fun execute(action: suspend () -> Unit) {

        return suspendCoroutine { continuation ->

            // Add the continuation to the collection, in a thread safe manner
            synchronized(this.lock) {
                this.callbacks.add(continuation)
            }

            // Perform the action for the first caller only
            if (this.callbacks.count() == 1) {

                val that = this@ConcurrentActionHandler
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        // Do the work
                        action()

                        // Resolve all continuations with the same success result
                        synchronized(that.lock) {
                            that.callbacks.forEach {
                                it.resume(Unit)
                            }

                            that.callbacks.clear()
                        }

                    } catch (ex: Throwable) {

                        // Resolve all continuations with the same error
                        synchronized(that.lock) {
                            that.callbacks.forEach {
                                it.resumeWithException(ex)
                            }

                            that.callbacks.clear()
                        }
                    }
                }
            }
        }
    }
}
