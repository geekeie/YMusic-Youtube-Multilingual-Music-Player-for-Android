package com.peecock.ymusic.ui.components

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.peecock.ymusic.LocalPlayerAwareWindowInsets
import com.peecock.ymusic.R
import com.peecock.ymusic.enums.NavigationBarType
import com.peecock.ymusic.enums.UiType
import com.peecock.ymusic.ui.styling.Dimensions
import com.peecock.ymusic.ui.styling.LocalAppearance
import com.peecock.ymusic.ui.styling.favoritesIcon
import com.peecock.ymusic.utils.UiTypeKey
import com.peecock.ymusic.utils.isLandscape
import com.peecock.ymusic.utils.navigationBarTypeKey
import com.peecock.ymusic.utils.rememberPreference
import com.peecock.ymusic.utils.semiBold

@Composable
inline fun NavigationRail(
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
    content: @Composable ColumnScope.(@Composable (Int, String, Int) -> Unit) -> Unit,
    hideTabs: Boolean? = false,
    modifier: Modifier = Modifier
) {
    val (colorPalette, typography) = LocalAppearance.current

    val isLandscape = isLandscape

    val paddingValues = LocalPlayerAwareWindowInsets.current
        .only(WindowInsetsSides.Vertical + WindowInsetsSides.Start).asPaddingValues()

    val navigationBarType by rememberPreference(navigationBarTypeKey, NavigationBarType.IconAndText)
    val uiType  by rememberPreference(UiTypeKey, UiType.RiMusic)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .verticalScroll(rememberScrollState())
            //.padding(paddingValues)
            //.border(BorderStroke(1.dp, Color.Green))
            //.fillMaxWidth()
    ) {

        if (hideTabs == false)
        //if(uiType == UiType.ViMusic)
            Box(
                contentAlignment = Alignment.TopCenter,
                modifier = Modifier
                    .height(
                        if(uiType == UiType.ViMusic)
                            if (showButton2) Dimensions.headerHeight else Dimensions.halfheaderHeight
                        else 0.dp
                    )
                    /*
                    .size(
                        //width = if (isLandscape) Dimensions.navigationRailWidthLandscape else Dimensions.navigationRailWidth,
                        width = Dimensions.navigationRailWidth,
                        height = if (showButton2) Dimensions.headerHeight else Dimensions.halfheaderHeight
                    )
                     */
                    //.border(BorderStroke(1.dp, Color.Red))
            ) {
                if (showButton1)
                    Image(
                        painter = painterResource(topIconButtonId),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(colorPalette.favoritesIcon), //ColorFilter.tint(colorPalette.textSecondary),
                        modifier = Modifier
                            .offset(
                                x = 0.dp, //if (isLandscape) 0.dp else Dimensions.navigationRailIconOffset,
                                y = 7.dp
                            )
                            .clip(CircleShape)
                            .clickable(onClick = onTopIconButtonClick)
                            //.padding(all = 12.dp)
                            .padding(top = 12.dp, bottom = 12.dp)
                            .size(24.dp)
                    )
                if (showButton2) {
                    Image(
                        painter = painterResource(topIconButton2Id),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(colorPalette.textSecondary),
                        modifier = Modifier
                            .offset(
                                x = 0.dp, //if (isLandscape) 0.dp else Dimensions.navigationRailIconOffset,
                                y = 70.dp
                            )
                            .clip(CircleShape)
                            .clickable(onClick = onTopIconButton2Click)
                            //.padding(all = 12.dp)
                            .padding(top = 12.dp, bottom = 12.dp)
                            .size(24.dp)
                    )
                }

            }


        if (hideTabs == false)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    //.border(BorderStroke(1.dp,Color.Green))
                    //.width(if (isLandscape) Dimensions.navigationRailWidthLandscape else Dimensions.navigationRailWidth)
                    //.width(Dimensions.navigationRailWidth)
                    //.fillMaxWidth()
                    //.border(BorderStroke(1.dp, Color.Magenta))
            ) {
                val transition = updateTransition(targetState = tabIndex, label = null)

                content { index, text, icon ->

                    val textColor by transition.animateColor(label = "") {
                        if (it == index) colorPalette.text else colorPalette.textDisabled
                    }
                    val dothAlpha by transition.animateFloat(label = "") {
                        if (it == index) 1f else 0f
                    }

                    val textContent: @Composable () -> Unit = {
                        if (navigationBarType == NavigationBarType.IconOnly) {
                            /*
                            BasicText(
                                text = "",
                                style = typography.xs.semiBold.center.color(textColor),
                                modifier = Modifier
                                    .padding(horizontal = 16.dp)
                            )
                             */
                        } else {
                            BasicText(
                                text = text,
                                //style = typography.xs.semiBold.center.color(textColor),
                                style = TextStyle(
                                    fontSize = typography.xs.semiBold.fontSize,
                                    fontWeight = typography.xs.semiBold.fontWeight,
                                    color = colorPalette.text,
                                    //textAlign = if(uiType != UiType.ViMusic) TextAlign.Center else TextAlign.End

                                ),
                                modifier = Modifier
                                    .vertical(enabled = !isLandscape)
                                    .rotate(if (isLandscape) 0f else -90f)
                                    .padding(horizontal = 16.dp)
                            )
                        }
                    }

                    val iconContent: @Composable () -> Unit = {
                        if (navigationBarType == NavigationBarType.IconOnly) {
                            Image(
                                painter = painterResource(icon),
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(textColor),
                                modifier = Modifier
                                    //.padding(all = 12.dp)
                                    .padding(top = 12.dp, bottom = 12.dp)
                                    .size(24.dp)
                            )
                        } else {
                            Image(
                                painter = painterResource(icon),
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(colorPalette.text),
                                modifier = Modifier
                                    .vertical(enabled = !isLandscape)
                                    .graphicsLayer {
                                        alpha = dothAlpha
                                        translationX = (1f - dothAlpha) * -48.dp.toPx()
                                        rotationZ = if (isLandscape) 0f else -90f
                                    }
                                    .size(Dimensions.navigationRailIconOffset * 2)
                            )
                        }
                    }

                    /*
                    val dothAlpha by transition.animateFloat(label = "") {
                        if (it == index) 1f else 0f
                    }

                    val textColor by transition.animateColor(label = "") {
                        if (it == index) colorPalette.text else colorPalette.textDisabled
                    }

                    val iconContent: @Composable () -> Unit = {
                        Image(
                            painter = painterResource(icon),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(colorPalette.text),
                            modifier = Modifier
                                .vertical(enabled = !isLandscape)
                                .graphicsLayer {
                                    alpha = dothAlpha
                                    //translationX = (1f - dothAlpha) * -48.dp.toPx()
                                    rotationZ = if (isLandscape) 0f else -90f
                                }
                                .size(Dimensions.navigationRailIconOffset * 2)
                        )
                    }

                    val textContent: @Composable () -> Unit = {
                        BasicText(
                            text = text,
                            style = typography.xs.semiBold.center.color(textColor),
                            modifier = Modifier
                                .vertical(enabled = !isLandscape)
                                .rotate(if (isLandscape) 0f else -90f)
                                .padding(horizontal = 16.dp)
                        )
                    }
                    */
                    val contentModifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .clickable(onClick = { onTabIndexChanged(index) })

                    if (isLandscape) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = contentModifier
                                .padding(vertical = 8.dp)
                        ) {
                            iconContent()
                            textContent()
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = contentModifier
                                .padding(horizontal = 8.dp)
                        ) {
                            iconContent()
                            textContent()
                        }
                    }
                }
            }

        if (showBottomButton == true)
            Box(
                contentAlignment = Alignment.TopCenter,
                modifier = Modifier
                    .size(
                        width = if (isLandscape) Dimensions.navigationRailWidthLandscape else Dimensions.navigationRailWidth,
                        height = Dimensions.halfheaderHeight
                    )
            ) {
                Image(
                    painter = painterResource(bottomIconButtonId ?: R.drawable.search ),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(colorPalette.textSecondary),
                    modifier = Modifier
                        .clickable(onClick = onBottomIconButtonClick )
                        .padding(all = 12.dp)
                        .size(24.dp)
                )
            }


    }
}

fun Modifier.vertical(enabled: Boolean = true) =
    if (enabled)
        layout { measurable, constraints ->
            val placeable = measurable.measure(constraints.copy(maxWidth = Int.MAX_VALUE))
            layout(placeable.height, placeable.width) {
                placeable.place(
                    x = -(placeable.width / 2 - placeable.height / 2),
                    y = -(placeable.height / 2 - placeable.width / 2)
                )
            }
        } else this
