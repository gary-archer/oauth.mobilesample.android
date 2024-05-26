package com.authsamples.basicmobileapp.views.headings

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.authsamples.basicmobileapp.R
import com.authsamples.basicmobileapp.plumbing.events.NavigatedEvent
import com.authsamples.basicmobileapp.views.utilities.TextStyles
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/*
 * The session view shows the session value written to API logs during API requests from the app
 */
@Composable
fun SessionView(eventBus: EventBus, apiSessionId: String) {

    val isVisible = remember { mutableStateOf(false) }

    /*
     * Subscribe to navigation events
     */
    val subscriber = object {

        @Subscribe(threadMode = ThreadMode.MAIN)
        fun onMessageEvent(event: NavigatedEvent) {
            isVisible.value = event.isAuthenticatedView
        }
    }

    // Manage event subscriptions
    DisposableEffect(Unit) {
        eventBus.register(subscriber)
        onDispose {
            eventBus.unregister(subscriber)
        }
    }

    if (isVisible.value) {

        Text(
            text = "${stringResource(R.string.api_session_id)} : $apiSessionId",
            style = TextStyles.sessionId,
            textAlign = TextAlign.Right,
            modifier = Modifier.fillMaxWidth().padding(10.dp)
        )
    }
}
