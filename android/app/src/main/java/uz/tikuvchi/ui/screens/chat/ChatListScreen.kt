package uz.tikuvchi.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import uz.tikuvchi.R
import uz.tikuvchi.data.ConversationWithProfile
import uz.tikuvchi.data.Reconnect
import uz.tikuvchi.data.model.MessageType
import uz.tikuvchi.ui.components.AppHeader
import uz.tikuvchi.ui.components.Avatar
import uz.tikuvchi.ui.components.AvatarSize
import uz.tikuvchi.ui.components.EmptyState
import uz.tikuvchi.ui.components.ErrorState
import uz.tikuvchi.ui.components.bottomNavSpace
import uz.tikuvchi.ui.theme.Cream50
import uz.tikuvchi.ui.theme.Ink500
import uz.tikuvchi.ui.theme.Ink900
import uz.tikuvchi.ui.theme.Terra600
import uz.tikuvchi.data.ProfileRepository
import uz.tikuvchi.util.formatChatTime

@Composable
fun ChatListScreen(
    onConversation: (id: String, name: String) -> Unit,
    vm: ChatListViewModel = viewModel(),
) {
    val s by vm.state.collectAsStateWithLifecycle()

    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(lifecycle) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            vm.refresh()
        }
    }

    Column(Modifier.fillMaxSize().background(Cream50).statusBarsPadding()) {
        AppHeader(
            title = stringResource(R.string.chat_title),
        )

        when {
            s.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Terra600)
            }

            s.error -> Box(Modifier.fillMaxSize().padding(16.dp)) {
                ErrorState(onRetry = Reconnect::request)
            }

            s.conversations.isEmpty() -> Box(Modifier.fillMaxSize().padding(16.dp)) {
                EmptyState(
                    icon = R.drawable.ic_chat,
                    title = stringResource(R.string.chat_empty),
                    hint = stringResource(R.string.chat_empty_hint),
                )
            }

            else -> {
                val currentUserId = remember { ProfileRepository.currentUserId() }
                LazyColumn(
                    contentPadding = PaddingValues(
                        top = 16.dp,
                        bottom = bottomNavSpace(),
                    ),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(s.conversations, key = { it.id }) { c ->
                        val otherId = if (c.clientId == currentUserId) c.ustaId else c.clientId
                        ConversationRowItem(c, Modifier.padding(horizontal = 16.dp)) {
                            onConversation(otherId, c.otherName)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ConversationRowItem(
    c: ConversationWithProfile,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val last = c.last
    val preview = when {
        last == null -> ""
        last.messageType == MessageType.PRICE_OFFER -> stringResource(R.string.chat_price_offer)
        last.messageType == MessageType.IMAGE -> stringResource(R.string.chat_photo)
        else -> last.content.orEmpty()
    }
    // Matnli xabarda ikonka yo'q — faqat maxsus turlar belgilanadi
    val previewIcon = when (last?.messageType) {
        MessageType.PRICE_OFFER -> R.drawable.ic_tag
        MessageType.IMAGE -> R.drawable.ic_image
        else -> null
    }

    Row(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Avatar(name = c.otherName, src = c.otherAvatar, size = AvatarSize.LG)
        Column(Modifier.weight(1f)) {
            Text(
                c.otherName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = if (c.unread) androidx.compose.ui.text.font.FontWeight.ExtraBold else androidx.compose.ui.text.font.FontWeight.SemiBold,
                color = Ink900,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (preview.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    previewIcon?.let {
                        Icon(
                            painter = painterResource(it),
                            contentDescription = null,
                            tint = Ink500,
                            modifier = Modifier.size(14.dp),
                        )
                    }
                    Text(
                        preview,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (c.unread) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal,
                        color = if (c.unread) Ink900 else Ink500,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
        Text(
            formatChatTime(c.lastMessageAt),
            style = MaterialTheme.typography.labelSmall,
            color = Ink500,
        )
    }
}
