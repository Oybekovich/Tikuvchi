package uz.tikuvchi.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import uz.tikuvchi.data.supabase
import uz.tikuvchi.ui.screens.auth.AuthScreen
import uz.tikuvchi.ui.theme.Terra600

/**
 * Ilova to'liq yopiq — web'dagi proxy.ts bilan bir xil mantiq: sessiya bo'lmasa
 * faqat login ekrani ko'rinadi, ilova ekranlariga umuman yo'l yo'q.
 *
 * Sessiya holati Supabase'ning o'zidan kuzatiladi, shuning uchun chiqish qayerda
 * bo'lmasin (yoki token eskirsa) ekran o'zi login'ga qaytadi.
 */
@Composable
fun TikuvchiRoot() {
    var status by remember { mutableStateOf<SessionStatus?>(null) }

    LaunchedEffect(Unit) {
        supabase.auth.sessionStatus.collect { status = it }
    }

    when (status) {
        null, is SessionStatus.Initializing -> Splash()
        is SessionStatus.Authenticated -> HomePlaceholder()
        // NotAuthenticated yoki RefreshFailure — ikkalasida ham kirish talab qilinadi
        else -> AuthScreen(onAuthenticated = { /* sessionStatus o'zi yangilanadi */ })
    }
}

@Composable
private fun Splash() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = Terra600)
    }
}

@Composable
private fun HomePlaceholder() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Kirdingiz — bosh sahifa keyingi bosqichda")
    }
}
