package uz.tikuvchi.ui.screens.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import uz.tikuvchi.R
import uz.tikuvchi.data.model.OrderDetail
import uz.tikuvchi.data.model.OrderItemDetail
import uz.tikuvchi.data.model.OrderSource
import uz.tikuvchi.data.model.OrderStatus
import uz.tikuvchi.data.model.PaymentStatus
import uz.tikuvchi.ui.components.bottomNavSpace
import uz.tikuvchi.ui.components.AppHeader
import uz.tikuvchi.ui.components.Avatar
import uz.tikuvchi.ui.components.AvatarSize
import uz.tikuvchi.ui.components.EmptyState
import uz.tikuvchi.ui.components.ErrorState
import uz.tikuvchi.ui.components.PriceTag
import uz.tikuvchi.ui.components.SecondaryButton
import uz.tikuvchi.ui.components.StatusChip
import uz.tikuvchi.ui.theme.Cream200
import uz.tikuvchi.ui.theme.Cream300
import uz.tikuvchi.ui.theme.Cream50
import uz.tikuvchi.ui.theme.Ink500
import uz.tikuvchi.ui.theme.Ink700
import uz.tikuvchi.ui.theme.Ink900
import uz.tikuvchi.ui.theme.Red700
import uz.tikuvchi.ui.theme.Terra600
import uz.tikuvchi.util.formatDate
import uz.tikuvchi.util.formatOrderNumber

@Composable
fun OrderDetailScreen(
    orderId: String,
    onBack: () -> Unit,
    onUsta: (String) -> Unit,
) {
    val vm: OrderDetailViewModel = viewModel(
        key = orderId,
        factory = viewModelFactory { initializer { OrderDetailViewModel(orderId) } },
    )
    val s by vm.state.collectAsStateWithLifecycle()
    var confirmCancel by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().background(Cream50).statusBarsPadding()) {
        AppHeader(title = stringResource(R.string.orders_details_title), onBack = onBack)

        when {
            s.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Terra600)
            }

            // Faqat ko'rsatadigan narsa qolmaganda. cancel() ham error qo'yadi —
            // u holda buyurtma o'z joyida turaveradi, butun ekran almashmaydi
            s.error && s.order == null -> Box(Modifier.fillMaxSize().padding(16.dp)) {
                ErrorState(onRetry = vm::load)
            }

            s.order == null -> Box(Modifier.fillMaxSize().padding(16.dp)) {
                EmptyState(icon = R.drawable.ic_package, title = stringResource(R.string.orders_not_found))
            }

            else -> {
                val order = s.order!!
                Column(
                    Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                        .padding(
                            bottom = bottomNavSpace(),
                        ),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                stringResource(R.string.orders_order_number) +
                                    formatOrderNumber(order.id),
                                style = MaterialTheme.typography.titleSmall,
                                color = Ink900,
                            )
                            Text(
                                stringResource(
                                    if (order.source == OrderSource.CHAT_NEGOTIATION) {
                                        R.string.orders_source_chat_negotiation
                                    } else {
                                        R.string.orders_source_catalog
                                    },
                                ),
                                style = MaterialTheme.typography.labelSmall,
                                color = Ink500,
                            )
                        }
                        StatusChip(order.status)
                    }

                    Card { StatusStepper(order) }
                    Card { UstaRow(order, onUsta) }
                    Card { Composition(order) }
                    Card { Payment(order.paymentStatus) }

                    // Faqat "kutilmoqda" holatida bekor qilish mumkin — web'dagi kabi
                    if (order.status == OrderStatus.PENDING) {
                        SecondaryButton(
                            text = stringResource(R.string.orders_cancel_order),
                            onClick = { confirmCancel = true },
                        )
                    }
                }
            }
        }
    }

    if (confirmCancel) {
        AlertDialog(
            onDismissRequest = { confirmCancel = false },
            title = { Text(stringResource(R.string.orders_cancel_order)) },
            text = { Text(stringResource(R.string.orders_cancel_confirm)) },
            confirmButton = {
                TextButton(onClick = { confirmCancel = false; vm.cancel() }) {
                    Text(stringResource(R.string.common_confirm), color = Red700)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmCancel = false }) {
                    Text(stringResource(R.string.common_cancel))
                }
            },
            containerColor = Cream50,
        )
    }
}

