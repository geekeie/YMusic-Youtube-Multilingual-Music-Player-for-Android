package com.peecock.ymusic.ui.screens.player


import android.annotation.SuppressLint
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import coil.compose.AsyncImage
import com.peecock.ymusic.LocalPlayerServiceBinder
import com.peecock.ymusic.R
import com.peecock.ymusic.enums.ClickLyricsText
import com.peecock.ymusic.enums.ColorPaletteName
import com.peecock.ymusic.ui.components.BottomSheet
import com.peecock.ymusic.ui.components.BottomSheetState
import com.peecock.ymusic.ui.components.themed.IconButton
import com.peecock.ymusic.ui.styling.Dimensions
import com.peecock.ymusic.ui.styling.LocalAppearance
import com.peecock.ymusic.ui.styling.collapsedPlayerProgressBar
import com.peecock.ymusic.ui.styling.px
import com.peecock.ymusic.utils.DisposableListener
import com.peecock.ymusic.utils.clickLyricsTextKey
import com.peecock.ymusic.utils.colorPaletteNameKey
import com.peecock.ymusic.utils.forceSeekToNext
import com.peecock.ymusic.utils.forceSeekToPrevious
import com.peecock.ymusic.utils.isLandscape
import com.peecock.ymusic.utils.medium
import com.peecock.ymusic.utils.rememberPreference
import com.peecock.ymusic.utils.secondary
import com.peecock.ymusic.utils.shouldBePlaying
import com.peecock.ymusic.utils.thumbnail

