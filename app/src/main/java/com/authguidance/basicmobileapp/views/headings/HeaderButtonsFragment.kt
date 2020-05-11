package com.authguidance.basicmobileapp.views.headings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.authguidance.basicmobileapp.app.MainActivity
import com.authguidance.basicmobileapp.databinding.FragmentHeaderButtonsBinding

/*
 * A simple fragment with the header buttons
 */
class HeaderButtonsFragment : androidx.fragment.app.Fragment() {

    private lateinit var binding: FragmentHeaderButtonsBinding

    /*
     * Initialise the view
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout
        this.binding = FragmentHeaderButtonsBinding.inflate(inflater, container, false)

        // Create the view model
        val activityState = (this.context as MainActivity).getChildViewModelState()
        this.binding.model = HeaderButtonsViewModel(
            activityState.isDataLoadedAccessor,
            activityState.onHome,
            activityState.onReload,
            activityState.onExpireAccessToken,
            activityState.onExpireRefreshToken,
            activityState.onLogout
        )

        return this.binding.root
    }

    /*
     * Wire up the reload click event which uses special handling to support long press events
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val model = this.binding.model!!
        this.binding.btnReloadData.setCustomClickListener(model.onReload)
    }

    /*
     * Called when the buttons need to redraw their enabled state
     */
    fun update() {
        this.binding.model?.updateSessionButtonEnabledState()
    }
}
