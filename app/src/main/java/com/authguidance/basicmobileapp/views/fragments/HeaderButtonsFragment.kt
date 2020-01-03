package com.authguidance.basicmobileapp.views.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.authguidance.basicmobileapp.R
import com.authguidance.basicmobileapp.databinding.FragmentHeaderButtonsBinding
import com.authguidance.basicmobileapp.views.activities.MainActivity

/*
 * A simple fragment with the header buttons
 */
class HeaderButtonsFragment : androidx.fragment.app.Fragment() {

    private lateinit var binding: FragmentHeaderButtonsBinding
    private lateinit var mainActivity: MainActivity
    private var startupError: Boolean = false

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

            if(this.startupError) {
                // If there was a startup error, ask the main activity to exit
                this.mainActivity.onAbortStartup()
            }
            else {
                // Navigate home otherwise
                this.mainActivity.onHome()
            }
        }

        // When refresh is clicked, handle the click in the active primary fragment
        this.binding.btnReloadData.setOnClickListener {

            val fragment = this.mainActivity.navHostFragment.childFragmentManager.primaryNavigationFragment
            if(fragment is ReloadableFragment) {
                fragment.loadData()
            }
        }

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
     * If there is a startup error
     */
    fun setStartupErrorState() {
        this.startupError = true
        this.binding.btnHome.text = this.getString(R.string.exit_app)
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
