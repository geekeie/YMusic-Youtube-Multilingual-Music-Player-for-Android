package com.peecock.ymusic.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.peecock.ymusic.R
import com.peecock.ymusic.enums.NavigationBarPosition
import com.peecock.ymusic.enums.PlayerPosition
import com.peecock.ymusic.enums.TransitionEffect
import com.peecock.ymusic.enums.UiType
import com.peecock.ymusic.ui.components.themed.appBar
import com.peecock.ymusic.ui.styling.LocalAppearance
import com.peecock.ymusic.utils.UiTypeKey
import com.peecock.ymusic.utils.navigationBarPositionKey
import com.peecock.ymusic.utils.playerPositionKey
import com.peecock.ymusic.utils.rememberPreference
import com.peecock.ymusic.utils.transitionEffectKey

@OptIn(ExperimentalMaterial3Api::class)
@ExperimentalAnimationApi
@Composable
fun Scaffold(
    navController: NavController,
    playerEssential: @Composable (() -> Unit)? = null,
    topIconButtonId: Int,
    onTopIconButtonClick: () -> Unit,
    showButton1: Boolean = false,
    topIconButton2Id: Int,
    onTopIconButton2Click: () -> Unit,
    showButton2: Boolean,
    bottomIconButtonId: Int? = R.drawable.search,
    onBottomIconButtonClick: (() -> Unit)? = {},
    showBottomButton: Boolean? = false,
    hideTabs: Boolean? = false,
    tabIndex: Int,
    onTabChanged: (Int) -> Unit,
    showTopActions: Boolean? = false,
    tabColumnContent: @Composable ColumnScope.(@Composable (Int, String, Int) -> Unit) -> Unit,
    onHomeClick: () -> Unit,
    onSettingsClick: (() -> Unit)? = {},
    onStatisticsClick: (() -> Unit)? = {},
    onHistoryClick: (() -> Unit)? = {},
    onSearchClick: (() -> Unit)? = {},
    modifier: Modifier = Modifier,
    content: @Composable AnimatedVisibilityScope.(Int) -> Unit
) {
    val (colorPalette, typography) = LocalAppearance.current
    val navigationBarPosition by rememberPreference(navigationBarPositionKey, NavigationBarPosition.Bottom)
    val uiType  by rememberPreference(UiTypeKey, UiType.RiMusic)
    val transitionEffect by rememberPreference(transitionEffectKey, TransitionEffect.Scale)
    val playerPosition by rememberPreference(playerPositionKey, PlayerPosition.Bottom)

    if (navigationBarPosition == NavigationBarPosition.Top || navigationBarPosition == NavigationBarPosition.Bottom) {
            ScaffoldTB(
                navController = navController,
                playerEssential = playerEssential,
                topIconButtonId = topIconButtonId,
                onTopIconButtonClick = onTopIconButtonClick,
                showButton1 = showButton1,
                topIconButton2Id = topIconButton2Id,
                onTopIconButton2Click = onTopIconButton2Click,
                showButton2 = showButton2,
                tabIndex = tabIndex,
                onTabChanged = onTabChanged,
                tabColumnContent = tabColumnContent,
                showBottomButton = showBottomButton,
                bottomIconButtonId = bottomIconButtonId,
                onBottomIconButtonClick = onBottomIconButtonClick ?: {},
                showTopActions = showTopActions,
                content = content,
                hideTabs = hideTabs,
                onHomeClick = onHomeClick,
                onStatisticsClick = onStatisticsClick,
                onSettingsClick = onSettingsClick,
                onHistoryClick = onHistoryClick,
                onSearchClick = onSearchClick
            )
    } else {
        //val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
        val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
        val customModifier = if(uiType == UiType.RiMusic)
            Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
        else Modifier


        androidx.compose.material3.Scaffold(
            modifier = customModifier,
            containerColor = colorPalette.background0,
            topBar = {
                if(uiType == UiType.RiMusic) {
                    appBar(navController)
                }
            },

            bottomBar = {
                /*
                if (playerEssential != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            //.background(colorPalette.background0)
                            .padding(
                                windowInsets
                                    .only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal)
                                    .asPaddingValues()
                            )
                    ) {
                        playerEssential()
                    }

                }

                 */
            }

        ) {

            //**
            Box(
                modifier = Modifier
                    .padding(it)
                    .fillMaxSize()
            ) {

                Row(
                    modifier = modifier
                        .background(colorPalette.background0)
                        .fillMaxSize()
                ) {
                    val navigationRail: @Composable () -> Unit = {
                        NavigationRail(
                            topIconButtonId = topIconButtonId,
                            onTopIconButtonClick = onTopIconButtonClick,
                            showButton1 = showButton1,
                            topIconButton2Id = topIconButton2Id,
                            onTopIconButton2Click = onTopIconButton2Click,
                            showButton2 = showButton2,
                            bottomIconButtonId = bottomIconButtonId,
                            onBottomIconButtonClick = onBottomIconButtonClick ?: {},
                            showBottomButton = showBottomButton,
                            tabIndex = tabIndex,
                            onTabIndexChanged = onTabChanged,
                            content = tabColumnContent,
                            hideTabs = hideTabs
                        )
                    }

                    if (navigationBarPosition == NavigationBarPosition.Left)
                        navigationRail()

                    val topPadding = if (uiType == UiType.ViMusic) 30.dp else 0.dp

                    AnimatedContent(
                        targetState = tabIndex,
                        transitionSpec = {
                            when (transitionEffect) {
                                TransitionEffect.None -> EnterTransition.None togetherWith ExitTransition.None
                                TransitionEffect.Expand -> expandIn(
                                    animationSpec = tween(
                                        350,
                                        easing = LinearOutSlowInEasing
                                    ), expandFrom = Alignment.BottomStart
                                ).togetherWith(
                                    shrinkOut(
                                        animationSpec = tween(
                                            350,
                                            easing = FastOutSlowInEasing
                                        ), shrinkTowards = Alignment.CenterStart
                                    )
                                )

                                TransitionEffect.Fade -> fadeIn(animationSpec = tween(350)).togetherWith(
                                    fadeOut(animationSpec = tween(350))
                                )

                                TransitionEffect.Scale -> scaleIn(animationSpec = tween(350)).togetherWith(
                                    scaleOut(animationSpec = tween(350))
                                )

                                TransitionEffect.SlideHorizontal, TransitionEffect.SlideVertical -> {
                                    val slideDirection = when (targetState > initialState) {
                                        true -> {
                                            if (transitionEffect == TransitionEffect.SlideHorizontal)
                                                AnimatedContentTransitionScope.SlideDirection.Left
                                            else AnimatedContentTransitionScope.SlideDirection.Up
                                        }

                                        false -> {
                                            if (transitionEffect == TransitionEffect.SlideHorizontal)
                                                AnimatedContentTransitionScope.SlideDirection.Right
                                            else AnimatedContentTransitionScope.SlideDirection.Down
                                        }
                                    }

                                    val animationSpec = spring(
                                        dampingRatio = 0.9f,
                                        stiffness = Spring.StiffnessLow,
                                        visibilityThreshold = IntOffset.VisibilityThreshold
                                    )

                                    slideIntoContainer(slideDirection, animationSpec) togetherWith
                                            slideOutOfContainer(slideDirection, animationSpec)
                                }
                            }
                        },
                        content = content, label = "",
                        modifier = Modifier
                            //.fillMaxWidth()
                            .fillMaxHeight()
                            .padding(top = topPadding)
                    )

                    if (navigationBarPosition == NavigationBarPosition.Right)
                        navigationRail()

                }
                //**
                Box(
                    modifier = Modifier
                        .padding(vertical = 5.dp)
                        .align(if (playerPosition == PlayerPosition.Top) Alignment.TopCenter
                        else Alignment.BottomCenter)
                ) {
                    playerEssential?.invoke()
                }
            }
        }
    }

}