/** Web'dagi OrderStatusStepper: Qabul qilindi → Tayyorlanmoqda → Tayyor. */
@Composable
private fun StatusStepper(order: OrderDetail) {
    val steps = listOf(
        R.string.order_status_accepted,
        R.string.order_status_in_progress,
        R.string.order_status_ready,
    )
    val doneCount = when (order.status) {
        OrderStatus.PENDING, OrderStatus.CANCELLED -> 0
        OrderStatus.ACCEPTED, OrderStatus.IN_PROGRESS -> 1
        OrderStatus.READY, OrderStatus.COMPLETED -> 3
    }

    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        steps.forEachIndexed { i, res ->
            val done = i < doneCount
            Column(
                Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(if (done) Terra600 else Cream300),
                    contentAlignment = Alignment.Center,
                ) {
                    if (done) {
                        Icon(
                            painter = painterResource(R.drawable.ic_check),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    stringResource(res),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (done) Ink900 else Ink500,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                )
            }
        }
    }
    order.estimatedReadyAt?.let {
        Spacer(Modifier.height(12.dp))
        Text(
            stringResource(R.string.orders_estimated_ready, formatDate(it)),
            style = MaterialTheme.typography.labelSmall,
            color = Ink500,
        )
    }
    Text(
        stringResource(R.string.orders_created_at, formatDate(order.createdAt)),
        style = MaterialTheme.typography.labelSmall,
        color = Ink500,
    )
}

@Composable
private fun UstaRow(order: OrderDetail, onUsta: (String) -> Unit) {
    val p = order.usta.profiles
    Row(
        Modifier.fillMaxWidth().clickable { onUsta(order.ustaId) },
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Avatar(name = p.fullName, src = p.avatarUrl, size = AvatarSize.MD)
        Column(Modifier.weight(1f)) {
            Text(p.fullName, style = MaterialTheme.typography.titleSmall, color = Ink900)
            order.usta.district?.let {
                Text(it, style = MaterialTheme.typography.labelSmall, color = Ink500)
            }
        }
    }
}

@Composable
private fun Composition(order: OrderDetail) {
    Text(
        stringResource(R.string.orders_composition),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.ExtraBold,
        color = Ink900,
    )
    Spacer(Modifier.height(12.dp))
    order.items.forEach { item -> ItemRow(item) }
    Spacer(Modifier.height(8.dp))
    Box(Modifier.fillMaxWidth().height(1.dp).background(Cream200))
    Spacer(Modifier.height(8.dp))
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            stringResource(R.string.orders_total),
            style = MaterialTheme.typography.titleSmall,
            color = Ink700,
        )
        PriceTag(amount = order.totalPrice)
    }
}

@Composable
private fun ItemRow(item: OrderItemDetail) {
    Row(
        Modifier.fillMaxWidth().padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(Modifier.weight(1f)) {
            Text(item.title, style = MaterialTheme.typography.titleSmall, color = Ink900)
            listOfNotNull(item.material, item.sizeNote, item.modelNote).forEach {
                Text(it, style = MaterialTheme.typography.labelSmall, color = Ink500)
            }
        }
        PriceTag(amount = item.price)
    }
}

@Composable
private fun Payment(status: PaymentStatus) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            stringResource(R.string.payment_title),
            style = MaterialTheme.typography.titleSmall,
            color = Ink700,
        )
        Text(
            stringResource(
                when (status) {
                    PaymentStatus.PENDING -> R.string.payment_pending
                    PaymentStatus.PARTIAL -> R.string.payment_partial
                    PaymentStatus.PAID -> R.string.payment_paid
                },
            ),
            style = MaterialTheme.typography.titleSmall,
            color = Ink900,
        )
    }
}

@Composable
private fun Card(content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(16.dp),
        content = content,
    )
}
