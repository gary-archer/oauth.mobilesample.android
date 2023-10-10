package com.authsamples.basicmobileapp.views.errors

import androidx.compose.foundation.clickable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.fragment.app.FragmentActivity
import com.authsamples.basicmobileapp.plumbing.errors.ErrorCodes
import com.authsamples.basicmobileapp.views.utilities.TextStyles

/*
 * The error summary view is a simple hyperlink that invokes a dialog with details
 */
@Composable
fun ErrorSummaryView(data: ErrorSummaryViewModelData, modifier: Modifier) {

    // Get the fragment manager needed to invoke the error details popup
    val fragmentManager = (LocalContext.current as? FragmentActivity)?.supportFragmentManager

    // Ignore non errors
    if (data.error?.errorCode != ErrorCodes.loginRequired) {

        // Render the hyperlink and invoke the details dialog when it is clicked
        Text(
            text = data.hyperlinkText,
            style = TextStyles.error,
            textAlign = TextAlign.Center,
            modifier = modifier.clickable {

                if (fragmentManager != null) {
                    val dialog = ErrorDetailsDialogFragment.create(data.dialogTitle, data.error!!)
                    dialog.show(fragmentManager, "Error Details")
                }
            }
        )
    }
}
