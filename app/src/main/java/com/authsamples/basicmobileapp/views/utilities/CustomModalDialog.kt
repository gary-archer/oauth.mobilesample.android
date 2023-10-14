package com.authsamples.basicmobileapp.views.utilities

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/*
 * A custom modal dialog based on a card
 */
@Composable
fun CustomModalDialog(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier,
    ) {
        Column(content = content)
    }
}