package uz.tikuvchi.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import uz.tikuvchi.data.model.ServiceCategory
import uz.tikuvchi.data.model.UstaCard
import uz.tikuvchi.ui.theme.Cream200
import uz.tikuvchi.ui.theme.Ink300
import uz.tikuvchi.ui.theme.Ink500
import uz.tikuvchi.ui.theme.Ink700
import uz.tikuvchi.ui.theme.Ink900
import uz.tikuvchi.ui.theme.Terra50

/** Web'dagi CategoryCard ICONS bilan bir xil. */
private val CategoryIcons = mapOf(
    "milliy" to "🧵",
    "kechki" to "✨",
    "kundalik" to "👗",
    "muslima" to "🧕",
    "toy" to "💍",
)

@Composable
fun CategoryCard(
    category: ServiceCategory,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier
            .width(96.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            Modifier.size(48.dp).clip(CircleShape).background(Terra50),
            contentAlignment = Alignment.Center,
        ) {
            Text(CategoryIcons[category.icon] ?: "🪡", style = MaterialTheme.typography.titleLarge)
        }
        Text(
            text = category.name,
            style = MaterialTheme.typography.labelSmall,
            color = Ink700,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun UstaCardItem(
    usta: UstaCard,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .clickable(onClick = onClick)
            .padding(16.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Avatar(name = usta.profiles.fullName, src = usta.profiles.avatarUrl, size = AvatarSize.LG)
            Column(Modifier.weight(1f)) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = usta.profiles.fullName,
                        style = MaterialTheme.typography.titleMedium,
                        color = Ink900,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    RatingBadge(usta.ratingAvg)
                }
                usta.district?.let { d ->
                    Spacer(Modifier.height(2.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.Filled.LocationOn,
                            contentDescription = null,
                            tint = Ink500,
                            modifier = Modifier.size(14.dp),
                        )
                        Text(d, style = MaterialTheme.typography.bodyMedium, color = Ink500)
                    }
                }
                if (usta.tags.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        usta.tags.take(3).forEach { Tag(it) }
                    }
                }
            }
        }

        usta.minPrice?.let { price ->
            Spacer(Modifier.height(12.dp))
            Box(Modifier.fillMaxWidth().height(1.dp).background(Cream200))
            Spacer(Modifier.height(12.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PriceTag(amount = price, from = true)
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = Ink300,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

@Composable
private fun Tag(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = Ink700,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier
            .clip(CircleShape)
            .background(Cream200)
            .padding(horizontal = 10.dp, vertical = 2.dp),
    )
}

@Composable
fun EmptyState(
    icon: String,
    title: String,
    modifier: Modifier = Modifier,
    hint: String? = null,
) {
    Column(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(horizontal = 24.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            Modifier.size(64.dp).clip(CircleShape).background(Terra50),
            contentAlignment = Alignment.Center,
        ) {
            Text(icon, style = MaterialTheme.typography.headlineSmall)
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = Ink900,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        hint?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = Ink500,
                textAlign = TextAlign.Center,
            )
        }
    }
}
