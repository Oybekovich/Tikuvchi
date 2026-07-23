package uz.tikuvchi.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.Color
import uz.tikuvchi.R
import uz.tikuvchi.ui.components.LabeledField
import uz.tikuvchi.ui.components.PhoneField
import uz.tikuvchi.ui.components.PrimaryButton
import uz.tikuvchi.ui.theme.Cream50
import uz.tikuvchi.ui.theme.Gold100
import uz.tikuvchi.ui.theme.Ink500
import uz.tikuvchi.ui.theme.Ink700
import uz.tikuvchi.ui.theme.Ink900
import uz.tikuvchi.ui.theme.Red50
import uz.tikuvchi.ui.theme.Red700
import uz.tikuvchi.ui.theme.Terra700
import uz.tikuvchi.util.PHONE_PREFIX

@Composable
fun AuthScreen(
    onAuthenticated: () -> Unit,
    vm: AuthViewModel = viewModel(),
) {
    val s by vm.state.collectAsStateWithLifecycle()
    val register = s.mode == AuthMode.REGISTER

    Box(
        Modifier
            .fillMaxSize()
            .background(Cream50)
            // Uzun forma (ro'yxatdan o'tish) status bar ostiga kirib ketmasin
            .safeDrawingPadding()
            .imePadding(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            Modifier
                .widthIn(max = 420.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painter = painterResource(R.mipmap.ic_launcher_bg),
                contentDescription = null,
                modifier = Modifier.size(72.dp).clip(RoundedCornerShape(18.dp)),
            )
            Spacer(Modifier.height(16.dp))
            Text(
                stringResource(if (register) R.string.auth_register_title else R.string.auth_login_title),
                style = MaterialTheme.typography.headlineSmall,
                color = Ink900,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                stringResource(if (register) R.string.auth_register_subtitle else R.string.auth_login_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = Ink500,
            )
            Spacer(Modifier.height(24.dp))

            if (register) {
                RegisterRoleToggle(
                    selected = s.registerRole,
                    onSelect = vm::setRegisterRole,
                )
                Spacer(Modifier.height(16.dp))
                LabeledField(
                    label = stringResource(R.string.auth_full_name),
                    value = s.fullName,
                    onValueChange = vm::setFullName,
                    placeholder = stringResource(R.string.auth_full_name_placeholder),
                )
                Spacer(Modifier.height(12.dp))
                PhoneField(
                    label = stringResource(R.string.auth_phone),
                    value = s.phone,
                    onValueChange = vm::setPhone,
                    placeholder = stringResource(R.string.auth_phone_placeholder),
                    // Fokuslanganda +998 o'zi qo'yiladi, hech narsa yozilmasa yana bo'shaydi
                    modifier = Modifier.onFocusChanged { f ->
                        if (f.isFocused && s.phone.isEmpty()) {
                            vm.setPhone(PHONE_PREFIX)
                        } else if (!f.isFocused && s.phone.trim() == PHONE_PREFIX.trim()) {
                            vm.setPhone("")
                        }
                    },
                )
                Spacer(Modifier.height(12.dp))
            }

            LabeledField(
                label = stringResource(R.string.auth_email),
                value = s.email,
                onValueChange = vm::setEmail,
                placeholder = stringResource(R.string.auth_email_placeholder),
                keyboardType = KeyboardType.Email,
            )
            Spacer(Modifier.height(12.dp))
            LabeledField(
                label = stringResource(R.string.auth_password),
                value = s.password,
                onValueChange = vm::setPassword,
                placeholder = stringResource(R.string.auth_password_min),
                keyboardType = KeyboardType.Password,
                isPassword = true,
            )

            s.error?.let { err ->
                Spacer(Modifier.height(12.dp))
                Banner(
                    text = stringResource(
                        when (err) {
                            AuthError.INVALID_CREDENTIALS -> R.string.auth_invalid_credentials
                            AuthError.EMAIL_TAKEN -> R.string.auth_email_taken
                            AuthError.GENERIC -> R.string.auth_generic_error
                        }
                    ),
                    bg = Red50,
                    fg = Red700,
                )
            }
            if (s.checkEmail) {
                Spacer(Modifier.height(12.dp))
                Banner(stringResource(R.string.auth_check_email), bg = Gold100, fg = Ink700)
            }

            Spacer(Modifier.height(16.dp))
            PrimaryButton(
                text = stringResource(if (register) R.string.auth_register_btn else R.string.auth_login_btn),
                onClick = { vm.submit(onAuthenticated) },
                loading = s.busy,
                enabled = s.email.isNotBlank() && s.password.length >= 6 &&
                    (!register || s.fullName.isNotBlank()),
            )

            Spacer(Modifier.height(20.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    stringResource(if (register) R.string.auth_have_account else R.string.auth_no_account),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Ink500,
                )
                Text(
                    stringResource(if (register) R.string.auth_login_link else R.string.auth_register_link),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Terra700,
                    modifier = Modifier.clickable {
                        vm.setMode(if (register) AuthMode.LOGIN else AuthMode.REGISTER)
                    },
                )
            }

            if (!register) {
                Spacer(Modifier.height(24.dp))
                Column(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color.White)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        stringResource(R.string.auth_demo_title),
                        style = MaterialTheme.typography.labelSmall,
                        color = Ink500,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        stringResource(R.string.auth_demo_hint),
                        style = MaterialTheme.typography.titleSmall,
                        color = Ink700,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

@Composable
private fun RegisterRoleToggle(
    selected: RegisterRole,
    onSelect: (RegisterRole) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Gold100)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        RoleOption(
            text = stringResource(R.string.auth_role_client),
            selected = selected == RegisterRole.CLIENT,
            onClick = { onSelect(RegisterRole.CLIENT) },
            modifier = Modifier.weight(1f),
        )
        RoleOption(
            text = stringResource(R.string.auth_role_usta),
            selected = selected == RegisterRole.USTA,
            onClick = { onSelect(RegisterRole.USTA) },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun RoleOption(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) Color.White else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 11.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = if (selected) Terra700 else Ink700,
        )
    }
}

@Composable
private fun Banner(text: String, bg: Color, fg: Color) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = fg,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .padding(horizontal = 16.dp, vertical = 12.dp),
    )
}
