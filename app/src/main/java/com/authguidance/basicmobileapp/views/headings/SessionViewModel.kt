package com.authguidance.basicmobileapp.views.headings

import androidx.databinding.BaseObservable

/*
 * A simple view model for the session view
 */
class SessionViewModel(
    private val sessionId: String,
    private val label: String
) : BaseObservable() {

    private val text = "${this.label}: ${this.sessionId}"
    private var isVisible = true

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
        this.notifyChange()
    }
}
