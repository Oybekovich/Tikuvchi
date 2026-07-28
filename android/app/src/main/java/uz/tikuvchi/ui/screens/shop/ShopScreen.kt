package uz.tikuvchi.ui.screens.shop

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import uz.tikuvchi.R
import uz.tikuvchi.ui.components.AppHeader
import uz.tikuvchi.ui.components.EmptyState
import uz.tikuvchi.ui.components.ErrorState
import uz.tikuvchi.ui.components.PrimaryButton
import uz.tikuvchi.ui.components.SecondaryButton
import uz.tikuvchi.ui.components.bottomNavSpace
import uz.tikuvchi.ui.theme.Cream50
import uz.tikuvchi.ui.theme.Green600
import uz.tikuvchi.ui.theme.Ink500
import uz.tikuvchi.ui.theme.Ink900
import uz.tikuvchi.ui.theme.Red600
import uz.tikuvchi.ui.theme.Terra600
import uz.tikuvchi.ui.theme.Terra700
import uz.tikuvchi.ui.theme.Terra50
import uz.tikuvchi.ui.theme.Cream200
import uz.tikuvchi.ui.components.PlainInput
import uz.tikuvchi.ui.components.Avatar
import uz.tikuvchi.ui.components.AvatarSize

private enum class ShopTab { PROFILE, SERVICES, SETTINGS }

@Composable
fun ShopScreen(
    vm: ShopViewModel = viewModel(),
) {
    val s by vm.state.collectAsStateWithLifecycle()
    val tabState = remember { mutableStateOf(ShopTab.PROFILE) }
    val tab = tabState.value

    Column(Modifier.fillMaxSize().background(Cream50).statusBarsPadding()) {
        AppHeader(title = stringResource(R.string.shop_title))

        Row(
            Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Cream200)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            TabChip(stringResource(R.string.shop_tab_profile), tab == ShopTab.PROFILE, Modifier.weight(1f)) { tabState.value = ShopTab.PROFILE }
            TabChip(stringResource(R.string.shop_tab_services), tab == ShopTab.SERVICES, Modifier.weight(1f)) { tabState.value = ShopTab.SERVICES }
            TabChip(stringResource(R.string.shop_tab_settings), tab == ShopTab.SETTINGS, Modifier.weight(1f)) { tabState.value = ShopTab.SETTINGS }
        }

        when {
            s.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Terra600)
            }
            s.error -> Box(Modifier.fillMaxSize().padding(16.dp)) {
                ErrorState(onRetry = vm::load)
            }
            else -> when (tab) {
                ShopTab.PROFILE -> ProfileTab(s, vm)
                ShopTab.SERVICES -> ServicesTab(s, vm)
                ShopTab.SETTINGS -> SettingsTab(s, vm)
            }
        }
    }
}

@Composable
private fun TabChip(text: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = if (selected) Terra700 else Ink500,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) Color.White else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
    )
}

@Composable
private fun ProfileTab(s: ShopUiState, vm: ShopViewModel) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp, bottom = bottomNavSpace()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            if (s.saved) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Green600.copy(alpha = 0.1f))
                        .padding(12.dp)
                ) {
                    Text(stringResource(R.string.shop_saved), color = Green600, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        item {
            ProfileCard(s, vm)
        }
        item {
            PrimaryButton(
                text = stringResource(R.string.shop_save_profile),
                onClick = vm::saveProfile,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun ProfileCard(s: ShopUiState, vm: ShopViewModel) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        s.profile?.let { p ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Avatar(name = p.profiles?.fullName ?: "", src = p.profiles?.avatarUrl, size = AvatarSize.LG)
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(p.profiles?.fullName ?: "", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(stringResource(R.string.role_usta), style = MaterialTheme.typography.labelSmall, color = Ink500)
                }
            }
        }
        LabeledField(stringResource(R.string.shop_bio), s.bio, vm::setBio)
        LabeledField(stringResource(R.string.shop_location), s.locationText, vm::setLocationText)
        LabeledField(stringResource(R.string.shop_district), s.district, vm::setDistrict)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PlainInput(
                value = s.workHoursStart,
                onValueChange = vm::setWorkHoursStart,
                placeholder = stringResource(R.string.shop_work_start),
                modifier = Modifier.weight(1f),
            )
            PlainInput(
                value = s.workHoursEnd,
                onValueChange = vm::setWorkHoursEnd,
                placeholder = stringResource(R.string.shop_work_end),
                modifier = Modifier.weight(1f),
            )
        }
        LabeledField(stringResource(R.string.shop_tags), s.tagsText, vm::setTagsText)
    }
}

