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
    val statusCode: Int
    val instanceId: Int
    val details: String

    // Additional details that can be shown during development
    private val url: String
    private val stackFrames: Array<String>

    // Give fields default values
    init {

        // TODO: Pass this data to error activity - do I need it to be parcelable?
        // https://proandroiddev.com/parcelable-in-kotlin-here-comes-parcelize-b998d5a5fcac

        this.utcTime = Timestamp(System.currentTimeMillis()).toString()
        this.statusCode = 0
        this.instanceId = 0
        this.details = ""
        this.url = ""
        this.stackFrames = arrayOf<String>()
    }
}