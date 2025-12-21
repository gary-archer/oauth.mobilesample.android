package com.authsamples.finalmobileapp.views.errors

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.authsamples.finalmobileapp.plumbing.errors.ErrorFormatter
import com.authsamples.finalmobileapp.plumbing.errors.ErrorLine
import com.authsamples.finalmobileapp.views.utilities.CustomColors
import com.authsamples.finalmobileapp.views.utilities.TextStyles

@SuppressLint("MutableCollectionMutableState")
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ErrorDetailsView(model: ErrorViewModel, onDismiss: () -> Unit) {

    // Rendered state
    val lines: MutableState<List<ErrorLine>> = remember { mutableStateOf(ArrayList()) }

    // When the view loads, process the error to create error line objects
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        lines.value = ErrorFormatter(context).getErrorLines(model.error)
    }

    Column {

        // Render the title and an X button to close
        Box(
            modifier = Modifier
                .background(CustomColors.primary)
                .height(56.dp)
                .wrapContentHeight(align = Alignment.CenterVertically),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
            ) {

                Text(
                    text = model.dialogTitle,
                    style = TextStyles.header,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .weight(20f),
                )

                Text(
                    text = "X",
                    textAlign = TextAlign.Right,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 10.dp)
                        .clickable { onDismiss() },
                )
            }
        }

        // Next render a scrollable list of error lines
        if (lines.value.isNotEmpty()) {

            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState()),
            ) {
                lines.value.forEach { errorLine ->
                    ErrorDetailsItemView(errorLine)
                }
            }
        }
    }
}
