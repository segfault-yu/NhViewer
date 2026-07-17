package com.example.nhviewer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.nhviewer.domain.model.ReadingHistory
import com.example.nhviewer.presentation.feature.home.HomeViewModel
import com.example.nhviewer.presentation.navigation.AppDrawerContent
import kotlinx.coroutines.launch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var settingsManager: SettingsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode by settingsManager.themeMode.collectAsState(initial = "system")
            val dynamicColor by settingsManager.dynamicColor.collectAsState(initial = true)

            val isDarkTheme = when (themeMode) {
                "light" -> false
                "dark" -> true
                else -> isSystemInDarkTheme()
            }

            NhViewerTheme(
                darkTheme = isDarkTheme,
                dynamicColor = dynamicColor
            ) {
                NhViewerApp()
            }
        }
    }
}

@Composable
fun NhViewerApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawerContent(
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
            modifier = Modifier.fillMaxSize()
        )
    }
}