@ExperimentalTextApi
@SuppressLint("SuspiciousIndentation")
@ExperimentalFoundationApi
@ExperimentalAnimationApi
@androidx.media3.common.util.UnstableApi
@Composable
fun FullLyricsSheet(
    //mediaId: String,
    backgroundColorProvider: () -> Color,
    layoutState: BottomSheetState,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
    onMaximize: () -> Unit,
    onRefresh: () -> Unit
) {
    val (colorPalette, typography, thumbnailShape) = LocalAppearance.current
    val (thumbnailSizeDp) = Dimensions.thumbnails.player.song.let {
        it to (it - 64.dp).px
    }

    val binder = LocalPlayerServiceBinder.current
    var shouldBePlaying by remember {
        mutableStateOf(false)
    }
    shouldBePlaying = binder?.player?.shouldBePlaying == true
    val shouldBePlayingTransition = updateTransition(shouldBePlaying, label = "shouldBePlaying")
    val playPauseRoundness by shouldBePlayingTransition.animateDp(
        transitionSpec = { tween(durationMillis = 100, easing = LinearEasing) },
        label = "playPauseRoundness",
        targetValueByState = { if (it == true) 24.dp else 12.dp }
    )

    binder?.player?.DisposableListener {
        object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {}

            override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                shouldBePlaying = binder.player.shouldBePlaying
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                shouldBePlaying = binder.player.shouldBePlaying
            }
        }
    }


    val size = thumbnailSizeDp
    var mediaId by remember {
        mutableStateOf(binder?.player?.currentMediaItem?.mediaId ?: "")
    }

    val colorPaletteName by rememberPreference(colorPaletteNameKey, ColorPaletteName.Dynamic)
    val clickLyricsText by rememberPreference(clickLyricsTextKey, ClickLyricsText.FullScreen)

    BottomSheet(
        state = layoutState,
        modifier = modifier,
        collapsedContent = {}
    ) {
        val binder = LocalPlayerServiceBinder.current
        binder?.player ?: return@BottomSheet
        val player = binder.player

        Column(
            verticalArrangement = Arrangement.Top,
            modifier = Modifier
                .background(Color.Black.copy(0.9f))
                .fillMaxHeight()
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .padding(vertical = 30.dp, horizontal = 4.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .height(Dimensions.collapsedPlayer * 2)
                        .aspectRatio(1f)
                ) {
                    AsyncImage(
                        model = player.currentMediaItem?.mediaMetadata?.artworkUri.thumbnail(
                            Dimensions.thumbnails.song.px
                        ),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .padding(all = 10.dp)
                            .clip(thumbnailShape)
                            .size(Dimensions.collapsedPlayer * 2)
                    )
                }


                Column(
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.SpaceAround,
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                ) {
                    BasicText(
                        text = player.currentMediaItem?.mediaMetadata?.title?.toString() ?: "",
                        style = typography.m.medium.secondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )

                    BasicText(
                        text = player.currentMediaItem?.mediaMetadata?.artist?.toString() ?: "",
                        style = typography.s.medium.secondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )


                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
                modifier = Modifier
                    .padding(horizontal = 4.dp)
            ) {


                //if (!player.currentMediaItem?.isLocal!!)
                    player.currentMediaItem?.mediaId?.let {
                        player.currentMediaItem!!::mediaMetadata.let { it1 ->
                            Lyrics(
                                enableClick = when (clickLyricsText) {
                                    ClickLyricsText.FullScreen, ClickLyricsText.Both -> true
                                    else -> false
                                },
                                mediaId = it,
                                isDisplayed = true,
                                onDismiss = {},
                                onMaximize = onMaximize,
                                ensureSongInserted = {
                                    /*
                                    query {
                                        Database.insert(player.currentMediaItem!!)
                                    }
                                     */
                                },
                                size = thumbnailSizeDp,
                                mediaMetadataProvider = it1,
                                durationProvider = player::getDuration,
                                isLandscape = isLandscape,
                                trailingContent = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {

                                        IconButton(
                                            icon = R.drawable.play_skip_back,
                                            color = colorPalette.collapsedPlayerProgressBar,
                                            onClick = {
                                                binder.player.forceSeekToPrevious()
                                                onRefresh()
                                            },
                                            modifier = Modifier
                                                .padding(horizontal = 2.dp, vertical = 8.dp)
                                                .size(24.dp)
                                        )

                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(playPauseRoundness))
                                                .clickable {
                                                    //Log.d("mediaItem", "$shouldBePlaying")
                                                    /*
                                                    shouldBePlaying = if (shouldBePlaying == true) {
                                                        binder.player.pause()
                                                        false
                                                    } else {
                                                        if (binder.player.playbackState == androidx.media3.common.Player.STATE_IDLE) {
                                                            binder.player.prepare()
                                                        }
                                                        binder.player.play()
                                                        true
                                                    }
                                                     */
                                                    if (shouldBePlaying)
                                                        binder.player.pause()
                                                    else {
                                                        if (binder.player.playbackState == Player.STATE_IDLE) {
                                                            binder.player.prepare()
                                                        }
                                                        binder.player.play()
                                                    }
                                                }
                                                .background(when (colorPaletteName) {
                                                    ColorPaletteName.PureBlack, ColorPaletteName.ModernBlack -> colorPalette.background4
                                                    else -> colorPalette.background2
                                                })
                                                .size(42.dp)
                                        ) {
                                            Image(
                                                painter = painterResource(if (shouldBePlaying == true) R.drawable.pause else R.drawable.play),
                                                contentDescription = null,
                                                colorFilter = ColorFilter.tint(colorPalette.collapsedPlayerProgressBar),
                                                modifier = Modifier

                                                    .align(Alignment.Center)
                                                    .size(24.dp)
                                            )
                                        }

                                        IconButton(
                                            icon = R.drawable.play_skip_forward,
                                            color = colorPalette.collapsedPlayerProgressBar,
                                            onClick = {
                                                binder.player.forceSeekToNext()
                                                onRefresh()
                                            },
                                            modifier = Modifier
                                                .padding(horizontal = 2.dp, vertical = 8.dp)
                                                .size(24.dp)
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxHeight(0.98f)

                            )
                        }
                    }
            }
        }
    }
}
