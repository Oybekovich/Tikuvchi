package uz.tikuvchi.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import uz.tikuvchi.data.supabase
import uz.tikuvchi.ui.screens.auth.AuthScreen
import uz.tikuvchi.ui.screens.home.HomeScreen
import uz.tikuvchi.ui.screens.search.SearchScreen
import uz.tikuvchi.ui.screens.usta.UstaScreen
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
        is SessionStatus.Authenticated -> AppNav()
        // NotAuthenticated yoki RefreshFailure — ikkalasida ham kirish talab qilinadi
        else -> AuthScreen(onAuthenticated = { /* sessionStatus o'zi yangilanadi */ })
    }
}

private object Route {
    const val HOME = "home"
    const val USTA = "usta/{id}"
    const val SEARCH = "search?q={q}&category={category}"

    fun usta(id: String) = "usta/$id"

    /** Bo'sh qiymatlar "-" bilan uzatiladi: NavHost bo'sh string argumentni yo'qotadi. */
    fun search(q: String = "", category: Long? = null) =
        "search?q=${q.ifBlank { "-" }}&category=${category ?: "-"}"
}

@Composable
private fun AppNav() {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = Route.HOME) {
        composable(Route.HOME) {
            HomeScreen(
                onMenu = {},
                onProfile = {},
                onSearch = { q -> nav.navigate(Route.search(q = q)) },
                onCategory = { id -> nav.navigate(Route.search(category = id)) },
                onUsta = { id -> nav.navigate(Route.usta(id)) },
            )
        }

        composable(
            Route.SEARCH,
            arguments = listOf(
                navArgument("q") { type = NavType.StringType; defaultValue = "-" },
                navArgument("category") { type = NavType.StringType; defaultValue = "-" },
            ),
        ) { entry ->
            val q = entry.arguments?.getString("q").orEmpty()
            val category = entry.arguments?.getString("category")
            SearchScreen(
                initialText = if (q == "-") "" else q,
                initialCategory = category?.toLongOrNull(),
                onBack = { nav.popBackStack() },
                onProfile = {},
                onUsta = { id -> nav.navigate(Route.usta(id)) },
            )
        }

        composable(
            Route.USTA,
            arguments = listOf(navArgument("id") { type = NavType.StringType }),
        ) { entry ->
            UstaScreen(
                ustaId = entry.arguments?.getString("id").orEmpty(),
                onBack = { nav.popBackStack() },
                onOrder = {},
                onChat = {},
            )
        }
    }
}

@Composable
private fun Splash() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = Terra600)
    }
}
