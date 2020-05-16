package com.authguidance.basicmobileapp.views.errors

import androidx.databinding.BaseObservable
import com.authguidance.basicmobileapp.plumbing.errors.UIError

/*
 * A simple view model class for the error summary view
 */
class ErrorSummaryViewModel(val onShowDetails: (dialogTitle: String, error: UIError) -> Unit) : BaseObservable() {

    // Properties
    var hyperlinkText: String? = null
    private var dialogTitle: String? = null
    private var error: UIError? = null

    /*
     * Called by the binding system
     */
    fun isError(): Boolean {
        return this.error != null
    }

    /*
     * Handle the command to show details
     */
    fun onDetails() {

        if (this.dialogTitle != null && this.error != null) {
            this.onShowDetails(this.dialogTitle!!, this.error!!)
        }
    }

    /*
     * Set error fields
     */
    fun setErrorDetails(hyperlinkText: String, dialogTitle: String, error: UIError) {
        this.hyperlinkText = hyperlinkText
        this.dialogTitle = dialogTitle
        this.error = error
        this.notifyChange()
    }

    /*
     * Clear error fields
     */
    fun clearErrorDetails() {
        this.hyperlinkText = null
        this.dialogTitle = null
        this.error = null
        this.notifyChange()
    }
}
