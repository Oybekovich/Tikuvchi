package uz.tikuvchi.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import uz.tikuvchi.R
import uz.tikuvchi.ui.theme.Gold100
import uz.tikuvchi.ui.theme.Gold400
import uz.tikuvchi.ui.theme.Ink500
import uz.tikuvchi.ui.theme.Ink900
import uz.tikuvchi.ui.theme.Terra700
import uz.tikuvchi.util.formatCurrency

/** Reyting: ★ 4.9 (12) — web'dagi RatingBadge "soft" ko'rinishi. */
@Composable
fun RatingBadge(
    rating: Double,
    count: Int? = null,
    modifier: Modifier = Modifier,
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
            Icons.Filled.Star,
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
