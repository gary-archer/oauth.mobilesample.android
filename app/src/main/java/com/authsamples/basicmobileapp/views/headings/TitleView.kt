package com.authsamples.basicmobileapp.views.headings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.authsamples.basicmobileapp.R
import com.authsamples.basicmobileapp.views.userinfo.UserInfoView

/*
 * The title bar contains the application name on the left and user info on the right
 */
@Composable
fun TitleView() {
    Row(
        Modifier.padding(10.dp)
    )
    {
        Text(
            text = stringResource(R.string.app_name),
            textAlign = TextAlign.Left,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = Color(0xFF212121),
            modifier = Modifier.weight(1f)
        )
        UserInfoView(
            Modifier.weight(1f)
        )
    }
}
