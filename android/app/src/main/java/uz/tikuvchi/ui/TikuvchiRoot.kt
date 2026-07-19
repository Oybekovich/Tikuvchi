package uz.tikuvchi.ui

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
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
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import uz.tikuvchi.data.supabase
import uz.tikuvchi.ui.components.BottomNav
import uz.tikuvchi.ui.components.NavTab
import uz.tikuvchi.ui.screens.auth.AuthScreen
import uz.tikuvchi.ui.screens.chat.ChatListScreen
import uz.tikuvchi.ui.screens.chat.ChatScreen
import uz.tikuvchi.ui.screens.home.HomeScreen
import uz.tikuvchi.ui.screens.measurements.MeasurementsScreen
import uz.tikuvchi.ui.screens.order.OrderWizardScreen
import uz.tikuvchi.ui.screens.orders.OrderDetailScreen
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
        // RefreshFailure — bu chiqish EMAS. Kutubxona uni faqat ikki holatda
        // chiqaradi: tarmoqqa yetib bo'lmadi yoki server 5xx qaytardi — ikkalasida
        // ham sessiya joyida turadi va qayta urinish davom etadi. Bu yerda login
        // ekraniga o'tkazilsa, tarmoq bir lahzaga uzilganda foydalanuvchi
        // sababsiz chiqib ketadi, keyin qayta urinish o'tgach yana kiradi.
        // Token haqiqatan yaroqsiz bo'lsa kutubxona NotAuthenticated beradi.
        is SessionStatus.RefreshFailure -> AppNav()
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
    const val ORDER_DETAIL = "orders/{id}"

    fun orderDetail(id: String) = "orders/$id"

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

    Box(Modifier.fillMaxSize()) {
        NavHost(
            navController = nav,
            startDestination = Route.HOME,
            modifier = Modifier.fillMaxSize(),
            // NavHost'ning o'z sozlamasi — 700 ms lik so'nish. Tab almashtirishda bu
            // butun ekranni yarim soniyadan ko'proq xiralashtirib turadi va ilova
            // sekin ishlayotgandek tuyuladi. Qisqa fade kifoya.
            enterTransition = { fadeIn(tween(120)) },
            exitTransition = { fadeOut(tween(90)) },
            popEnterTransition = { fadeIn(tween(120)) },
            popExitTransition = { fadeOut(tween(90)) },
        ) {
            composable(Route.HOME) {
                HomeScreen(
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
                    onProfile = { nav.navigate(Route.PROFILE) },
                    onOrder = { id -> nav.navigate(Route.orderDetail(id)) },
                )
            }

            composable(
                Route.ORDER_DETAIL,
                arguments = listOf(navArgument("id") { type = NavType.StringType }),
            ) { entry ->
                OrderDetailScreen(
                    orderId = entry.arguments?.getString("id").orEmpty(),
                    onBack = { nav.popBackStack() },
                    onUsta = { id -> nav.navigate(Route.usta(id)) },
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
                    onProfile = { nav.navigate(Route.PROFILE) },
                )
            }

            composable(Route.CHAT) {
                ChatListScreen(
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

        NavBar(nav)
    }
}

/**
 * Ataylab alohida composable: joriy yo'nalish shu yerda o'qiladi, AppNav'da emas.
 * Aks holda har bir o'tishda AppNav qayta tuzilib, NavHost'ning butun ekranlar
 * ro'yxati qaytadan yig'ilardi — panelning o'zi esa shundoq ham yangilanadi.
 */
@Composable
private fun BoxScope.NavBar(nav: NavHostController) {
    val currentRoute = nav.currentBackStackEntryAsState().value?.destination?.route

    /**
     * Web'dagi AppShell bilan bir xil: buyurtma sehrgari va chat oynasida
     * pastki navigatsiya yashiriladi (chatda pastda xabar yozish paneli turadi,
     * sehrgar esa chalg'itmaslik uchun to'liq ekranli).
     */
    val showBottomNav = currentRoute != null &&
        currentRoute != Route.ORDER_NEW &&
        currentRoute != Route.CHAT_WITH

    if (!showBottomNav) return

    val currentTab = when (currentRoute) {
        Route.HOME -> NavTab.HOME
        Route.ORDERS, Route.ORDER_DETAIL -> NavTab.ORDERS
        Route.MEASUREMENTS -> NavTab.MEASUREMENTS
        Route.CHAT -> NavTab.CHAT
        Route.PROFILE -> NavTab.PROFILE
        else -> null
    }

    BottomNav(
        modifier = Modifier.align(Alignment.BottomCenter),
        current = currentTab,
        onSelect = { tab ->
            val route = when (tab) {
                NavTab.HOME -> Route.HOME
                NavTab.ORDERS -> Route.ORDERS
                NavTab.MEASUREMENTS -> Route.MEASUREMENTS
                NavTab.CHAT -> Route.CHAT
                NavTab.PROFILE -> Route.PROFILE
            }
            nav.navigate(route) {
                // saveState/restoreState juftligi tab holatini saqlaydi: qaytganda
                // ro'yxat o'sha joyidan ochiladi, ekran noldan yuklanmaydi
                popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        },
    )
}

@Composable
private fun Splash() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = Terra600)
    }
}
