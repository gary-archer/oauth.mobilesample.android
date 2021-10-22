package com.authguidance.basicmobileapp.views.headings

import androidx.databinding.BaseObservable
import com.authguidance.basicmobileapp.api.client.ApiClient

/*
 * A simple view model for the session view
 */
class SessionViewModel(
    private val apiClient: ApiClient,
    private val label: String
) : BaseObservable() {

    private var sessionId: String = ""

    /*
     * Get the session id and inform the binding system
     */
    fun getSessionId(): String {
        return "${this.label}: ${this.sessionId}"
    }

    /*
     * Return false if space should be hidden via Visibility.GONE
     */
    fun isSessionIdVisible(): Boolean {
        return this.sessionId.length > 0
    }

    /*
     * Show the data
     */
    fun showData() {
        this.setSessionId(this.apiClient.sessionId)
    }

    /*
     * Clear the content when logged out
     */
    fun clearData() {
        this.setSessionId("")
    }

    /*
     * Update the session id and inform the binding system
     */
    private fun setSessionId(value: String) {
        this.sessionId = value
        this.notifyChange()
    }
}
