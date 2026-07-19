package uz.tikuvchi.ui.screens.chat

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
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
import uz.tikuvchi.data.Reconnect
import uz.tikuvchi.data.model.Message
import uz.tikuvchi.data.model.MessageType
import uz.tikuvchi.data.model.PriceOfferStatus
import uz.tikuvchi.ui.components.AppHeader
import uz.tikuvchi.ui.components.ErrorState
import uz.tikuvchi.ui.components.PlainInput
import uz.tikuvchi.ui.theme.Cream100
import uz.tikuvchi.ui.theme.Cream200
import uz.tikuvchi.ui.theme.Cream50
import uz.tikuvchi.ui.theme.Gold100
import uz.tikuvchi.ui.theme.Ink500
import uz.tikuvchi.ui.theme.Ink700
import uz.tikuvchi.ui.theme.Ink900
import uz.tikuvchi.ui.theme.Terra600
import uz.tikuvchi.ui.theme.Terra700
import uz.tikuvchi.util.formatCurrency
import uz.tikuvchi.util.imageUrl

@Composable
fun ChatScreen(
    ustaId: String,
    ustaName: String?,
    onBack: () -> Unit,
) {
    val vm: ChatViewModel = viewModel(
        key = ustaId,
        factory = viewModelFactory { initializer { ChatViewModel(ustaId) } },
    )
    val s by vm.state.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    // Yangi xabar kelganda pastga tushamiz
    LaunchedEffect(s.messages.size) {
        if (s.messages.isNotEmpty()) listState.animateScrollToItem(s.messages.lastIndex)
    }

    Column(Modifier.fillMaxSize().background(Cream50).statusBarsPadding().imePadding()) {
        AppHeader(title = ustaName, onBack = onBack)

        if (s.loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Terra600)
            }
            return@Column
        }

        // Yozishmalar yuklanmagan bo'lsa bo'sh suhbat ko'rsatilmaydi — foydalanuvchi
        // eski xabarlar yo'qolgan deb o'ylab, qaytadan yozib yubormasligi uchun
        if (s.error && s.messages.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(16.dp)) {
                ErrorState(onRetry = Reconnect::request)
            }
            return@Column
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(s.messages, key = { it.id }) { m ->
                MessageBubble(m, mine = m.senderId == s.myId)
            }
        }

        Row(
            Modifier
                .fillMaxWidth()
                .background(Cream50)
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .padding(
                    bottom = WindowInsets.navigationBars.asPaddingValues()
                        .calculateBottomPadding(),
                ),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PlainInput(
                value = s.input,
                onValueChange = vm::setInput,
                placeholder = stringResource(R.string.chat_input_placeholder),
                modifier = Modifier.weight(1f),
            )
            IconButton(
                onClick = vm::send,
                enabled = s.input.isNotBlank() && !s.sending,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (s.input.isNotBlank()) Terra600 else Cream200),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_send),
                    contentDescription = stringResource(R.string.chat_send),
                    tint = if (s.input.isNotBlank()) Color.White else Ink500,
                )
            }
        }
    }
}

@Composable
private fun MessageBubble(m: Message, mine: Boolean) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = if (mine) Arrangement.End else Arrangement.Start,
    ) {
        Column(
            Modifier
                .widthIn(max = 280.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (mine) 16.dp else 4.dp,
                        bottomEnd = if (mine) 4.dp else 16.dp,
                    ),
                )
                .background(
                    when {
                        m.messageType == MessageType.PRICE_OFFER -> Gold100
                        m.messageType == MessageType.IMAGE -> Color.White
                        mine -> Terra600
                        else -> Color.White
                    },
                )
                .padding(if (m.messageType == MessageType.IMAGE) 4.dp else 12.dp),
        ) {
            when (m.messageType) {
                MessageType.PRICE_OFFER -> PriceOffer(m)
                // content — rasmning Storage'dagi manzili
                MessageType.IMAGE -> AsyncImage(
                    model = imageUrl(m.content),
                    contentDescription = stringResource(R.string.chat_photo),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .widthIn(max = 240.dp)
                        .heightIn(max = 320.dp)
                        .clip(RoundedCornerShape(12.dp)),
                )
                MessageType.TEXT -> Text(
                    m.content.orEmpty(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (mine) Color.White else Ink900,
                )
            }
        }
    }
}

@Composable
private fun PriceOffer(m: Message) {
    Text(
        stringResource(R.string.chat_price_offer),
        style = MaterialTheme.typography.labelSmall,
        color = Ink500,
    )
    Spacer(Modifier.height(4.dp))
    Text(
        formatCurrency(m.priceOfferAmount ?: 0),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.ExtraBold,
        color = Terra700,
    )
    m.priceOfferDurationDays?.let { days ->
        Text(
            stringResource(R.string.chat_offer_duration) + ": " +
                stringResource(R.string.chat_offer_duration_days, days),
            style = MaterialTheme.typography.labelSmall,
            color = Ink700,
        )
    }
    m.priceOfferNote?.let {
        Spacer(Modifier.height(4.dp))
        Text(it, style = MaterialTheme.typography.bodyMedium, color = Ink700)
    }
    // Taklifga javob usta ilovasida beriladi — mijozda faqat holati ko'rinadi
    m.priceOfferStatus?.let { st ->
        Spacer(Modifier.height(6.dp))
        Text(
            stringResource(
                when (st) {
                    PriceOfferStatus.ACCEPTED -> R.string.chat_offer_accepted
                    PriceOfferStatus.DECLINED -> R.string.chat_offer_declined
                    PriceOfferStatus.PENDING -> R.string.order_status_pending
                },
            ),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = Ink700,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Cream100)
                .padding(horizontal = 8.dp, vertical = 2.dp),
        )
    }
}
