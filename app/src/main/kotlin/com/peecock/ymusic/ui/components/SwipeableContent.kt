package com.peecock.ymusic.ui.components

import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.platform.ViewConfiguration
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import com.peecock.innertube.Innertube
import com.peecock.ymusic.Database
import com.peecock.ymusic.R
import com.peecock.ymusic.ui.components.themed.SmartMessage
import com.peecock.ymusic.ui.styling.LocalAppearance
import com.peecock.ymusic.utils.albumItemToggleBookmarked
import com.peecock.ymusic.utils.isSwipeToActionEnabledKey
import com.peecock.ymusic.utils.mediaItemToggleLike
import com.peecock.ymusic.utils.rememberPreference
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun SwipeableContent(
    swipeToLeftIcon: Int,
    swipeToRightIcon: Int,
    onSwipeToLeft: () -> Unit,
    onSwipeToRight: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val (colorPalette) = LocalAppearance.current
    val hapticFeedback = LocalHapticFeedback.current
    val dismissState = rememberSwipeToDismissBoxState(
        positionalThreshold = { distance: Float -> distance * 0.25f },
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.StartToEnd) {onSwipeToRight();hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)}
            else if (value == SwipeToDismissBoxValue.EndToStart) {onSwipeToLeft();hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)}

            return@rememberSwipeToDismissBoxState false
        }
    )
    val isSwipeToActionEnabled by rememberPreference(isSwipeToActionEnabledKey, true)

    val current = LocalViewConfiguration.current
    CompositionLocalProvider(LocalViewConfiguration provides object : ViewConfiguration by current{
        override val touchSlop: Float
            get() = current.touchSlop * 5f
    }) {
        SwipeToDismissBox(
            gesturesEnabled = isSwipeToActionEnabled,
            modifier = modifier,
            //.padding(horizontal = 16.dp)
            //.clip(RoundedCornerShape(12.dp)),
            state = dismissState,
            backgroundContent = {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        //.background(colorPalette.background1)
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = when (dismissState.targetValue) {
                        SwipeToDismissBoxValue.StartToEnd -> Arrangement.Start
                        SwipeToDismissBoxValue.EndToStart -> Arrangement.End
                        SwipeToDismissBoxValue.Settled -> Arrangement.Center
                    },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val icon = when (dismissState.targetValue) {
                        SwipeToDismissBoxValue.StartToEnd -> ImageVector.vectorResource(
                            swipeToRightIcon
                        )

                        SwipeToDismissBoxValue.EndToStart -> ImageVector.vectorResource(
                            swipeToLeftIcon
                        )

                        SwipeToDismissBoxValue.Settled -> null
                    }
                    if (icon != null)
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = colorPalette.accent,
                        )
                }
            }
        ) {
            content()
        }
    }
}

@Composable
fun SwipeableQueueItem(
    mediaItem: MediaItem,
    onSwipeToLeft: () -> Unit,
    onSwipeToRight: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {

    SwipeableContent(
        swipeToLeftIcon = R.drawable.trash,
        swipeToRightIcon = R.drawable.play_skip_forward,
        onSwipeToLeft = onSwipeToLeft,
        onSwipeToRight = onSwipeToRight,
        modifier = modifier
    ) {
        content()
    }

}

@Composable
fun SwipeablePlaylistItem(
    mediaItem: MediaItem,
    onSwipeToRight: () -> Unit,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    var likedAt by rememberSaveable {
        mutableStateOf<Long?>(null)
    }
    LaunchedEffect(mediaItem.mediaId) {
        Database.likedAt(mediaItem.mediaId).distinctUntilChanged().collect { likedAt = it }
    }
    var updateLike by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(updateLike) {
        if (updateLike) {
            mediaItemToggleLike(mediaItem)
            updateLike = false
            if (likedAt == null)
                SmartMessage(context.resources.getString(R.string.added_to_favorites), context = context)
            else
                SmartMessage("\"" + mediaItem.mediaMetadata.title?.toString() + " - " + mediaItem.mediaMetadata.artist?.toString() + "\" " + context.resources.getString(R.string.removed_from_favorites), context = context, durationLong = true)
        }
    }

    SwipeableContent(
        swipeToLeftIcon = if (likedAt == null) R.drawable.heart_outline else R.drawable.heart,
        swipeToRightIcon = R.drawable.play_skip_forward,
        onSwipeToLeft = { updateLike = true },
        onSwipeToRight = onSwipeToRight
    ) {
        content()
    }

}

@OptIn(androidx.media3.common.util.UnstableApi::class)
@Composable
fun SwipeableAlbumItem(
    albumItem: Innertube.AlbumItem,
    onSwipeToLeft: () -> Unit,
    onSwipeToRight: () -> Unit,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    var bookmarkedAt by rememberSaveable {
        mutableStateOf<Long?>(null)
    }
    LaunchedEffect(albumItem.key) {
        Database.albumBookmarkedAt(albumItem.key).distinctUntilChanged().collect { bookmarkedAt = it }
    }
    var updateLike by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(updateLike) {
        if (updateLike) {
            albumItemToggleBookmarked(albumItem)
            updateLike = false
            if (bookmarkedAt == null)
                SmartMessage(context.resources.getString(R.string.added_to_favorites), context = context)
            else
                SmartMessage("\"" + albumItem.info?.name + " - " + albumItem.authors?.joinToString("") { it.name + "\" " + context.resources.getString(R.string.removed_from_favorites)}, context = context, durationLong = true)
        }
    }

    SwipeableContent(
        swipeToLeftIcon = R.drawable.play_skip_forward,
        swipeToRightIcon = if (bookmarkedAt == null) R.drawable.bookmark_outline else R.drawable.bookmark,
        onSwipeToLeft = onSwipeToLeft,
        onSwipeToRight = onSwipeToRight
    ) {
        content()
    }

}