package com.authguidance.basicmobileapp.plumbing.errors

import java.text.SimpleDateFormat
import java.util.Locale

/*
 * An error entity for the UI
 */
class UIError(area: String, errorCode: String, userMessage: String) : RuntimeException(userMessage) {

    var area: String = area
    var errorCode: String = errorCode
    var statusCode: Int
    var utcTime: String
    var appAuthCode: String
    var instanceId: Int
    var details: String?
    var url: String

    /*
     * Initialise fields during construction
     */
    init {

        // Give unsupplied fields their default values
        this.statusCode = 0
        this.appAuthCode = ""
        this.instanceId = 0
        this.details = ""
        this.url = ""

        // Format the current time
        val formatter = SimpleDateFormat("MMM dd yyyy HH:mm")
        this.utcTime = formatter.format(System.currentTimeMillis())
    }

    /*
     * Override details when an API 500 error is handled
     */
    fun setApiErrorDetails(area: String, id: Int, utcTimestamp: String) {
        this.area = area
        this.instanceId = id

        // Get the API timestamp into the display format
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ROOT)
        val formatter = SimpleDateFormat("MMM dd yyyy HH:mm")
        val parsed = parser.parse(utcTimestamp)
        if (parsed != null) {
            this.utcTime = formatter.format(parsed)
        }
    }
}
