package com.authguidance.basicmobileapp.plumbing.errors

import android.content.Context
import com.authguidance.basicmobileapp.BuildConfig
import com.authguidance.basicmobileapp.R
import java.lang.StringBuilder
import kotlin.collections.ArrayList

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

        result.add(ErrorLine(context.getString(R.string.error_utc_time), error.utcTime))

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

        if (!error.url.isBlank()) {
            result.add(ErrorLine(context.getString(R.string.error_url), error.url))
        }

        // Show stack trace details in debug builds
        if (BuildConfig.DEBUG) {
            result.add(
                ErrorLine(
                    context.getString(R.string.error_stack),
                    this.getFormattedStackTrace(error)
                )
            )
        }

        return result
    }

    /*
     * Return the stack trace in a readable format
     */
    private fun getFormattedStackTrace(error: UIError): String {

        val text = StringBuilder()

        val frames = error.stackTrace
        if (frames.isNotEmpty()) {
            for (frame in frames) {
                text.appendln(frame.toString())
                text.appendln()
            }
        }

        return text.toString()
    }
}