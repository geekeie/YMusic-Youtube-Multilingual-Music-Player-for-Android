package com.peecock.ymusic.ui.screens.player

import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import coil.compose.AsyncImage
import com.peecock.ymusic.Database
import com.peecock.ymusic.LocalPlayerServiceBinder
import com.peecock.ymusic.R
import com.peecock.ymusic.enums.ClickLyricsText
import com.peecock.ymusic.enums.PopupType
import com.peecock.ymusic.enums.ThumbnailType
import com.peecock.ymusic.service.LoginRequiredException
import com.peecock.ymusic.service.NoInternetException
import com.peecock.ymusic.service.PlayableFormatNonSupported
import com.peecock.ymusic.service.PlayableFormatNotFoundException
import com.peecock.ymusic.service.TimeoutException
import com.peecock.ymusic.service.UnknownException
import com.peecock.ymusic.service.UnplayableException
import com.peecock.ymusic.service.VideoIdMismatchException
import com.peecock.ymusic.service.isLocal
import com.peecock.ymusic.ui.components.themed.SmartMessage
import com.peecock.ymusic.ui.styling.Dimensions
import com.peecock.ymusic.ui.styling.LocalAppearance
import com.peecock.ymusic.ui.styling.px
import com.peecock.ymusic.utils.DisposableListener
import com.peecock.ymusic.utils.clickLyricsTextKey
import com.peecock.ymusic.utils.currentWindow
import com.peecock.ymusic.utils.doubleShadowDrop
import com.peecock.ymusic.utils.isLandscape
import com.peecock.ymusic.utils.rememberPreference
import com.peecock.ymusic.utils.resize
import com.peecock.ymusic.utils.showlyricsthumbnailKey
import com.peecock.ymusic.utils.showvisthumbnailKey
import com.peecock.ymusic.utils.thumbnailTypeKey
import com.peecock.ymusic.utils.thumbnailpauseKey
import timber.log.Timber
import java.net.UnknownHostException
import java.nio.channels.UnresolvedAddressException

