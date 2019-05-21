package authguidance.mobilesample.plumbing.errors

import java.sql.Timestamp

/*
 * An error entity for the UI
 */
class UIError(area: String, errorCode: String, userMessage: String) : RuntimeException(userMessage) {

    // Technical fields to display
    val area: String = area
    val errorCode: String = errorCode
    val utcTime: String
    var statusCode: Int
    var instanceId: Int
    var details: String

    // Additional details that can be shown during development
    var url: String

    init {

        // Give unsupplied fields their default values
        this.utcTime = Timestamp(System.currentTimeMillis()).toString()
        this.statusCode = 0
        this.instanceId = 0
        this.details = ""
        this.url = ""
    }
}