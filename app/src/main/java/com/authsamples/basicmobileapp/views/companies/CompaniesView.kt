package com.authsamples.basicmobileapp.views.companies

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.authsamples.basicmobileapp.views.utilities.NavigationHelper
import com.authsamples.basicmobileapp.views.utilities.TextStyles

/*
 * The companies view renders summary information per company
 */
@Composable
fun CompaniesView(model: CompaniesViewModel, navigationHelper: NavigationHelper) {

    Column {
        Text(
            text = "Companies List",
            style = TextStyles.header,
            textAlign = TextAlign.Left,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = "Companies default item",
            style = TextStyles.label,
            textAlign = TextAlign.Left,
            modifier = Modifier.weight(1f)
        )
    }
}
