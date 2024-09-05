package com.peecock.ymusic.ui.screens.player

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.Icon
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.neverEqualPolicy
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.peecock.ymusic.Database
import com.peecock.ymusic.LocalPlayerServiceBinder
import com.peecock.ymusic.R
import com.peecock.ymusic.enums.BackgroundProgress
import com.peecock.ymusic.enums.NavRoutes
import com.peecock.ymusic.ui.styling.Dimensions
import com.peecock.ymusic.ui.styling.LocalAppearance
import com.peecock.ymusic.ui.styling.favoritesOverlay
import com.peecock.ymusic.ui.styling.px
import com.peecock.ymusic.utils.DisposableListener
import com.peecock.ymusic.utils.backgroundProgressKey
import com.peecock.ymusic.utils.disableClosingPlayerSwipingDownKey
import com.peecock.ymusic.utils.effectRotationKey
import com.peecock.ymusic.utils.forceSeekToNext
import com.peecock.ymusic.utils.forceSeekToPrevious
import com.peecock.ymusic.utils.mediaItemToggleLike
import com.peecock.ymusic.utils.miniPlayerTypeKey
import com.peecock.ymusic.utils.positionAndDurationState
import com.peecock.ymusic.utils.rememberPreference
import com.peecock.ymusic.utils.semiBold
import com.peecock.ymusic.utils.shouldBePlaying
import com.peecock.ymusic.utils.thumbnail
import kotlinx.coroutines.flow.distinctUntilChanged
import com.peecock.ymusic.enums.MiniPlayerType
import com.peecock.ymusic.ui.components.themed.SmartMessage
import com.peecock.ymusic.ui.items.EXPLICIT_PREFIX
import com.peecock.ymusic.ui.styling.favoritesIcon
import com.peecock.ymusic.utils.cleanPrefix
import com.peecock.ymusic.utils.getLikedIcon
import com.peecock.ymusic.utils.getUnlikedIcon

import kotlin.math.absoluteValue

@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalFoundationApi::class,
    ExperimentalFoundationApi::class, ExperimentalFoundationApi::class,
    ExperimentalFoundationApi::class)
