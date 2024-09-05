package com.peecock.ymusic.ui.components

import androidx.annotation.OptIn
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import com.peecock.ymusic.LocalPlayerSheetState
import com.peecock.ymusic.R
import com.peecock.ymusic.enums.NavigationBarPosition
import com.peecock.ymusic.enums.NavigationBarType
import com.peecock.ymusic.ui.styling.Dimensions
import com.peecock.ymusic.ui.styling.LocalAppearance
import com.peecock.ymusic.ui.styling.favoritesIcon
import com.peecock.ymusic.utils.navigationBarPositionKey
import com.peecock.ymusic.utils.navigationBarTypeKey
import com.peecock.ymusic.utils.rememberPreference
import com.peecock.ymusic.utils.semiBold

@OptIn(UnstableApi::class)
@Composable
inline fun NavigationRailTB(
    navController: NavController,
    topIconButtonId: Int,
    noinline onTopIconButtonClick: () -> Unit,
    showButton1: Boolean = true,
    topIconButton2Id: Int,
    noinline onTopIconButton2Click: () -> Unit,
    showButton2: Boolean,
    bottomIconButtonId: Int? = R.drawable.search,
    noinline onBottomIconButtonClick: () -> Unit,
    showBottomButton: Boolean? = false,
    tabIndex: Int,
    crossinline onTabIndexChanged: (Int) -> Unit,
    content: @Composable() (ColumnScope.(@Composable (Int, String, Int) -> Unit) -> Unit),
    hideTabs: Boolean? = false,
    modifier: Modifier = Modifier
) {
    val (colorPalette, typography, thumbnailShape) = LocalAppearance.current

    /*
    val isLandscape = isLandscape

    val paddingValues = LocalPlayerAwareWindowInsets.current
        .only(WindowInsetsSides.Vertical + WindowInsetsSides.Start).asPaddingValues()

     */

    val navigationBarType by rememberPreference(navigationBarTypeKey, NavigationBarType.IconAndText)
    val density = LocalDensity.current
    val windowsInsets = WindowInsets.systemBars
    val bottomDp = with(density) { windowsInsets.getBottom(density).toDp() }

    val navigationBarPosition by rememberPreference(navigationBarPositionKey, NavigationBarPosition.Bottom)
    val localSheetState = LocalPlayerSheetState.current
    /*
    val bottomPadding = if (navigationBarPosition == NavigationBarPosition.Bottom)
        if (localSheetState.isCollapsed) bottomDp + Dimensions.navigationBarHeight else bottomDp
    else 0.dp
     */
    val bottomPadding = if (navigationBarPosition == NavigationBarPosition.Bottom) bottomDp else 5.dp

    //val topPadding = if (navigationBarPosition == NavigationBarPosition.Top) 30.dp else 0.dp
    val topPadding = 0.dp
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom,
        modifier = modifier
            //.border(BorderStroke(1.dp, Color.Yellow))
            //.padding(top = 30.dp)
            .padding(top = topPadding, bottom = bottomPadding) //bottom navigation
            //.background(colorPalette.background0)
            //.background(colorPalette.background1)
            //.background(Color.Transparent)



    ) {

        if (hideTabs == false)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceAround,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimensions.navigationBarHeight-10.dp)
                    .padding(horizontal = 5.dp)
            ) {
                val transition = updateTransition(targetState = tabIndex, label = null)

                val listIconContent = mutableListOf<@Composable () -> Unit>()

                content { index, text, icon ->

                    val textColor by transition.animateColor(label = "") {
                        if (it == index) colorPalette.text else colorPalette.textDisabled
                    }

                    val contentModifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(onClick = { onTabIndexChanged(index) })

                    val itemContent: @Composable () -> Unit = {
                        Box(
                            modifier = contentModifier
                        ) {
                            if (navigationBarType == NavigationBarType.IconOnly) {
                                Image(
                                    painter = painterResource(icon),
                                    contentDescription = null,
                                    colorFilter = ColorFilter.tint(textColor),
                                    modifier = Modifier
                                        .padding(all = 12.dp)
                                        .size(20.dp)
                                )
                            } else {
                                Column (
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.SpaceAround,
                                    modifier = Modifier
                                        .padding(all = 5.dp)
                                        .fillMaxSize()
                                ){
                                    Image(
                                        painter = painterResource(icon),
                                        contentDescription = null,
                                        colorFilter = ColorFilter.tint(textColor),
                                        modifier = Modifier
                                            .size(Dimensions.navigationRailIconOffset * 3)
                                    )
                                        Spacer(modifier = Modifier.height(5.dp))
                                        BasicText(
                                            text = text,
                                            style = TextStyle(
                                                fontSize = typography.xs.semiBold.fontSize,
                                                fontWeight = typography.xs.semiBold.fontWeight,
                                                fontFamily = typography.xs.semiBold.fontFamily,
                                                color = textColor,
                                            ),
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                }
                            }
                        }
                    }


                    listIconContent.add { itemContent() }

                   }

                val scrollState = rememberScrollState()
                val roundedCornerShape = if (navigationBarPosition == NavigationBarPosition.Bottom)
                    RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
                else RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(roundedCornerShape)
                        .background(colorPalette.background1)
                ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxSize()
                        .horizontalScroll(scrollState)
                        //.padding(horizontal = 5.dp)
                        //.padding(top = 4.dp, bottom = 4.dp)
                        //.padding(top = 1.dp, bottom = 1.dp)

                ) {

                        //if (listIconContent.size > 1) //hide if only one icon is present
                        listIconContent.forEach {
                            it()
                        }

                        if (showButton1)
                            Image(
                                painter = painterResource(topIconButtonId),
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(colorPalette.favoritesIcon), //ColorFilter.tint(colorPalette.textSecondary),
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable(onClick = onTopIconButtonClick)
                                    .padding(all = 4.dp)
                                    .size(24.dp)
                            )

                        if (showButton2)
                            Image(
                                painter = painterResource(topIconButton2Id),
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(colorPalette.textSecondary),
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable(onClick = onTopIconButton2Click)
                                    .padding(all = 4.dp)
                                    .size(24.dp)
                            )


                        if (showBottomButton == true)
                            Image(
                                painter = painterResource(bottomIconButtonId ?: R.drawable.search),
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(colorPalette.textSecondary),
                                modifier = Modifier
                                    .clickable(onClick = onBottomIconButtonClick)
                                    .padding(all = 4.dp)
                                    .size(24.dp)
                            )
                    }
                }

            }


    }
}
