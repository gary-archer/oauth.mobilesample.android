package com.authsamples.basicmobileapp.views.errors

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidViewBinding
import com.authsamples.basicmobileapp.databinding.FragmentErrorContainerBinding

@Composable
fun ErrorContainerViewBinding(data: ErrorSummaryViewModelData) {

    AndroidViewBinding(FragmentErrorContainerBinding::inflate) {
        val errorSummaryFragment = errorContainerFragment.getFragment<ErrorSummaryFragment>()
        errorSummaryFragment.receiveErrorFromParent(data)
    }
}
