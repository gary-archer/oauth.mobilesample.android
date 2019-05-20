package authguidance.mobilesample.plumbing.errors

import java.sql.Timestamp

/*
 * An error entity for the UI
 */
class UIError(area: String, errorCode: String, userMessage: String) : RuntimeException(userMessage) {

    // Technical fields to display
    private val area: String
    private val errorCode: String
    private val utcTime: String
    private val statusCode: Int
    private val instanceId: Int
    private val details: String

    // Additional details that can be shown during development
    private val url: String
    private val stackFrames: Array<String>

    // Give fields default values
    init {

        // TODO: Pass this to error activity - do I need it to be parcelable?
        this.area = area
        this.errorCode = errorCode
        this.utcTime = Timestamp(System.currentTimeMillis()).toString()
        this.statusCode = 0
        this.instanceId = 0
        this.details = ""
        this.url = ""
        this.stackFrames = arrayOf<String>()
    }

    /*
     * Return the details that are populated
     */
    fun toErrorItemList(): List<ErrorItem> {

        val result = ArrayList<ErrorItem>()
        result.add(ErrorItem("Area", area))
        result.add(ErrorItem("ErrorCode", errorCode))

        // TODO: Get this differently
        if (this.message != null) {
            result.add(ErrorItem("UserMessage", this.message))
        }

        return result
    }
}