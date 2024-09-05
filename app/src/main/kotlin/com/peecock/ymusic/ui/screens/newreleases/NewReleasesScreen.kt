package com.peecock.ymusic.ui.screens.newreleases

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
import com.peecock.ymusic.enums.NavRoutes
import com.peecock.ymusic.enums.UiType
import com.peecock.ymusic.ui.components.Scaffold
import com.peecock.ymusic.ui.screens.globalRoutes
import com.peecock.ymusic.utils.UiTypeKey
import com.peecock.ymusic.utils.rememberPreference

@ExperimentalMaterialApi
@ExperimentalTextApi
@ExperimentalFoundationApi
@ExperimentalComposeUiApi
@ExperimentalAnimationApi
@UnstableApi
@Composable
fun NewreleasesScreen(
    navController: NavController,
    playerEssential: @Composable () -> Unit = {},
) {
    val saveableStateHolder = rememberSaveableStateHolder()

    var tabIndex by rememberSaveable {
        mutableStateOf(0)
    }

    PersistMapCleanup(tagPrefix = "newreleases")

    RouteHandler(listenToGlobalEmitter = true) {
        globalRoutes()

        val uiType by rememberPreference(UiTypeKey, UiType.RiMusic)

        host {
            Scaffold(
                navController = navController,
                playerEssential = playerEssential,
                topIconButtonId = R.drawable.chevron_back,
                onTopIconButtonClick = pop,
                showButton1 = if (uiType == UiType.RiMusic) false else true,
                topIconButton2Id = R.drawable.chevron_back,
                onTopIconButton2Click = pop,
                showButton2 = false,
                tabIndex = tabIndex,
                onTabChanged = { tabIndex = it },
                onHomeClick = { navController.navigate(NavRoutes.home.name) },
                showTopActions = false,
                tabColumnContent = { item ->
                    item(0, stringResource(R.string.new_albums), R.drawable.album)
                    //item(1, stringResource(R.string.new_albums_of_your_artists), R.drawable.album)
                }
            ) { currentTabIndex ->
                saveableStateHolder.SaveableStateProvider(key = currentTabIndex) {
                    when (currentTabIndex) {
                        0 -> NewAlbums(
                            navController = navController,
                        )
                        1 -> NewAlbumsFromArtists(
                            navController = navController,
                        )
                    }
                }
            }
        }
    }
}
