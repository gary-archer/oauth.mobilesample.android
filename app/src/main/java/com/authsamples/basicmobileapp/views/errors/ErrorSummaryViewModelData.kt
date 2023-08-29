package com.authsamples.basicmobileapp.views.errors

import com.authsamples.basicmobileapp.plumbing.errors.UIError

/*
 * Data to pass from parent views to the error summary view, to populate the model
 */
data class ErrorSummaryViewModelData(
    val hyperlinkText: String,
    val dialogTitle: String,
    val error: UIError?
)
