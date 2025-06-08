package com.authsamples.finalmobileapp.plumbing.utilities

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Collections
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/*
 * Used when multiple UI fragments attempt an action that needs to be synchronised
 */
class ConcurrentActionHandler {

    // Queue all requests in an array of continuations
    private val continuations = Collections.synchronizedList(ArrayList<Continuation<Unit>>())

    /*
     * Run the supplied action the first time only and return a continuation to the caller
     */
    suspend fun execute(action: suspend () -> Unit) {

        return suspendCoroutine { continuation ->

            // Add the continuation to the collection
            this.continuations.add(continuation)

            // Perform the action for the first caller only
            if (this.continuations.count() == 1) {

                val that = this@ConcurrentActionHandler
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        // Do the work
                        action()

                        // Resolve all continuations with the same success result
                        that.continuations.forEach {
                            it.resume(Unit)
                        }

                        that.continuations.clear()

                    } catch (ex: Throwable) {

                        // Resolve all continuations with the same error
                        that.continuations.forEach {
                            it.resumeWithException(ex)
                        }

                        that.continuations.clear()
                    }
                }
            }
        }
    }
}
