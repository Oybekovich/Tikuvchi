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
import androidx.compose.runtime.mutableIntStateOf
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
import uz.tikuvchi.data.ChatUnreadStore
import uz.tikuvchi.data.ProfileRepository
import uz.tikuvchi.data.fetchUnreadConversations
import uz.tikuvchi.data.supabase
import uz.tikuvchi.ui.components.BottomNav
import uz.tikuvchi.ui.components.NavTab
import uz.tikuvchi.ui.components.UstaBottomNav
import uz.tikuvchi.ui.components.UstaNavTab
import uz.tikuvchi.ui.screens.auth.AuthScreen
import uz.tikuvchi.ui.screens.chat.ChatListScreen
import uz.tikuvchi.ui.screens.chat.ChatScreen
import uz.tikuvchi.ui.screens.dashboard.DashboardScreen
import uz.tikuvchi.ui.screens.home.HomeScreen
import uz.tikuvchi.ui.screens.measurements.MeasurementsScreen
import uz.tikuvchi.ui.screens.order.OrderWizardScreen
import uz.tikuvchi.ui.screens.orders.OrderDetailScreen
import uz.tikuvchi.ui.screens.orders.OrdersScreen
import uz.tikuvchi.ui.screens.profile.ProfileScreen
import uz.tikuvchi.ui.screens.search.SearchScreen
import uz.tikuvchi.ui.screens.shop.ShopScreen
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
        is SessionStatus.RefreshFailure -> AppNav()
        else -> AuthScreen(onAuthenticated = { })
    }
}

private object Route {
    const val HOME = "home"
    const val DASHBOARD = "dashboard"
    const val USTA = "usta/{id}"
    const val SEARCH = "search?q={q}&category={category}"
    const val PROFILE = "profile"
    const val ORDERS = "orders"
    const val MEASUREMENTS = "measurements"
    const val CHAT = "chat"
    const val CHAT_WITH = "chat/{ustaId}?name={name}"
    const val ORDER_NEW = "usta/{id}/buyurtma"
    const val ORDER_DETAIL = "orders/{id}"
    const val SHOP = "shop"

    fun orderDetail(id: String) = "orders/$id"
    fun chatWith(ustaId: String, name: String?) =
        "chat/$ustaId?name=${name?.ifBlank { null } ?: "-"}"
    fun orderNew(ustaId: String) = "usta/$ustaId/buyurtma"
    fun usta(id: String) = "usta/$id"
    fun search(q: String = "", category: Long? = null) =
        "search?q=${q.ifBlank { "-" }}&category=${category ?: "-"}"
}

