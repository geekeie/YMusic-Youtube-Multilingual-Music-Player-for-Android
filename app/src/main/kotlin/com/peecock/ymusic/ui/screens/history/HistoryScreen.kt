package com.peecock.ymusic.ui.screens.history

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
import com.peecock.ymusic.enums.UiType
import com.peecock.ymusic.ui.components.Scaffold
import com.peecock.ymusic.ui.screens.globalRoutes
import com.peecock.ymusic.ui.screens.homeRoute
import com.peecock.ymusic.utils.UiTypeKey
import com.peecock.ymusic.utils.rememberPreference

@ExperimentalMaterialApi
@ExperimentalTextApi
@ExperimentalFoundationApi
@ExperimentalComposeUiApi
@ExperimentalAnimationApi
@UnstableApi
@Composable
fun HistoryScreen(
    navController: NavController,
    playerEssential: @Composable () -> Unit = {},
) {
    val saveableStateHolder = rememberSaveableStateHolder()

    PersistMapCleanup(tagPrefix = "history")

    RouteHandler(listenToGlobalEmitter = true) {
        globalRoutes()

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
                showButton2 = false,
                tabIndex = 0,
                onTabChanged = { },
                onHomeClick = { homeRoute() },
                showTopActions = false,
                /*
                onSettingsClick = { settingsRoute() },
                onStatisticsClick = { statisticsTypeRoute(StatisticsType.Today) },
                onHistoryClick = { historyRoute() },
                onSearchClick = { searchRoute("") },

                 */
                tabColumnContent = { item ->
                    item(0, stringResource(R.string.history), R.drawable.history)
                }
            ) { currentTabIndex ->
                saveableStateHolder.SaveableStateProvider(key = currentTabIndex) {
                    when (currentTabIndex) {
                        0 -> HistoryList(
                            navController = navController,
                        )
                    }
                }
            }
        }
    }
}
