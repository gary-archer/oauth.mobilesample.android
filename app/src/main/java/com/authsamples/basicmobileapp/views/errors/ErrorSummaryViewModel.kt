package com.authsamples.basicmobileapp.views.errors

import androidx.databinding.Observable
import androidx.databinding.PropertyChangeRegistry
import androidx.lifecycle.ViewModel
import com.authsamples.basicmobileapp.plumbing.errors.UIError

/*
 * A simple view model class for the error summary view
 */
class ErrorSummaryViewModel(
    val onShowDetails: (dialogTitle: String, error: UIError) -> Unit
) : ViewModel(), Observable {

    // Observable data for which the UI must be notified upon change
    var hyperlinkText: String? = null
    private var dialogTitle: String? = null
    private var error: UIError? = null
    private val callbacks = PropertyChangeRegistry()

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
        callbacks.notifyCallbacks(this, 0, null)
    }

    /*
     * Clear error fields
     */
    fun clearErrorDetails() {
        this.hyperlinkText = null
        this.dialogTitle = null
        this.error = null
        callbacks.notifyCallbacks(this, 0, null)
    }

    /*
     * Observable plumbing to allow XML views to register
     */
    override fun addOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback) {
        callbacks.add(callback)
    }

    /*
     * Observable plumbing to allow XML views to unregister
     */
    override fun removeOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback) {
        callbacks.remove(callback)
    }
}
