package com.authsamples.basicmobileapp.views.errors

import com.authsamples.basicmobileapp.plumbing.errors.UIError

/*
 * Data supplied by views that render errors
 */
data class ErrorViewModel(
    val error: UIError,
    var hyperlinkText: String,
    var dialogTitle: String,
)
