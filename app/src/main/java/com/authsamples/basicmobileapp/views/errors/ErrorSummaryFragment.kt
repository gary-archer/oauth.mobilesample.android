package com.authsamples.basicmobileapp.views.errors

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.authsamples.basicmobileapp.R
import com.authsamples.basicmobileapp.databinding.FragmentErrorSummaryBinding
import com.authsamples.basicmobileapp.plumbing.errors.ErrorCodes
import com.authsamples.basicmobileapp.plumbing.errors.UIError
import com.authsamples.basicmobileapp.plumbing.events.SetErrorEvent
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/*
 * The fragment to show an initial error indication
 */
class ErrorSummaryFragment : androidx.fragment.app.Fragment() {

    private lateinit var binding: FragmentErrorSummaryBinding
    private lateinit var containingViewName: String
    private lateinit var hyperlinkText: String
    private lateinit var dialogTitle: String

    /*
     * Read fields specified in the markup file, for this instance of the error summary fragment
     */
    override fun onInflate(context: Context, attrs: AttributeSet, savedInstanceState: Bundle?) {
        super.onInflate(context, attrs, savedInstanceState)

        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.ErrorAttributes)

        val containingViewName = typedArray.getString(R.styleable.ErrorAttributes_containingViewName)
        if (containingViewName != null) {
            this.containingViewName = containingViewName
        }

        val hyperlinkText = typedArray.getString(R.styleable.ErrorAttributes_hyperlinkText)
        if (hyperlinkText != null) {
            this.hyperlinkText = hyperlinkText
        }

        val dialogTitle = typedArray.getString(R.styleable.ErrorAttributes_dialogTitle)
        if (dialogTitle != null) {
            this.dialogTitle = dialogTitle
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
        this.binding.model = ViewModelProvider(this, factory).get(ErrorSummaryViewModel::class.java)

        return binding.root
    }

    /*
     * Register for events during view initialization
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        EventBus.getDefault().register(this)
    }

    /*
     * Receive and handle errors from the parent view
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: SetErrorEvent) {

        // Ensure that the error is for this instance of the error summary view
        if (event.containingViewName == this.containingViewName) {

            if (event.error == null) {

                // Clear previous error details before retrying an operation
                this.binding.model!!.clearErrorDetails()

            } else {

                // Set details unless this is an ignored error, to terminate failed API calls
                if (!event.error.errorCode.equals(ErrorCodes.loginRequired)) {

                    this.binding.model!!.setErrorDetails(
                        this.hyperlinkText,
                        this.dialogTitle,
                        event.error
                    )
                }
            }
        }
    }

    /*
     * Unsubscribe from events upon exit
     */
    override fun onDestroyView() {
        super.onDestroyView()
        EventBus.getDefault().unregister(this)
    }

    /*
     * Invoke a modal error details dialog when the red error summary text is clicked
     */
    private fun showDetailsDialog(dialogTitle: String, error: UIError) {

        val dialog = ErrorDetailsDialogFragment.create(dialogTitle, error)
        dialog.show(this.childFragmentManager, "Error Details")
    }
}
