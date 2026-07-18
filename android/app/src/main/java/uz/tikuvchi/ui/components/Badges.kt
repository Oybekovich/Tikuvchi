package uz.tikuvchi.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import uz.tikuvchi.R
import uz.tikuvchi.data.model.OrderStatus
import uz.tikuvchi.ui.theme.Cream200
import uz.tikuvchi.ui.theme.Gold100
import uz.tikuvchi.ui.theme.Gold400
import uz.tikuvchi.ui.theme.Green100
import uz.tikuvchi.ui.theme.Green800
import uz.tikuvchi.ui.theme.Ink500
import uz.tikuvchi.ui.theme.Ink700
import uz.tikuvchi.ui.theme.Ink900
import uz.tikuvchi.ui.theme.Red50
import uz.tikuvchi.ui.theme.Red700
import uz.tikuvchi.ui.theme.Terra100
import uz.tikuvchi.ui.theme.Terra700
import uz.tikuvchi.ui.theme.Terra800
import uz.tikuvchi.util.formatCurrency

/** Reyting: [yulduz] 4.9 (12) — web'dagi RatingBadge "soft" ko'rinishi. */
@Composable
fun RatingBadge(
    rating: Double,
    modifier: Modifier = Modifier,
    count: Int? = null,
) {
    Row(
        modifier
            .clip(CircleShape)
            .background(Gold100)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_star),
            contentDescription = null,
            tint = Gold400,
            modifier = Modifier.size(15.dp),
        )
        Text(
            text = String.format(java.util.Locale.US, "%.1f", rating),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = Ink900,
        )
        if (count != null) {
            Text(
                text = "($count)",
                style = MaterialTheme.typography.labelSmall,
                color = Ink500,
            )
        }
    }
}

/** Buyurtma holati — ranglar va matnlar web'dagi StatusChip bilan bir xil. */
@Composable
fun StatusChip(status: OrderStatus, modifier: Modifier = Modifier) {
    val (bg, fg) = when (status) {
        OrderStatus.PENDING -> Gold100 to Ink700
        OrderStatus.ACCEPTED, OrderStatus.IN_PROGRESS -> Terra100 to Terra800
        OrderStatus.READY -> Green100 to Green800
        OrderStatus.COMPLETED -> Cream200 to Ink700
        OrderStatus.CANCELLED -> Red50 to Red700
    }
    val label = when (status) {
        OrderStatus.PENDING -> R.string.order_status_pending
        OrderStatus.ACCEPTED -> R.string.order_status_accepted
        OrderStatus.IN_PROGRESS -> R.string.order_status_in_progress
        OrderStatus.READY -> R.string.order_status_ready
        OrderStatus.COMPLETED -> R.string.order_status_completed
        OrderStatus.CANCELLED -> R.string.order_status_cancelled
    }
    Text(
        text = stringResource(label),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = fg,
        maxLines = 1,
        modifier = modifier
            .clip(CircleShape)
            .background(bg)
            .padding(horizontal = 12.dp, vertical = 4.dp),
    )
}

/** Narx: 450 000 so'm [dan boshlab] */
@Composable
fun PriceTag(
    amount: Long,
    modifier: Modifier = Modifier,
    from: Boolean = false,
    color: Color = Terra700,
) {
    Row(
        modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        Text(
            text = formatCurrency(amount),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.ExtraBold,
            color = color,
        )
        if (from) {
            Text(
                text = stringResource(R.string.common_from),
                style = MaterialTheme.typography.labelSmall,
                color = Ink500,
            )
        }
    }
}
