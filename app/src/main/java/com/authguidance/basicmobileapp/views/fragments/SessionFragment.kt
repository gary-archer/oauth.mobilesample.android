package com.authguidance.basicmobileapp.views.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.authguidance.basicmobileapp.R
import com.authguidance.basicmobileapp.api.client.ApiClient
import com.authguidance.basicmobileapp.databinding.FragmentSessionBinding
import com.authguidance.basicmobileapp.app.MainActivity

/*
 * Render the UI session id used in API logs
 */
class SessionFragment : androidx.fragment.app.Fragment() {

    // Binding properties
    private lateinit var binding: FragmentSessionBinding

    // Details passed from the main activity
    private lateinit var apiClientAccessor: () -> ApiClient?

    /*
     * Get properties from the main activity
     */
    override fun onAttach(context: Context) {
        super.onAttach(context)

        val mainActivity = context as MainActivity
        this.apiClientAccessor = mainActivity::getApiClient
    }

    /*
     * Inflate the layout
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        this.binding = FragmentSessionBinding.inflate(inflater, container, false)
        return this.binding.root
    }

    /*
     * Show the content
     */
    fun show() {

        // Show nothing if the app has not initialised yet
        val apiClient = this.apiClientAccessor()
        if (apiClient == null) {
            return
        }

        // Otherwise render the API client's session id
        val label = this.getString(R.string.api_session_id)
        val value = apiClient.sessionId
        this.binding.apiSessionId.text = "$label: $value"
        this.binding.apiSessionId.visibility = View.VISIBLE
    }

    /*
     * Clear the content when logged out
     */
    fun clear() {
        this.binding.apiSessionId.text = ""
        this.binding.apiSessionId.visibility = View.GONE
    }
}
