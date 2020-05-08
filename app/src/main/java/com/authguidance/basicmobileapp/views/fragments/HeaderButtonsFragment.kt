package com.authguidance.basicmobileapp.views.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.authguidance.basicmobileapp.databinding.FragmentHeaderButtonsBinding
import com.authguidance.basicmobileapp.app.MainActivity

/*
 * A simple fragment with the header buttons
 */
class HeaderButtonsFragment : androidx.fragment.app.Fragment() {

    // Binding properties
    private lateinit var binding: FragmentHeaderButtonsBinding

    // Callbacks to the parent
    private lateinit var onHome: () -> Unit
    private lateinit var onReload: (Boolean) -> Unit
    private lateinit var onExpireAccessToken: () -> Unit
    private lateinit var onExpireRefreshToken: () -> Unit
    private lateinit var onLogout: () -> Unit

    /*
     * Get properties from the main activity
     */
    override fun onAttach(context: Context) {
        super.onAttach(context)

        // Get callbacks
        val mainActivity = context as MainActivity
        this.onHome = mainActivity::onHome
        this.onReload = mainActivity::onReloadData
        this.onExpireAccessToken = mainActivity::onExpireAccessToken
        this.onExpireRefreshToken = mainActivity::onExpireRefreshToken
        this.onLogout = mainActivity::onStartLogout
    }

    /*
     * Inflate the layout
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        this.binding = FragmentHeaderButtonsBinding.inflate(inflater, container, false)
        return this.binding.root
    }

    /*
     * Wire up button click events to call back the activity
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        this.binding.btnHome.setOnClickListener {
            this.onHome()
        }

        // Reload clicks use special handling that supports long clicks
        this.binding.btnReloadData.setCustomClickListener(this.onReload)
        this.binding.btnReloadData.isEnabled = false

        // When expire access token is clicked, call the main activity
        this.binding.btnExpireAccessToken.setOnClickListener {
            this.onExpireAccessToken()
        }

        // When expire refresh token is clicked, call the main activity
        this.binding.btnExpireRefreshToken.setOnClickListener {
            this.onExpireRefreshToken()
        }

        // When logout is clicked, call the main activity
        this.binding.btnLogout.setOnClickListener {
            this.onLogout()
        }
    }

    /*
     * Set the button state when the main fragment loads
     */
    fun setButtonEnabledState(enabled: Boolean) {

        this.binding.btnReloadData.isEnabled = enabled
        this.binding.btnExpireAccessToken.isEnabled = enabled
        this.binding.btnExpireRefreshToken.isEnabled = enabled
        this.binding.btnLogout.isEnabled = enabled
    }
}
