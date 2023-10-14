package com.authsamples.basicmobileapp.views.errors

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.authsamples.basicmobileapp.views.utilities.TextStyles

@Composable
fun ErrorDetailsView(onDismiss: () -> Unit) {

    Text(
        text = "ERROR DETAILS INNIT",
        style = TextStyles.value,
        textAlign = TextAlign.Left,
        modifier = Modifier.fillMaxWidth().padding(10.dp).clickable { onDismiss() }
    )
}
