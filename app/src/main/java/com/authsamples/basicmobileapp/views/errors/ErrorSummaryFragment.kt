package com.authsamples.basicmobileapp.views.errors

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.BindingAdapter
import androidx.fragment.app.FragmentContainerView
import androidx.lifecycle.ViewModelProvider
import com.authsamples.basicmobileapp.R
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
     * Read fields specified in the markup file, for this instance of the error summary fragment
     */
    override fun onInflate(context: Context, attrs: AttributeSet, savedInstanceState: Bundle?) {
        super.onInflate(context, attrs, savedInstanceState)

        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.ErrorAttributes)
        val keyName = typedArray.getString(R.styleable.ErrorAttributes_keyName)
        if (keyName != null) {
            this.keyName = keyName
        }

        typedArray.recycle()
    }

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
        this.binding.model = ViewModelProvider(this, factory).get(this.keyName, ErrorSummaryViewModel::class.java)
        return binding.root
    }

    /*
     * Receive the error from the parent view and update the model
     */
    companion object {
        @JvmStatic
        @BindingAdapter("error")
        fun setCurrentError(view: FragmentContainerView, data: ErrorSummaryViewModelData) {

            val errorSummaryView = view.getFragment<ErrorSummaryFragment>()
            if (data.error == null) {

                // Clear any existing error details
                errorSummaryView.binding.model!!.clearErrorDetails()

            } else {

                // Populate any error details but ignore expected errors
                if (!data.error.errorCode.equals(ErrorCodes.loginRequired)) {

                    errorSummaryView.binding.model!!.setErrorDetails(
                        data.hyperlinkText,
                        data.dialogTitle,
                        data.error
                    )
                }
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
