package uz.tikuvchi.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uz.tikuvchi.R
import uz.tikuvchi.ui.theme.Cream200
import uz.tikuvchi.ui.theme.Cream50
import uz.tikuvchi.ui.theme.Ink500
import uz.tikuvchi.ui.theme.Terra600

/**
 * Pastki navigatsiya — web'dagi BottomNav.tsx bilan bir xil 5 ta tab.
 * Qidiruv bu yerda yo'q (web'dagi kabi): u header'dagi qidiruv maydonidan ochiladi.
 */
enum class NavTab(val labelRes: Int, @DrawableRes val icon: Int) {
    HOME(R.string.nav_home, R.drawable.ic_home),
    ORDERS(R.string.nav_orders, R.drawable.ic_orders),
    MEASUREMENTS(R.string.nav_measurements, R.drawable.ic_ruler),
    CHAT(R.string.nav_chat, R.drawable.ic_chat),
    PROFILE(R.string.nav_profile, R.drawable.ic_profile),
}

@Composable
fun BottomNav(
    current: NavTab?,
    onSelect: (NavTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxWidth().background(Cream50)) {
        Spacer(Modifier.fillMaxWidth().height(1.dp).background(Cream200))
        Row(
            Modifier.fillMaxWidth().navigationBarsPadding(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            NavTab.entries.forEach { tab ->
                Tab(tab = tab, selected = tab == current, onClick = { onSelect(tab) })
            }
        }
    }
}

@Composable
private fun Tab(tab: NavTab, selected: Boolean, onClick: () -> Unit) {
    val color = if (selected) Terra600 else Ink500
    Column(
        Modifier
            // Ripple o'chirilgan — web'da ham bosilganda faqat rang o'zgaradi
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(vertical = 8.dp, horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Icon(
            painter = painterResource(tab.icon),
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(22.dp),
        )
        Text(
            text = stringResource(tab.labelRes),
            style = MaterialTheme.typography.labelSmall,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
            color = color,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
    }
}
