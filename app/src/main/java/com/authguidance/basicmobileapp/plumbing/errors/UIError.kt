package com.authguidance.basicmobileapp.plumbing.errors

import java.sql.Timestamp

/*
 * An error entity for the UI
 */
class UIError(area: String, errorCode: String, userMessage: String) : RuntimeException(userMessage) {

    // Technical fields to display
    val area: String = area
    var errorCode: String = errorCode
    val utcTime: String
    var statusCode: Int
    var appAuthCode: String
    var instanceId: Int
    var details: String?
    var stackFrames: ArrayList<String>

    // Additional details that can be shown during development
    var url: String

    init {

        // Give unsupplied fields their default values
        this.utcTime = Timestamp(System.currentTimeMillis()).toString()
        this.statusCode = 0
        this.appAuthCode = ""
        this.instanceId = 0
        this.details = ""
        this.url = ""
        this.stackFrames = ArrayList()
    }

    /*
     * For some types of error we report a stack trace
     */
    fun addToStackTrace(ex: Exception) {

        var frames = ex.getStackTrace();
        if(frames.isNotEmpty()) {
            for (frame in frames) {
                this.stackFrames.add(frame.toString())
            }
        }
    }
}