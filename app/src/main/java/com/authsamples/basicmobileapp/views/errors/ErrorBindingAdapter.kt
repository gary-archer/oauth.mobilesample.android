package com.authsamples.basicmobileapp.views.errors

import androidx.databinding.BindingAdapter
import androidx.fragment.app.FragmentContainerView

/*
 * A fragment such as companies uses a FragmentContainerView with an app:error element
 * This passes the error data to the error summary fragment, causing this binding adapter to be called
 */
object ErrorBindingAdapter {
    @BindingAdapter("error")
    @JvmStatic
    fun receiveErrorFromParent(view: FragmentContainerView, data: ErrorSummaryViewModelData) {

        val errorSummaryView = view.getFragment<ErrorSummaryFragment>()
        errorSummaryView.receiveErrorFromParent(data)
    }
}
