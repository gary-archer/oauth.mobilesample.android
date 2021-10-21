package com.authguidance.basicmobileapp.plumbing.errors

import java.text.SimpleDateFormat
import java.util.Locale

/*
 * An error entity for the UI
 */
class UIError(var area: String, var errorCode: String, userMessage: String) : RuntimeException(userMessage) {

    var statusCode = 0
    var utcTime: String
    var appAuthCode = ""
    var instanceId = 0
    var details: String? = null
    var url: String = ""

    /*
     * Initialise fields during construction
     */
    init {

        val formatter = SimpleDateFormat("MMM dd yyyy HH:mm", Locale.getDefault())
        this.utcTime = formatter.format(System.currentTimeMillis())
    }

    /*
     * Override details when an API 500 error is handled
     */
    fun setApiErrorDetails(area: String, id: Int, utcTimestamp: String) {
        this.area = area
        this.instanceId = id

        // Get the API timestamp into the display format
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val formatter = SimpleDateFormat("MMM dd yyyy HH:mm", Locale.getDefault())
        val parsed = parser.parse(utcTimestamp)
        if (parsed != null) {
            this.utcTime = formatter.format(parsed)
        }
    }
}
