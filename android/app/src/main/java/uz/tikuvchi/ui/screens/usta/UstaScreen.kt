package uz.tikuvchi.ui.screens.usta

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.annotation.DrawableRes
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import coil3.compose.AsyncImage
import uz.tikuvchi.R
import uz.tikuvchi.data.model.ReviewItem
import uz.tikuvchi.data.model.ServiceItem
import uz.tikuvchi.ui.components.bottomNavSpace
import uz.tikuvchi.ui.components.AppHeader
import uz.tikuvchi.ui.components.Avatar
import uz.tikuvchi.ui.components.AvatarSize
import uz.tikuvchi.ui.components.EmptyState
import uz.tikuvchi.ui.components.PriceTag
import uz.tikuvchi.ui.components.PrimaryButton
import uz.tikuvchi.ui.components.RatingBadge
import uz.tikuvchi.ui.components.SecondaryButton
import uz.tikuvchi.ui.theme.Cream200
import uz.tikuvchi.ui.theme.Cream50
import uz.tikuvchi.ui.theme.Ink500
import uz.tikuvchi.ui.theme.Ink700
import uz.tikuvchi.ui.theme.Ink900
import uz.tikuvchi.ui.theme.Terra200
import uz.tikuvchi.ui.theme.Terra400
import uz.tikuvchi.ui.theme.Terra600
import uz.tikuvchi.util.formatDate
import uz.tikuvchi.util.formatTime
import uz.tikuvchi.util.imageUrl

@Composable
fun UstaScreen(
    ustaId: String,
    onBack: () -> Unit,
    onOrder: (String) -> Unit,
    onChat: (id: String, name: String) -> Unit,
) {
    val vm: UstaViewModel = viewModel(
        key = ustaId,
        factory = viewModelFactory { initializer { UstaViewModel(ustaId) } },
    )
    val s by vm.state.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize().background(Cream50).statusBarsPadding()) {
        AppHeader(title = s.usta?.profiles?.fullName, onBack = onBack)

        when {
            s.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Terra600)
            }

            s.error || s.usta == null -> Box(
                Modifier.fillMaxSize().padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                EmptyState(icon = R.drawable.ic_search_off, title = stringResource(R.string.usta_not_found))
            }

            else -> {
                val usta = s.usta!!
                val name = usta.profiles.fullName
                Box(Modifier.fillMaxSize()) {
                    LazyColumn(
                        contentPadding = PaddingValues(
                            bottom = 96.dp,
                        ),
                    ) {
                        item { Cover(usta.coverImageUrl, usta.ratingAvg, usta.ratingCount) }

                        item {
                            // Karta hero rasm ustiga chiqadi — web'dagi -mt-8 kabi
                            Column(Modifier.offset(y = (-32).dp).padding(horizontal = 16.dp)) {
                                InfoCard(
                                    name = name,
                                    avatar = usta.profiles.avatarUrl,
                                    location = usta.locationText,
                                    workHours = if (usta.workHoursStart != null && usta.workHoursEnd != null) {
                                        stringResource(
                                            R.string.usta_work_hours,
                                            formatTime(usta.workHoursStart),
                                            formatTime(usta.workHoursEnd),
                                        )
                                    } else {
                                        null
                                    },
                                    tags = usta.tags,
                                    bio = usta.bio,
                                )
                            }
                        }

                        item { Section(stringResource(R.string.usta_services)) }
                        items2(usta.services) { ServiceRow(it) }

                        item { Section(stringResource(R.string.usta_portfolio)) }
                        if (usta.portfolio.isEmpty()) {
                            item {
                                EmptyState(
                                    icon = R.drawable.ic_images,
                                    title = stringResource(R.string.usta_no_portfolio),
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                )
                            }
                        } else {
                            // Web'dagi kabi 2 ustunli setka
                            items2(usta.portfolio.sortedBy { it.sortOrder }.chunked(2)) { pair ->
                                Row(
                                    Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    pair.forEach { item ->
                                        PortfolioCell(item.imageUrl, item.caption, name, Modifier.weight(1f))
                                    }
                                    // Toq sonda oxirgisi yarim kenglikda qolsin
                                    if (pair.size == 1) Spacer(Modifier.weight(1f))
                                }
                            }
                        }

                        item { Section(stringResource(R.string.usta_reviews)) }
                        if (s.reviews.isEmpty()) {
                            item {
                                EmptyState(
                                    icon = R.drawable.ic_chat,
                                    title = stringResource(R.string.usta_no_reviews),
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                )
                            }
                        } else {
                            items2(s.reviews) { ReviewRow(it) }
                        }
                    }

                    // Yopishqoq CTA
                    Row(
                        Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .background(Cream50)
                            .padding(16.dp)
                            .padding(
                                bottom = bottomNavSpace(),
                            ),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        PrimaryButton(
                            text = stringResource(R.string.usta_order_cta),
                            onClick = { onOrder(ustaId) },
                            modifier = Modifier.weight(1f),
                        )
                        SecondaryButton(
                            text = stringResource(R.string.usta_chat_cta),
                            onClick = { onChat(ustaId, name) },
                            modifier = Modifier.weight(0.6f),
                        )
                    }
                }
            }
        }
    }
}

