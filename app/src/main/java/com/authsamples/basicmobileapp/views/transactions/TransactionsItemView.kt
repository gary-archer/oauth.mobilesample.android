package com.authsamples.basicmobileapp.views.transactions

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.authsamples.basicmobileapp.R
import com.authsamples.basicmobileapp.api.entities.Transaction
import com.authsamples.basicmobileapp.views.utilities.TextStyles
import com.authsamples.basicmobileapp.views.utilities.pxToDp
import java.util.Locale

@Composable
@Suppress("LongMethod")
fun TransactionsItemView(transaction: Transaction) {

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
                text = stringResource(R.string.transaction_id),
                style = TextStyles.label,
                textAlign = TextAlign.Left,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = (size.value.width / 12).pxToDp())
                    .fillMaxWidth(fraction = 0.5f)
            )

            Text(
                text = transaction.id,
                style = TextStyles.value,
                textAlign = TextAlign.Left,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = (size.value.width / 12).pxToDp())
                    .fillMaxWidth(fraction = 0.5f)
            )
        }

        Row(
            modifier = Modifier.padding(10.dp)
        ) {

            Text(
                text = stringResource(R.string.investor_id),
                style = TextStyles.label,
                textAlign = TextAlign.Left,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = (size.value.width / 12).pxToDp())
                    .fillMaxWidth(fraction = 0.5f)
            )

            Text(
                text = transaction.investorId,
                style = TextStyles.value,
                textAlign = TextAlign.Left,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = (size.value.width / 12).pxToDp())
                    .fillMaxWidth(fraction = 0.5f)
            )
        }

        Row(
            modifier = Modifier.padding(10.dp)
        ) {

            Text(
                text = stringResource(R.string.amount_usd),
                style = TextStyles.label,
                textAlign = TextAlign.Left,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = (size.value.width / 12).pxToDp())
                    .fillMaxWidth(fraction = 0.5f)
            )

            Text(
                text = String.format(Locale.getDefault(), "%,d", transaction.amountUsd),
                style = TextStyles.money,
                textAlign = TextAlign.Left,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = (size.value.width / 12).pxToDp())
                    .fillMaxWidth(fraction = 0.5f)
            )
        }

        HorizontalDivider(thickness = 1.dp, modifier = Modifier.padding(10.dp))
    }
}