@ExperimentalAnimationApi
@UnstableApi
@Composable
fun Thumbnail(
    thumbnailTapEnabledKey: Boolean,
    isShowingLyrics: Boolean,
    onShowLyrics: (Boolean) -> Unit,
    isShowingStatsForNerds: Boolean,
    onShowStatsForNerds: (Boolean) -> Unit,
    isShowingVisualizer: Boolean,
    onShowEqualizer: (Boolean) -> Unit,
    onMaximize: () -> Unit,
    onDoubleTap: () -> Unit,
    showthumbnail: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val binder = LocalPlayerServiceBinder.current
    val player = binder?.player ?: return

    val (thumbnailSizeDp, thumbnailSizePx) = Dimensions.thumbnails.player.song.let {
        it to (it - 64.dp).px
    }

    var showlyricsthumbnail by rememberPreference(showlyricsthumbnailKey, false)
    var nullableWindow by remember {
        mutableStateOf(player.currentWindow)
    }

    var error by remember {
        mutableStateOf<PlaybackException?>(player.playerError)
    }

    val localMusicFileNotFoundError = stringResource(R.string.error_local_music_not_found)
    val networkerror = stringResource(R.string.error_a_network_error_has_occurred)
    val notfindplayableaudioformaterror =
        stringResource(R.string.error_couldn_t_find_a_playable_audio_format)
    val originalvideodeletederror =
        stringResource(R.string.error_the_original_video_source_of_this_song_has_been_deleted)
    val songnotplayabledueserverrestrictionerror =
        stringResource(R.string.error_this_song_cannot_be_played_due_to_server_restrictions)
    val videoidmismatcherror =
        stringResource(R.string.error_the_returned_video_id_doesn_t_match_the_requested_one)
    val unknownplaybackerror =
        stringResource(R.string.error_an_unknown_playback_error_has_occurred)

    val unknownerror = stringResource(R.string.error_unknown)
    val nointerneterror = stringResource(R.string.error_no_internet)
    val timeouterror = stringResource(R.string.error_timeout)

    val formatUnsupported = stringResource(R.string.error_file_unsupported_format)

    var artImageAvailable by remember {
        mutableStateOf(true)
    }

    val clickLyricsText by rememberPreference(clickLyricsTextKey, ClickLyricsText.FullScreen)
    var showvisthumbnail by rememberPreference(showvisthumbnailKey, false)
    //var expandedlyrics by rememberPreference(expandedlyricsKey,false)

    player.DisposableListener {
        object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                nullableWindow = player.currentWindow
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                error = player.playerError
            }

            override fun onPlayerError(playbackException: PlaybackException) {
                error = playbackException
                binder.stopRadio()
                //context.stopService(context.intent<PlayerService>())
                //context.stopService(context.intent<MyDownloadService>())
            }
        }
    }

    val window = nullableWindow ?: return

    AnimatedContent(
        targetState = window,
        transitionSpec = {
            val duration = 500
            val slideDirection = if (targetState.firstPeriodIndex > initialState.firstPeriodIndex)
                AnimatedContentTransitionScope.SlideDirection.Left
            else AnimatedContentTransitionScope.SlideDirection.Right

            ContentTransform(
                targetContentEnter = slideIntoContainer(
                    towards = slideDirection,
                    animationSpec = tween(duration)
                ) + fadeIn(
                    animationSpec = tween(duration)
                ) + scaleIn(
                    initialScale = 0.85f,
                    animationSpec = tween(duration)
                ),
                initialContentExit = slideOutOfContainer(
                    towards = slideDirection,
                    animationSpec = tween(duration)
                ) + fadeOut(
                    animationSpec = tween(duration)
                ) + scaleOut(
                    targetScale = 0.85f,
                    animationSpec = tween(duration)
                ),
                sizeTransform = SizeTransform(clip = false)
            )
        },
        contentAlignment = Alignment.Center, label = ""
    ) { currentWindow ->

        val thumbnailType by rememberPreference(thumbnailTypeKey, ThumbnailType.Modern)
        /*
        val playerControlsType by rememberPreference(
            playerControlsTypeKey,
            PlayerControlsType.Modern
        )
         */
        var modifierUiType by remember { mutableStateOf(modifier) }

        if (showthumbnail)
            if ((!isShowingLyrics && !isShowingVisualizer) || (isShowingVisualizer && showvisthumbnail) || (isShowingLyrics && showlyricsthumbnail))
                if (thumbnailType == ThumbnailType.Modern)
                    modifierUiType = modifier
                        .padding(vertical = 8.dp)
                        .aspectRatio(1f)
                        //.size(thumbnailSizeDp)
                        .fillMaxSize()
                        //.dropShadow(LocalAppearance.current.thumbnailShape, LocalAppearance.current.colorPalette.overlay.copy(0.1f), 6.dp, 2.dp, 2.dp)
                        //.dropShadow(LocalAppearance.current.thumbnailShape, LocalAppearance.current.colorPalette.overlay.copy(0.1f), 6.dp, (-2).dp, (-2).dp)
                        .doubleShadowDrop(LocalAppearance.current.thumbnailShape, 4.dp, 8.dp)
                        .clip(LocalAppearance.current.thumbnailShape)
                //.padding(14.dp)
                else modifierUiType = modifier
                    .aspectRatio(1f)
                    //.size(thumbnailSizeDp)
                    //.padding(14.dp)
                    .fillMaxSize()
                    .clip(LocalAppearance.current.thumbnailShape)



        Box(
            modifier = modifierUiType
        ) {
            if (showthumbnail) {
                if ((!isShowingLyrics && !isShowingVisualizer) || (isShowingVisualizer && showvisthumbnail) || (isShowingLyrics && showlyricsthumbnail))
                    if (artImageAvailable)
                        AsyncImage(
                            model = currentWindow.mediaItem.mediaMetadata.artworkUri.toString()
                                .resize(1200, 1200),
                            /*
                            model = currentWindow.mediaItem.mediaMetadata.artworkUri.thumbnail(
                                thumbnailSizePx
                            ),
                             */
                            /*
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(currentWindow.mediaItem.mediaMetadata.artworkUri.toString().resize(1200, 1200))
                                .size(Size.ORIGINAL)
                                .scale(Scale.FIT)
                                .build(),
                             */
                            onSuccess = {
                                artImageAvailable = true
                            },
                            onError = {
                                artImageAvailable = false
                            },
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onLongPress = { onShowStatsForNerds(true) },
                                        onTap = if (thumbnailTapEnabledKey) {
                                            {
                                                onShowLyrics(true)
                                                onShowEqualizer(false)
                                            }
                                        } else null,
                                        onDoubleTap = { onDoubleTap() }
                                    )

                                }
                                .fillMaxSize()
                                .clip(LocalAppearance.current.thumbnailShape)


                        )

                if (!artImageAvailable)
                    Image(
                        painter = painterResource(R.drawable.app_icon),
                        colorFilter = ColorFilter.tint(LocalAppearance.current.colorPalette.accent),
                        modifier = Modifier
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onLongPress = { onShowStatsForNerds(true) },
                                    onTap = if (thumbnailTapEnabledKey) {
                                        {
                                            onShowLyrics(true)
                                            onShowEqualizer(false)
                                        }
                                    } else null,
                                    onDoubleTap = { onDoubleTap() }
                                )

                            }
                            .fillMaxSize()
                            .clip(LocalAppearance.current.thumbnailShape),
                        contentDescription = "Background Image",
                        contentScale = ContentScale.Fit
                    )
                //if (!currentWindow.mediaItem.isLocal)
                if (showlyricsthumbnail)
                    Lyrics(
                        mediaId = currentWindow.mediaItem.mediaId,
                        isDisplayed = isShowingLyrics && error == null,
                        onDismiss = {
                            //if (thumbnailTapEnabledKey)
                            onShowLyrics(false)
                        },
                        ensureSongInserted = { Database.insert(currentWindow.mediaItem) },
                        size = thumbnailSizeDp,
                        mediaMetadataProvider = currentWindow.mediaItem::mediaMetadata,
                        durationProvider = player::getDuration,
                        onMaximize = onMaximize,
                        isLandscape = isLandscape,
                        enableClick = when (clickLyricsText) {
                            ClickLyricsText.Player, ClickLyricsText.Both -> true
                            else -> false
                        }
                    )

                StatsForNerds(
                    mediaId = currentWindow.mediaItem.mediaId,
                    isDisplayed = isShowingStatsForNerds && error == null,
                    onDismiss = { onShowStatsForNerds(false) }
                )
                if (showvisthumbnail) {
                    NextVisualizer(
                        isDisplayed = isShowingVisualizer
                    )
                }

                var errorCounter by remember { mutableIntStateOf(0) }

                if (error != null) {
                    errorCounter = errorCounter.plus(1)
                    if (errorCounter < 3) {
                        Timber.e("Playback error: ${error?.cause?.cause}")
                        SmartMessage(
                            if (currentWindow.mediaItem.isLocal) localMusicFileNotFoundError
                            else when (error?.cause?.cause) {
                                is UnresolvedAddressException, is UnknownHostException -> networkerror
                                is PlayableFormatNotFoundException -> notfindplayableaudioformaterror
                                is UnplayableException -> originalvideodeletederror
                                is LoginRequiredException -> songnotplayabledueserverrestrictionerror
                                is VideoIdMismatchException -> videoidmismatcherror
                                is PlayableFormatNonSupported -> formatUnsupported
                                is NoInternetException -> nointerneterror
                                is TimeoutException -> timeouterror
                                is UnknownException -> unknownerror
                                else -> unknownplaybackerror
                            }, PopupType.Error, context = context
                        )
                    //    player.seekToNext()
                    } else errorCounter = 0
                }
            }
            /*
            PlaybackError(
                isDisplayed = error != null,
                messageProvider = {
                    if (currentWindow.mediaItem.isLocal) localMusicFileNotFoundError
                    else when (error?.cause?.cause) {
                        is UnresolvedAddressException, is UnknownHostException -> networkerror
                        is PlayableFormatNotFoundException -> notfindplayableaudioformaterror
                        is UnplayableException -> originalvideodeletederror
                        is LoginRequiredException -> songnotplayabledueserverrestrictionerror
                        is VideoIdMismatchException -> videoidmismatcherror
                        is PlayableFormatNonSupported -> formatUnsupported
                        else -> unknownplaybackerror
                    }
                },
                onDismiss = {
                    //player::prepare
                    //player.stop()
                    player.seekToNext()
                }
            )
             */
        }
    }
}

@OptIn(UnstableApi::class)
fun Modifier.thumbnailpause(
    shouldBePlaying: Boolean
) = composed {
    var thumbnailpause by rememberPreference(thumbnailpauseKey, false)
    val scale by animateFloatAsState(if ((thumbnailpause) && (!shouldBePlaying)) 0.9f else 1f)

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }

}
