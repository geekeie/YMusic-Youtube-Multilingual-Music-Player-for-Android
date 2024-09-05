package com.peecock.ymusic.ui.screens.home

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import com.peecock.compose.persist.PersistMapCleanup
import com.peecock.compose.routing.RouteHandler
import com.peecock.compose.routing.defaultStacking
import com.peecock.compose.routing.defaultStill
import com.peecock.compose.routing.defaultUnstacking
import com.peecock.compose.routing.isStacking
import com.peecock.compose.routing.isUnknown
import com.peecock.compose.routing.isUnstacking
import com.peecock.ymusic.R
import com.peecock.ymusic.enums.CheckUpdateState
import com.peecock.ymusic.enums.HomeScreenTabs
import com.peecock.ymusic.enums.NavRoutes
import com.peecock.ymusic.enums.UiType
import com.peecock.ymusic.models.toUiMood
import com.peecock.ymusic.ui.components.themed.ConfirmationDialog
import com.peecock.ymusic.ui.components.Scaffold
import com.peecock.ymusic.ui.screens.globalRoutes
import com.peecock.ymusic.ui.screens.searchResultRoute
import com.peecock.ymusic.ui.screens.searchRoute
import com.peecock.ymusic.utils.CheckAvailableNewVersion
import com.peecock.ymusic.utils.UiTypeKey
import com.peecock.ymusic.utils.checkUpdateStateKey
import com.peecock.ymusic.utils.enableQuickPicksPageKey
import com.peecock.ymusic.utils.getEnum
import com.peecock.ymusic.utils.homeScreenTabIndexKey
import com.peecock.ymusic.utils.indexNavigationTabKey
import com.peecock.ymusic.utils.preferences
import com.peecock.ymusic.utils.rememberPreference
import com.peecock.ymusic.utils.showSearchTabKey
import com.peecock.ymusic.utils.showStatsInNavbarKey

const val PINNED_PREFIX = "pinned:"
const val MODIFIED_PREFIX = "modified:"

