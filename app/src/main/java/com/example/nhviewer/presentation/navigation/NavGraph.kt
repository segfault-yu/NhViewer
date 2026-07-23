package com.example.nhviewer.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.example.nhviewer.presentation.feature.auth.AuthScreen
import com.example.nhviewer.presentation.feature.detail.DetailScreen
import com.example.nhviewer.presentation.feature.history.HistoryScreen
import com.example.nhviewer.presentation.feature.home.HomeScreen
import com.example.nhviewer.presentation.feature.profile.ProfileScreen
import com.example.nhviewer.presentation.feature.reader.ReaderScreen
import com.example.nhviewer.presentation.feature.search.SearchScreen
import com.example.nhviewer.presentation.feature.tagged.TaggedGalleriesScreen
import com.example.nhviewer.presentation.feature.tags.TagsScreen
import com.example.nhviewer.presentation.feature.settings.SettingsScreen
import com.example.nhviewer.presentation.feature.settings.sessions.SessionScreen
import com.example.nhviewer.presentation.feature.settings.apikeys.ApiKeyScreen

@Composable
fun NhViewerNavGraph(
    navController: NavHostController,
    onOpenDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Route.Home,
        modifier = modifier
    ) {
        composable<Route.Home> {
            HomeScreen(
                onNavigateToDetail = { galleryId ->
                    navController.navigate(Route.GalleryDetail(galleryId))
                },
                onNavigateToReader = { galleryId, page ->
                    navController.navigate(Route.Reader(galleryId, page))
                },
                onOpenDrawer = onOpenDrawer
            )
        }

        composable<Route.Tags> {
            TagsScreen(
                onBackClick = { navController.popBackStack() },
                onNavigateToTaggedGalleries = { tagId, tagName ->
                    navController.navigate(Route.TaggedGalleries(tagId, tagName))
                }
            )
        }

        composable<Route.TaggedGalleries> { backStackEntry ->
            val route: Route.TaggedGalleries = backStackEntry.toRoute()
            TaggedGalleriesScreen(
                tagId = route.tagId,
                tagName = route.tagName,
                onBackClick = { navController.popBackStack() },
                onNavigateToDetail = { galleryId ->
                    navController.navigate(Route.GalleryDetail(galleryId))
                }
            )
        }

        composable<Route.Favorites> {
            com.example.nhviewer.presentation.feature.favorites.FavoritesScreen(
                onBackClick = { navController.popBackStack() },
                onNavigateToDetail = { galleryId ->
                    navController.navigate(Route.GalleryDetail(galleryId))
                },
                onNavigateToAuth = {
                    navController.navigate(Route.Auth)
                }
            )
        }

        composable<Route.Auth> {
            AuthScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable<Route.Blacklist> {
            com.example.nhviewer.presentation.feature.blacklist.BlacklistScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable<Route.History> {
            HistoryScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onNavigateToReader = { galleryId, page ->
                    navController.navigate(Route.Reader(galleryId, page))
                }
            )
        }

        composable<Route.GalleryDetail> { backStackEntry ->
            val route: Route.GalleryDetail = backStackEntry.toRoute()
            DetailScreen(
                galleryId = route.galleryId,
                onBackClick = { navController.popBackStack() },
                onStartReading = { galleryId, startPage ->
                    navController.navigate(Route.Reader(galleryId, startPage))
                },
                onTagClick = { tagId, tagName ->
                    navController.navigate(Route.TaggedGalleries(tagId, tagName))
                },
                onNavigateToDetail = { galleryId ->
                    navController.navigate(Route.GalleryDetail(galleryId))
                }
            )
        }

        composable<Route.Reader> { backStackEntry ->
            val route: Route.Reader = backStackEntry.toRoute()
            ReaderScreen(
                galleryId = route.galleryId,
                startPage = route.startPage,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable<Route.Settings> {
            SettingsScreen(
                onBackClick = { navController.popBackStack() },
                onNavigateToSessions = { navController.navigate(Route.Sessions) },
                onNavigateToApiKeys = { navController.navigate(Route.ApiKeys) },
                onNavigateToBlacklist = { navController.navigate(Route.Blacklist) }
            )
        }

        composable<Route.Sessions> {
            SessionScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable<Route.ApiKeys> {
            ApiKeyScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
