package uz.tikuvchi.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
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
import kotlinx.coroutines.launch
import uz.tikuvchi.data.supabase
import uz.tikuvchi.ui.components.AppDrawer
import uz.tikuvchi.ui.components.MenuItem
import uz.tikuvchi.ui.screens.auth.AuthScreen
import uz.tikuvchi.ui.screens.chat.ChatListScreen
import uz.tikuvchi.ui.screens.chat.ChatScreen
import uz.tikuvchi.ui.screens.home.HomeScreen
import uz.tikuvchi.ui.screens.measurements.MeasurementsScreen
import uz.tikuvchi.ui.screens.order.OrderWizardScreen
import uz.tikuvchi.ui.screens.orders.OrdersScreen
import uz.tikuvchi.ui.screens.profile.ProfileScreen
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
    const val PROFILE = "profile"
    const val ORDERS = "orders"
    const val MEASUREMENTS = "measurements"
    const val CHAT = "chat"
    const val CHAT_WITH = "chat/{ustaId}?name={name}"
    const val ORDER_NEW = "usta/{id}/buyurtma"

    fun chatWith(ustaId: String, name: String?) =
        "chat/$ustaId?name=${name?.ifBlank { null } ?: "-"}"

    fun orderNew(ustaId: String) = "usta/$ustaId/buyurtma"

    fun usta(id: String) = "usta/$id"

    /** Bo'sh qiymatlar "-" bilan uzatiladi: NavHost bo'sh string argumentni yo'qotadi. */
    fun search(q: String = "", category: Long? = null) =
        "search?q=${q.ifBlank { "-" }}&category=${category ?: "-"}"
}

@Composable
private fun AppNav() {
    val nav = rememberNavController()
    val drawer = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    fun go(route: String) {
        scope.launch { drawer.close() }
        // Menyudan o'tishda stack o'smasin — bosh sahifagacha tozalaymiz
        nav.navigate(route) {
            popUpTo(Route.HOME) { inclusive = route == Route.HOME }
            launchSingleTop = true
        }
    }

    ModalNavigationDrawer(
        drawerState = drawer,
        drawerContent = {
            AppDrawer(onSelect = { item ->
                go(
                    when (item) {
                        MenuItem.HOME -> Route.HOME
                        MenuItem.SEARCH -> Route.search()
                        MenuItem.ORDERS -> Route.ORDERS
                        MenuItem.MEASUREMENTS -> Route.MEASUREMENTS
                        MenuItem.CHAT -> Route.CHAT
                        MenuItem.PROFILE -> Route.PROFILE
                        // TODO: chat ekrani keyingi qadamda
                    },
                )
            })
        },
    ) {
        NavHost(navController = nav, startDestination = Route.HOME) {
            composable(Route.HOME) {
                HomeScreen(
                    onMenu = { scope.launch { drawer.open() } },
                    onProfile = { nav.navigate(Route.PROFILE) },
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
                    onProfile = { nav.navigate(Route.PROFILE) },
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
                    onOrder = { id -> nav.navigate(Route.orderNew(id)) },
                    onChat = { id, name -> nav.navigate(Route.chatWith(id, name)) },
                )
            }

            composable(Route.PROFILE) {
                ProfileScreen(onBack = { nav.popBackStack() })
            }

            composable(Route.ORDERS) {
                OrdersScreen(
                    onMenu = { scope.launch { drawer.open() } },
                    onProfile = { nav.navigate(Route.PROFILE) },
                    onOrder = {},
                )
            }

            composable(
                Route.ORDER_NEW,
                arguments = listOf(navArgument("id") { type = NavType.StringType }),
            ) { entry ->
                OrderWizardScreen(
                    ustaId = entry.arguments?.getString("id").orEmpty(),
                    onClose = { nav.popBackStack() },
                    onCreated = {
                        // Buyurtma yaratilgach ro'yxatga o'tamiz, wizard stack'da qolmasin
                        nav.navigate(Route.ORDERS) {
                            popUpTo(Route.HOME)
                            launchSingleTop = true
                        }
                    },
                )
            }

            composable(Route.MEASUREMENTS) {
                MeasurementsScreen(
                    onMenu = { scope.launch { drawer.open() } },
                    onProfile = { nav.navigate(Route.PROFILE) },
                )
            }

            composable(Route.CHAT) {
                ChatListScreen(
                    onMenu = { scope.launch { drawer.open() } },
                    onProfile = { nav.navigate(Route.PROFILE) },
                    onConversation = { ustaId, name -> nav.navigate(Route.chatWith(ustaId, name)) },
                )
            }

            composable(
                Route.CHAT_WITH,
                arguments = listOf(
                    navArgument("ustaId") { type = NavType.StringType },
                    navArgument("name") { type = NavType.StringType; defaultValue = "-" },
                ),
            ) { entry ->
                val name = entry.arguments?.getString("name")
                ChatScreen(
                    ustaId = entry.arguments?.getString("ustaId").orEmpty(),
                    ustaName = name?.takeIf { it != "-" },
                    onBack = { nav.popBackStack() },
                )
            }
        }
    }
}

@Composable
private fun Splash() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = Terra600)
    }
}