@ExperimentalMaterialApi
@ExperimentalTextApi
@ExperimentalFoundationApi
@ExperimentalAnimationApi
@ExperimentalComposeUiApi
@UnstableApi
@Composable
fun HomeScreen(
    navController: NavController,
    onPlaylistUrl: (String) -> Unit,
    playerEssential: @Composable () -> Unit = {},
    openTabFromShortcut: Int
) {
    var showNewversionDialog by remember {
        mutableStateOf(true)
    }

    var checkUpdateState by rememberPreference(checkUpdateStateKey, CheckUpdateState.Disabled)

    val saveableStateHolder = rememberSaveableStateHolder()

    val preferences = LocalContext.current.preferences
    val showSearchTab by rememberPreference(showSearchTabKey, false)
    val showStatsInNavbar by rememberPreference(showStatsInNavbarKey, false)
    val uiType  by rememberPreference(UiTypeKey, UiType.RiMusic)
    val enableQuickPicksPage by rememberPreference(enableQuickPicksPageKey, true)

    PersistMapCleanup("home/")

    RouteHandler(
        listenToGlobalEmitter = true,
        transitionSpec = {
            when {
                isStacking -> defaultStacking
                isUnstacking -> defaultUnstacking
                isUnknown -> when {
                    initialState.route == searchRoute && targetState.route == searchResultRoute -> defaultStacking
                    initialState.route == searchResultRoute && targetState.route == searchRoute -> defaultUnstacking
                    else -> defaultStill
                }

                else -> defaultStill
            }
        }
    ) {
        globalRoutes()
/*
        settingsRoute {
            SettingsScreen(
                navController = navController,
            )
        }

        historyRoute {
            HistoryScreen(
                navController = navController,
            )
        }

        localPlaylistRoute { playlistId ->
            LocalPlaylistScreen(
                navController = navController,
                playlistId = playlistId ?: error("playlistId cannot be null")
            )
        }
*/
        /*
        builtInPlaylistRoute { builtInPlaylist ->
            BuiltInPlaylistScreen(
                navController = navController,
                builtInPlaylist = builtInPlaylist
            )
        }

        playlistRoute { browseId, params, maxDepth ->
            PlaylistScreen(
                navController = navController,
                browseId = browseId ?: error("browseId cannot be null"),
                params = params,
                maxDepth = maxDepth
            )
        }
        */
        /*
        playlistRoute { browseId, params ->
            PlaylistScreen(
                browseId = browseId ?: "",
                params = params
            )
        }


        searchResultRoute { query ->
            SearchResultScreen(
                navController = navController,
                query = query,
                onSearchAgain = {
                    searchRoute(query)
                }
            )
        }

        searchRoute { initialTextInput ->
            val context = LocalContext.current

            SearchScreen(
                navController = navController,
                initialTextInput = initialTextInput,
                onSearch = { query ->
                    pop()
                    searchResultRoute(query)

                    if (!context.preferences.getBoolean(pauseSearchHistoryKey, false)) {
                        query {
                            Database.insert(SearchQuery(query = query))
                        }
                    }
                },
                onViewPlaylist = onPlaylistUrl,
                onDismiss = { homeRoute::global }
            )
        }
*/
        host {


            var (tabIndex, onTabChanged) =
                when (openTabFromShortcut) {
                    -1 -> when (preferences.getEnum(indexNavigationTabKey, HomeScreenTabs.Default)) {
                            HomeScreenTabs.Default -> rememberPreference(homeScreenTabIndexKey,
                            HomeScreenTabs.QuickPics.index)
                          else -> remember {
                                mutableIntStateOf(preferences.getEnum(indexNavigationTabKey, HomeScreenTabs.QuickPics).index)
                          }
                        }
                    else -> remember { mutableIntStateOf(openTabFromShortcut) }
                }


            /*
            var (tabIndex, onTabChanged) =
                if (preferences.getEnum(indexNavigationTabKey, HomeScreenTabs.Default) == HomeScreenTabs.Default)
                    rememberPreference(
                        homeScreenTabIndexKey,
                        HomeScreenTabs.QuickPics.index
                    ) else
                    remember {
                        mutableStateOf(preferences.getEnum(indexNavigationTabKey, HomeScreenTabs.QuickPics).index)
                    }
            */

            if (tabIndex == -2) navController.navigate(NavRoutes.search.name)


            if (!enableQuickPicksPage && tabIndex==0) tabIndex = 1

            Scaffold(
                navController = navController,
                playerEssential = playerEssential,
                topIconButtonId = R.drawable.settings,
                onTopIconButtonClick = {
                    //settingsRoute()
                    navController.navigate(NavRoutes.settings.name)
                },
                showButton1 = if(uiType == UiType.RiMusic) false else true,
                topIconButton2Id = R.drawable.stats_chart,
                onTopIconButton2Click = {
                    //statisticsTypeRoute(StatisticsType.Today)
                    navController.navigate(NavRoutes.statistics.name)
                },
                showButton2 = if(uiType == UiType.RiMusic) false else showStatsInNavbar,
                showBottomButton = if(uiType == UiType.RiMusic) false else showSearchTab,
                onBottomIconButtonClick = {
                    //searchRoute("")
                    navController.navigate(NavRoutes.search.name)
                },
                tabIndex = tabIndex,
                onTabChanged = onTabChanged,
                showTopActions = true,
                onHomeClick = {},
                onSettingsClick = {
                    //settingsRoute()
                    navController.navigate(NavRoutes.settings.name)
                },
                onStatisticsClick = {
                    //statisticsTypeRoute(StatisticsType.Today)
                    navController.navigate(NavRoutes.statistics.name)
                },
                onHistoryClick = {
                    //historyRoute()
                    navController.navigate(NavRoutes.history.name)
                },
                onSearchClick = {
                    //searchRoute("")
                    navController.navigate(NavRoutes.search.name)
                },
                tabColumnContent = { Item ->
                    if (enableQuickPicksPage)
                        Item(0, stringResource(R.string.quick_picks), R.drawable.sparkles)
                    Item(1, stringResource(R.string.songs), R.drawable.musical_notes)
                    Item(2, stringResource(R.string.artists), R.drawable.artists)
                    Item(3, stringResource(R.string.albums), R.drawable.album)
                    Item(4, stringResource(R.string.playlists), R.drawable.library)
                    //Item(5, stringResource(R.string.discovery), R.drawable.megaphone)
                    //if (showSearchTab)
                    //Item(6, stringResource(R.string.search), R.drawable.search)
                    //Item(6, "Equalizer", R.drawable.musical_notes)
                    //Item(6, "Settings", R.drawable.equalizer)
                }
            ) { currentTabIndex ->
                saveableStateHolder.SaveableStateProvider(key = currentTabIndex) {
                    when (currentTabIndex) {
                        0 -> QuickPicksModern(
                            onAlbumClick = {
                                //albumRoute(it)
                                navController.navigate(route = "${NavRoutes.album.name}/$it")
                            },
                            onArtistClick = {
                                //artistRoute(it)
                                navController.navigate(route = "${NavRoutes.artist.name}/$it")
                            },
                            onPlaylistClick = {
                                //playlistRoute(it)
                                navController.navigate(route = "${NavRoutes.playlist.name}/$it")
                            },
                            onSearchClick = {
                                //searchRoute("")
                                navController.navigate(NavRoutes.search.name)
                            },
                            onMoodClick = { mood ->
                                //moodRoute(mood.toUiMood())
                                navController.currentBackStackEntry?.savedStateHandle?.set("mood", mood.toUiMood())
                                navController.navigate(NavRoutes.mood.name)
                            },
                            onSettingsClick = {
                                //settingsRoute()
                                navController.navigate(NavRoutes.settings.name)
                            },
                            navController = navController

                        )

                        1 -> HomeSongsModern(
                            navController = navController,
                            onSearchClick = {
                                //searchRoute("")
                                navController.navigate(NavRoutes.search.name)
                            },
                            onSettingsClick = {
                                //settingsRoute()
                                navController.navigate(NavRoutes.settings.name)
                            }
                        )

                        2 -> HomeArtistsModern(
                            onArtistClick = {
                                //artistRoute(it.id)
                                navController.navigate(route = "${NavRoutes.artist.name}/${it.id}")
                            },
                            onSearchClick = {
                                //searchRoute("")
                                navController.navigate(NavRoutes.search.name)
                            },
                            onSettingsClick = {
                                //settingsRoute()
                                navController.navigate(NavRoutes.settings.name)
                            }
                        )

                        3 -> HomeAlbumsModern(
                            onAlbumClick = {
                                //albumRoute(it.id)
                                navController.navigate(route = "${NavRoutes.album.name}/${it.id}")
                            },
                            onSearchClick = {
                                //searchRoute("")
                                navController.navigate(NavRoutes.search.name)
                            },
                            onSettingsClick = {
                                //settingsRoute()
                                navController.navigate(NavRoutes.settings.name)
                            }
                        )

                        4 -> HomeLibraryModern(
                            onBuiltInPlaylist = {
                                //builtInPlaylistRoute(it)
                                navController.navigate(route = "${NavRoutes.builtInPlaylist.name}/${it.ordinal}")
                            },
                            onPlaylistClick = {
                                //localPlaylistRoute(it.id)
                                navController.navigate(route = "${NavRoutes.localPlaylist.name}/${it.id}")
                            },
                            onSearchClick = {
                                //searchRoute("")
                                navController.navigate(NavRoutes.search.name)
                            },
                            onDeviceListSongsClick = {
                                //deviceListSongRoute("")
                                navController.navigate(NavRoutes.onDevice.name)
                            },
                            onStatisticsClick = {
                                //statisticsTypeRoute(StatisticsType.Today)
                                navController.navigate(NavRoutes.statistics.name)
                            },
                            onSettingsClick = {
                                //settingsRoute()
                                navController.navigate(NavRoutes.settings.name)
                            }

                        )
                        /*
                        5 -> HomeDiscovery(
                            onMoodClick = { mood -> moodRoute(mood.toUiMood()) },
                            onNewReleaseAlbumClick = { albumRoute(it) },
                            onSearchClick = { searchRoute("") }
                        )
                         */

                        //6 -> HomeEqualizer( )
                        /*
                        5 -> HomeStatistics(
                            onStatisticsType = { statisticsTypeRoute(it)},
                            onBuiltInPlaylist = { builtInPlaylistRoute(it) },
                            onPlaylistClick = { localPlaylistRoute(it.id) },
                            onSearchClick = { searchRoute("") }
                        )
                         */

                        /*
                        6 -> HomeSearch(
                            onSearchType = { searchTypeRoute(it) }
                        )
                         */
                    }
                }
            }
        }
    }

    if (showNewversionDialog && checkUpdateState == CheckUpdateState.Enabled)
        CheckAvailableNewVersion(
            onDismiss = { showNewversionDialog = false },
            updateAvailable = {}
        )

    if (checkUpdateState == CheckUpdateState.Ask)
        ConfirmationDialog(
            text = stringResource(R.string.check_at_github_for_updates) + "\n\n" +
                    stringResource(R.string.when_an_update_is_available_you_will_be_asked_if_you_want_to_install_info) + "\n\n" +
                    stringResource(R.string.but_these_updates_would_not_go_through) + "\n\n" +
                    stringResource(R.string.you_can_still_turn_it_on_or_off_from_the_settings),
            confirmText = stringResource(R.string.enable),
            cancelText = stringResource(R.string.don_t_enable),
            cancelBackgroundPrimary = true,
            onCancel = { checkUpdateState = CheckUpdateState.Disabled },
            onDismiss = { checkUpdateState = CheckUpdateState.Disabled },
            onConfirm = { checkUpdateState = CheckUpdateState.Enabled },
        )
    
}
