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

    private lateinit var binding: FragmentHeaderButtonsBinding
    private lateinit var mainActivity: MainActivity

    /*
     * Get a reference to the main activity
     */
    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.mainActivity = context as MainActivity
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
            this.mainActivity.onHome()
        }

        // Handle refresh data clicks specially
        this.binding.btnReloadData.setCustomClickListener(this.mainActivity::reloadData)

        // When expire access token is clicked, call the main activity
        this.binding.btnExpireAccessToken.setOnClickListener {
            this.mainActivity.expireAccessToken()
        }

        // When expire refresh token is clicked, call the main activity
        this.binding.btnExpireRefreshToken.setOnClickListener {
            this.mainActivity.expireRefreshToken()
        }

        // When logout is clicked, call the main activity
        this.binding.btnLogout.setOnClickListener {
            this.mainActivity.startLogout()
        }

        this.binding.btnReloadData.isEnabled = false
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
