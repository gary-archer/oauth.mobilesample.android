package com.authsamples.finalmobileapp.views.companies

import androidx.compose.foundation.clickable
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
import com.authsamples.finalmobileapp.R
import com.authsamples.finalmobileapp.api.entities.Company
import com.authsamples.finalmobileapp.views.utilities.MainView
import com.authsamples.finalmobileapp.views.utilities.NavigationHelper
import com.authsamples.finalmobileapp.views.utilities.TextStyles
import com.authsamples.finalmobileapp.views.utilities.pxToDp
import java.util.Locale

@Composable
@Suppress("LongMethod")
fun CompaniesItemView(company: Company, navigationHelper: NavigationHelper) {

    /*
     * Handle navigation when an item is clicked
     */
    fun moveToTransactions() {
        navigationHelper.navigateToPath("${MainView.Transactions}/${company.id}")
    }

    val size = remember { mutableStateOf(IntSize.Zero) }

    Column(
        modifier = Modifier
            .padding(top = 10.dp)
            .clickable {
                moveToTransactions()
            }
            .onGloballyPositioned { coordinates ->
                size.value = coordinates.size
            }

    ) {
        Row(
            modifier = Modifier.padding(10.dp)
        ) {

            Text(
                text = company.name,
                style = TextStyles.header,
                textAlign = TextAlign.Left,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = (size.value.width / 12).pxToDp())
                    .fillMaxWidth(fraction = 0.5f)
            )

            Text(
                text = company.region,
                style = TextStyles.header,
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
                text = stringResource(R.string.target_usd),
                style = TextStyles.label,
                textAlign = TextAlign.Left,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = (size.value.width / 12).pxToDp())
                    .fillMaxWidth(fraction = 0.5f)
            )

            Text(
                text = String.format(Locale.getDefault(), "%,d", company.targetUsd),
                style = TextStyles.money,
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
                text = stringResource(R.string.investment_usd),
                style = TextStyles.label,
                textAlign = TextAlign.Left,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = (size.value.width / 12).pxToDp())
                    .fillMaxWidth(fraction = 0.5f)
            )

            Text(
                text = String.format(Locale.getDefault(), "%,d", company.investmentUsd),
                style = TextStyles.money,
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
                text = stringResource(R.string.no_investors),
                style = TextStyles.label,
                textAlign = TextAlign.Left,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = (size.value.width / 12).pxToDp())
                    .fillMaxWidth(fraction = 0.5f)
            )

            Text(
                text = String.format(Locale.getDefault(), "%d", company.noInvestors),
                style = TextStyles.value,
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
