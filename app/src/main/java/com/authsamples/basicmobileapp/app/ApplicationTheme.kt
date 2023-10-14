package com.authsamples.basicmobileapp.app

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import com.authsamples.basicmobileapp.views.utilities.CustomColors

/*
 * Set the status and navigation bar to match the app's colors
 */
@Composable
fun ApplicationTheme(
    content: @Composable () -> Unit
) {

    val view = LocalView.current
    SideEffect {
        val window = (view.context as Activity).window
        window.statusBarColor = CustomColors.primary.toArgb()
        window.navigationBarColor = CustomColors.primary.toArgb()
    }

    MaterialTheme(
        content = content
    )
}