/** LazyListScope.items ustidan qisqa yordamchi — kalitsiz oddiy ro'yxatlar uchun. */
private inline fun <T> androidx.compose.foundation.lazy.LazyListScope.items2(
    list: List<T>,
    crossinline row: @Composable (T) -> Unit,
) = items(list.size) { i -> row(list[i]) }

@Composable
private fun Cover(url: String?, rating: Double, count: Int) {
    Box(Modifier.fillMaxWidth().height(208.dp)) {
        val resolved = imageUrl(url)
        if (resolved != null) {
            AsyncImage(
                model = resolved,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Box(
                Modifier.fillMaxSize().background(
                    Brush.linearGradient(listOf(Terra200, Terra400)),
                ),
            )
        }
        RatingBadge(
            rating = rating,
            count = count,
            modifier = Modifier.align(Alignment.BottomStart).padding(start = 16.dp, bottom = 44.dp),
        )
    }
}

@Composable
private fun InfoCard(
    name: String,
    avatar: String?,
    location: String?,
    workHours: String?,
    tags: List<String>,
    bio: String?,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(16.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Avatar(name = name, src = avatar, size = AvatarSize.XL)
            Column {
                Text(
                    name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Ink900,
                )
                location?.let { IconLine(R.drawable.ic_location, it) }
                workHours?.let { IconLine(R.drawable.ic_clock, it) }
            }
        }
        if (tags.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                tags.forEach { tag ->
                    Text(
                        tag,
                        style = MaterialTheme.typography.labelSmall,
                        color = Ink700,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Cream200)
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                    )
                }
            }
        }
        bio?.let {
            Spacer(Modifier.height(12.dp))
            Text(it, style = MaterialTheme.typography.bodyMedium, color = Ink700)
        }
    }
}

@Composable
private fun IconLine(@DrawableRes icon: Int, text: String) {
    Row(
        Modifier.padding(top = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = Ink500,
            modifier = Modifier.size(15.dp),
        )
        Text(text, style = MaterialTheme.typography.bodyMedium, color = Ink500)
    }
}

@Composable
private fun Section(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.ExtraBold,
        color = Ink900,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
    )
}

@Composable
private fun ServiceRow(s: ServiceItem) {
    Row(
        Modifier
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(s.title, style = MaterialTheme.typography.titleSmall, color = Ink900)
            s.description?.let {
                Text(it, style = MaterialTheme.typography.labelSmall, color = Ink500)
            }
        }
        PriceTag(amount = s.basePrice)
    }
}

@Composable
private fun PortfolioCell(url: String?, caption: String?, name: String, modifier: Modifier = Modifier) {
    Column(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White),
    ) {
        AsyncImage(
            model = imageUrl(url),
            contentDescription = caption ?: name,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxWidth().aspectRatio(3f / 4f),
        )
        caption?.let {
            Text(
                it,
                style = MaterialTheme.typography.labelSmall,
                color = Ink500,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            )
        }
    }
}

@Composable
private fun ReviewRow(r: ReviewItem) {
    Column(
        Modifier
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(16.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Avatar(name = r.profiles.fullName, src = r.profiles.avatarUrl, size = AvatarSize.MD)
            Column(Modifier.weight(1f)) {
                Text(
                    r.profiles.fullName,
                    style = MaterialTheme.typography.titleSmall,
                    color = Ink900,
                )
                Text(
                    formatDate(r.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = Ink500,
                )
            }
            RatingBadge(rating = r.rating.toDouble())
        }
        r.comment?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, style = MaterialTheme.typography.bodyMedium, color = Ink700)
        }
    }
}
