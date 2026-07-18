package uz.tikuvchi.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uz.tikuvchi.R
import uz.tikuvchi.ui.theme.Cream200
import uz.tikuvchi.ui.theme.Cream50
import uz.tikuvchi.ui.theme.Ink500
import uz.tikuvchi.ui.theme.Terra50
import uz.tikuvchi.ui.theme.Terra600

/**
 * Pastki navigatsiya. Bosh sahifa markazda, doira ichida — qolgan to'rttasi
 * ikki chetda faqat ikonka bo'lib turadi va tanlanganda kengayib yozuvi chiqadi.
 * Shu tufayli besh tabga tor ekranda ham joy yetadi.
 */
enum class NavTab(val labelRes: Int, @DrawableRes val icon: Int) {
    ORDERS(R.string.nav_orders, R.drawable.ic_orders),
    MEASUREMENTS(R.string.nav_measurements, R.drawable.ic_ruler),
    HOME(R.string.nav_home, R.drawable.ic_home),
    CHAT(R.string.nav_chat, R.drawable.ic_chat),
    PROFILE(R.string.nav_profile, R.drawable.ic_profile),
}

private val SIDE_TABS_LEFT = listOf(NavTab.ORDERS, NavTab.MEASUREMENTS)
private val SIDE_TABS_RIGHT = listOf(NavTab.CHAT, NavTab.PROFILE)

@Composable
fun BottomNav(
    current: NavTab?,
    onSelect: (NavTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxWidth().background(Cream50)) {
        Spacer(Modifier.fillMaxWidth().height(1.dp).background(Cream200))
        Row(
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SIDE_TABS_LEFT.forEach { tab ->
                SideTab(tab, tab == current) { onSelect(tab) }
            }
            HomeButton(NavTab.HOME == current) { onSelect(NavTab.HOME) }
            SIDE_TABS_RIGHT.forEach { tab ->
                SideTab(tab, tab == current) { onSelect(tab) }
            }
        }
    }
}

/** Markazdagi bosh sahifa — doim to'ldirilgan doira, shuning uchun darhol ko'zga tashlanadi. */
@Composable
private fun HomeButton(selected: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .size(52.dp)
            // Faol bo'lmaganda soya yumshoqroq — ortiqcha e'tibor tortmasin
            .shadow(if (selected) 8.dp else 3.dp, CircleShape, clip = false)
            .clip(CircleShape)
            .background(Terra600)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(NavTab.HOME.icon),
            contentDescription = stringResource(NavTab.HOME.labelRes),
            tint = Color.White,
            modifier = Modifier.size(if (selected) 26.dp else 24.dp),
        )
    }
}

/** Chetdagi tab: tanlanmagan bo'lsa faqat ikonka, tanlanganda kengayib yozuv chiqadi. */
@Composable
private fun SideTab(tab: NavTab, selected: Boolean, onClick: () -> Unit) {
    val label = stringResource(tab.labelRes)
    val tint by animateColorAsState(if (selected) Terra600 else Ink500, label = "tabTint")

    Row(
        Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) Terra50 else Color.Transparent)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = if (selected) 12.dp else 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(if (selected) 6.dp else 0.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(tab.icon),
            // Yozuv yashiringanda ham ekran o'quvchisi tabni ayta olishi kerak
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(22.dp),
        )
        AnimatedVisibility(
            visible = selected,
            enter = expandHorizontally(spring()) + fadeIn(),
            exit = shrinkHorizontally(spring()) + fadeOut(),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Terra600,
                maxLines = 1,
            )
        }
    }
}
