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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.peecock.compose.persist.persistList
import com.peecock.ymusic.Database
import com.peecock.ymusic.LocalPlayerAwareWindowInsets
import com.peecock.ymusic.R
import com.peecock.ymusic.enums.BuiltInPlaylist
import com.peecock.ymusic.enums.NavigationBarPosition
import com.peecock.ymusic.enums.PlaylistSortBy
import com.peecock.ymusic.enums.SortOrder
import com.peecock.ymusic.enums.StatisticsType
import com.peecock.ymusic.enums.UiType
import com.peecock.ymusic.models.Playlist
import com.peecock.ymusic.models.PlaylistPreview
import com.peecock.ymusic.query
import com.peecock.ymusic.ui.components.themed.FloatingActionsContainerWithScrollToTop
import com.peecock.ymusic.ui.components.themed.HeaderWithIcon
import com.peecock.ymusic.ui.components.themed.InputTextDialog
import com.peecock.ymusic.ui.items.PlaylistItem
import com.peecock.ymusic.ui.styling.Dimensions
import com.peecock.ymusic.ui.styling.LocalAppearance
import com.peecock.ymusic.ui.styling.favoritesIcon
import com.peecock.ymusic.ui.styling.px
import com.peecock.ymusic.utils.UiTypeKey
import com.peecock.ymusic.utils.navigationBarPositionKey
import com.peecock.ymusic.utils.playlistSortByKey
import com.peecock.ymusic.utils.playlistSortOrderKey
import com.peecock.ymusic.utils.rememberPreference
import com.peecock.ymusic.utils.showSearchTabKey

