package uz.tikuvchi.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import uz.tikuvchi.R
import uz.tikuvchi.data.model.OrderStatus
import uz.tikuvchi.ui.components.AppHeader
import uz.tikuvchi.ui.components.Avatar
import uz.tikuvchi.ui.components.AvatarSize
import uz.tikuvchi.ui.components.EmptyState
import uz.tikuvchi.ui.components.ErrorState
import uz.tikuvchi.ui.components.PrimaryButton
import uz.tikuvchi.ui.components.SecondaryButton
import uz.tikuvchi.ui.components.StatusChip
import uz.tikuvchi.ui.components.bottomNavSpace
import uz.tikuvchi.ui.theme.Cream50
import uz.tikuvchi.ui.theme.Ink500
import uz.tikuvchi.ui.theme.Ink900
import uz.tikuvchi.ui.theme.Terra600
import uz.tikuvchi.ui.theme.Terra50
import uz.tikuvchi.ui.theme.Terra700
import uz.tikuvchi.util.formatOrderNumber

@Composable
fun DashboardScreen(
    onOrder: (String) -> Unit,
    vm: DashboardViewModel = viewModel(),
) {
    val s by vm.state.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize().background(Cream50).statusBarsPadding()) {
        AppHeader(title = stringResource(R.string.app_name))

        when {
            s.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Terra600)
            }
            s.error -> Box(Modifier.fillMaxSize().padding(16.dp)) {
                ErrorState(onRetry = vm::load)
            }
            else -> LazyColumn(
                contentPadding = PaddingValues(bottom = bottomNavSpace()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item { StatsRow(s) }
                item {
                    Text(
                        stringResource(R.string.dashboard_pending_title),
                        style = MaterialTheme.typography.titleSmall,
                        color = Ink900,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }
                if (s.recentPending.isEmpty()) {
                    item {
                        Box(Modifier.padding(16.dp)) {
                            EmptyState(
                                icon = R.drawable.ic_package,
                                title = stringResource(R.string.dashboard_no_pending),
                                hint = stringResource(R.string.dashboard_no_pending_hint),
                            )
                        }
                    }
                } else {
                    items(s.recentPending, key = { it.id }) { order ->
                        PendingOrderCard(
                            order = order,
                            modifier = Modifier.padding(horizontal = 16.dp),
                            onClick = { onOrder(order.id) },
                            onAccept = { vm.acceptOrder(order.id) },
                            onReject = { vm.rejectOrder(order.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatsRow(s: DashboardUiState) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        StatCard(
            label = stringResource(R.string.dashboard_active_orders),
            value = s.activeOrders.toString(),
            color = Terra600,
            modifier = Modifier.weight(1f),
        )
        StatCard(
            label = stringResource(R.string.dashboard_pending),
            value = s.pendingOrders.toString(),
            color = Color(0xFFF59E0B),
            modifier = Modifier.weight(1f),
        )
        StatCard(
            label = stringResource(R.string.dashboard_rating),
            value = if (s.ratingCount > 0) String.format("%.1f", s.ratingAvg) else "—",
            color = Color(0xFF10B981),
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = color,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = Ink500,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun PendingOrderCard(
    order: uz.tikuvchi.data.model.OrderRow,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onAccept: () -> Unit,
    onReject: () -> Unit,
) {
    Column(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .clickable(onClick = onClick)
            .padding(16.dp),
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                stringResource(R.string.orders_order_number) + formatOrderNumber(order.id),
                style = MaterialTheme.typography.labelSmall,
                color = Ink500,
            )
            StatusChip(order.status)
        }
        Spacer(Modifier.height(12.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Avatar(
                name = order.usta.profiles.fullName,
                src = order.usta.profiles.avatarUrl,
                size = AvatarSize.MD,
            )
            Column(Modifier.weight(1f)) {
                Text(
                    order.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = Ink900,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    order.usta.profiles.fullName,
                    style = MaterialTheme.typography.labelSmall,
                    color = Ink500,
                )
            }
            Text(
                formatPrice(order.totalPrice),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Terra700,
            )
        }
        Spacer(Modifier.height(12.dp))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            PrimaryButton(
                text = stringResource(R.string.order_accept),
                onClick = onAccept,
                modifier = Modifier.weight(1f),
            )
            SecondaryButton(
                text = stringResource(R.string.order_reject),
                onClick = onReject,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

private fun formatPrice(amount: Long): String {
    val s = amount.toString()
    val sb = StringBuilder()
    var count = 0
    for (i in s.lastIndex downTo 0) {
        if (count > 0 && count % 3 == 0) sb.insert(0, ' ')
        sb.insert(0, s[i])
        count++
    }
    return "$sb so'm"
}
