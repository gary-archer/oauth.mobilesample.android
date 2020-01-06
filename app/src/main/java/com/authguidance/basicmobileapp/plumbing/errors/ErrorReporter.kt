package com.authguidance.basicmobileapp.plumbing.errors

import android.content.Context
import com.authguidance.basicmobileapp.R

/*
 * A helper class to format error fields for display
 */
class ErrorReporter(private val context: Context) {

    /*
     * Return a collection of error lines
     */
    fun getErrorLines(error: UIError): ArrayList<ErrorLine> {

        val result = ArrayList<ErrorLine>()

        // Show production details
        if (!error.message.isNullOrBlank()) {
            result.add(ErrorLine(context.getString(R.string.error_user_message), error.message))
        }

        if (!error.area.isNullOrBlank()) {
            result.add(ErrorLine(context.getString(R.string.error_area), error.area))
        }

        if (!error.errorCode.isNullOrBlank()) {
            result.add(ErrorLine(context.getString(R.string.error_code), error.errorCode))
        }

        if (!error.appAuthCode.isNullOrBlank()) {
            result.add(
                ErrorLine(
                    context.getString(R.string.error_appauth_code),
                    error.appAuthCode
                )
            )
        }

        if (!error.utcTime.isNullOrBlank()) {
            result.add(ErrorLine(context.getString(R.string.error_utc_time), error.utcTime))
        }

        if (error.statusCode != 0) {
            result.add(
                ErrorLine(
                    context.getString(R.string.error_status),
                    error.statusCode.toString()
                )
            )
        }

        if (error.instanceId != 0) {
            result.add(
                ErrorLine(
                    context.getString(R.string.error_instance_id),
                    error.instanceId.toString()
                )
            )
        }

        if (!error.details.isNullOrBlank()) {
            result.add(ErrorLine(context.getString(R.string.error_details), error.details))
        }

        // Show additional technical details when configured
        if (!error.url.isNullOrBlank()) {
            result.add(ErrorLine(context.getString(R.string.error_url), error.url))
        }

        if (error.stackFrames.size > 0) {
            result.add(
                ErrorLine(
                    context.getString(R.string.error_stack),
                    error.stackFrames.joinToString("\n\n")
                )
            )
        }

        return result
    }
}