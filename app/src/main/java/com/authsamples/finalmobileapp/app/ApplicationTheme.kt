package com.authsamples.finalmobileapp.app

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.authsamples.finalmobileapp.views.utilities.CustomColors

/*
 * Set the theme in code and use color objects
 */
@Composable
fun ApplicationTheme(
    content: @Composable () -> Unit,
) {

    val view = LocalView.current
    SideEffect {
        val window = (view.context as Activity).window
        window.isNavigationBarContrastEnforced = false
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.isAppearanceLightStatusBars = true

    }

    val colorScheme = lightColorScheme(
        primary = CustomColors.primary,
    )

    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
    )
}
