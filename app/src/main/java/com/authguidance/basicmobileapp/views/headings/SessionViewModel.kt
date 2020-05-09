package com.authguidance.basicmobileapp.views.headings

import androidx.databinding.BaseObservable
import com.authguidance.basicmobileapp.api.client.ApiClient

/*
 * A simple view model for the session view
 */
class SessionViewModel(
    private val apiClientAccessor: () -> ApiClient?,
    private val label: String
) : BaseObservable() {

    // Properties
    private var sessionId: String? = null

    /*
     * Get the session id and inform the binding system
     */
    fun getSessionId(): String {

        if (this.sessionId == null) {
            return ""
        }

        return "${this.label}: ${this.sessionId}"
    }

    /*
     * Return false if space should be hidden via Visibility.GONE
     */
    fun isSessionIdVisible(): Boolean {
        return this.sessionId != null
    }

    /*
     * Update the view model
     */
    fun updateData() {

        // Show nothing if the app has not initialised yet
        val apiClient = this.apiClientAccessor()
        if (apiClient == null) {
            return
        }

        // Otherwise set the API client's session id
        this.setSessionId(apiClient.sessionId)
    }

    /*
     * Clear the content when logged out
     */
    fun clearData() {
        this.setSessionId(null)
    }

    /*
     * Update the session id and inform the binding system
     */
    private fun setSessionId(value: String?) {
        this.sessionId = value
        this.notifyChange()
    }
}