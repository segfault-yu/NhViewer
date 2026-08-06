package com.example.nhviewer.presentation.navigation

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.example.nhviewer.domain.model.Tag
import com.example.nhviewer.presentation.feature.auth.AuthScreen
import com.example.nhviewer.presentation.feature.detail.DetailScreen
import com.example.nhviewer.presentation.feature.history.HistoryScreen
import com.example.nhviewer.presentation.feature.home.HomeScreen
import com.example.nhviewer.presentation.feature.profile.ProfileScreen
import com.example.nhviewer.presentation.feature.reader.ReaderScreen
import com.example.nhviewer.presentation.feature.search.SearchScreen
import com.example.nhviewer.presentation.feature.tags.TagsScreen
import com.example.nhviewer.presentation.feature.settings.SettingsScreen
import com.example.nhviewer.presentation.feature.settings.sessions.SessionScreen
import com.example.nhviewer.presentation.feature.settings.apikeys.ApiKeyScreen
import com.example.nhviewer.presentation.feature.settings.about.AboutScreen
import com.example.nhviewer.ui.theme.NhMotion
import com.example.nhviewer.util.i18n.LocalTagLanguage
import com.example.nhviewer.util.i18n.TagTranslationProvider

/** 等价于 [composable]，额外把当前目的地的 [AnimatedContentScope] 通过 CompositionLocal 下发，供共享元素转场使用。 */
inline fun <reified T : Any> NavGraphBuilder.animatedComposable(
    noinline content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit
) {
    composable<T> { backStackEntry ->
        CompositionLocalProvider(LocalNavAnimatedVisibilityScope provides this) {
            content(backStackEntry)
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun NhViewerNavGraph(
    navController: NavHostController,
    onOpenDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tagLanguage = LocalTagLanguage.current
    val onTagClick: (Tag) -> Unit = { tag ->
        navController.navigate(Route.Search(TagTranslationProvider.toSearchQueryToken(tag, tagLanguage)))
    }

    SharedTransitionLayout {
        CompositionLocalProvider(LocalSharedTransitionScope provides this) {
            NavHost(
                navController = navController,
                startDestination = Route.Home,
                enterTransition = { with(NhMotion) { enterSlide() } },
                exitTransition = { with(NhMotion) { exitSlide() } },
                popEnterTransition = { with(NhMotion) { popEnter() } },
                popExitTransition = { with(NhMotion) { popExit() } },
                modifier = modifier
            ) {
        animatedComposable<Route.Home> {
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

        animatedComposable<Route.Tags> {
            TagsScreen(
                onBackClick = { navController.popBackStack() },
                onNavigateToSearch = onTagClick
            )
        }

        animatedComposable<Route.Search> { backStackEntry ->
            val route: Route.Search = backStackEntry.toRoute()
            SearchScreen(
                initialQuery = route.initialQuery,
                onBackClick = { navController.popBackStack() },
                onNavigateToDetail = { galleryId ->
                    navController.navigate(Route.GalleryDetail(galleryId))
                }
            )
        }

        animatedComposable<Route.Favorites> {
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

        animatedComposable<Route.Auth> {
            AuthScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        animatedComposable<Route.Blacklist> {
            com.example.nhviewer.presentation.feature.blacklist.BlacklistScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        animatedComposable<Route.History> {
            HistoryScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onNavigateToReader = { galleryId, page ->
                    navController.navigate(Route.Reader(galleryId, page))
                }
            )
        }

        animatedComposable<Route.GalleryDetail> { backStackEntry ->
            val route: Route.GalleryDetail = backStackEntry.toRoute()
            DetailScreen(
                galleryId = route.galleryId,
                onBackClick = { navController.popBackStack() },
                onStartReading = { galleryId, startPage ->
                    navController.navigate(Route.Reader(galleryId, startPage))
                },
                onTagClick = onTagClick,
                onNavigateToDetail = { galleryId ->
                    navController.navigate(Route.GalleryDetail(galleryId))
                }
            )
        }

        animatedComposable<Route.Reader> { backStackEntry ->
            val route: Route.Reader = backStackEntry.toRoute()
            ReaderScreen(
                galleryId = route.galleryId,
                startPage = route.startPage,
                onBackClick = { navController.popBackStack() }
            )
        }

        animatedComposable<Route.Settings> {
            SettingsScreen(
                onBackClick = { navController.popBackStack() },
                onNavigateToSessions = { navController.navigate(Route.Sessions) },
                onNavigateToApiKeys = { navController.navigate(Route.ApiKeys) },
                onNavigateToBlacklist = { navController.navigate(Route.Blacklist) },
                onNavigateToCache = { navController.navigate(Route.CacheManagement) },
                onNavigateToAbout = { navController.navigate(Route.About) }
            )
        }

        animatedComposable<Route.CacheManagement> {
            com.example.nhviewer.presentation.feature.settings.cache.CacheScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        animatedComposable<Route.Sessions> {
            SessionScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        animatedComposable<Route.ApiKeys> {
            ApiKeyScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        animatedComposable<Route.About> {
            AboutScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
            }
        }
    }
}
