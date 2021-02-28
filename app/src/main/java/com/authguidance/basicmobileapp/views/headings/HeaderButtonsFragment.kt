package com.authguidance.basicmobileapp.views.headings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.authguidance.basicmobileapp.app.MainActivitySharedViewModel
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

        // Get details that the main activity supplies to child views
        val sharedViewModel: MainActivitySharedViewModel by activityViewModels()

        // Create this fragment's view model
        this.binding.model = HeaderButtonsViewModel(
            sharedViewModel.isMainViewLoadedAccessor,
            sharedViewModel.onHome,
            sharedViewModel.onReload,
            sharedViewModel.onExpireAccessToken,
            sharedViewModel.onExpireRefreshToken,
            sharedViewModel.onLogout
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
