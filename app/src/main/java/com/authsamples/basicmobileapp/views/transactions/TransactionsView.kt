package com.authsamples.basicmobileapp.views.transactions

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.authsamples.basicmobileapp.views.utilities.NavigationHelper
import com.authsamples.basicmobileapp.views.utilities.TextStyles

/*
 * The transactions view renders detailed information per company
 */
@Composable
fun TransactionsView(
    companyId: String,
    model: TransactionsViewModel,
    navigationHelper: NavigationHelper
) {

    Column {
        Text(
            text = "Today's Transactions for Company $companyId",
            style = TextStyles.header,
            textAlign = TextAlign.Left,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = "Transactions Default Item",
            style = TextStyles.label,
            textAlign = TextAlign.Left,
            modifier = Modifier.weight(1f)
        )
    }
}
