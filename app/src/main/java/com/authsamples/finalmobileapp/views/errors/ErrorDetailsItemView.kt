package com.authsamples.finalmobileapp.views.errors

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.authsamples.finalmobileapp.plumbing.errors.ErrorLine
import com.authsamples.finalmobileapp.views.utilities.TextStyles

/*
 * Render a label and value for an error field
 */
@Composable
fun ErrorDetailsItemView(errorLine: ErrorLine) {

    val size = remember { mutableStateOf(IntSize.Zero) }

    Column(
        modifier = Modifier
            .padding(top = 10.dp)
            .onGloballyPositioned { coordinates ->
                size.value = coordinates.size
            }
    ) {
        Row(
            modifier = Modifier.padding(10.dp)
        ) {

            Text(
                text = errorLine.name,
                style = TextStyles.label,
                textAlign = TextAlign.Left,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 10.dp)
                    .fillMaxWidth(fraction = 0.5f)
            )

            Text(
                text = errorLine.value,
                style = TextStyles.value,
                color = errorLine.valueColor,
                textAlign = TextAlign.Left,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 10.dp, end = 5.dp)
                    .fillMaxWidth(fraction = 0.5f)
            )
        }

        HorizontalDivider(thickness = 1.dp, modifier = Modifier.padding(10.dp))
    }
}