@Composable
private fun LabeledField(label: String, value: String, onChange: (String) -> Unit) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Ink500)
        Spacer(Modifier.height(4.dp))
        PlainInput(
            value = value,
            onValueChange = onChange,
            placeholder = "",
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun ServicesTab(s: ShopUiState, vm: ShopViewModel) {
    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        LazyColumn(
            contentPadding = PaddingValues(16.dp, bottom = bottomNavSpace()),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                if (s.serviceSaved) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Green600.copy(alpha = 0.1f))
                            .padding(12.dp)
                    ) {
                        Text(stringResource(R.string.shop_service_saved), color = Green600, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            if (s.services.isEmpty()) {
                item {
                    EmptyState(
                        icon = R.drawable.ic_chat,
                        title = stringResource(R.string.shop_no_services),
                        hint = stringResource(R.string.shop_no_services_hint),
                    )
                }
            } else {
                items(s.services, key = { it.id }) { svc ->
                    ServiceCard(svc, onEdit = { vm.showEditService(svc) }, onDelete = { vm.deleteService(svc.id) })
                }
            }
            item {
                Spacer(Modifier.height(8.dp))
                PrimaryButton(
                    text = stringResource(R.string.shop_add_service),
                    onClick = vm::showAddService,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        if (s.showServiceForm) {
            ServiceFormDialog(s, vm)
        }
    }
}

@Composable
private fun ServiceCard(svc: uz.tikuvchi.data.model.ServiceItem, onEdit: () -> Unit, onDelete: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(svc.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Ink900)
            svc.description?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = Ink500, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            Spacer(Modifier.height(4.dp))
            Text(formatPrice(svc.basePrice), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Terra700)
        }
        Row {
            IconButton(onClick = onEdit) {
                Icon(painterResource(R.drawable.ic_edit), contentDescription = null, tint = Terra600)
            }
            IconButton(onClick = onDelete) {
                Icon(painterResource(R.drawable.ic_delete), contentDescription = null, tint = Red600)
            }
        }
    }
}

@Composable
private fun ServiceFormDialog(s: ShopUiState, vm: ShopViewModel) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = vm::hideServiceForm,
        title = {
            Text(
                if (s.editingServiceId != null) stringResource(R.string.shop_edit_service) else stringResource(R.string.shop_add_service),
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                PlainInput(
                    value = s.serviceTitle,
                    onValueChange = vm::setServiceTitle,
                    placeholder = stringResource(R.string.shop_service_title),
                    modifier = Modifier.fillMaxWidth(),
                )
                PlainInput(
                    value = s.serviceDescription,
                    onValueChange = vm::setServiceDescription,
                    placeholder = stringResource(R.string.shop_service_desc),
                    modifier = Modifier.fillMaxWidth(),
                )
                PlainInput(
                    value = s.servicePrice,
                    onValueChange = vm::setServicePrice,
                    placeholder = stringResource(R.string.shop_service_price),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            PrimaryButton(text = stringResource(R.string.save), onClick = vm::saveService)
        },
        dismissButton = {
            SecondaryButton(text = stringResource(R.string.cancel), onClick = vm::hideServiceForm)
        },
    )
}

@Composable
private fun SettingsTab(s: ShopUiState, vm: ShopViewModel) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp, bottom = bottomNavSpace()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            if (s.saved) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Green600.copy(alpha = 0.1f))
                        .padding(12.dp)
                ) {
                    Text(stringResource(R.string.shop_saved), color = Green600, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        item {
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                ToggleRow(stringResource(R.string.shop_available), s.available) { vm.setAvailable(it) }
                ToggleRow(stringResource(R.string.shop_visible), s.visible) { vm.setVisible(it) }
            }
        }
        item {
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(stringResource(R.string.shop_gender_segment), style = MaterialTheme.typography.titleSmall, color = Ink900)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GenderButton(stringResource(R.string.gender_women), s.genderSegment == uz.tikuvchi.data.model.GenderSegment.WOMEN) { vm.setGenderSegment(uz.tikuvchi.data.model.GenderSegment.WOMEN) }
                    GenderButton(stringResource(R.string.gender_men), s.genderSegment == uz.tikuvchi.data.model.GenderSegment.MEN) { vm.setGenderSegment(uz.tikuvchi.data.model.GenderSegment.MEN) }
                }
            }
        }
        item {
            PrimaryButton(
                text = stringResource(R.string.shop_save_settings),
                onClick = vm::saveProfile,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = Ink900)
        androidx.compose.material3.Switch(
            checked = checked,
            onCheckedChange = onChange,
            colors = androidx.compose.material3.SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Terra600,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Ink500.copy(alpha = 0.3f),
            ),
        )
    }
}

@Composable
private fun GenderButton(text: String, selected: Boolean, onClick: () -> Unit) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
        color = if (selected) Color.White else Ink500,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) Terra600 else Cream200)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
    )
}

private fun formatPrice(amount: Long): String {
    val s = amount.toString()
    val sb = StringBuilder()
    var count = 0
    for (i in s.lastIndex downTo 0) {
        if (count > 0 && count % 3 == 0) sb.insert(0, ' ')
        sb.insert(0, s[i])
        count++
    }
    return "$sb so'm"
}
