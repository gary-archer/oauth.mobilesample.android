package com.authsamples.basicmobileapp.views.security

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.authsamples.basicmobileapp.R
import com.authsamples.basicmobileapp.plumbing.events.NavigatedEvent
import com.authsamples.basicmobileapp.views.utilities.CustomColors
import com.authsamples.basicmobileapp.views.utilities.TextStyles
import org.greenrobot.eventbus.EventBus

/*
 * The main view when the device lock screen does not have a secure PIN or higher security
 */
@Composable
fun DeviceNotSecuredView(eventBus: EventBus, onOpenLockSettings: () -> Unit) {

    // Indicate that we are not running API views yet
    LaunchedEffect(Unit) {
        eventBus.post(NavigatedEvent(false))
    }

    // Render the layout
    Column(
        Modifier.height(IntrinsicSize.Min).padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = stringResource(R.string.device_not_secured_message),
            style = TextStyles.value,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(10.dp)
        )

        Button(
            onClick = { onOpenLockSettings() },
            shape = RoundedCornerShape(10),
            colors = ButtonDefaults.textButtonColors(
                containerColor = CustomColors.paleGreen,
                contentColor = CustomColors.value
            ),
            modifier = Modifier.padding(10.dp)

        ) {

            Text(
                stringResource(R.string.open_device_settings),
                style = TextStyles.headerButton,
                modifier = Modifier.padding(2.dp)
            )
        }
    }
}
