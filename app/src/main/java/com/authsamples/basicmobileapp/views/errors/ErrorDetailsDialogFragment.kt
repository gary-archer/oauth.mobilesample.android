package com.authsamples.basicmobileapp.views.errors

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.authsamples.basicmobileapp.databinding.FragmentErrorDetailsBinding
import com.authsamples.basicmobileapp.plumbing.errors.UIError
import com.authsamples.basicmobileapp.views.utilities.ViewConstants

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
            args.putString(ViewConstants.ARG_ERROR_TITLE, dialogTitle)
            args.putSerializable(ViewConstants.ARG_ERROR_DATA, error)
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
    ): View {

        // Inflate the layout
        this.binding = FragmentErrorDetailsBinding.inflate(inflater, container, false)

        // Create the view model from data passed in
        @Suppress("DEPRECATION")
        val error = this.arguments?.getSerializable(ViewConstants.ARG_ERROR_DATA) as UIError
        val title = this.arguments?.getString(ViewConstants.ARG_ERROR_TITLE)
        val factory = ErrorDetailsViewModelFactory(title!!, error, this::dismiss)
        this.binding.model = ViewModelProvider(this, factory)[ErrorDetailsViewModel::class.java]

        // Create the recycler view
        val list = this.binding.listErrorItems
        list.layoutManager = LinearLayoutManager(this.context)
        list.adapter = ErrorItemArrayAdapter(this.requireContext(), this.binding.model!!)

        return binding.root
    }
}
