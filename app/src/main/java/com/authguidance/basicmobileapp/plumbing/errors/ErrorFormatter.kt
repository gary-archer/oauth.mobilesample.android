package com.authguidance.basicmobileapp.plumbing.errors

import android.content.Context
import com.authguidance.basicmobileapp.BuildConfig
import com.authguidance.basicmobileapp.R
import java.lang.StringBuilder
import kotlin.collections.ArrayList

/*
 * A helper class to format error fields for display
 */
class ErrorFormatter(private val context: Context) {

    /*
     * Return a collection of error lines
     */
    @Suppress("LongMethod")
    fun getErrorLines(error: UIError): ArrayList<ErrorLine> {

        val result = ArrayList<ErrorLine>()

        val valueColour = context.getColor(R.color.text_value)
        val userActionValueColour = context.getColor(R.color.text_green)
        val errorIdValueColour = context.getColor(R.color.text_error)

        /* FIELDS FOR THE END USER */

        // Keep the user informed and suggest an action
        if (!error.message.isNullOrBlank()) {
            result.add(
                this.createErrorLine(
                    R.string.error_user_action,
                    "Please retry the operation",
                    userActionValueColour
                )
            )
        }

        // Give the user summary level info, such as 'Network error'
        if (!error.message.isNullOrBlank()) {
            result.add(
                this.createErrorLine(
                    R.string.error_info,
                    error.message,
                    valueColour
                )
            )
        }

        /* FIELDS FOR TECHNICAL SUPPORT STAFF */

        // Show the time of the error
        result.add(
            this.createErrorLine(
                R.string.error_utc_time,
                error.utcTime,
                valueColour
            )
        )

        // Indicate the area of the system, such as which component failed
        if (!error.area.isNullOrBlank()) {
            result.add(
                this.createErrorLine(
                    R.string.error_area,
                    error.area,
                    valueColour
                )
            )
        }

        // Indicate the type of error
        if (!error.errorCode.isNullOrBlank()) {
            result.add(
                this.createErrorLine(
                    R.string.error_code,
                    error.errorCode,
                    valueColour
                )
            )
        }

        // Show the AppAuth error code if applicable
        if (!error.appAuthCode.isNullOrBlank()) {
            result.add(
                this.createErrorLine(
                    R.string.error_appauth_code,
                    error.appAuthCode,
                    valueColour
                )
            )
        }

        // Link to API logs if applicable
        if (error.instanceId != 0) {
            result.add(
                this.createErrorLine(
                    R.string.error_instance_id,
                    error.instanceId.toString(),
                    errorIdValueColour
                )
            )
        }

        // Show the HTTP status if applicable
        if (error.statusCode != 0) {
            result.add(
                this.createErrorLine(
                    R.string.error_status,
                    error.statusCode.toString(),
                    valueColour
                )
            )
        }

        /* FIELDS FOR DEVELOPERS */

        // Show details for some types of error
        val errorDetails = error.details
        if (!errorDetails.isNullOrBlank()) {
            result.add(
                this.createErrorLine(
                    R.string.error_details,
                    errorDetails,
                    valueColour
                )
            )
        }

        // Show the URL that failed if applicable
        if (!error.url.isBlank()) {
            result.add(
                this.createErrorLine(
                    R.string.error_url,
                    error.url,
                    valueColour
                )
            )
        }

        // Show stack trace details in debug builds
        if (BuildConfig.DEBUG) {
            result.add(
                this.createErrorLine(
                    R.string.error_stack,
                    this.getFormattedStackTrace(error),
                    valueColour
                )
            )
        }

        return result
    }

    /*
     * Return an error line as an object
     */
    private fun createErrorLine(labelId: Int, value: String, colourId: Int): ErrorLine {

        return ErrorLine(
            context.getString(labelId),
            value,
            colourId
        )
    }

    /*
     * Return the stack trace in a readable format
     */
    private fun getFormattedStackTrace(error: UIError): String {

        val text = StringBuilder()

        val frames = error.stackTrace
        if (frames.isNotEmpty()) {
            for (frame in frames) {
                text.appendLine(frame.toString())
                text.appendLine()
            }
        }

        return text.toString()
    }
}
