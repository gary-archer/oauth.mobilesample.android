package com.authguidance.basicmobileapp.plumbing.errors

/*
 * A simple field value pair to be shown on a line
 */
data class ErrorLine(

    // The label
    val name: String,

    // The formatted value
    val value: String?,

    // The colour for the value
    val valueColour: Int
)
