package com.example.nhviewer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.nhviewer.domain.model.ReadingHistory
import com.example.nhviewer.presentation.feature.home.HomeViewModel
import com.example.nhviewer.presentation.navigation.AppDrawerContent
import kotlinx.coroutines.launch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.nhviewer.presentation.navigation.NhViewerNavGraph
import com.example.nhviewer.presentation.navigation.Route
import com.example.nhviewer.ui.theme.NhViewerTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.example.nhviewer.data.local.SettingsManager
import androidx.compose.runtime.collectAsState
import androidx.compose.foundation.isSystemInDarkTheme
import com.example.nhviewer.domain.model.AuthEvent
import com.example.nhviewer.domain.repository.UserRepository

import com.example.nhviewer.domain.repository.BlacklistRepository
import com.example.nhviewer.domain.model.Tag
import com.example.nhviewer.util.i18n.LanguageManager
import com.example.nhviewer.util.i18n.LocalAddToBlacklist
import com.example.nhviewer.util.i18n.LocalBlacklistedTagIds
import com.example.nhviewer.util.i18n.LocalTagLanguage
import androidx.compose.runtime.CompositionLocalProvider
import com.example.nhviewer.util.i18n.LocalTagDisplayMode
import com.example.nhviewer.util.i18n.TagTranslationProvider
import androidx.compose.ui.platform.LocalContext

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var settingsManager: SettingsManager

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var blacklistRepository: BlacklistRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        TagTranslationProvider.init(this)
        enableEdgeToEdge()
        setContent {
            val themeMode by settingsManager.themeMode.collectAsState(initial = "system")
            val dynamicColor by settingsManager.dynamicColor.collectAsState(initial = true)
            val appLanguage by settingsManager.appLanguage.collectAsState(initial = "system")
            val tagLanguage by settingsManager.tagLanguage.collectAsState(initial = "zh")
            val tagDisplayMode by settingsManager.tagDisplayMode.collectAsState(initial = "only_translation")
            val blacklistedTagIds by blacklistRepository.blacklistedTagIds.collectAsState()

            val scope = rememberCoroutineScope()
            val context = LocalContext.current
            val localeContext = remember(appLanguage, context) {
                LanguageManager.createLocaleContext(context, appLanguage)
            }

            val onAddToBlacklist: (Tag) -> Unit = remember {
                { tag ->
                    scope.launch {
                        blacklistRepository.addToBlacklist(tag)
                    }
                }
            }

            val isDarkTheme = when (themeMode) {
                "light" -> false
                "dark" -> true
                else -> isSystemInDarkTheme()
            }

            CompositionLocalProvider(
                LocalContext provides localeContext,
                LocalTagLanguage provides tagLanguage,
                LocalTagDisplayMode provides tagDisplayMode,
                LocalBlacklistedTagIds provides blacklistedTagIds,
                LocalAddToBlacklist provides onAddToBlacklist
            ) {
                NhViewerTheme(
                    darkTheme = isDarkTheme,
                    dynamicColor = dynamicColor
                ) {
                    NhViewerApp(userRepository = userRepository)
                }
            }
        }
    }
}

@Composable
fun NhViewerApp(userRepository: UserRepository) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(key1 = true) {
        userRepository.authEvents.collect { event ->
            when (event) {
                is AuthEvent.SessionExpired -> {
                    val currentRoute = currentDestination?.route
                    val needsAuth = currentRoute != null && (
                        currentRoute.contains("Route.Favorites") ||
                        currentRoute.contains("Route.Sessions") ||
                        currentRoute.contains("Route.ApiKeys") ||
                        currentRoute.contains("Route.Profile")
                    )
                    if (needsAuth) {
                        android.widget.Toast.makeText(context, "您的登录信息已过期，请重新登录", android.widget.Toast.LENGTH_SHORT).show()
                        navController.navigate(Route.Auth) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        // 抽屉只在首页才允许左边缘滑动唤出；其余页面的左边缘滑动是"返回上一页"的预见式返回手势，
        // 二者共用同一块识别区域会互相打架，非首页必须关掉抽屉手势
        gesturesEnabled = currentDestination?.hasRoute<Route.Home>() == true,
        drawerContent = {
            AppDrawerContent(
                drawerState = drawerState,
                currentDestination = currentDestination,
                onNavigate = { route ->
                    scope.launch { drawerState.close() }
                    if (route == Route.Home) {
                        navController.popBackStack(Route.Home, inclusive = false)
                    } else {
                        navController.navigate(route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                onNavigateToAuth = {
                    scope.launch { drawerState.close() }
                    navController.navigate(Route.Auth)
                }
            )
        }
    ) {
        NhViewerNavGraph(
            navController = navController,
            onOpenDrawer = { scope.launch { drawerState.open() } },
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        )
    }
}