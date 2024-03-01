package com.authsamples.basicmobileapp.views.userinfo

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.authsamples.basicmobileapp.R
import com.authsamples.basicmobileapp.plumbing.events.NavigatedEvent
import com.authsamples.basicmobileapp.plumbing.events.ReloadDataEvent
import com.authsamples.basicmobileapp.views.errors.ErrorSummaryView
import com.authsamples.basicmobileapp.views.errors.ErrorViewModel
import com.authsamples.basicmobileapp.views.utilities.CustomColors
import com.authsamples.basicmobileapp.views.utilities.TextStyles
import com.authsamples.basicmobileapp.views.utilities.ViewLoadOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/*
 * The user info view renders logged in user information from two sources
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun UserInfoView(model: UserInfoViewModel, modifier: Modifier) {

    /*
     * Ask the view model to load data
     */
    fun loadData(options: ViewLoadOptions? = null) {
        model.callApi(options)
    }

    /*
     * Create an event subscriber
     */
    val subscriber = object {

        /*
         * Handle navigation
         */
        @Subscribe(threadMode = ThreadMode.MAIN)
        fun onMessageEvent(event: NavigatedEvent) {

            if (event.isMainView) {

                // Load user info if required after navigating to a main view
                loadData()

            } else {

                // When not in a main view we are logged out, so clear user info
                model.clearUserInfo()
            }
        }

        /*
         * Handle reload events
         */
        @Subscribe(threadMode = ThreadMode.MAIN)
        fun onMessageEvent(event: ReloadDataEvent) {
            loadData(ViewLoadOptions(true, event.causeError))
        }
    }

    // Manage event subscriptions during view loads and unloads
    DisposableEffect(Unit) {
        model.eventBus.register(subscriber)
        onDispose {
            model.eventBus.unregister(subscriber)
        }
    }

    // Render based on the current view model data
    if (model.error.value == null) {

        val tooltipState = rememberTooltipState()
        val scope = remember { CoroutineScope(Dispatchers.Main) }

        // Render a tooltip with further information when the user name is clicked
        TooltipBox(
            positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
            state = tooltipState,
            tooltip = {
                PlainTooltip(
                    containerColor = CustomColors.label,
                ) {
                    Column {
                        Text(
                            text = model.getUserTitle(),
                            style = TextStyles.tooltip,
                            modifier = Modifier.width(150.dp)
                        )
                        Text(
                            text = model.getUserRegions(),
                            style = TextStyles.tooltip,
                            modifier = Modifier.width(150.dp)
                        )
                    }
                }
            }
        ) {

            // Render the user name
            Text(
                text = model.getUserName(),
                style = TextStyles.label,
                textAlign = TextAlign.Right,
                modifier = modifier.clickable {
                    scope.launch { tooltipState.show() }
                }
            )
        }

    } else {

        // Otherwise render error details
        ErrorSummaryView(
            ErrorViewModel(
                model.error.value!!,
                stringResource(R.string.userinfo_error_hyperlink),
                stringResource(R.string.userinfo_error_dialogtitle)
            ),
            Modifier
        )
    }
}
