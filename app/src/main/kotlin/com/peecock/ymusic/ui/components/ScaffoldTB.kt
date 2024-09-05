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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomNavigationDefaults.windowInsets
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
import com.peecock.ymusic.ui.components.themed.appBar
import com.peecock.ymusic.ui.styling.LocalAppearance
import com.peecock.ymusic.utils.navigationBarPositionKey
import com.peecock.ymusic.utils.playerPositionKey
import com.peecock.ymusic.utils.rememberPreference
import com.peecock.ymusic.utils.transitionEffectKey


@OptIn(ExperimentalMaterial3Api::class)
@ExperimentalAnimationApi
@Composable
fun ScaffoldTB(
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
    showTopActions: Boolean? = false,
    onHomeClick: () -> Unit,
    onSettingsClick: (() -> Unit)? = {},
    onStatisticsClick: (() -> Unit)? = {},
    onHistoryClick: (() -> Unit)? = {},
    onSearchClick: (() -> Unit)? = {},
    tabIndex: Int,
    onTabChanged: (Int) -> Unit,
    tabColumnContent: @Composable() (ColumnScope.(@Composable (Int, String, Int) -> Unit) -> Unit),
    modifier: Modifier = Modifier,
    content: @Composable AnimatedVisibilityScope.(Int) -> Unit
) {
    val (colorPalette, typography) = LocalAppearance.current
    val navigationBarPosition by rememberPreference(navigationBarPositionKey, NavigationBarPosition.Bottom)

    val navigationRailTB: @Composable () -> Unit = {
        NavigationRailTB(
            navController = navController,
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

    //val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    //var expanded by remember { mutableStateOf(false) }
    val transitionEffect by rememberPreference(transitionEffectKey, TransitionEffect.Scale)
    val playerPosition by rememberPreference(playerPositionKey, PlayerPosition.Bottom)

    androidx.compose.material3.Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
       containerColor = colorPalette.background0,
        topBar = {
            Column(
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                appBar(navController)

                if (navigationBarPosition == NavigationBarPosition.Top)
                    navigationRailTB()

                /*
                if (playerEssential != null && playerPosition == PlayerPosition.Top) {
                    val modifierBottomPadding = Modifier
                        .padding(bottom = 5.dp)

                    Row (
                        modifier = modifierBottomPadding
                    ) {
                        playerEssential()
                    }
                }
                 */
            }
        },

        bottomBar = {

        /*

                Column(
                    verticalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colorPalette.background0)
                ){

         */
                    /*
                    if (playerEssential != null && playerPosition == PlayerPosition.Bottom) {
                        val modifierBottomPadding = if (navigationBarPosition != NavigationBarPosition.Bottom)
                            Modifier.padding( windowInsets
                                .only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal)
                                .asPaddingValues()
                                )
                                .padding(bottom = 5.dp)
                        else Modifier
                            .padding(bottom = 5.dp)

                        Row (
                            modifier = modifierBottomPadding
                        ) {
                            playerEssential()
                        }
                    }
                     */

                    if (navigationBarPosition == NavigationBarPosition.Bottom)
                            navigationRailTB()

                //}
        }

    ) {
        val modifierBoxPadding =  if (navigationBarPosition != NavigationBarPosition.Top)
            Modifier
                .padding(it)
                .fillMaxSize()
            else Modifier
                .padding(it)
                .padding(
                    windowInsets
                        .only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal)
                        .asPaddingValues()
                )
                .fillMaxSize()

        Box(
            modifier = modifierBoxPadding
        ) {

        Row(
            modifier = modifier
                .background(colorPalette.background0)
                .fillMaxSize()
        ) {
            AnimatedContent(
                targetState = tabIndex,
                transitionSpec = {
                    when (transitionEffect) {
                        TransitionEffect.None-> EnterTransition.None togetherWith ExitTransition.None
                        TransitionEffect.Expand -> expandIn(animationSpec = tween(350, easing = LinearOutSlowInEasing), expandFrom = Alignment.TopStart).togetherWith(
                            shrinkOut(animationSpec = tween(350, easing = FastOutSlowInEasing),shrinkTowards = Alignment.TopStart)
                        )
                        TransitionEffect.Fade -> fadeIn(animationSpec = tween(350)).togetherWith(fadeOut(animationSpec = tween(350)))
                        TransitionEffect.Scale -> scaleIn(animationSpec = tween(350)).togetherWith(scaleOut(animationSpec = tween(350)))
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
                content = content,
                label = "",
                modifier = Modifier
                    .fillMaxHeight()
            )
        }
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
