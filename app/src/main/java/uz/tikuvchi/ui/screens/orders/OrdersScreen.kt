package uz.tikuvchi.ui.screens.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import uz.tikuvchi.R
import uz.tikuvchi.data.model.OrderRow
import uz.tikuvchi.ui.components.AppHeader
import uz.tikuvchi.ui.components.Avatar
import uz.tikuvchi.ui.components.AvatarSize
import uz.tikuvchi.ui.components.EmptyState
import uz.tikuvchi.ui.components.ErrorState
import uz.tikuvchi.ui.components.PriceTag
import uz.tikuvchi.ui.components.StatusChip
import uz.tikuvchi.ui.theme.Cream200
import uz.tikuvchi.ui.theme.Cream50
import uz.tikuvchi.ui.theme.Ink500
import uz.tikuvchi.ui.theme.Ink900
import uz.tikuvchi.ui.theme.Terra600
import uz.tikuvchi.ui.theme.Terra700
import uz.tikuvchi.util.formatDate
import uz.tikuvchi.util.formatOrderNumber

@Composable
fun OrdersScreen(
    onMenu: () -> Unit,
    onProfile: () -> Unit,
    onOrder: (String) -> Unit,
    vm: OrdersViewModel = viewModel(),
) {
    val s by vm.state.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize().background(Cream50).statusBarsPadding()) {
        AppHeader(
            title = stringResource(R.string.orders_title),
            onMenu = onMenu,
            onProfile = onProfile,
        )

        // Holat bo'yicha filtr
        Row(
            Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Cream200)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Tab(stringResource(R.string.orders_active), !s.finished, Modifier.weight(1f)) {
                vm.setFinished(false)
            }
            Tab(stringResource(R.string.orders_finished), s.finished, Modifier.weight(1f)) {
                vm.setFinished(true)
            }
        }

        when {
            s.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Terra600)
            }

            // Xato bo'sh ro'yxatdan oldin tekshiriladi — aks holda tarmoq uzilganda
            // foydalanuvchi buyurtmalarim yo'qolibdi deb o'ylaydi
            s.error -> Box(Modifier.fillMaxSize().padding(16.dp)) {
                ErrorState(onRetry = vm::load)
            }

            s.orders.isEmpty() -> Box(Modifier.fillMaxSize().padding(16.dp)) {
                EmptyState(
                    icon = R.drawable.ic_package,
                    title = stringResource(R.string.orders_empty),
                    hint = stringResource(R.string.orders_empty_hint),
                )
            }

            else -> LazyColumn(
                contentPadding = PaddingValues(
                    bottom = 24.dp + WindowInsets.navigationBars.asPaddingValues()
                        .calculateBottomPadding(),
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(s.orders, key = { it.id }) { order ->
                    OrderCard(order, Modifier.padding(horizontal = 16.dp)) { onOrder(order.id) }
                }
            }
        }
    }
}

@Composable
private fun Tab(text: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = if (selected) Terra700 else Ink500,
        textAlign = TextAlign.Center,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) Color.White else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
    )
}

@Composable
private fun OrderCard(order: OrderRow, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val usta = order.usta.profiles
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
            Avatar(name = usta.fullName, src = usta.avatarUrl, size = AvatarSize.MD)
            Column(Modifier.weight(1f)) {
                Text(
                    order.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = Ink900,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(usta.fullName, style = MaterialTheme.typography.labelSmall, color = Ink500)
            }
            PriceTag(amount = order.totalPrice)
        }

        order.estimatedReadyAt?.let {
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(R.string.orders_estimated_ready, formatDate(it)),
                style = MaterialTheme.typography.labelSmall,
                color = Ink500,
            )
        }
    }
}
