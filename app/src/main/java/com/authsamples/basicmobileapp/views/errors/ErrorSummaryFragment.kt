package com.authsamples.basicmobileapp.views.errors

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.authsamples.basicmobileapp.databinding.FragmentErrorSummaryBinding
import com.authsamples.basicmobileapp.plumbing.errors.ErrorCodes
import com.authsamples.basicmobileapp.plumbing.errors.UIError

/*
 * The fragment to show an initial error indication
 */
class ErrorSummaryFragment : androidx.fragment.app.Fragment() {

    private lateinit var binding: FragmentErrorSummaryBinding
    private lateinit var keyName: String

    /*
     * Initialise the view
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Inflate the binding
        this.binding = FragmentErrorSummaryBinding.inflate(inflater, container, false)

        // Create the view model with default settings
        val factory = ErrorSummaryViewModelFactory(this::showDetailsDialog)
        this.binding.model = ViewModelProvider(this, factory)[this.keyName, ErrorSummaryViewModel::class.java]
        return binding.root
    }

    /*
     * Receive the error data from the parent view and update the model
     */
    fun receiveErrorFromParent(data: ErrorSummaryViewModelData) {

        if (data.error == null) {

            // Clear any existing error details
            this.binding.model!!.clearErrorDetails()

        } else {

            // Populate any error details but ignore expected errors
            if (!data.error.errorCode.equals(ErrorCodes.loginRequired)) {

                this.binding.model!!.setErrorDetails(
                    data.hyperlinkText,
                    data.dialogTitle,
                    data.error
                )
            }
        }
    }

    /*
     * Invoke a modal error details dialog when the red error summary text is clicked
     */
    private fun showDetailsDialog(dialogTitle: String, error: UIError) {

        val dialog = ErrorDetailsDialogFragment.create(dialogTitle, error)
        dialog.show(this.childFragmentManager, "Error Details")
    }
}
