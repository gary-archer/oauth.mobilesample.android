package com.authsamples.finalmobileapp.views.transactions

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.authsamples.finalmobileapp.R
import com.authsamples.finalmobileapp.plumbing.events.NavigatedEvent
import com.authsamples.finalmobileapp.plumbing.events.ReloadDataEvent
import com.authsamples.finalmobileapp.views.errors.ErrorSummaryView
import com.authsamples.finalmobileapp.views.errors.ErrorViewModel
import com.authsamples.finalmobileapp.views.utilities.CustomColors
import com.authsamples.finalmobileapp.views.utilities.MainView
import com.authsamples.finalmobileapp.views.utilities.NavigationHelper
import com.authsamples.finalmobileapp.views.utilities.TextStyles
import com.authsamples.finalmobileapp.views.utilities.ViewLoadOptions
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/*
 * The transactions view renders detailed information per company
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun TransactionsView(
    companyId: String,
    model: TransactionsViewModel,
    navigationHelper: NavigationHelper,
) {

    /*
     * Ask the view model to load data
     */
    fun loadData(options: ViewLoadOptions? = null) {

        // Navigate back to the home view if a deep link tries to access unauthorized data
        val onForbidden = {
            navigationHelper.navigateToPath(MainView.COMPANIES)
        }

        // Ask the model class to do the work
        model.callApi(companyId, options, onForbidden)
    }

    /*
     * Create an event subscriber and handle reload events
     */
    val subscriber = object {

        @Subscribe(threadMode = ThreadMode.MAIN)
        fun onMessageEvent(event: ReloadDataEvent) {
            loadData(ViewLoadOptions(true, event.causeError))
        }
    }

    // Manage view loads and unloads
    DisposableEffect(Unit) {

        // Register for events and notify that the main view has changed
        model.eventBus.register(subscriber)
        model.eventBus.post(NavigatedEvent(true))

        // Do the initial data load
        loadData()

        // Unregister for events on disposal
        onDispose {
            model.eventBus.unregister(subscriber)
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        // Render the header in an app bar
        TopAppBar(
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = CustomColors.primary,
            ),
            title = {
                Text(
                    text = String.format(stringResource(R.string.transactions_title), companyId),
                    style = TextStyles.header,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentSize(),
                )
            },
        )

        // Render a scrollable list on success
        if (model.transactionsList.value.isNotEmpty()) {

            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState()),
            ) {
                model.transactionsList.value.forEach { transaction ->
                    TransactionsItemView(transaction)
                }
            }
        }

        // Render error details on failure
        if (model.error.value != null) {

            ErrorSummaryView(
                ErrorViewModel(
                    model.error.value!!,
                    stringResource(R.string.transactions_error_hyperlink),
                    stringResource(R.string.transactions_error_dialogtitle),
                ),
                Modifier
                    .fillMaxWidth()
                    .wrapContentSize()
                    .padding(top = 10.dp),
            )
        }
    }
}
