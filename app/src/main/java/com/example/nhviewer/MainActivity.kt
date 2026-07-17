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
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
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

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NhViewerTheme {
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

    // Show navigation bar only if the current route is one of the top level destinations
    val showBottomBar = currentDestination != null && (
        currentDestination.hasRoute<Route.Home>() ||
        currentDestination.hasRoute<Route.Search>() ||
        currentDestination.hasRoute<Route.Tags>() ||
        currentDestination.hasRoute<Route.Favorites>() ||
        currentDestination.hasRoute<Route.Profile>()
    )

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            if (showBottomBar) {
                AppDestinations.entries.forEach { dest ->
                    item(
                        icon = {
                            Icon(
                                imageVector = dest.icon,
                                contentDescription = dest.label
                            )
                        },
                        label = { Text(dest.label) },
                        selected = currentDestination.hasRoute(dest.route::class),
                        onClick = {
                            navController.navigate(dest.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) {
        NhViewerNavGraph(
            navController = navController,
            modifier = Modifier.fillMaxSize()
        )
    }
}

enum class AppDestinations(
    val label: String,
    val icon: ImageVector,
    val route: Route
) {
    HOME("首页", Icons.Default.Home, Route.Home),
    SEARCH("搜索", Icons.Default.Search, Route.Search),
    TAGS("标签", Icons.Default.LocalOffer, Route.Tags),
    FAVORITES("收藏", Icons.Default.Favorite, Route.Favorites),
    PROFILE("我的", Icons.Default.AccountCircle, Route.Profile),
}