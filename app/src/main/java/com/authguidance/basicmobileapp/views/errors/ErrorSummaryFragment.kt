package com.authguidance.basicmobileapp.views.errors

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

    /*
     * Initialise the view
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the binding
        this.binding = FragmentErrorSummaryBinding.inflate(inflater, container, false)

        // Create the view model with default settings
        this.binding.model = ErrorSummaryViewModel(this::showDetailsDialog)
        return binding.root
    }

    /*
     * Set the title and store the details we will render in a modal dialog later
     */
    fun reportError(hyperlinkText: String, dialogTitle: String, error: UIError) {

        // Record error details unless this a login is required, which is not a real error
        if (!error.errorCode.equals(ErrorCodes.loginRequired)) {
            this.binding.model?.setErrorDetails(hyperlinkText, dialogTitle, error)
        }
    }

    /*
     * Clear error details when required
     */
    fun clearError() {
        this.binding.model?.clearErrorDetails()
    }

    /*
     * Invoke a modal error details dialog when the red error summary text is clicked
     */
    private fun showDetailsDialog(dialogTitle: String, error: UIError) {

        val dialog = ErrorDetailsDialogFragment.create(dialogTitle, error)
        dialog.show(this.childFragmentManager, "Error Details")
    }
}
