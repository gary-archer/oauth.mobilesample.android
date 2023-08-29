package com.authsamples.basicmobileapp.views.headings

import android.app.Application
import androidx.databinding.Observable
import androidx.databinding.PropertyChangeRegistry
import androidx.lifecycle.AndroidViewModel
import com.authsamples.basicmobileapp.R

/*
 * A simple view model for the session view
 */
class SessionViewModel(
    private val sessionId: String,
    val app: Application
) : AndroidViewModel(app), Observable {

    // Observable data for which the UI must be notified upon change
    private var isVisible = true
    private val callbacks = PropertyChangeRegistry()

    // The text to display when visible
    private val text = "${app.getString(R.string.api_session_id)}: ${this.sessionId}"

    /*
     * Get the session id and inform the binding system
     */
    fun getSessionText(): String {
        return this.text
    }

    /*
     * Return false if space should be hidden via Visibility.GONE
     */
    fun isSessionIdVisible(): Boolean {
        return this.isVisible
    }

    /*
     * Update the session id and inform the binding system
     */
    fun setVisibility(isVisible: Boolean) {
        this.isVisible = isVisible
        this.callbacks.notifyCallbacks(this, 0, null)
    }

    /*
     * Observable plumbing to allow XML views to register
     */
    override fun addOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback) {
        this.callbacks.add(callback)
    }

    /*
     * Observable plumbing to allow XML views to unregister
     */
    override fun removeOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback) {
        this.callbacks.remove(callback)
    }
}
