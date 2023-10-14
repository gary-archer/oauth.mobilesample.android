package com.authsamples.basicmobileapp.views.errors

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.fragment.app.FragmentActivity
import com.authsamples.basicmobileapp.plumbing.errors.ErrorCodes
import com.authsamples.basicmobileapp.views.utilities.TextStyles

/*
 * The error summary view is a simple hyperlink that invokes a dialog with details
 */
@Composable
fun ErrorSummaryView(data: ErrorSummaryViewModelData, modifier: Modifier) {

    // Ignore non errors
    if (data.error?.errorCode == ErrorCodes.loginRequired) {
        return
    }

    val showDialog = remember { mutableStateOf(false) }

    // Render the hyperlink
    Text(
        text = data.hyperlinkText,
        style = TextStyles.error,
        textAlign = TextAlign.Center,
        modifier = modifier.clickable {
            showDialog.value = true
        }
    )

    val onDismiss = { showDialog.value = false }

    // Show the modal dialog when the hyperlink is clicked
    if (showDialog.value) {

        Dialog(onDismissRequest = onDismiss) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(5.dp),
                //shape = RoundedCornerShape(16.dp),
            ) {
                ErrorDetailsView(onDismiss)
            }
        }
    }
}
