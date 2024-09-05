package com.peecock.ymusic.ui.screens.mood

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
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
fun MoodsPageScreen(
    navController: NavController
) {
    val saveableStateHolder = rememberSaveableStateHolder()

    PersistMapCleanup(tagPrefix = "playlist/$defaultBrowseId")

    RouteHandler(listenToGlobalEmitter = true) {
        globalRoutes()
        val uiType  by rememberPreference(UiTypeKey, UiType.RiMusic)
        host {
            Scaffold(
                navController = navController,
                topIconButtonId = R.drawable.chevron_back,
                showButton1 = if(uiType == UiType.RiMusic) false else true,
                onTopIconButtonClick = pop,
                topIconButton2Id = R.drawable.chevron_back,
                onTopIconButton2Click = pop,
                showButton2 = false,
                tabIndex = 0,
                onTabChanged = { },
                onHomeClick = {
                    //homeRoute()
                    navController.navigate(NavRoutes.home.name)
                },
                tabColumnContent = { item ->
                    item(0, stringResource(R.string.moods_and_genres), R.drawable.album)
                }
            ) { currentTabIndex ->
                saveableStateHolder.SaveableStateProvider(key = currentTabIndex) {
                    when (currentTabIndex) {
                        0 -> MoodsPage(
                            navController = navController
                        )
                    }
                }
            }
        }
    }
}
