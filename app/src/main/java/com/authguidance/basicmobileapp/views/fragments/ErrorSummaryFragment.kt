package com.authguidance.basicmobileapp.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.authguidance.basicmobileapp.R
import com.authguidance.basicmobileapp.databinding.FragmentErrorSummaryBinding
import com.authguidance.basicmobileapp.plumbing.errors.UIError
import com.authguidance.basicmobileapp.plumbing.utilities.Constants

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

        // Get data passed in
        this.error = this.arguments?.getSerializable(Constants.ARG_ERROR_DATA) as UIError

        // Inflate the view
        this.binding = FragmentErrorSummaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    /*
     * Initialise the error view from data
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        this.binding.fragmentHeadingText.text = this.getString(R.string.error_title)
    }
}