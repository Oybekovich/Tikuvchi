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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import uz.tikuvchi.R
import uz.tikuvchi.data.model.ServiceCategory
import uz.tikuvchi.data.model.UstaCard
import uz.tikuvchi.util.imageUrl
import uz.tikuvchi.ui.theme.Cream200
import uz.tikuvchi.ui.theme.Ink300
import uz.tikuvchi.ui.theme.Ink500
import uz.tikuvchi.ui.theme.Ink700
import uz.tikuvchi.ui.theme.Ink900
import uz.tikuvchi.ui.theme.Terra50
import uz.tikuvchi.ui.theme.Terra600

/**
 * Kategoriya ikonkalari — Phosphor Icons (MIT), chiziqli uslub.
 * Kalit DB'dagi service_categories.icon ustuni bilan bir xil.
 */
private val CategoryIcons = mapOf(
    "milliy" to R.drawable.ic_cat_milliy,
    "kechki" to R.drawable.ic_cat_kechki,
    "kundalik" to R.drawable.ic_cat_kundalik,
    "muslima" to R.drawable.ic_cat_muslima,
    "toy" to R.drawable.ic_cat_toy,
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
            Icon(
                painter = painterResource(CategoryIcons[category.icon] ?: R.drawable.ic_needle),
                contentDescription = null,
                tint = Terra600,
                modifier = Modifier.size(24.dp),
            )
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
            .clickable(onClick = onClick),
    ) {
        // Cover lentasi — usta ishini kartochkadan chiqmasdan ko'rsatadi.
        // Rasmi yo'q ustada butunlay tashlab ketiladi: bo'sh kulrang to'rtburchak
        // hech narsa qo'shmaydi, faqat kartochkani cho'zadi.
        usta.coverImageUrl?.let { cover ->
            AsyncImage(
                model = imageUrl(cover),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(88.dp)
                    .background(Cream200),
            )
        }
        Column(Modifier.padding(16.dp)) {
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
                                painter = painterResource(R.drawable.ic_location),
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
                        painter = painterResource(R.drawable.ic_arrow_right),
                        contentDescription = null,
                        tint = Ink300,
                        modifier = Modifier.size(18.dp),
                    )
                }
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
    @DrawableRes icon: Int,
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
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = Terra600,
                modifier = Modifier.size(28.dp),
            )
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

/**
 * Tarmoq xatosi holati. EmptyState'dan ataylab ajratilgan: "ma'lumot yo'q" va
 * "ma'lumotni ololmadik" — bu ikki boshqa narsa. Ilgari xato bo'lganda ekranlar
 * bo'sh ro'yxat ko'rsatib, foydalanuvchiga buyurtmalari yo'qolgandek tuyulardi.
 */
@Composable
fun ErrorState(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
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
            Icon(
                painter = painterResource(R.drawable.ic_wifi_off),
                contentDescription = null,
                tint = Terra600,
                modifier = Modifier.size(28.dp),
            )
        }
        Text(
            text = stringResource(R.string.common_load_error),
            style = MaterialTheme.typography.titleMedium,
            color = Ink900,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(R.string.common_load_error_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = Ink500,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(4.dp))
        PrimaryButton(
            text = stringResource(R.string.common_retry),
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth(0.7f),
        )
    }
}
