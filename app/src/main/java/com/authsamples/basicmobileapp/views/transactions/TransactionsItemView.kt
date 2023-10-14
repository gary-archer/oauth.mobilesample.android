package com.authsamples.basicmobileapp.views.transactions

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextAlign
import com.authsamples.basicmobileapp.api.entities.Transaction
import com.authsamples.basicmobileapp.views.utilities.TextStyles

@Composable
fun TransactionsItemView(transaction: Transaction) {

    Text(
        text = transaction.id,
        style = TextStyles.label,
        textAlign = TextAlign.Left
    )
}