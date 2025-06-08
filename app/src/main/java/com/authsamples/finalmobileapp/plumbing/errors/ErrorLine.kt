package com.authsamples.finalmobileapp.plumbing.errors

import androidx.compose.ui.graphics.Color

/*
 * A simple field value pair to be shown on a line
 */
data class ErrorLine(

    // The label
    val name: String,

    // The formatted value
    val value: String,

    // The color for the value
    val valueColor: Color
)
