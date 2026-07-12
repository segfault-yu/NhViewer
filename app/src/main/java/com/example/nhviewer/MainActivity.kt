package com.example.nhviewer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
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
                                painter = painterResource(dest.icon),
                                contentDescription = dest.label
                            )
                        },
                        label = { Text(dest.label) },
                        selected = currentDestination.hasRoute(dest.route::class),
                        onClick = {
                            navController.navigate(dest.route) {
                                popUpTo(Route.Home) {
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
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            NhViewerNavGraph(
                navController = navController,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

enum class AppDestinations(
    val label: String,
    val icon: Int,
    val route: Route
) {
    HOME("Home", R.drawable.ic_home, Route.Home),
    FAVORITES("Favorites", R.drawable.ic_favorite, Route.Favorites),
    PROFILE("Profile", R.drawable.ic_account_box, Route.Profile),
}