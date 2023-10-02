package com.authsamples.basicmobileapp.views.headings

import ReloadHeaderButton
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.authsamples.basicmobileapp.R
import com.authsamples.basicmobileapp.plumbing.events.NavigatedEvent
import com.authsamples.basicmobileapp.plumbing.events.ViewModelFetchEvent
import com.authsamples.basicmobileapp.views.utilities.Controls.HeaderButton
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/*
 * A view for the header buttons
 */
@Suppress("LongParameterList")
@Composable
fun HeaderButtonsView(
    eventBus: EventBus,
    onHome: () -> Unit,
    onReload: (causeError: Boolean) -> Unit,
    onExpireAccessToken: () -> Unit,
    onExpireRefreshToken: () -> Unit,
    onLogout: () -> Unit,
) {
    val hasData = remember { mutableStateOf(false) }

    /*
     * Create an event subscriber
     */
    val subscriber = object {

        /*
         * Update button state during fetch events
         */
        @Subscribe(threadMode = ThreadMode.MAIN)
        fun onMessageEvent(event: ViewModelFetchEvent) {
            hasData.value = event.loaded
        }

        /*
         * Set button state to disabled when we move to the login required view
         */
        @Subscribe(threadMode = ThreadMode.MAIN)
        fun onMessageEvent(event: NavigatedEvent) {

            if (!event.isMainView) {
                hasData.value = false
            }
        }

    }

    // Manage event subscriptions
    DisposableEffect(key1 = null) {
        eventBus.register(subscriber)
        onDispose {
            eventBus.unregister(subscriber)
        }
    }

    // Do the rendering
    Row(
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        modifier = Modifier.height(IntrinsicSize.Max).padding(2.dp)

    ) {

        HeaderButton(
            modifier = Modifier.weight(1f).fillMaxHeight(),
            enabled = true,
            buttonTextId = R.string.home_button,
            onClick = { onHome() },

        )

        ReloadHeaderButton(
            modifier = Modifier.weight(1f).fillMaxHeight(),
            enabled = hasData.value,
            buttonTextId = R.string.reload_button,
            onReload = onReload,
        )

        HeaderButton(
            modifier = Modifier.weight(1f).fillMaxHeight(),
            enabled = hasData.value,
            buttonTextId = R.string.expire_access_token_button,
            onClick = { onExpireAccessToken() }
        )

        HeaderButton(
            modifier = Modifier.weight(1f).fillMaxHeight(),
            enabled = hasData.value,
            buttonTextId = R.string.expire_refresh_token_button,
            onClick = { onExpireRefreshToken() }
        )

        HeaderButton(
            modifier = Modifier.weight(1f).fillMaxHeight(),
            enabled = hasData.value,
            buttonTextId = R.string.logout_button,
            onClick = { onLogout() }
        )
    }
}
