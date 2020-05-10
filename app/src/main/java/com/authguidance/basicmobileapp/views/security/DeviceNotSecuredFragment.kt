package com.authguidance.basicmobileapp.views.security

import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.authguidance.basicmobileapp.databinding.FragmentDeviceNotSecuredBinding
import com.authguidance.basicmobileapp.plumbing.utilities.Constants
import com.authguidance.basicmobileapp.plumbing.utilities.DeviceSecurity

/*
 * An empty fragment rendered as the default
 */
class DeviceNotSecuredFragment : androidx.fragment.app.Fragment() {

    // Binding properties
    private lateinit var binding: FragmentDeviceNotSecuredBinding

    /*
     * Get properties from the main activity
     */
    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    /*
     * Initialise the view
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        this.binding = FragmentDeviceNotSecuredBinding.inflate(inflater, container, false)
        return this.binding.root
    }

    /*
     * Wire up button click events to call back the activity
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        this.binding.btnOpenDeviceSettings.setOnClickListener {
        }
    }

    private fun onOpenSettingsClick() {

        // Set top most to false when clicked
        this.openLockScreenSettings()
    }

    /*
     * Handle invoking the lock screen
     */
    private fun openLockScreenSettings() {

        // When the user selects the Settings option, an intent and open Lock Screen Settings
        val agreeCallback = {
            val intent = Intent(DevicePolicyManager.ACTION_SET_NEW_PASSWORD)
            this.startActivityForResult(intent, Constants.SET_LOCK_SCREEN_REQUEST_CODE)
        }

        // When the user selects the Exit option, exit the Android App
        val declineCallback = {
        }

        // Invoke the dialog to prompt the user
        DeviceSecurity.forceLockScreenUpdate(this.requireContext(), agreeCallback, declineCallback)
    }

    private fun onMainActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        // See if this is a response from the lock screen
        if (requestCode == Constants.SET_LOCK_SCREEN_REQUEST_CODE) {
            if (DeviceSecurity.isDeviceSecured(this.requireContext())) {

                // Call back the parent to navigate and reset isTopMost to true
            }
        }
    }
}