package com.authsamples.basicmobileapp.views.headings

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.authsamples.basicmobileapp.R

/*
 * The title bar contains the application name on the left and user info on the right
 */
@Composable
fun TitleView() {
    Text(
        text = stringResource(R.string.app_name)
    )
}
