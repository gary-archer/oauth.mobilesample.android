package com.authsamples.basicmobileapp.views.errors

import androidx.compose.foundation.clickable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.authsamples.basicmobileapp.views.utilities.TextStyles

@Composable
fun ErrorSummaryView(data: ErrorSummaryViewModelData, modifier: Modifier) {

    Text(
        text = data.hyperlinkText,
        style = TextStyles.error,
        textAlign = TextAlign.Center,
        modifier = modifier.clickable {

            // val dialog = ErrorDetailsDialogFragment.create(data.dialogTitle, data.error!!)
            // dialog.show(null, "Error Details")
        }
    )
}
