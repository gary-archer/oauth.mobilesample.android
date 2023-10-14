package com.authsamples.basicmobileapp.views.errors

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.authsamples.basicmobileapp.plumbing.errors.ErrorCodes
import com.authsamples.basicmobileapp.views.utilities.TextStyles

/*
 * The error summary view is a simple hyperlink that invokes a dialog with details
 */
@Composable
fun ErrorSummaryView(model: ErrorViewModel, modifier: Modifier) {

    val showDialog = remember { mutableStateOf(false) }

    // Ignore non errors
    if (model.error.errorCode == ErrorCodes.loginRequired) {
        return
    }

    // Render the hyperlink
    Text(
        text = model.hyperlinkText,
        style = TextStyles.error,
        textAlign = TextAlign.Center,
        modifier = modifier.clickable {
            showDialog.value = true
        }
    )

    // Show the modal dialog when the hyperlink is clicked
    if (showDialog.value) {

        val onDismiss = { showDialog.value = false }
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(usePlatformDefaultWidth = false),
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color.White,
                ),
                modifier = Modifier
                    .fillMaxHeight(0.95f)
                    .fillMaxWidth(1f)
            ) {
                ErrorDetailsView(model, onDismiss)
            }
        }
    }
}
