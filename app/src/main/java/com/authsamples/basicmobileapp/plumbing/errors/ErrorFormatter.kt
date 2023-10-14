package com.authsamples.basicmobileapp.plumbing.errors

import android.content.Context
import androidx.compose.ui.graphics.Color
import com.authsamples.basicmobileapp.BuildConfig
import com.authsamples.basicmobileapp.R
import com.authsamples.basicmobileapp.views.utilities.CustomColors
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

        val valueColor = CustomColors.value
        val userActionValueColor = CustomColors.paleGreen
        val errorIdValueColor = CustomColors.error

        /* FIELDS FOR THE END USER */

        // Keep the user informed and suggest an action
        if (!error.message.isNullOrBlank()) {
            result.add(
                this.createErrorLine(
                    R.string.error_user_action,
                    "Please retry the operation",
                    userActionValueColor
                )
            )
        }

        // Give the user summary level info, such as 'Network error'
        if (!error.message.isNullOrBlank()) {
            result.add(
                this.createErrorLine(
                    R.string.error_info,
                    error.message,
                    valueColor
                )
            )
        }

        /* FIELDS FOR TECHNICAL SUPPORT STAFF */

        // Show the time of the error
        result.add(
            this.createErrorLine(
                R.string.error_utc_time,
                error.utcTime,
                valueColor
            )
        )

        // Indicate the area of the system, such as which component failed
        if (error.area.isNotBlank()) {
            result.add(
                this.createErrorLine(
                    R.string.error_area,
                    error.area,
                    valueColor
                )
            )
        }

        // Indicate the type of error
        if (error.errorCode.isNotBlank()) {
            result.add(
                this.createErrorLine(
                    R.string.error_code,
                    error.errorCode,
                    valueColor
                )
            )
        }

        // Show the AppAuth error code if applicable
        if (error.appAuthCode.isNotBlank()) {
            result.add(
                this.createErrorLine(
                    R.string.error_appauth_code,
                    error.appAuthCode,
                    valueColor
                )
            )
        }

        // Link to API logs if applicable
        if (error.instanceId != 0) {
            result.add(
                this.createErrorLine(
                    R.string.error_instance_id,
                    error.instanceId.toString(),
                    errorIdValueColor
                )
            )
        }

        // Show the HTTP status if applicable
        if (error.statusCode != 0) {
            result.add(
                this.createErrorLine(
                    R.string.error_status,
                    error.statusCode.toString(),
                    valueColor
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
                    valueColor
                )
            )
        }

        // Show the URL that failed if applicable
        if (error.url.isNotBlank()) {
            result.add(
                this.createErrorLine(
                    R.string.error_url,
                    error.url,
                    valueColor
                )
            )
        }

        // Show stack trace details in debug builds
        if (BuildConfig.DEBUG) {
            result.add(
                this.createErrorLine(
                    R.string.error_stack,
                    this.getFormattedStackTrace(error),
                    valueColor
                )
            )
        }

        return result
    }

    /*
     * Return an error line as an object
     */
    private fun createErrorLine(labelId: Int, value: String?, color: Color): ErrorLine {

        return ErrorLine(
            context.getString(labelId),
            value,
            color
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
