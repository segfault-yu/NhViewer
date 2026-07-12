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
import com.example.nhviewer.presentation.feature.detail.DetailScreen
import com.example.nhviewer.presentation.feature.home.HomeScreen
import com.example.nhviewer.presentation.feature.reader.ReaderScreen
import com.example.nhviewer.presentation.feature.search.SearchScreen
import com.example.nhviewer.presentation.feature.tagged.TaggedGalleriesScreen
import com.example.nhviewer.presentation.feature.tags.TagsScreen

@Composable
fun NhViewerNavGraph(
    navController: NavHostController,
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
                }
            )
        }

        composable<Route.Search> {
            SearchScreen(
                onNavigateToDetail = { galleryId ->
                    navController.navigate(Route.GalleryDetail(galleryId))
                }
            )
        }

        composable<Route.Tags> {
            TagsScreen(
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
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Favorites Screen (Phase 5)")
            }
        }

        composable<Route.Profile> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Profile Screen (Phase 4)")
            }
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
    }
}
