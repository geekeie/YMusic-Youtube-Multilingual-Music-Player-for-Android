package com.peecock.ymusic.ui.screens.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import com.peecock.compose.persist.PersistMapCleanup
import com.peecock.compose.routing.RouteHandler
import com.peecock.ymusic.R
import com.peecock.ymusic.enums.NavRoutes
import com.peecock.ymusic.enums.UiType
import com.peecock.ymusic.ui.components.themed.IconButton
import com.peecock.ymusic.ui.components.Scaffold
import com.peecock.ymusic.ui.screens.globalRoutes
import com.peecock.ymusic.ui.styling.LocalAppearance
import com.peecock.ymusic.ui.styling.favoritesIcon
import com.peecock.ymusic.utils.UiTypeKey
import com.peecock.ymusic.utils.applyFontPaddingKey
import com.peecock.ymusic.utils.rememberPreference
import com.peecock.ymusic.utils.secondary
@ExperimentalMaterialApi
@ExperimentalTextApi
@ExperimentalFoundationApi
@ExperimentalAnimationApi
@ExperimentalComposeUiApi
@UnstableApi
@Composable
fun SearchScreen(
    navController: NavController,
    playerEssential: @Composable () -> Unit = {},
    initialTextInput: String,
    onSearch: (String) -> Unit,
    onViewPlaylist: (String) -> Unit,
    onDismiss: (() -> Unit)? = null,
) {
    val saveableStateHolder = rememberSaveableStateHolder()
    val (colorPalette) = LocalAppearance.current

    val (tabIndex, onTabChanged) = rememberSaveable {
        mutableStateOf(0)
    }

    val (textFieldValue, onTextFieldValueChanged) = rememberSaveable(
        initialTextInput,
        stateSaver = TextFieldValue.Saver
    ) {
        mutableStateOf(
            TextFieldValue(
                text = initialTextInput,
                selection = TextRange(initialTextInput.length)
            )
        )
    }

    val applyFontPadding by rememberPreference(applyFontPaddingKey, false)
    val uiType  by rememberPreference(UiTypeKey, UiType.RiMusic)

    PersistMapCleanup(tagPrefix = "search/")

    RouteHandler(listenToGlobalEmitter = true) {
        globalRoutes()
        //val onGoToHome = homeRoute::global

        host {
            val decorationBox: @Composable (@Composable () -> Unit) -> Unit = { innerTextField ->

                Box(
                    contentAlignment = Alignment.CenterStart,
                    modifier = Modifier
                       // .weight(1f)
                        .padding(horizontal = 10.dp)
                ) {
                    if (uiType == UiType.ViMusic)
                        Column (
                            verticalArrangement = Arrangement.Center
                        ) {
                           // if (applyFontPadding)
                               Spacer(modifier = Modifier.padding(top = 5.dp))

                            IconButton(
                                onClick = {},
                                icon = R.drawable.search,
                                color = colorPalette.favoritesIcon,
                                modifier = Modifier
                                    //.align(Alignment.CenterStart)
                                    .size(20.dp)
                            )
                        }


                }

                Box(
                    contentAlignment = Alignment.CenterStart,
                    modifier = Modifier
                       // .weight(1f)
                        .padding(horizontal = 40.dp)
                ) {
                    AnimatedVisibility(
                        visible = textFieldValue.text.isEmpty(),
                        enter = fadeIn(tween(300)),
                        exit = fadeOut(tween(300)),
                        modifier = Modifier
                            .align(Alignment.Center)
                    ) {
                        BasicText(
                            text = stringResource(R.string.search), //stringResource(R.string.enter_a_name),
                            maxLines = 1,
                            style = LocalAppearance.current.typography.l.secondary,

                        )
                    }

                    innerTextField()
                }
                Box(
                    contentAlignment = Alignment.CenterEnd,
                    modifier = Modifier
                        // .weight(1f)
                        .padding(horizontal = 10.dp)
                ) {
                    Column (
                        verticalArrangement = Arrangement.Center
                    ) {

                        Spacer(modifier = Modifier.padding(top = 5.dp))

                        IconButton(
                            onClick = { onTextFieldValueChanged(TextFieldValue("")) },
                            icon = R.drawable.close,
                            color = colorPalette.favoritesIcon,
                            modifier = Modifier
                                .size(24.dp)
                        )
                    }


                }
            }

            Scaffold(
                navController = navController,
                playerEssential = playerEssential,
                topIconButtonId = R.drawable.chevron_back,
                showButton1 = if(uiType == UiType.RiMusic) false else true,
                onTopIconButtonClick = {
                    //onGoToHome()
                    navController.navigate(NavRoutes.home.name)
                },
                topIconButton2Id = R.drawable.chevron_back,
                onTopIconButton2Click = pop,
                showButton2 = false,
                //hideTabs = false,
                tabIndex = tabIndex,
                onTabChanged = onTabChanged,
                onHomeClick = {
                    //homeRoute()
                    navController.navigate(NavRoutes.home.name)
                },
                tabColumnContent = { Item ->
                    Item(0, stringResource(R.string.online), R.drawable.globe)
                    Item(1, stringResource(R.string.library), R.drawable.library)
                    Item(2, stringResource(R.string.go_to_link), R.drawable.link)
                }
            ) { currentTabIndex ->
                saveableStateHolder.SaveableStateProvider(currentTabIndex) {
                    when (currentTabIndex) {
                        0 -> OnlineSearch(
                            navController = navController,
                            textFieldValue = textFieldValue,
                            onTextFieldValueChanged = onTextFieldValueChanged,
                            onSearch = onSearch,
                            decorationBox = decorationBox
                        )

                        1 -> LocalSongSearch(
                            navController = navController,
                            textFieldValue = textFieldValue,
                            onTextFieldValueChanged = onTextFieldValueChanged,
                            decorationBox = decorationBox,
                            onAction1 = { onTabChanged(0) },
                            onAction2 = { onTabChanged(1) },
                            onAction3 = { onTabChanged(2) },
                            onAction4 = {
                                //onGoToHome()
                                pop()
                            },
                        )

                        2 -> GoToLink(
                            navController = navController,
                            textFieldValue = textFieldValue,
                            onTextFieldValueChanged = onTextFieldValueChanged,
                            decorationBox = decorationBox,
                            onAction1 = { onTabChanged(0) },
                            onAction2 = { onTabChanged(1) },
                            onAction3 = { onTabChanged(2) },
                            onAction4 = {
                                //onGoToHome()
                                pop()
                            },
                        )
                    }
                }
            }
        }
    }
}
