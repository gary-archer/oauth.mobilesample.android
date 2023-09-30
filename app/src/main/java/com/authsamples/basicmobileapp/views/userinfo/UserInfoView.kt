package com.authsamples.basicmobileapp.views.userinfo

import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.PlainTooltipBox
import androidx.compose.material3.PlainTooltipState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidViewBinding
import com.authsamples.basicmobileapp.databinding.FragmentErrorContainerBinding
import com.authsamples.basicmobileapp.plumbing.events.NavigatedEvent
import com.authsamples.basicmobileapp.plumbing.events.ReloadDataEvent
import com.authsamples.basicmobileapp.views.errors.ErrorSummaryFragment
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

            println("GJA: navigated received by userinfo")
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

    // Manage event subscriptions
    DisposableEffect(key1 = model) {
        model.eventBus.register(subscriber)
        onDispose {
            model.eventBus.unregister(subscriber)
        }
    }

    // Do the rendering
    if (model.errorData() == null) {

        val tooltipState = remember { PlainTooltipState() }
        val scope = remember { CoroutineScope(Dispatchers.Main) }

        // Render a tooltip with further information when the user name is clicked
        PlainTooltipBox(
            tooltip = { Text(model.getLoggedInUserDescription()) },
            tooltipState = tooltipState,
            containerColor = Color.Gray,
        ) {

            // Render the user name
            Text(
                text = AnnotatedString(model.getLoggedInUser()),
                textAlign = TextAlign.Right,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color(0xFF727272),
                modifier = modifier.clickable {
                    scope.launch { tooltipState.show() }
                }
            )
        }

    } else {

        // Otherwise render error details
        AndroidViewBinding(FragmentErrorContainerBinding::inflate) {
            val errorSummaryFragment = errorContainerFragment.getFragment<ErrorSummaryFragment>()
            errorSummaryFragment.receiveErrorFromParent(model.errorSummaryData())
        }
    }
}
