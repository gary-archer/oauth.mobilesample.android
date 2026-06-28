package com.authsamples.finalmobileapp.plumbing.errors

import android.content.Context

/*
 * A helper class to output error details to the console and avoid end users
 */
object ErrorConsoleReporter {

    /*
     * Output names and values
     */
    fun output(error: UIError, context: Context) {

        val fields = ErrorFormatter(context).getErrorFields(error)
        fields.forEach {
            println("finalmobileapp Error: ${it.name} = ${it.value}")
        }
    }
}
