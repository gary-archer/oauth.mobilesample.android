package com.authsamples.finalmobileapp.views.utilities

import kotlin.io.encoding.Base64

/*
 * Base64 URL utilities
 */
object Base64Url {

    /*
     * Decode to readable text
     */
    fun decode(input: String): String {
        val bytes = Base64.UrlSafe.decode(input.toByteArray(charset("UTF-8")))
        return String(bytes, charset("UTF-8"))
    }
}