@Composable
fun PlayerEssential(
    showPlayer: () -> Unit,
    hidePlayer: () -> Unit,
    navController: NavController? = null
) {
    val binder = LocalPlayerServiceBinder.current
    binder?.player ?: return

    val context = LocalContext.current

    var nullableMediaItem by remember {
        mutableStateOf(
            binder.player.currentMediaItem,
            neverEqualPolicy()
        )
    }
    var shouldBePlaying by remember { mutableStateOf(binder.player.shouldBePlaying) }
    val hapticFeedback = LocalHapticFeedback.current

    var playerError by remember {
        mutableStateOf<PlaybackException?>(binder.player.playerError)
    }

    binder.player.DisposableListener {
        object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                nullableMediaItem = mediaItem
            }

            override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                shouldBePlaying = if (playerError == null) binder.player.shouldBePlaying else false
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                playerError = binder.player.playerError
                shouldBePlaying = if (playerError == null) binder.player.shouldBePlaying else false
            }

            override fun onPlayerError(playbackException: PlaybackException) {
                playerError = playbackException
                binder.stopRadio()
            }
        }
    }

    val mediaItem = nullableMediaItem ?: return

    playerError?.let { PlayerError(error = it) }

    var likedAt by rememberSaveable {
        mutableStateOf<Long?>(null)
    }
    var miniPlayerType by rememberPreference(
        miniPlayerTypeKey,
        MiniPlayerType.Modern
    )
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
                SmartMessage(context.resources.getString(R.string.removed_from_favorites), context = context)
        }
    }

    val positionAndDuration by binder.player.positionAndDurationState()


    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.StartToEnd) if (miniPlayerType == MiniPlayerType.Essential) {updateLike = true;hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)} else {binder.player.seekToPrevious();hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)}
            else if (value == SwipeToDismissBoxValue.EndToStart) {binder.player.seekToNext();hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)}
            return@rememberSwipeToDismissBoxState false
        }
    )
    val (colorPalette, typography, thumbnailShape) = LocalAppearance.current
    val backgroundProgress by rememberPreference(backgroundProgressKey, BackgroundProgress.Both)
    val effectRotationEnabled by rememberPreference(effectRotationKey, true)
    val shouldBePlayingTransition = updateTransition(shouldBePlaying, label = "shouldBePlaying")
    val playPauseRoundness by shouldBePlayingTransition.animateDp(
        transitionSpec = { tween(durationMillis = 100, easing = LinearEasing) },
        label = "playPauseRoundness",
        targetValueByState = { if (it) 24.dp else 12.dp }
    )

    var isRotated by rememberSaveable { mutableStateOf(false) }
    val rotationAngle by animateFloatAsState(
        targetValue = if (isRotated) 360F else 0f,
        animationSpec = tween(durationMillis = 200), label = ""
    )
    val disableClosingPlayerSwipingDown by rememberPreference(disableClosingPlayerSwipingDownKey, true)

    SwipeToDismissBox(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(12.dp)),
        state = dismissState,
        backgroundContent = {
            /*
            val color by animateColorAsState(
                targetValue = when (dismissState.targetValue) {
                    SwipeToDismissBoxValue.StartToEnd -> MaterialTheme.colorScheme.primaryContainer
                    SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.primaryContainer
                    SwipeToDismissBoxValue.Settled -> Color.Transparent
                },
                label = "background"
            )
             */

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colorPalette.background1)
                    .padding(horizontal = 16.dp),
                horizontalArrangement = when (dismissState.targetValue) {
                    SwipeToDismissBoxValue.StartToEnd -> Arrangement.Start
                    SwipeToDismissBoxValue.EndToStart -> Arrangement.End
                    SwipeToDismissBoxValue.Settled -> Arrangement.Center
                },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = when (dismissState.targetValue) {
                        SwipeToDismissBoxValue.StartToEnd -> {
                            if (miniPlayerType == MiniPlayerType.Modern) ImageVector.vectorResource(R.drawable.play_skip_back) else
                             if (likedAt == null)
                             ImageVector.vectorResource(R.drawable.heart_outline)
                             else ImageVector.vectorResource(R.drawable.heart)
                        }
                        SwipeToDismissBoxValue.EndToStart ->  ImageVector.vectorResource(R.drawable.play_skip_forward)
                        SwipeToDismissBoxValue.Settled ->  ImageVector.vectorResource(R.drawable.play)
                    },
                    contentDescription = null,
                    tint = colorPalette.iconButtonPlayer,
                )
            }
        }
    ) {

        /***** */
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
            modifier = Modifier
                .combinedClickable(
                    onLongClick = {
                        navController?.navigate(NavRoutes.queue.name);
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    },
                    onClick = {
                        //if (showPlayer != null)
                        showPlayer()
                        //else
                        //    navController?.navigate("player")
                    }
                )
                //.clickable(onClick = showPlayer)
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onVerticalDrag = { _, dragAmount ->
                            if (dragAmount < 0) showPlayer()
                            else if (dragAmount > 20) {
                                if (!disableClosingPlayerSwipingDown) {
                                    binder.stopRadio()
                                    binder.player.clearMediaItems()
                                    hidePlayer()
                                } else
                                    SmartMessage(context.resources.getString(R.string.player_swiping_down_is_disabled), context = context)
                            }
                        }
                    )
                }
                .background(colorPalette.background2)
                .fillMaxWidth()
                .drawBehind {
                    if (backgroundProgress == BackgroundProgress.Both || backgroundProgress == BackgroundProgress.MiniPlayer) {
                        drawRect(
                            color = colorPalette.favoritesOverlay,
                            topLeft = Offset.Zero,
                            size = Size(
                                width = positionAndDuration.first.toFloat() /
                                        positionAndDuration.second.absoluteValue * size.width,
                                height = size.maxDimension
                            )
                        )
                    }
                }
        ) {

            Spacer(
                modifier = Modifier
                    .width(2.dp)
            )

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .height(Dimensions.collapsedPlayer)
            ) {
                AsyncImage(
                    model = mediaItem.mediaMetadata.artworkUri.thumbnail(Dimensions.thumbnails.song.px),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .clip(thumbnailShape)
                        .size(48.dp)
                )
            }

            Column(
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .height(Dimensions.collapsedPlayer)
                    .weight(1f)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (mediaItem.mediaMetadata.title?.startsWith(EXPLICIT_PREFIX) == true)
                        com.peecock.ymusic.ui.components.themed.IconButton(
                            icon = R.drawable.explicit,
                            color = colorPalette.text,
                            enabled = true,
                            onClick = {},
                            modifier = Modifier
                                .size(14.dp)
                        )
                    BasicText(
                        text = cleanPrefix(mediaItem.mediaMetadata.title?.toString() ?: ""),
                        style = typography.xxs.semiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .basicMarquee(iterations = Int.MAX_VALUE)
                    )
                }

                BasicText(
                    text = mediaItem.mediaMetadata.artist?.toString() ?: "",
                    style = typography.xxs.semiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .basicMarquee(iterations = Int.MAX_VALUE)
                )
            }

            Spacer(
                modifier = Modifier
                    .width(2.dp)
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .height(Dimensions.collapsedPlayer)
            ) {
               if (miniPlayerType == MiniPlayerType.Essential)
                com.peecock.ymusic.ui.components.themed.IconButton(
                    icon = R.drawable.play_skip_back,
                    color = colorPalette.iconButtonPlayer,
                    onClick = {
                        binder.player.forceSeekToPrevious()
                        if (effectRotationEnabled) isRotated = !isRotated
                    },
                    modifier = Modifier
                        .rotate(rotationAngle)
                        .padding(horizontal = 2.dp, vertical = 8.dp)
                        .size(24.dp)
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(playPauseRoundness))
                        .clickable {
                            if (shouldBePlaying) {
                                binder.callPause({})
                                //binder.player.pause()
                            } else {
                                if (binder.player.playbackState == Player.STATE_IDLE) {
                                    binder.player.prepare()
                                }
                                binder.player.play()
                            }
                            if (effectRotationEnabled) isRotated = !isRotated
                        }
                        .background(colorPalette.background2)
                        .size(42.dp)
                ) {
                    Image(
                        painter = painterResource(if (shouldBePlaying) R.drawable.pause else R.drawable.play),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(colorPalette.iconButtonPlayer),
                        modifier = Modifier
                            .rotate(rotationAngle)
                            .align(Alignment.Center)
                            .size(24.dp)
                    )
                }
               if (miniPlayerType == MiniPlayerType.Essential)
                com.peecock.ymusic.ui.components.themed.IconButton(
                    icon = R.drawable.play_skip_forward,
                    color = colorPalette.iconButtonPlayer,
                    onClick = {
                        binder.player.forceSeekToNext()
                        if (effectRotationEnabled) isRotated = !isRotated
                    },
                    modifier = Modifier
                        .rotate(rotationAngle)
                        .padding(horizontal = 2.dp, vertical = 8.dp)
                        .size(24.dp)
                )
                if (miniPlayerType == MiniPlayerType.Modern)
                 com.peecock.ymusic.ui.components.themed.IconButton(
                     icon = if (likedAt == null) getUnlikedIcon() else getLikedIcon(),
                     color = colorPalette.favoritesIcon,
                     onClick = {
                         updateLike = true
                     },
                     modifier = Modifier
                         .rotate(rotationAngle)
                         .padding(horizontal = 2.dp, vertical = 8.dp)
                         .size(24.dp)
                 )

            }

            Spacer(
                modifier = Modifier
                    .width(2.dp)
            )
        }
        /*****  */

    }
}