package uz.tikuvchi.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import uz.tikuvchi.R
import uz.tikuvchi.data.model.UserRole
import uz.tikuvchi.ui.components.AppHeader
import uz.tikuvchi.ui.components.Avatar
import uz.tikuvchi.ui.components.AvatarSize
import uz.tikuvchi.ui.components.LabeledField
import uz.tikuvchi.ui.components.PhoneField
import uz.tikuvchi.ui.components.PrimaryButton
import uz.tikuvchi.ui.components.SecondaryButton
import uz.tikuvchi.ui.theme.Cream50
import uz.tikuvchi.ui.theme.Ink500
import uz.tikuvchi.ui.theme.Ink900
import uz.tikuvchi.ui.theme.Sage500
import uz.tikuvchi.ui.theme.Terra50
import uz.tikuvchi.ui.theme.Terra600
import uz.tikuvchi.ui.theme.Terra700
import uz.tikuvchi.util.PHONE_PREFIX

@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    vm: ProfileViewModel = viewModel(),
) {
    val s by vm.state.collectAsStateWithLifecycle()
    var confirmLogout by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().background(Cream50).statusBarsPadding().imePadding()) {
        AppHeader(title = stringResource(R.string.profile_title), onBack = onBack)

        if (s.loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Terra600)
            }
            return@Column
        }

        Column(
            Modifier.verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Card {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Avatar(name = s.fullName.ifBlank { s.email }, size = AvatarSize.XL)
                    Column {
                        Text(
                            s.fullName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Ink900,
                        )
                        Text(s.email, style = MaterialTheme.typography.bodyMedium, color = Ink500)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            stringResource(
                                if (s.role == UserRole.USTA) R.string.profile_role_usta
                                else R.string.profile_role_client
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            color = Terra700,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Terra50)
                                .padding(horizontal = 12.dp, vertical = 2.dp),
                        )
                    }
                }
            }

            Card {
                Text(
                    stringResource(R.string.profile_settings),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Ink900,
                )
                Spacer(Modifier.height(12.dp))
                LabeledField(
                    label = stringResource(R.string.profile_full_name),
                    value = s.fullName,
                    onValueChange = vm::setFullName,
                    placeholder = stringResource(R.string.auth_full_name_placeholder),
                )
                Spacer(Modifier.height(12.dp))
                PhoneField(
                    label = stringResource(R.string.profile_phone),
                    value = s.phone,
                    onValueChange = vm::setPhone,
                    placeholder = stringResource(R.string.auth_phone_placeholder),
                    modifier = Modifier.onFocusChanged { f ->
                        if (f.isFocused && s.phone.isEmpty()) {
                            vm.setPhone(PHONE_PREFIX)
                        } else if (!f.isFocused && s.phone.trim() == PHONE_PREFIX.trim()) {
                            vm.setPhone("")
                        }
                    },
                )
                Spacer(Modifier.height(12.dp))
                // Email o'zgartirilmaydi — hisob shu bilan bog'langan
                LabeledField(
                    label = stringResource(R.string.profile_email),
                    value = s.email,
                    onValueChange = {},
                    enabled = false,
                )
                Spacer(Modifier.height(16.dp))
                PrimaryButton(
                    text = stringResource(R.string.profile_save_changes),
                    onClick = vm::save,
                    loading = s.saving,
                    enabled = s.fullName.isNotBlank(),
                )
                if (s.saved) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        stringResource(R.string.profile_saved),
                        style = MaterialTheme.typography.labelLarge,
                        color = Sage500,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                if (s.error) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        stringResource(R.string.common_error),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            SecondaryButton(
                text = stringResource(R.string.profile_logout),
                onClick = { confirmLogout = true },
            )
        }
    }

    if (confirmLogout) {
        AlertDialog(
            onDismissRequest = { confirmLogout = false },
            title = { Text(stringResource(R.string.profile_logout)) },
            text = { Text(stringResource(R.string.profile_logout_confirm)) },
            confirmButton = {
                TextButton(onClick = { confirmLogout = false; vm.logout() }) {
                    Text(stringResource(R.string.profile_logout), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmLogout = false }) {
                    Text(stringResource(R.string.common_cancel))
                }
            },
            containerColor = Cream50,
        )
    }
}

@Composable
private fun Card(content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(16.dp),
        content = content,
    )
}
