package com.peecock.ymusic.ui.screens

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.peecock.ymusic.Database
import com.peecock.ymusic.enums.BuiltInPlaylist
import com.peecock.ymusic.enums.DeviceLists
import com.peecock.ymusic.enums.NavRoutes
import com.peecock.ymusic.enums.StatisticsType
import com.peecock.ymusic.enums.TransitionEffect
import com.peecock.ymusic.extensions.games.pacman.Pacman
import com.peecock.ymusic.extensions.games.snake.SnakeGame
import com.peecock.ymusic.models.Mood
import com.peecock.ymusic.models.SearchQuery
import com.peecock.ymusic.ui.components.CustomModalBottomSheet
import com.peecock.ymusic.ui.screens.album.AlbumScreen
import com.peecock.ymusic.ui.screens.artist.ArtistScreen
import com.peecock.ymusic.ui.screens.builtinplaylist.BuiltInPlaylistScreen
import com.peecock.ymusic.ui.screens.history.HistoryScreen
import com.peecock.ymusic.ui.screens.home.HomeScreen
import com.peecock.ymusic.ui.screens.localplaylist.LocalPlaylistScreen
import com.peecock.ymusic.ui.screens.mood.MoodScreen
import com.peecock.ymusic.ui.screens.mood.MoodsPageScreen
import com.peecock.ymusic.ui.screens.newreleases.NewreleasesScreen
import com.peecock.ymusic.ui.screens.ondevice.DeviceListSongsScreen
import com.peecock.ymusic.ui.screens.player.PlayerModern
import com.peecock.ymusic.ui.screens.player.QueueModern
import com.peecock.ymusic.ui.screens.player.rememberPlayerSheetState
import com.peecock.ymusic.ui.screens.playlist.PlaylistScreen
import com.peecock.ymusic.ui.screens.podcast.PodcastScreen
import com.peecock.ymusic.ui.screens.search.SearchScreen
import com.peecock.ymusic.ui.screens.searchresult.SearchResultScreen
import com.peecock.ymusic.ui.screens.settings.SettingsScreen
import com.peecock.ymusic.ui.screens.statistics.StatisticsScreen
import com.peecock.ymusic.ui.styling.Dimensions
import com.peecock.ymusic.utils.pauseSearchHistoryKey
import com.peecock.ymusic.utils.preferences
import com.peecock.ymusic.utils.rememberPreference
import com.peecock.ymusic.utils.transitionEffectKey

