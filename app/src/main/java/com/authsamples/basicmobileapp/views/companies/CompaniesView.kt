package com.authsamples.basicmobileapp.views.companies

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.authsamples.basicmobileapp.plumbing.events.NavigatedEvent
import com.authsamples.basicmobileapp.plumbing.events.ReloadDataEvent
import com.authsamples.basicmobileapp.views.errors.ErrorSummaryView
import com.authsamples.basicmobileapp.views.utilities.NavigationHelper
import com.authsamples.basicmobileapp.views.utilities.TextStyles
import com.authsamples.basicmobileapp.views.utilities.ViewLoadOptions
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/*
 * The companies view renders summary information per company
 */
@Composable
fun CompaniesView(model: CompaniesViewModel, navigationHelper: NavigationHelper) {

    /*
     * Ask the view model to load data
     */
    fun loadData(options: ViewLoadOptions? = null) {
        model.callApi(options)
    }

    /*
     * Handle navigation when an item is clicked
     */
    fun onItemClick() {

        // val args = Bundle()
        // args.putString(ViewConstants.ARG_COMPANY_ID, viewModelItem.company.id.toString())
        // findNavController().navigate(R.id.transactions_fragment, args)
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

    // Render based on the current view model data
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // Render the header
        Text(
            text = "Companies List",
            style = TextStyles.header,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().wrapContentSize()
        )

        if (model.errorData() == null) {

            // Render a list on success
            model.companiesList.value.forEach { company ->

                Text(
                    text = company.name,
                    style = TextStyles.label,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().wrapContentSize()
                )
            }

        } else {

            // Otherwise render error details
            ErrorSummaryView(
                model.errorSummaryData(),
                modifier = Modifier.fillMaxWidth().wrapContentSize()
            )
        }
    }
}
