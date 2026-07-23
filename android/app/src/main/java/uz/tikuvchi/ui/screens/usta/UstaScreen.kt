package uz.tikuvchi.ui.screens.usta

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import coil3.compose.AsyncImage
import uz.tikuvchi.R
import uz.tikuvchi.data.model.PortfolioImage
import uz.tikuvchi.data.model.ReviewItem
import uz.tikuvchi.ui.components.AppHeader
import uz.tikuvchi.ui.components.Avatar
import uz.tikuvchi.ui.components.AvatarSize
import uz.tikuvchi.ui.components.EmptyState
import uz.tikuvchi.ui.components.PrimaryButton
import uz.tikuvchi.ui.components.RatingBadge
import uz.tikuvchi.ui.theme.Cream100
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
                var selectedIndex by remember { mutableStateOf(-1) }
                val portfolioList = remember(usta.portfolio) { usta.portfolio.sortedBy { it.sortOrder } }
                Box(Modifier.fillMaxSize()) {
                    LazyColumn(
                        contentPadding = PaddingValues(
                            bottom = 24.dp,
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
                                    onChat = { onChat(ustaId, name) },
                                )
                            }
                        }

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
                            items2(portfolioList.chunked(2)) { pair ->
                                Row(
                                    Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    pair.forEach { item ->
                                        PortfolioCell(
                                            item = item,
                                            name = name,
                                            onClick = {
                                                val idx = portfolioList.indexOf(item)
                                                if (idx >= 0) selectedIndex = idx
                                            },
                                            modifier = Modifier.weight(1f),
                                        )
                                    }
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
                }

                if (selectedIndex >= 0 && selectedIndex < portfolioList.size) {
                    PortfolioDetailsSheet(
                        items = portfolioList,
                        initialIndex = selectedIndex,
                        ustaName = name,
                        onDismiss = { selectedIndex = -1 },
                        onOrder = {
                            selectedIndex = -1
                            onOrder(ustaId)
                        },
                    )
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
    onChat: () -> Unit,
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
        Spacer(Modifier.height(16.dp))
        PrimaryButton(
            text = stringResource(R.string.usta_chat_cta),
            onClick = onChat,
        )
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
private fun PortfolioCell(
    item: PortfolioImage,
    name: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White),
    ) {
        AsyncImage(
            model = imageUrl(item.imageUrl),
            contentDescription = item.caption ?: name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(3f / 4f)
                .clickable(onClick = onClick),
        )
        item.caption?.let {
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
private fun PortfolioDetailsSheet(
    items: List<PortfolioImage>,
    initialIndex: Int,
    ustaName: String,
    onDismiss: () -> Unit,
    onOrder: () -> Unit,
) {
    val pagerState = rememberPagerState(
        initialPage = initialIndex,
        pageCount = { items.size },
    )

    LaunchedEffect(initialIndex) {
        pagerState.scrollToPage(initialIndex)
    }

    val currentItem by remember {
        derivedStateOf { items.getOrNull(pagerState.currentPage) }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.BottomCenter,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Color.White,
                        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    )
                    .clickable(enabled = false, onClick = {}),
            ) {
                // Tutqich chizig'i
                Box(
                    Modifier
                        .padding(top = 10.dp)
                        .align(Alignment.CenterHorizontally)
                        .width(40.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Cream200),
                )

                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 28.dp),
                ) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp),
                    ) { page ->
                        val item = items.getOrNull(page) ?: return@HorizontalPager
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Cream100),
                            contentAlignment = Alignment.Center,
                        ) {
                            AsyncImage(
                                model = imageUrl(item.imageUrl),
                                contentDescription = item.caption ?: ustaName,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.fillMaxSize(),
                            )
                        }
                    }

                    if (items.size > 1) {
                        Spacer(Modifier.height(10.dp))
                        PageIndicator(
                            pageCount = items.size,
                            currentPage = pagerState.currentPage,
                        )
                    }

                    Spacer(Modifier.height(14.dp))
                    Text(
                        text = currentItem?.caption
                            ?: stringResource(R.string.usta_portfolio_item_fallback, ustaName),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Ink900,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        stringResource(R.string.usta_portfolio_item_hint),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Ink500,
                    )
                    Spacer(Modifier.height(16.dp))
                    PrimaryButton(
                        text = stringResource(R.string.usta_order_cta),
                        onClick = onOrder,
                    )
                }
            }
        }
    }
}

@Composable
private fun PageIndicator(pageCount: Int, currentPage: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
    ) {
        repeat(pageCount) { i ->
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .size(if (i == currentPage) 10.dp else 8.dp)
                    .clip(CircleShape)
                    .background(if (i == currentPage) Terra600 else Cream200),
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
