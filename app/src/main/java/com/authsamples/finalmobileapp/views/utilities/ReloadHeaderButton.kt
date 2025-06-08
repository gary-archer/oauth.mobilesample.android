import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.authsamples.finalmobileapp.views.utilities.CustomColors
import com.authsamples.finalmobileapp.views.utilities.TextStyles

/*
 * A button that invokes the supplied callback with a different boolean flag
 */
@Composable
fun ReloadHeaderButton(
    modifier: Modifier,
    enabled: Boolean,
    buttonTextId: Int,
    onReload: (Boolean) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed = interactionSource.collectIsPressedAsState()
    val longPressMilliseconds = 2000
    var longPressStartTime: Long? = null

    fun isLongPress(): Boolean {

        // Get the time taken and then reset
        val start = longPressStartTime ?: return false
        val timeTaken = System.currentTimeMillis() - start
        longPressStartTime = null

        // A long press occurs when the touch has taken longer than 2 seconds
        return timeTaken > longPressMilliseconds
    }

    fun handlePress() {
        longPressStartTime = System.currentTimeMillis()
    }

    fun handleRelease() {
        val longClicked = isLongPress()
        onReload(longClicked)
    }

    if (enabled && isPressed.value) {
        DisposableEffect(Unit) {
            handlePress()
            onDispose {
                handleRelease()
            }
        }
    }

    Button(
        modifier = modifier,
        onClick = {},
        shape = RoundedCornerShape(10),
        enabled = enabled,
        contentPadding = PaddingValues(2.dp, 4.dp),
        colors = ButtonDefaults.textButtonColors(
            containerColor = CustomColors.primary,
            contentColor = CustomColors.value,
            disabledContainerColor = CustomColors.primary,
            disabledContentColor = CustomColors.label
        ),
        interactionSource = interactionSource
    ) {
        Text(
            stringResource(buttonTextId),
            style = TextStyles.headerButton,
            modifier = Modifier.padding(2.dp)
        )
    }
}
