package com.authguidance.basicmobileapp.views.dialogs

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.authguidance.basicmobileapp.plumbing.errors.UIError
import com.authguidance.basicmobileapp.plumbing.utilities.Constants
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.authguidance.basicmobileapp.R
import com.authguidance.basicmobileapp.databinding.FragmentErrorDetailsBinding
import com.authguidance.basicmobileapp.plumbing.errors.ErrorField
import com.authguidance.basicmobileapp.views.activities.MainActivity
import com.authguidance.basicmobileapp.views.adapters.ErrorItemArrayAdapter

/*
 * A custom modal dialog based on the error details fragment
 */
class ErrorDetailsDialogFragment : DialogFragment() {

    private lateinit var binding: FragmentErrorDetailsBinding
    private lateinit var mainActivity: MainActivity
    private var error: UIError? = null

    /*
     * The factory method to create the dialog
     */
    companion object {
        fun create(error: UIError): ErrorDetailsDialogFragment {
            val dialog = ErrorDetailsDialogFragment()
            val args = Bundle()
            args.putSerializable(Constants.ARG_ERROR_DATA, error)
            dialog.arguments = args
            return dialog
        }
    }

    /*
     * Get a reference to the main activity
     */
    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.mainActivity = context as MainActivity
    }

    /*
     * Inflate the view when created
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        // Get data passed in
        this.error = this.arguments?.getSerializable(Constants.ARG_ERROR_DATA) as UIError

        // Inflate the layout
        this.binding = FragmentErrorDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    /*
     * Initialise the error view from data
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        this.binding.fragmentHeadingText.text = this.getString(R.string.error_title)
        this.renderData()
    }

    /*
     * Render error items in the list view
     */
    private fun renderData() {

        if(this.error != null) {

            val errorItems = this.getErrorItemList(this.error)

            val list = this.binding.listErrorItems
            list.layoutManager = LinearLayoutManager(this.mainActivity)
            list.adapter = ErrorItemArrayAdapter(mainActivity, errorItems)
            list.adapter = ErrorItemArrayAdapter(mainActivity, errorItems)
        }
    }

    /*
     * Translate the error object into an object list to be rendered in a list view
     */
    private fun getErrorItemList(error: UIError?): List<ErrorField> {

        val result = ArrayList<ErrorField>()

        if(error != null) {

            // Show production details
            if (!error.message.isNullOrBlank()) {
                result.add(ErrorField(this.getString(R.string.error_user_message), error.message))
            }

            if (!error.area.isNullOrBlank()) {
                result.add(ErrorField(this.getString(R.string.error_area), error.area))
            }

            if (!error.errorCode.isNullOrBlank()) {
                result.add(ErrorField(this.getString(R.string.error_code), error.errorCode))
            }

            if (!error.appAuthCode.isNullOrBlank()) {
                result.add(
                    ErrorField(
                        this.getString(R.string.error_appauth_code),
                        error.appAuthCode
                    )
                )
            }

            if (!error.utcTime.isNullOrBlank()) {
                result.add(ErrorField(this.getString(R.string.error_utc_time), error.utcTime))
            }

            if (error.statusCode != 0) {
                result.add(
                    ErrorField(
                        this.getString(R.string.error_status),
                        error.statusCode.toString()
                    )
                )
            }

            if (error.instanceId != 0) {
                result.add(
                    ErrorField(
                        this.getString(R.string.error_instance_id),
                        error.instanceId.toString()
                    )
                )
            }

            if (!error.details.isNullOrBlank()) {
                result.add(ErrorField(this.getString(R.string.error_details), error.details))
            }

            // Show additional technical details when configured
            if (!error.url.isNullOrBlank()) {
                result.add(ErrorField(this.getString(R.string.error_url), error.url))
            }

            if (error.stackFrames.size > 0) {
                result.add(
                    ErrorField(
                        this.getString(R.string.error_stack),
                        error.stackFrames.joinToString("\n\n")
                    )
                )
            }
        }

        return result
    }
}
