package authguidance.mobilesample.logic.fragments

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import authguidance.mobilesample.R
import authguidance.mobilesample.databinding.FragmentErrorBinding
import authguidance.mobilesample.logic.activities.MainActivity
import authguidance.mobilesample.logic.adapters.ErrorItemArrayAdapter
import authguidance.mobilesample.plumbing.errors.ErrorField
import authguidance.mobilesample.plumbing.errors.UIError
import authguidance.mobilesample.plumbing.utilities.Constants

/*
 * The fragment to show error details
 */
class ErrorFragment : Fragment() {

    private lateinit var binding: FragmentErrorBinding
    private lateinit var mainActivity: MainActivity
    private var error: UIError? = null

    /*
     * Get a reference to the main activity
     */
    override fun onAttach(context: Context?) {
        super.onAttach(context)
        this.mainActivity = context as MainActivity
    }

    /*
     * Initialise the view
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        // Get data passed in
        this.error = this.arguments?.getSerializable(Constants.ARG_ERROR_DATA) as UIError

        // Inflate the view
        this.binding = FragmentErrorBinding.inflate(inflater, container, false)
        return binding.root
    }

    /*
     * Wire up button click events to call back the activity
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        this.renderData()
    }

    /*
     * Render error items in the list view
     */
    private fun renderData() {

        if(this.error != null) {

            // Get data to render
            val items = this.getErrorItemList(this.error!!)

            // Output it in a list
            val list = getView()?.findViewById<ListView>(R.id.listErrorItems)
            list?.adapter = ErrorItemArrayAdapter(mainActivity, items.toList())
        }
    }

    /*
     * Translate the error object into an object list to be rendered in a list view
     */
    private fun getErrorItemList(error: UIError): List<ErrorField> {

        val result = ArrayList<ErrorField>()

        if (!error.message.isNullOrBlank()) {
            result.add(ErrorField("UserMessage", error.message))
        }

        if (!error.area.isNullOrBlank()) {
            result.add(ErrorField("Area", error.area))
        }

        if (!error.errorCode.isNullOrBlank()) {
            result.add(ErrorField("Error Code", error.errorCode))
        }

        if (!error.utcTime.isNullOrBlank()) {
            result.add(ErrorField("UTC Time", error.utcTime))
        }

        if (error.statusCode != 0) {
            result.add(ErrorField("Status Code", error.statusCode.toString()))
        }

        if (error.instanceId != 0) {
            result.add(ErrorField("Instance Id", error.instanceId.toString()))
        }

        if (!error.details.isNullOrBlank()) {
            result.add(ErrorField("Details", error.details))
        }

        if (!error.url.isNullOrBlank()) {
            result.add(ErrorField("URL", error.url))
        }

        return result
    }
}
