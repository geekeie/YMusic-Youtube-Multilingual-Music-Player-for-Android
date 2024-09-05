package com.peecock.ymusic.ui.screens.builtinplaylist

import android.annotation.SuppressLint
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import com.peecock.compose.persist.PersistMapCleanup
import com.peecock.compose.routing.RouteHandler
import com.peecock.ymusic.R
import com.peecock.ymusic.enums.BuiltInPlaylist
import com.peecock.ymusic.enums.DeviceLists
import com.peecock.ymusic.enums.ExoPlayerDiskCacheMaxSize
import com.peecock.ymusic.enums.ExoPlayerDiskDownloadCacheMaxSize
import com.peecock.ymusic.enums.MaxTopPlaylistItems
import com.peecock.ymusic.enums.NavRoutes
import com.peecock.ymusic.enums.UiType
import com.peecock.ymusic.ui.components.Scaffold
import com.peecock.ymusic.ui.screens.globalRoutes
import com.peecock.ymusic.ui.screens.ondevice.DeviceListSongs
import com.peecock.ymusic.utils.MaxTopPlaylistItemsKey
import com.peecock.ymusic.utils.UiTypeKey
import com.peecock.ymusic.utils.exoPlayerDiskCacheMaxSizeKey
import com.peecock.ymusic.utils.exoPlayerDiskDownloadCacheMaxSizeKey
import com.peecock.ymusic.utils.rememberPreference
import com.peecock.ymusic.utils.showCachedPlaylistKey
import com.peecock.ymusic.utils.showDownloadedPlaylistKey
import com.peecock.ymusic.utils.showFavoritesPlaylistKey
import com.peecock.ymusic.utils.showMyTopPlaylistKey
import com.peecock.ymusic.utils.showOnDevicePlaylistKey
import com.peecock.ymusic.utils.showSearchTabKey
import com.peecock.ymusic.utils.showStatsInNavbarKey

@ExperimentalMaterialApi
@ExperimentalTextApi
@SuppressLint("SuspiciousIndentation")
@ExperimentalFoundationApi
@ExperimentalAnimationApi
@ExperimentalComposeUiApi
@UnstableApi
@Composable
fun BuiltInPlaylistScreen(
    navController: NavController,
    builtInPlaylist: BuiltInPlaylist,
    playerEssential: @Composable () -> Unit = {},
) {
    val saveableStateHolder = rememberSaveableStateHolder()

    val (tabIndex, onTabIndexChanged) = rememberSaveable {
        mutableStateOf(when (builtInPlaylist) {
            BuiltInPlaylist.Favorites -> 0
            BuiltInPlaylist.Offline -> 1
            BuiltInPlaylist.Downloaded -> 2
            BuiltInPlaylist.Top -> 3
            BuiltInPlaylist.OnDevice -> 4
            else -> 5
        })
    }

    var exoPlayerDiskCacheMaxSize by rememberPreference(
        exoPlayerDiskCacheMaxSizeKey,
        ExoPlayerDiskCacheMaxSize.`32MB`
    )

    var exoPlayerDiskDownloadCacheMaxSize by rememberPreference(
        exoPlayerDiskDownloadCacheMaxSizeKey,
        ExoPlayerDiskDownloadCacheMaxSize.`2GB`
    )

    val maxTopPlaylistItems by rememberPreference(
        MaxTopPlaylistItemsKey,
        MaxTopPlaylistItems.`10`
    )
    val showFavoritesPlaylist by rememberPreference(showFavoritesPlaylistKey, true)
    val showCachedPlaylist by rememberPreference(showCachedPlaylistKey, true)
    val showMyTopPlaylist by rememberPreference(showMyTopPlaylistKey, true)
    val showDownloadedPlaylist by rememberPreference(showDownloadedPlaylistKey, true)
    val showOnDevicePlaylist by rememberPreference(showOnDevicePlaylistKey, true)
    val showSearchTab by rememberPreference(showSearchTabKey, false)
    val showStatsInNavbar by rememberPreference(showStatsInNavbarKey, false)

    PersistMapCleanup(tagPrefix = "${builtInPlaylist.name}/")

    RouteHandler(listenToGlobalEmitter = true) {
        globalRoutes()
        /*
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
                onViewPlaylist = {}
            )
        }
*/
        val uiType  by rememberPreference(UiTypeKey, UiType.RiMusic)

        host {
            Scaffold(
                navController = navController,
                playerEssential = playerEssential,
                topIconButtonId = R.drawable.chevron_back,
                onTopIconButtonClick = pop,
                showButton1 = if(uiType == UiType.RiMusic) false else true,
                topIconButton2Id = R.drawable.chevron_back,
                onTopIconButton2Click = pop,
                showButton2 = if(uiType == UiType.RiMusic) false else showStatsInNavbar,
                showBottomButton = if(uiType == UiType.RiMusic) false else showSearchTab,
                onBottomIconButtonClick = {
                    //searchRoute("")
                    navController.navigate(NavRoutes.search.name)
                },
                tabIndex = tabIndex,
                onTabChanged = onTabIndexChanged,
                onHomeClick = {
                    //homeRoute()
                    navController.navigate(NavRoutes.home.name)
                },
                tabColumnContent = { Item ->
                    if(showFavoritesPlaylist)
                        Item(0, stringResource(R.string.favorites), R.drawable.heart)
                    if(showCachedPlaylist)
                        Item(1, stringResource(R.string.cached), R.drawable.sync)
                    if(showDownloadedPlaylist)
                        Item(2, stringResource(R.string.downloaded), R.drawable.downloaded)
                    if(showMyTopPlaylist)
                        Item(3, stringResource(R.string.my_playlist_top).format(maxTopPlaylistItems.number) , R.drawable.trending)
                    if(showOnDevicePlaylist)
                        Item(4, stringResource(R.string.on_device), R.drawable.musical_notes)
                }
            ) { currentTabIndex ->
                saveableStateHolder.SaveableStateProvider(key = currentTabIndex) {
                    when (currentTabIndex) {
                        0 -> BuiltInPlaylistSongs(
                            navController = navController,
                            builtInPlaylist = BuiltInPlaylist.Favorites,
                            onSearchClick = {
                                //searchRoute("")
                                navController.navigate(NavRoutes.search.name)
                            }
                        )
                        1 -> BuiltInPlaylistSongs(
                            navController = navController,
                            builtInPlaylist = BuiltInPlaylist.Offline,
                            onSearchClick = {
                                //searchRoute("")
                                navController.navigate(NavRoutes.search.name)
                            }
                        )
                        2 -> BuiltInPlaylistSongs(
                            navController = navController,
                            builtInPlaylist = BuiltInPlaylist.Downloaded,
                            onSearchClick = {
                                //searchRoute("")
                                navController.navigate(NavRoutes.search.name)
                            }
                        )
                        3 -> BuiltInPlaylistSongs(
                            navController = navController,
                            builtInPlaylist = BuiltInPlaylist.Top,
                            onSearchClick = {
                                //searchRoute("")
                                navController.navigate(NavRoutes.search.name)
                            }
                        )
                        4 -> DeviceListSongs(
                            navController = navController,
                            deviceLists = DeviceLists.LocalSongs,
                            onSearchClick = {
                                //searchRoute("")
                                navController.navigate(NavRoutes.search.name)
                            }
                        )

                    }
                }
            }
        }
    }
}