@SuppressLint("SuspiciousIndentation")
@ExperimentalAnimationApi
@ExperimentalFoundationApi
@Composable
fun HomeStatistics(
    onStatisticsType: (StatisticsType) -> Unit,
    onBuiltInPlaylist: (BuiltInPlaylist) -> Unit,
    onPlaylistClick: (Playlist) -> Unit,
    onSearchClick: () -> Unit,
) {
    val (colorPalette, typography, thumbnailShape) = LocalAppearance.current
    val uiType  by rememberPreference(UiTypeKey, UiType.RiMusic)

    var isCreatingANewPlaylist by rememberSaveable {
        mutableStateOf(false)
    }

    if (isCreatingANewPlaylist) {
        InputTextDialog(
            onDismiss = { isCreatingANewPlaylist = false },
            title = stringResource(R.string.enter_the_playlist_name),
            value = "",
            placeholder = stringResource(R.string.enter_the_playlist_name),
            setValue = { text ->
                query {
                    Database.insert(Playlist(name = text))
                }
            }
        )
        /*
        TextFieldDialog(
            hintText = stringResource(R.string.enter_the_playlist_name),
            onDismiss = {
                isCreatingANewPlaylist = false
            },
            onDone = { text ->
                query {
                    Database.insert(Playlist(name = text))
                }
            }
        )
         */
    }

    var sortBy by rememberPreference(playlistSortByKey, PlaylistSortBy.DateAdded)
    var sortOrder by rememberPreference(playlistSortOrderKey, SortOrder.Descending)

    var items by persistList<PlaylistPreview>("home/playlists")

    LaunchedEffect(sortBy, sortOrder) {
        Database.playlistPreviews(sortBy, sortOrder).collect { items = it }
    }
/*
    val sortOrderIconRotation by animateFloatAsState(
        targetValue = if (sortOrder == SortOrder.Ascending) 0f else 180f,
        animationSpec = tween(durationMillis = 400, easing = LinearEasing)
    )
*/
    val thumbnailSizeDp = 108.dp
    val thumbnailSizePx = thumbnailSizeDp.px

    val lazyGridState = rememberLazyGridState()

    val context = LocalContext.current
    val navigationBarPosition by rememberPreference(navigationBarPositionKey, NavigationBarPosition.Bottom)

    val showSearchTab by rememberPreference(showSearchTabKey, false)

    Box(
        modifier = Modifier
            .background(colorPalette.background0)
            //.fillMaxSize()
            .fillMaxHeight()
            .fillMaxWidth(
                if (navigationBarPosition == NavigationBarPosition.Left ||
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
                    title = stringResource(R.string.statistics),
                    iconId = R.drawable.search,
                    enabled = true,
                    showIcon = !showSearchTab,
                    modifier = Modifier,
                    onClick = onSearchClick
                )

            }

            item(key = "today") {
                PlaylistItem(
                    icon = R.drawable.query_stats,
                    colorTint = colorPalette.favoritesIcon,
                    name = stringResource(R.string.today),
                    songCount = null,
                    thumbnailSizeDp = thumbnailSizeDp,
                    alternative = true,
                    modifier = Modifier
                        .clip(thumbnailShape)
                        .clickable(onClick = { onStatisticsType(StatisticsType.Today) })
                        .animateItemPlacement()

                )
            }

            item(key = "oneweek") {
                PlaylistItem(
                    icon = R.drawable.query_stats,
                    colorTint = colorPalette.favoritesIcon,
                    name = stringResource(R.string._1_week),
                    songCount = null,
                    thumbnailSizeDp = thumbnailSizeDp,
                    alternative = true,
                    modifier = Modifier
                        .clip(thumbnailShape)
                        .clickable(onClick = { onStatisticsType(StatisticsType.OneWeek) })
                        .animateItemPlacement()

                )
            }

            item(key = "onemonth") {
                PlaylistItem(
                    icon = R.drawable.query_stats,
                    colorTint = colorPalette.favoritesIcon,
                    name = stringResource(R.string._1_month),
                    songCount = null,
                    thumbnailSizeDp = thumbnailSizeDp,
                    alternative = true,
                    modifier = Modifier
                        .clip(thumbnailShape)
                        .clickable(onClick = { onStatisticsType(StatisticsType.OneMonth) })
                        .animateItemPlacement()
                )
            }

            item(key = "threemonths") {
                PlaylistItem(
                    icon = R.drawable.query_stats,
                    colorTint = colorPalette.favoritesIcon,
                    name = stringResource(R.string._3_month),
                    songCount = null,
                    thumbnailSizeDp = thumbnailSizeDp,
                    alternative = true,
                    modifier = Modifier
                        .clip(thumbnailShape)
                        .clickable(onClick = { onStatisticsType(StatisticsType.ThreeMonths) })
                        .animateItemPlacement()
                )
            }

            item(key = "sixmonths") {
                PlaylistItem(
                    icon = R.drawable.query_stats,
                    colorTint = colorPalette.favoritesIcon,
                    name = stringResource(R.string._6_month),
                    songCount = null,
                    thumbnailSizeDp = thumbnailSizeDp,
                    alternative = true,
                    modifier = Modifier
                        .clip(thumbnailShape)
                        .clickable(onClick = { onStatisticsType(StatisticsType.SixMonths) })
                        .animateItemPlacement()
                )
            }

            item(key = "oneyear") {
                PlaylistItem(
                    icon = R.drawable.query_stats,
                    colorTint = colorPalette.favoritesIcon,
                    name = stringResource(R.string._1_year),
                    songCount = null,
                    thumbnailSizeDp = thumbnailSizeDp,
                    alternative = true,
                    modifier = Modifier
                        .clip(thumbnailShape)
                        .clickable(onClick = { onStatisticsType(StatisticsType.OneYear) })
                        .animateItemPlacement()
                )
            }

            item(key = "all") {
                PlaylistItem(
                    icon = R.drawable.query_stats,
                    colorTint = colorPalette.favoritesIcon,
                    name = stringResource(R.string.all),
                    songCount = null,
                    thumbnailSizeDp = thumbnailSizeDp,
                    alternative = true,
                    modifier = Modifier
                        .clip(thumbnailShape)
                        .clickable(onClick = { onStatisticsType(StatisticsType.All) })
                        .animateItemPlacement()
                )
            }

        }
        if(uiType == UiType.ViMusic)
        FloatingActionsContainerWithScrollToTop(
            lazyGridState = lazyGridState,
            iconId = R.drawable.search,
            onClick = onSearchClick
        )


    }
}
