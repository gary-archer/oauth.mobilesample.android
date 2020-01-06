package com.authguidance.basicmobileapp.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.authguidance.basicmobileapp.databinding.FragmentErrorSummaryBinding
import com.authguidance.basicmobileapp.plumbing.errors.ErrorCodes
import com.authguidance.basicmobileapp.plumbing.errors.UIError
import com.authguidance.basicmobileapp.views.dialogs.ErrorDetailsDialogFragment

/*
 * The fragment to show an initial error indication
 */
class ErrorSummaryFragment : androidx.fragment.app.Fragment() {

    private lateinit var binding: FragmentErrorSummaryBinding
    private var error: UIError? = null
    private var dialogTitle: String = "";

    /*
     * Initialise the view
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        this.binding = FragmentErrorSummaryBinding.inflate(inflater, container, false)
        return binding.root
    }


    /*
     * Wire up click events
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        this.binding.errorSummaryText.setOnClickListener {
            this.onClick()
        }
    }

    /*
     * Set the title and store the details to render in a modal dialog
     */
    fun reportError(hyperlinkText: String, dialogTitle: String, error: UIError) {

        // Record error details unless this a login is required, which is not a real error
        if (!error.errorCode.equals(ErrorCodes.loginRequired)) {
            this.binding.errorSummaryText.text = hyperlinkText
            this.dialogTitle = dialogTitle
            this.error = error
        }
    }

    /*
     * Clear error details when required
     */
    fun clearError() {
        this.binding.errorSummaryText.text = ""
    }

    /*
     * Invoke a modal error details dialog when the red error summary text is clicked
     */
    private fun onClick() {

        val error = this.error
        if(error != null) {

            val dialog = ErrorDetailsDialogFragment.create(dialogTitle, error)
            dialog.show(this.childFragmentManager, "errorDetails")
        }
    }
}