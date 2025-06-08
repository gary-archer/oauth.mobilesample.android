package com.authsamples.finalmobileapp.views.security

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.authsamples.finalmobileapp.R
import com.authsamples.finalmobileapp.plumbing.events.NavigatedEvent
import com.authsamples.finalmobileapp.views.utilities.TextStyles
import org.greenrobot.eventbus.EventBus

/*
 * The login required view just shows some text but also sends a navigated event upon logout
 */
@Composable
fun LoginRequiredView(eventBus: EventBus) {

    Text(
        text = stringResource(R.string.logged_out_message),
        style = TextStyles.value,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth().padding(10.dp)
    )

    LaunchedEffect(Unit) {
        eventBus.post(NavigatedEvent(false))
    }
}
