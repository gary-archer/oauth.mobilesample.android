package com.authguidance.basicmobileapp.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.authguidance.basicmobileapp.databinding.FragmentErrorSummaryBinding
import com.authguidance.basicmobileapp.plumbing.errors.ErrorCodes
import com.authguidance.basicmobileapp.plumbing.errors.UIError

/*
 * The fragment to show an initial error indication
 */
class ErrorSummaryFragment : androidx.fragment.app.Fragment() {

    private lateinit var binding: FragmentErrorSummaryBinding
    private var error: UIError? = null

    /*
     * Initialise the view
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        this.binding = FragmentErrorSummaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    /*
     * Set the title and store the details to render in a modal dialog
     */
    fun reportError(title: String, error: UIError) {

        // Record error details unless this a login is required, which is not a real error
        if (!error.errorCode.equals(ErrorCodes.loginRequired)) {
            this.binding.errorSummaryText.text = title
            this.error = error
        }

        println("GJA: Error Summary: ${error.errorCode}, ${error.message}")
    }

    /*
     * Clear error details when required
     */
    fun clearError() {
        this.binding.errorSummaryText.text = ""
    }

    /*
     * TODO: Invoke a modal dialog when the red summary text is clicked
     */
    private fun onClick() {

        // Otherwise navigate to the error fragment and render error details
        /*val args = Bundle()
        args.putSerializable(Constants.ARG_ERROR_DATA, error as Serializable)
        NavigationHelper().navigate(
            this.navController,
            this.navHostFragment.childFragmentManager.primaryNavigationFragment,
            R.id.errorFragment,
            args)*/
    }
}