package com.authsamples.basicmobileapp.views.transactions

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.authsamples.basicmobileapp.plumbing.events.NavigatedEvent
import com.authsamples.basicmobileapp.plumbing.events.ReloadDataEvent
import com.authsamples.basicmobileapp.views.utilities.NavigationHelper
import com.authsamples.basicmobileapp.views.utilities.TextStyles
import com.authsamples.basicmobileapp.views.utilities.ViewLoadOptions
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/*
 * The transactions view renders detailed information per company
 */
@Composable
fun TransactionsView(
    companyId: String,
    model: TransactionsViewModel,
    navigationHelper: NavigationHelper
) {

    /*
     * Ask the view model to load data
     */
    fun loadData(options: ViewLoadOptions? = null) {

        // Navigate back to the home view if a deep link tries to access unauthorized data
        val onForbidden = {
            navigationHelper.navigateTo("companies")
        }

        // Ask the model class to do the work
        println("GJA: calling API")
        model.callApi(companyId, options, onForbidden)
        println("GJA: called API")
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
    DisposableEffect(key1 = null) {

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

    Column {
        Text(
            text = "Today's Transactions for Company $companyId",
            style = TextStyles.header,
            textAlign = TextAlign.Left,
            modifier = Modifier.weight(1f)
        )
    }
}
