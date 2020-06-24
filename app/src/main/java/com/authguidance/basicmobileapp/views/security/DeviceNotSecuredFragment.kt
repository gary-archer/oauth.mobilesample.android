package com.authguidance.basicmobileapp.views.security

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.authguidance.basicmobileapp.app.MainActivity
import com.authguidance.basicmobileapp.databinding.FragmentDeviceNotSecuredBinding

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
            (this.context as MainActivity).openLockScreenSettings()
        }
    }
}
