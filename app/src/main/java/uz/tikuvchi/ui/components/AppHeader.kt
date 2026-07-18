package uz.tikuvchi.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import uz.tikuvchi.R
import uz.tikuvchi.ui.theme.Cream200
import uz.tikuvchi.ui.theme.Cream50
import uz.tikuvchi.ui.theme.Ink900

/**
 * Yagona umumiy header — web'dagi AppHeader kabi: chapda menyu yoki orqaga,
 * markazda logo yoki sarlavha, o'ngda profil.
 */
@Composable
fun AppHeader(
    modifier: Modifier = Modifier,
    title: String? = null,
    onBack: (() -> Unit)? = null,
    onMenu: (() -> Unit)? = null,
    onProfile: (() -> Unit)? = null,
) {
    Row(
        modifier
            .fillMaxWidth()
            .background(Cream50)
            .height(56.dp)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        if (onBack != null) {
            IconButton(onClick = onBack) {
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_back),
                    contentDescription = stringResource(R.string.common_back),
                    tint = Ink900,
                )
            }
        } else {
            IconButton(onClick = { onMenu?.invoke() }) {
                Icon(
                    painter = painterResource(R.drawable.ic_menu),
                    contentDescription = stringResource(R.string.menu_title),
                    tint = Ink900,
                )
            }
        }

        Text(
            text = title ?: stringResource(R.string.app_name),
            style = if (title != null) {
                MaterialTheme.typography.titleMedium
            } else {
                MaterialTheme.typography.titleLarge
            },
            color = Ink900,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false).padding(horizontal = 8.dp),
        )

        IconButton(onClick = { onProfile?.invoke() }) {
            Icon(
                painter = painterResource(R.drawable.ic_profile),
                contentDescription = stringResource(R.string.nav_profile),
                tint = Ink900,
            )
        }
    }
    // Header ostidagi ingichka chiziq
    Row(Modifier.fillMaxWidth().height(1.dp).background(Cream200)) {}
}
