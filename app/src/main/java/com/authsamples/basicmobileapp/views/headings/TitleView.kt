package com.authsamples.basicmobileapp.views.headings

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.authsamples.basicmobileapp.R
import com.authsamples.basicmobileapp.views.userinfo.UserInfoView
import com.authsamples.basicmobileapp.views.userinfo.UserInfoViewModel
import com.authsamples.basicmobileapp.views.utilities.TextStyles

/*
 * The title bar contains the application name on the left and user info on the right
 */
@Composable
fun TitleView(userInfoViewModel: UserInfoViewModel) {
    Row(
        Modifier.padding(10.dp)
    ) {
        Text(
            text = stringResource(R.string.app_name),
            style = TextStyles.value,
            textAlign = TextAlign.Left,
            modifier = Modifier.weight(1f)
        )
        UserInfoView(
            model = userInfoViewModel,
            modifier = Modifier.weight(1f)
        )
    }
}
