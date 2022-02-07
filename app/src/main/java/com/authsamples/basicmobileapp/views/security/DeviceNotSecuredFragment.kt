package com.authsamples.basicmobileapp.views.security

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.authsamples.basicmobileapp.app.MainActivity
import com.authsamples.basicmobileapp.databinding.FragmentDeviceNotSecuredBinding
import com.authsamples.basicmobileapp.plumbing.events.NavigatedEvent
import org.greenrobot.eventbus.EventBus

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
    ): View {

        // Inflate the layout
        this.binding = FragmentDeviceNotSecuredBinding.inflate(inflater, container, false)

        // Notify that the main view has changed
        EventBus.getDefault().post(NavigatedEvent(false))
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
