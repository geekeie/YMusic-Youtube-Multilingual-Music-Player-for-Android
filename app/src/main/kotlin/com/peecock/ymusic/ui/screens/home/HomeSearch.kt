package com.peecock.ymusic.ui.screens.home

import android.annotation.SuppressLint
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.peecock.ymusic.LocalPlayerAwareWindowInsets
import com.peecock.ymusic.R
import com.peecock.ymusic.enums.NavigationBarPosition
import com.peecock.ymusic.enums.SearchType
import com.peecock.ymusic.ui.components.themed.HeaderWithIcon
import com.peecock.ymusic.ui.items.PlaylistItem
import com.peecock.ymusic.ui.styling.Dimensions
import com.peecock.ymusic.ui.styling.LocalAppearance
import com.peecock.ymusic.ui.styling.favoritesIcon
import com.peecock.ymusic.ui.styling.px
import com.peecock.ymusic.utils.navigationBarPositionKey
import com.peecock.ymusic.utils.rememberPreference

@SuppressLint("SuspiciousIndentation")
@ExperimentalAnimationApi
@ExperimentalFoundationApi
@Composable
fun HomeSearch(
    onSearchType: (SearchType) -> Unit
) {
    val (colorPalette, typography, thumbnailShape) = LocalAppearance.current

    val thumbnailSizeDp = 108.dp
    val thumbnailSizePx = thumbnailSizeDp.px

    val lazyGridState = rememberLazyGridState()

    val navigationBarPosition by rememberPreference(navigationBarPositionKey, NavigationBarPosition.Bottom)

    Box(
        modifier = Modifier
            .background(colorPalette.background0)
            //.fillMaxSize()
            .fillMaxHeight()
            .fillMaxWidth(if (navigationBarPosition == NavigationBarPosition.Left ||
                navigationBarPosition == NavigationBarPosition.Top ||
                navigationBarPosition == NavigationBarPosition.Bottom) 1f
            else Dimensions.contentWidthRightBar)
    ) {
        LazyVerticalGrid(
            state = lazyGridState,
            columns = GridCells.Adaptive(Dimensions.thumbnails.song * 2 + Dimensions.itemsVerticalPadding * 2),
            contentPadding = LocalPlayerAwareWindowInsets.current
                .only(WindowInsetsSides.Vertical + WindowInsetsSides.End).asPaddingValues(),
            verticalArrangement = Arrangement.spacedBy(Dimensions.itemsVerticalPadding * 2),
            horizontalArrangement = Arrangement.spacedBy(
                space = Dimensions.itemsVerticalPadding * 2,
                alignment = Alignment.CenterHorizontally
            ),
            modifier = Modifier
                .fillMaxSize()
                .background(colorPalette.background0)
        ) {
            item(key = "header", contentType = 0, span = { GridItemSpan(maxLineSpan) }) {

                HeaderWithIcon(
                    title = stringResource(R.string.search),
                    iconId = R.drawable.search,
                    enabled = false,
                    showIcon = false,
                    modifier = Modifier,
                    onClick = {}
                )

            }

            item(key = "online") {
                PlaylistItem(
                    icon = R.drawable.globe,
                    colorTint = colorPalette.favoritesIcon,
                    name = "${stringResource(R.string.search)} ${stringResource(R.string.online)}",
                    songCount = null,
                    thumbnailSizeDp = thumbnailSizeDp,
                    alternative = true,
                    modifier = Modifier
                        .clip(thumbnailShape)
                        .clickable(onClick = { onSearchType(SearchType.Online) })
                        .animateItemPlacement()

                )
            }

            item(key = "library") {
                PlaylistItem(
                    icon = R.drawable.library,
                    colorTint = colorPalette.favoritesIcon,
                    name = "${stringResource(R.string.search)} ${stringResource(R.string.library)}",
                    songCount = null,
                    thumbnailSizeDp = thumbnailSizeDp,
                    alternative = true,
                    modifier = Modifier
                        .clip(thumbnailShape)
                        .clickable(onClick = { onSearchType(SearchType.Library) })
                        .animateItemPlacement()

                )
            }

            item(key = "gotolink") {
                Modifier
                    .clip(thumbnailShape)
                    .clickable(onClick = { onSearchType(SearchType.Gotolink) })
                PlaylistItem(
                    icon = R.drawable.query_stats,
                    colorTint = colorPalette.favoritesIcon,
                    name = stringResource(R.string.go_to_link),
                    songCount = null,
                    thumbnailSizeDp = thumbnailSizeDp,
                    alternative = true,
                    modifier = Modifier.animateItem(fadeInSpec = null, fadeOutSpec = null)
                )
            }

        }

    }
}