@Composable
private fun AppNav() {
    val nav = rememberNavController()
    var isUsta by remember { mutableStateOf<Boolean?>(null) }

    LaunchedEffect(Unit) {
        try {
            val profile = ProfileRepository.me()
            isUsta = profile?.role == uz.tikuvchi.data.model.UserRole.USTA
        } catch (_: Exception) {
            isUsta = false
        }
    }

    if (isUsta == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Terra600)
        }
        return
    }

    val startRoute = if (isUsta == true) Route.DASHBOARD else Route.HOME

    Box(Modifier.fillMaxSize()) {
        NavHost(
            navController = nav,
            startDestination = startRoute,
            modifier = Modifier.fillMaxSize(),
            enterTransition = { fadeIn(tween(120)) },
            exitTransition = { fadeOut(tween(90)) },
            popEnterTransition = { fadeIn(tween(120)) },
            popExitTransition = { fadeOut(tween(90)) },
        ) {
            // ---- Client routes ----
            composable(Route.HOME) {
                HomeScreen(
                    onSearch = { q -> nav.navigate(Route.search(q = q)) },
                    onCategory = { id -> nav.navigate(Route.search(category = id)) },
                    onUsta = { id -> nav.navigate(Route.usta(id)) },
                )
            }

            composable(Route.SEARCH, arguments = listOf(
                navArgument("q") { type = NavType.StringType; defaultValue = "-" },
                navArgument("category") { type = NavType.StringType; defaultValue = "-" },
            )) { entry ->
                val q = entry.arguments?.getString("q").orEmpty()
                val category = entry.arguments?.getString("category")
                SearchScreen(
                    initialText = if (q == "-") "" else q,
                    initialCategory = category?.toLongOrNull(),
                    onBack = { nav.popBackStack() },
                    onUsta = { id -> nav.navigate(Route.usta(id)) },
                )
            }

            composable(Route.USTA, arguments = listOf(navArgument("id") { type = NavType.StringType })) { entry ->
                UstaScreen(
                    ustaId = entry.arguments?.getString("id").orEmpty(),
                    onBack = { nav.popBackStack() },
                    onOrder = { id -> nav.navigate(Route.orderNew(id)) },
                    onChat = { id, name -> nav.navigate(Route.chatWith(id, name)) },
                )
            }

            composable(Route.MEASUREMENTS) {
                MeasurementsScreen()
            }

            // ---- Usta routes ----
            composable(Route.DASHBOARD) {
                DashboardScreen(
                    onOrder = { id -> nav.navigate(Route.orderDetail(id)) },
                )
            }

            composable(Route.SHOP) {
                ShopScreen()
            }

            // ---- Shared routes ----
            composable(Route.PROFILE) {
                ProfileScreen(
                    onBack = {
                        val home = if (isUsta == true) Route.DASHBOARD else Route.HOME
                        if (!nav.popBackStack(home, inclusive = false)) {
                            nav.navigate(home) { launchSingleTop = true }
                        }
                    },
                )
            }

            composable(Route.ORDERS) {
                OrdersScreen(
                    onOrder = { id -> nav.navigate(Route.orderDetail(id)) },
                )
            }

            composable(Route.ORDER_DETAIL, arguments = listOf(navArgument("id") { type = NavType.StringType })) { entry ->
                OrderDetailScreen(
                    orderId = entry.arguments?.getString("id").orEmpty(),
                    onBack = { nav.popBackStack() },
                    onUsta = { id -> nav.navigate(Route.usta(id)) },
                )
            }

            composable(Route.ORDER_NEW, arguments = listOf(navArgument("id") { type = NavType.StringType })) { entry ->
                OrderWizardScreen(
                    ustaId = entry.arguments?.getString("id").orEmpty(),
                    onClose = { nav.popBackStack() },
                    onCreated = { orderId ->
                        nav.navigate(Route.orderDetail(orderId)) {
                            popUpTo(Route.HOME)
                            launchSingleTop = true
                        }
                    },
                )
            }

            composable(Route.CHAT) {
                ChatListScreen(
                    onConversation = { ustaId, name -> nav.navigate(Route.chatWith(ustaId, name)) },
                )
            }

            composable(Route.CHAT_WITH, arguments = listOf(
                navArgument("ustaId") { type = NavType.StringType },
                navArgument("name") { type = NavType.StringType; defaultValue = "-" },
            )) { entry ->
                val name = entry.arguments?.getString("name")
                ChatScreen(
                    ustaId = entry.arguments?.getString("ustaId").orEmpty(),
                    ustaName = name?.takeIf { it != "-" },
                    onBack = { nav.popBackStack() },
                )
            }
        }

        if (isUsta == true) {
            UstaNavBar(nav)
        } else {
            ClientNavBar(nav)
        }
    }
}

@Composable
private fun BoxScope.ClientNavBar(nav: NavHostController) {
    val currentRoute = nav.currentBackStackEntryAsState().value?.destination?.route
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

    var unreadCount by remember { mutableIntStateOf(0) }
    LaunchedEffect(currentRoute) { unreadCount = fetchUnreadConversations().size }

    BottomNav(
        modifier = Modifier.align(Alignment.BottomCenter),
        current = currentTab,
        unreadChatCount = unreadCount,
        onSelect = { tab ->
            val route = when (tab) {
                NavTab.HOME -> Route.HOME
                NavTab.ORDERS -> Route.ORDERS
                NavTab.MEASUREMENTS -> Route.MEASUREMENTS
                NavTab.CHAT -> Route.CHAT
                NavTab.PROFILE -> Route.PROFILE
            }
            nav.navigate(route) {
                popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        },
    )
}

@Composable
private fun BoxScope.UstaNavBar(nav: NavHostController) {
    val currentRoute = nav.currentBackStackEntryAsState().value?.destination?.route
    val showBottomNav = currentRoute != null &&
        currentRoute != Route.ORDER_NEW &&
        currentRoute != Route.CHAT_WITH
    if (!showBottomNav) return

    val currentTab = when (currentRoute) {
        Route.DASHBOARD -> UstaNavTab.DASHBOARD
        Route.ORDERS, Route.ORDER_DETAIL -> UstaNavTab.ORDERS
        Route.CHAT -> UstaNavTab.CHAT
        Route.SHOP -> UstaNavTab.SHOP
        Route.PROFILE -> UstaNavTab.PROFILE
        else -> null
    }

    var unreadCount by remember { mutableIntStateOf(0) }
    LaunchedEffect(currentRoute) { unreadCount = fetchUnreadConversations().size }

    UstaBottomNav(
        modifier = Modifier.align(Alignment.BottomCenter),
        current = currentTab,
        unreadChatCount = unreadCount,
        onSelect = { tab ->
            val route = when (tab) {
                UstaNavTab.DASHBOARD -> Route.DASHBOARD
                UstaNavTab.ORDERS -> Route.ORDERS
                UstaNavTab.CHAT -> Route.CHAT
                UstaNavTab.SHOP -> Route.SHOP
                UstaNavTab.PROFILE -> Route.PROFILE
            }
            nav.navigate(route) {
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
