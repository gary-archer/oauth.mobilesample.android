package com.authsamples.basicmobileapp.views.utilities

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

/*
 * Shared controls to reduce code in other areas
 */
object Controls {

    /*
     * The header buttons
     */
    @Composable
    fun HeaderButton(
        modifier: Modifier,
        enabled: Boolean,
        buttonTextId: Int,
        onClick: () -> Unit
    ) {

        Button(
            modifier = modifier,
            onClick = { onClick() },
            shape = RoundedCornerShape(10),
            enabled = enabled,
            contentPadding = PaddingValues(2.dp, 4.dp),
            colors = ButtonDefaults.textButtonColors(
                containerColor = CustomColors.primary,
                contentColor = CustomColors.value,
                disabledContainerColor = CustomColors.primary,
                disabledContentColor = CustomColors.label
            )
        ) {
            Text(
                stringResource(buttonTextId),
                style = TextStyles.headerButton,
                modifier = Modifier.padding(2.dp)
            )
        }
    }
}
