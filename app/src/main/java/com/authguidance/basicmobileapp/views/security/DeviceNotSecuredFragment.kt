package com.authguidance.basicmobileapp.views.security

import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.authguidance.basicmobileapp.databinding.FragmentDeviceNotSecuredBinding
import com.authguidance.basicmobileapp.views.utilities.Constants

/*
 * Handle prompting the user to secure their lock screen
 */
class DeviceNotSecuredFragment : androidx.fragment.app.Fragment() {

    // Binding properties
    private lateinit var binding: FragmentDeviceNotSecuredBinding

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
     * Initialise the view
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Handle the click event programmatically
        this.binding.btnOpenDeviceSettings.setOnClickListener {

            // Invoke the system dialog from the activity so that request codes are handled as expected
            val intent = Intent(DevicePolicyManager.ACTION_SET_NEW_PASSWORD)
            (this.context as Activity).startActivityForResult(intent, Constants.SET_LOCK_SCREEN_REQUEST_CODE)
        }
    }
}