@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalFoundationApi::class, ExperimentalAnimationApi::class,
    ExperimentalMaterialApi::class, ExperimentalTextApi::class, ExperimentalComposeUiApi::class,
    ExperimentalMaterial3Api::class
)
@Composable
fun AppNavigation(
    navController: NavHostController,
    playerEssential: @Composable () -> Unit = {},
    openTabFromShortcut: Int = 0
) {
    val transitionEffect by rememberPreference(transitionEffectKey, TransitionEffect.Scale)

    @Composable
    fun customScaffold(content: @Composable () -> Unit) {
        Scaffold(
            bottomBar = {  }
        ) { paddingValues ->
            Surface(
                modifier = Modifier.padding(paddingValues),
                content = content
            )
        }
    }

    @Composable
    fun modalBottomSheedPage(content: @Composable () -> Unit) {
        var showSheet by rememberSaveable { mutableStateOf(true) }
        CustomModalBottomSheet(
            showSheet = showSheet,
            onDismissRequest = {
                if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED)
                    navController.popBackStack()
            },
            containerColor = Color.Transparent,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            dragHandle = {
                Surface(
                    modifier = Modifier.padding(vertical = 0.dp),
                    color = Color.Transparent,
                    //shape = thumbnailShape
                ) {}
            }
        ) {
            content()
        }
    }

    NavHost(
        navController = navController,
        startDestination = NavRoutes.home.name,
        enterTransition = {
            when (transitionEffect) {
                TransitionEffect.None -> EnterTransition.None
                TransitionEffect.Expand -> expandIn(animationSpec = tween(350, easing = LinearOutSlowInEasing), expandFrom = Alignment.TopStart)
                TransitionEffect.Fade -> fadeIn(animationSpec = tween(350))
                TransitionEffect.Scale -> scaleIn(animationSpec = tween(350))
                TransitionEffect.SlideVertical -> slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Up)
                TransitionEffect.SlideHorizontal -> slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left)
            }
        },
        exitTransition = {
            when (transitionEffect) {
                TransitionEffect.None -> ExitTransition.None
                TransitionEffect.Expand -> shrinkOut(animationSpec = tween(350, easing = FastOutSlowInEasing),shrinkTowards = Alignment.TopStart)
                TransitionEffect.Fade -> fadeOut(animationSpec = tween(350))
                TransitionEffect.Scale -> scaleOut(animationSpec = tween(350))
                TransitionEffect.SlideVertical -> slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Down)
                TransitionEffect.SlideHorizontal -> slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right)
            }
        },
        popEnterTransition = {
            when (transitionEffect) {
                TransitionEffect.None -> EnterTransition.None
                TransitionEffect.Expand -> expandIn(animationSpec = tween(350, easing = LinearOutSlowInEasing), expandFrom = Alignment.TopStart)
                TransitionEffect.Fade -> fadeIn(animationSpec = tween(350))
                TransitionEffect.Scale -> scaleIn(animationSpec = tween(350))
                TransitionEffect.SlideVertical -> slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Up)
                TransitionEffect.SlideHorizontal -> slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left)
            }
        },
        popExitTransition = {
            when (transitionEffect) {
                TransitionEffect.None -> ExitTransition.None
                TransitionEffect.Expand -> shrinkOut(animationSpec = tween(350, easing = FastOutSlowInEasing),shrinkTowards = Alignment.TopStart)
                TransitionEffect.Fade -> fadeOut(animationSpec = tween(350))
                TransitionEffect.Scale -> scaleOut(animationSpec = tween(350))
                TransitionEffect.SlideVertical -> slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Down)
                TransitionEffect.SlideHorizontal -> slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right)
            }
        }
    ) {
        val navigateToAlbum =
            { browseId: String -> navController.navigate(route = "${NavRoutes.album.name}/$browseId") }
        val navigateToArtist = { browseId: String -> navController.navigate("${NavRoutes.artist.name}/$browseId") }
        val navigateToPlaylist = { browseId: String -> navController.navigate("${NavRoutes.playlist.name}/$browseId") }
        val pop = {
            if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) navController.popBackStack()
        }



        composable(route = NavRoutes.home.name) {
            HomeScreen(
                navController = navController,
                onPlaylistUrl = navigateToPlaylist,
                playerEssential = playerEssential,
                openTabFromShortcut = openTabFromShortcut
            )
        }

        composable(route = NavRoutes.gamePacman.name) {
            modalBottomSheedPage {
                Pacman()
            }

        }

        composable(route = NavRoutes.gameSnake.name) {
            modalBottomSheedPage {
                SnakeGame()
            }

        }

        composable(route = NavRoutes.queue.name) {
            modalBottomSheedPage {
                QueueModern(
                    navController = navController,
                    onDismiss = {},
                )
            }
        }

        composable(route = NavRoutes.player.name) {
            val density = LocalDensity.current
            val windowsInsets = WindowInsets.systemBars
            val bottomDp = with(density) { windowsInsets.getBottom(density).toDp() }
            val playerSheetState = rememberPlayerSheetState(
                dismissedBound = 0.dp,
                collapsedBound = Dimensions.collapsedPlayer + bottomDp,
                //collapsedBound = Dimensions.collapsedPlayer, // bottom navigation
                expandedBound = 1500.dp,
            )
            val playerState =
                rememberModalBottomSheetState(skipPartiallyExpanded = true)
            modalBottomSheedPage {
                PlayerModern(
                    navController = navController,
                    layoutState = playerSheetState,
                    playerState = playerState,
                    onDismiss = {}
                )
            }
        }

        composable(
            route = "${NavRoutes.artist.name}/{id}",
            arguments = listOf(
                navArgument(
                    name = "id",
                    builder = { type = NavType.StringType }
                )
            )
        ) { navBackStackEntry ->
            val id = navBackStackEntry.arguments?.getString("id") ?: ""
                ArtistScreen(
                    navController = navController,
                    browseId = id,
                    playerEssential = playerEssential,
                )
            }

        composable(
            route = "${NavRoutes.album.name}/{id}",
            arguments = listOf(
                navArgument(
                    name = "id",
                    builder = { type = NavType.StringType }
                )
            )
        ) { navBackStackEntry ->
            val id = navBackStackEntry.arguments?.getString("id") ?: ""
            AlbumScreen(
                navController = navController,
                browseId = id,
                playerEssential = playerEssential,
            )
        }

        composable(
            route = "${NavRoutes.playlist.name}/{id}",
            arguments = listOf(
                navArgument(
                    name = "id",
                    builder = { type = NavType.StringType }
                )
            )
        ) { navBackStackEntry ->
            val id = navBackStackEntry.arguments?.getString("id") ?: ""
            PlaylistScreen(
                navController = navController,
                browseId = id,
                params = null,
                playerEssential = playerEssential,
            )
        }

        composable(
            route = "${NavRoutes.podcast.name}/{id}",
            arguments = listOf(
                navArgument(
                    name = "id",
                    builder = { type = NavType.StringType }
                )
            )
        ) { navBackStackEntry ->
            val id = navBackStackEntry.arguments?.getString("id") ?: ""
            PodcastScreen(
                navController = navController,
                browseId = id,
                params = null,
                playerEssential = playerEssential,
            )
        }

        composable(route = NavRoutes.settings.name) {
                SettingsScreen(
                    navController = navController,
                    playerEssential = playerEssential,
                    //pop = popDestination,
                    //onGoToSettingsPage = { index -> navController.navigate("settingsPage/$index") }
                )
        }

        composable(route = NavRoutes.statistics.name) {
            StatisticsScreen(
                navController = navController,
                statisticsType = StatisticsType.Today,
                playerEssential = playerEssential,
            )
        }

        composable(route = NavRoutes.history.name) {
            HistoryScreen(
                navController = navController,
                playerEssential = playerEssential,

            )
        }

        /*
        composable(
            route = "settingsPage/{index}",
            arguments = listOf(
                navArgument(
                    name = "index",
                    builder = { type = NavType.IntType }
                )
            )
        ) { navBackStackEntry ->
            val index = navBackStackEntry.arguments?.getInt("index") ?: 0

            PlayerScaffold {
                SettingsPage(
                    section = SettingsSection.entries[index],
                    pop = popDestination
                )
            }
        }
         */

        composable(
            route = "${NavRoutes.search.name}?text={text}",
            arguments = listOf(
                navArgument(
                    name = "text",
                    builder = {
                        type = NavType.StringType
                        defaultValue = ""
                    }
                )
            )
        ) { navBackStackEntry ->
            val context = LocalContext.current
            val text = navBackStackEntry.arguments?.getString("text") ?: ""

            SearchScreen(
                navController = navController,
                playerEssential = playerEssential,
                initialTextInput = text,
                onViewPlaylist = {},
                //pop = popDestination,
                onSearch = { query ->
                    navController.navigate(route = "${NavRoutes.searchResults.name}/$query")

                    if (!context.preferences.getBoolean(pauseSearchHistoryKey, false)) {
                        com.peecock.ymusic.query {
                            Database.insert(SearchQuery(query = query))
                        }
                    }
                },

            )
        }

        composable(
            route = "${NavRoutes.searchResults.name}/{query}",
            arguments = listOf(
                navArgument(
                    name = "query",
                    builder = { type = NavType.StringType }
                )
            )
        ) { navBackStackEntry ->
            val query = navBackStackEntry.arguments?.getString("query") ?: ""

            SearchResultScreen(
                navController = navController,
                playerEssential = playerEssential,
                query = query,
                onSearchAgain = {}
            )
        }

        composable(
            route = "${NavRoutes.builtInPlaylist.name}/{index}",
            arguments = listOf(
                navArgument(
                    name = "index",
                    builder = { type = NavType.IntType }
                )
            )
        ) { navBackStackEntry ->
            val index = navBackStackEntry.arguments?.getInt("index") ?: 0

            BuiltInPlaylistScreen(
                navController = navController,
                builtInPlaylist = BuiltInPlaylist.entries[index],
                playerEssential = playerEssential,
            )
        }

        composable(
            route = "${NavRoutes.localPlaylist.name}/{id}",
            arguments = listOf(
                navArgument(
                    name = "id",
                    builder = { type = NavType.LongType }
                )
            )
        ) { navBackStackEntry ->
            val id = navBackStackEntry.arguments?.getLong("id") ?: 0L

            LocalPlaylistScreen(
                navController = navController,
                playlistId = id,
                playerEssential = playerEssential,
                //onDelete = popDestination
            )
        }

        composable(
            route = NavRoutes.mood.name,
        ) { navBackStackEntry ->
            val mood: Mood? = navController.previousBackStackEntry?.savedStateHandle?.get("mood")
            if (mood != null) {
                MoodScreen(
                    navController = navController,
                    mood = mood,
                    playerEssential = playerEssential,
                )
            }
        }

        composable(
            route = NavRoutes.moodsPage.name
        ) { navBackStackEntry ->
            /*
            SimpleScaffold(navController = navController) {
                MoodsPage(
                    navController = navController
                )
            }
             */
            MoodsPageScreen(
                navController = navController
            )

        }

        composable(
            route = NavRoutes.onDevice.name
        ) { navBackStackEntry ->
              DeviceListSongsScreen(
                navController = navController,
                deviceLists = DeviceLists.LocalSongs,
                playerEssential = playerEssential,
            )
        }

        composable(
            route = NavRoutes.newAlbums.name
        ) { navBackStackEntry ->
            NewreleasesScreen(
                navController = navController,
                playerEssential = playerEssential,
            )
        }

    }
}