package com.authsamples.finalmobileapp.views.errors

import com.authsamples.finalmobileapp.plumbing.errors.UIError

/*
 * Data supplied by views that render errors
 */
data class ErrorViewModel(
    val error: UIError,
    var hyperlinkText: String,
    var dialogTitle: String,
)
