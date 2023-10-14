package com.authsamples.basicmobileapp.views.errors

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.authsamples.basicmobileapp.plumbing.errors.ErrorFormatter
import com.authsamples.basicmobileapp.plumbing.errors.ErrorLine
import com.authsamples.basicmobileapp.views.utilities.CustomColors
import com.authsamples.basicmobileapp.views.utilities.TextStyles

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ErrorDetailsView(model: ErrorViewModel, onDismiss: () -> Unit) {

    var lines: ArrayList<ErrorLine>
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        lines = ErrorFormatter(context).getErrorLines(model.error)
    }

    Column(

    ) {
        // First show the title and an X button to close
        TopAppBar(
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = CustomColors.primary
            ),
            title = {

                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Text(
                        text = model.dialogTitle,
                        style = TextStyles.header,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .weight(20f)
                    )

                    Text(
                        text = "X",
                        textAlign = TextAlign.Right,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 10.dp)
                            .clickable { onDismiss() }
                    )
                }
            }
        )
    }
}
