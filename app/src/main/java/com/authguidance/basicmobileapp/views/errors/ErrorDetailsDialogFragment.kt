package com.authguidance.basicmobileapp.views.errors

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.authguidance.basicmobileapp.databinding.FragmentErrorDetailsBinding
import com.authguidance.basicmobileapp.plumbing.errors.ErrorFormatter
import com.authguidance.basicmobileapp.plumbing.errors.UIError
import com.authguidance.basicmobileapp.views.utilities.Constants

/*
 * A custom modal dialog based on the error details fragment
 */
class ErrorDetailsDialogFragment : DialogFragment() {

    private lateinit var binding: FragmentErrorDetailsBinding

    /*
     * A factory method to create the dialog
     */
    companion object {

        fun create(dialogTitle: String, error: UIError): ErrorDetailsDialogFragment {
            val dialog = ErrorDetailsDialogFragment()
            val args = Bundle()
            args.putString(Constants.ARG_ERROR_TITLE, dialogTitle)
            args.putSerializable(Constants.ARG_ERROR_DATA, error)
            dialog.arguments = args
            return dialog
        }
    }

    /*
     * Initialise the view
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout
        this.binding = FragmentErrorDetailsBinding.inflate(inflater, container, false)

        // Create the view model from data passed in
        val title = this.arguments?.getString(Constants.ARG_ERROR_TITLE)
        val error = this.arguments?.getSerializable(Constants.ARG_ERROR_DATA) as UIError
        this.binding.model = ErrorDetailsViewModel(title!!, error, this::dismiss)

        return binding.root
    }

    /*
     * Render data after the view is created
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)
        this.renderData()
    }

    /*
     * Render error items in the list view
     */
    private fun renderData() {

        // Get error lines as a collection
        val lines = ErrorFormatter(this.requireContext()).getErrorLines(this.binding.model!!.error)

        // Get view model items from the above data
        val viewModelItems = lines.map {
            ErrorListItemViewModel(it)
        }

        // Render them via an adapter
        val list = this.binding.listErrorItems
        list.layoutManager = LinearLayoutManager(this.context)
        list.adapter = ErrorItemArrayAdapter(this.requireContext(), viewModelItems)
    }
}
