package com.authsamples.basicmobileapp.views.companies

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.viewinterop.AndroidViewBinding
import com.authsamples.basicmobileapp.databinding.FragmentErrorContainerBinding
import com.authsamples.basicmobileapp.plumbing.events.NavigatedEvent
import com.authsamples.basicmobileapp.plumbing.events.ReloadDataEvent
import com.authsamples.basicmobileapp.views.errors.ErrorSummaryFragment
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

    // Manage event subscriptions
    DisposableEffect(key1 = null) {
        model.eventBus.register(subscriber)
        onDispose {
            model.eventBus.unregister(subscriber)
        }
    }

    // Do the initial data load and notify that the main view has changed
    loadData()
    model.eventBus.post(NavigatedEvent(true))

    // Render based on the view model data
    Column {
        Text(
            text = "Companies List",
            style = TextStyles.header,
            textAlign = TextAlign.Left,
            modifier = Modifier.weight(1f)
        )

        // Do the rendering
        if (model.errorData() == null) {

            Text(
                text = "Companies success",
                style = TextStyles.label,
                textAlign = TextAlign.Left,
                modifier = Modifier.weight(1f)
            )

        } else {

            // Otherwise render error details
            AndroidViewBinding(FragmentErrorContainerBinding::inflate) {
                val errorSummaryFragment = errorContainerFragment.getFragment<ErrorSummaryFragment>()
                errorSummaryFragment.receiveErrorFromParent(model.errorSummaryData())
            }
        }
    }
}
