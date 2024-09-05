package com.peecock.ymusic.ui.screens.player

import android.annotation.SuppressLint
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import com.peecock.ymusic.Database
import com.peecock.ymusic.LocalPlayerServiceBinder
import com.peecock.ymusic.enums.ButtonState
import com.peecock.ymusic.enums.PlayerControlsType
import com.peecock.ymusic.enums.PlayerInfoType
import com.peecock.ymusic.enums.PlayerPlayButtonType
import com.peecock.ymusic.enums.PlayerTimelineSize
import com.peecock.ymusic.enums.PlayerTimelineType
import com.peecock.ymusic.enums.PlayerType
import com.peecock.ymusic.models.Info
import com.peecock.ymusic.models.ui.UiMedia
import com.peecock.ymusic.ui.screens.player.components.controls.InfoAlbumAndArtistEssential
import com.peecock.ymusic.ui.screens.player.components.controls.InfoAlbumAndArtistModern
import com.peecock.ymusic.ui.styling.LocalAppearance
import com.peecock.ymusic.utils.GetControls
import com.peecock.ymusic.utils.GetSeekBar
import com.peecock.ymusic.utils.buttonzoomoutKey
import com.peecock.ymusic.utils.disableScrollingTextKey
import com.peecock.ymusic.utils.downloadedStateMedia
import com.peecock.ymusic.utils.effectRotationKey
import com.peecock.ymusic.utils.isCompositionLaunched
import com.peecock.ymusic.utils.playerControlsTypeKey
import com.peecock.ymusic.utils.playerInfoTypeKey
import com.peecock.ymusic.utils.playerPlayButtonTypeKey
import com.peecock.ymusic.utils.playerSwapControlsWithTimelineKey
import com.peecock.ymusic.utils.playerTimelineSizeKey
import com.peecock.ymusic.utils.playerTimelineTypeKey
import com.peecock.ymusic.utils.rememberPreference
import kotlinx.coroutines.flow.distinctUntilChanged
import com.peecock.ymusic.utils.isLandscape
import com.peecock.ymusic.utils.playerTypeKey
import com.peecock.ymusic.utils.showlyricsthumbnailKey
import com.peecock.ymusic.utils.showthumbnailKey
import com.peecock.ymusic.utils.transparentBackgroundPlayerActionBarKey


@OptIn(ExperimentalMaterial3Api::class)
@ExperimentalTextApi
@SuppressLint("SuspiciousIndentation")
@ExperimentalFoundationApi
@ExperimentalAnimationApi
@UnstableApi
@Composable
fun Controls(
    navController: NavController,
    onCollapse: () -> Unit,
    onBlurScaleChange: (Float) -> Unit,
    expandedplayer: Boolean,
    layoutState: PlayerSheetState,
    media: UiMedia,
    mediaId: String,
    title: String?,
    artist: String?,
    artistIds: List<Info>?,
    albumId: String?,
    shouldBePlaying: Boolean,
    position: Long,
    duration: Long,
    modifier: Modifier = Modifier
) {
    val (colorPalette, typography) = LocalAppearance.current


    val binder = LocalPlayerServiceBinder.current
    binder?.player ?: return


    var scrubbingPosition by remember(mediaId) {
        mutableStateOf<Long?>(null)
    }

    //val onGoToArtist = artistRoute::global
    //val onGoToAlbum = albumRoute::global


    var likedAt by rememberSaveable {
        mutableStateOf<Long?>(null)
    }

    /*
    var nextmediaItemIndex = binder.player.nextMediaItemIndex ?: -1
    var nextmediaItemtitle = ""


    if (nextmediaItemIndex.toShort() > -1)
        nextmediaItemtitle = binder.player.getMediaItemAt(nextmediaItemIndex).mediaMetadata.title.toString()
    */


    var effectRotationEnabled by rememberPreference(effectRotationKey, true)
    var disableScrollingText by rememberPreference(disableScrollingTextKey, false)
    var playerTimelineType by rememberPreference(playerTimelineTypeKey, PlayerTimelineType.Default)


    val scope = rememberCoroutineScope()
    val animatedPosition = remember { Animatable(position.toFloat()) }
    var isSeeking by remember { mutableStateOf(false) }


    val compositionLaunched = isCompositionLaunched()
    LaunchedEffect(mediaId) {
        if (compositionLaunched) animatedPosition.animateTo(0f)
    }
    LaunchedEffect(position) {
        if (!isSeeking && !animatedPosition.isRunning)
            animatedPosition.animateTo(
                position.toFloat(), tween(
                    durationMillis = 1000,
                    easing = LinearEasing
                )
            )
    }
    //val durationVisible by remember(isSeeking) { derivedStateOf { isSeeking } }


    LaunchedEffect(mediaId) {
        Database.likedAt(mediaId).distinctUntilChanged().collect { likedAt = it }
    }

    var isDownloaded by rememberSaveable {
        mutableStateOf<Boolean>(false)
    }

    isDownloaded = downloadedStateMedia(mediaId)

    //val menuState = LocalMenuState.current


    var showSelectDialog by remember { mutableStateOf(false) }

    var playerTimelineSize by rememberPreference(
        playerTimelineSizeKey,
        PlayerTimelineSize.Biggest
    )


    /*
    var windows by remember {
        mutableStateOf(binder.player.currentTimeline.windows)
    }
    var queuedSongs by remember {
        mutableStateOf<List<Song>>(emptyList())
    }
    LaunchedEffect(mediaId, windows) {
        Database.getSongsList(
            windows.map {
                it.mediaItem.mediaId
            }
        ).collect{ queuedSongs = it}
    }

    var totalPlayTimes = 0L
    queuedSongs.forEach {
        totalPlayTimes += it.durationText?.let { it1 ->
            durationTextToMillis(it1)
        }?.toLong() ?: 0
    }
     */

    /*
    var showLyrics by rememberSaveable {
        mutableStateOf(false)
    }
     */
    val playerInfoType by rememberPreference(playerInfoTypeKey, PlayerInfoType.Modern)
    var playerSwapControlsWithTimeline by rememberPreference(
        playerSwapControlsWithTimelineKey,
        false
    )
    var showlyricsthumbnail by rememberPreference(showlyricsthumbnailKey, false)
    var transparentBackgroundActionBarPlayer by rememberPreference(
        transparentBackgroundPlayerActionBarKey,
        false
    )
    var playerControlsType by rememberPreference(playerControlsTypeKey, PlayerControlsType.Modern)
    var playerPlayButtonType by rememberPreference(playerPlayButtonTypeKey, PlayerPlayButtonType.Default)
    var showthumbnail by rememberPreference(showthumbnailKey, false)
    var playerType by rememberPreference(playerTypeKey, PlayerType.Essential)
    val expandedlandscape = (isLandscape && playerType == PlayerType.Modern) || (expandedplayer && !showthumbnail)

    Box(
        modifier = Modifier
            .animateContentSize()
    ) {
        if ((!isLandscape) and (expandedplayer && !showlyricsthumbnail))
            Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier
                    .padding(horizontal = playerTimelineSize.size.dp)
            ) {
                if (playerInfoType == PlayerInfoType.Modern)
                    InfoAlbumAndArtistModern(
                        binder = binder,
                        navController = navController,
                        media = media,
                        title = title,
                        albumId = albumId,
                        mediaId = mediaId,
                        likedAt = likedAt,
                        onCollapse = onCollapse,
                        disableScrollingText = disableScrollingText,
                        artist = artist,
                        artistIds = artistIds,
                    )

                if (playerInfoType == PlayerInfoType.Essential)
                    InfoAlbumAndArtistEssential(
                        binder = binder,
                        navController = navController,
                        media = media,
                        title = title,
                        albumId = albumId,
                        mediaId = mediaId,
                        likedAt = likedAt,
                        onCollapse = onCollapse,
                        disableScrollingText = disableScrollingText,
                        artist = artist,
                        artistIds = artistIds,
                    )
                Spacer(
                    modifier = Modifier
                        .height(10.dp)
                )
                GetSeekBar(
                    position = position,
                    duration = duration,
                    media = media,
                    mediaId = mediaId
                )
                Spacer(
                    modifier = Modifier
                        .height(if (playerPlayButtonType != PlayerPlayButtonType.Disabled) 10.dp else 5.dp)
                )
                GetControls(
                    binder = binder,
                    position = position,
                    shouldBePlaying = shouldBePlaying,
                    likedAt = likedAt,
                    mediaId = mediaId,
                    onBlurScaleChange = onBlurScaleChange
                )
                Spacer(
                    modifier = Modifier
                        .height(5.dp)
                )
                if (((playerControlsType == PlayerControlsType.Modern) || (!transparentBackgroundActionBarPlayer)) && (playerPlayButtonType != PlayerPlayButtonType.Disabled)) {
                    Spacer(
                        modifier = Modifier
                            .height(10.dp)
                    )
                }
            }
        else if (!isLandscape)
            Column(
                horizontalAlignment = Alignment.Start,
                modifier = modifier
                    .fillMaxWidth()
                    .padding(horizontal = playerTimelineSize.size.dp)
                    //.fillMaxHeight(0.40f)
            ) {

                if (playerInfoType == PlayerInfoType.Modern)
                    InfoAlbumAndArtistModern(
                        binder = binder,
                        navController = navController,
                        media = media,
                        title = title,
                        albumId = albumId,
                        mediaId = mediaId,
                        likedAt = likedAt,
                        onCollapse = onCollapse,
                        disableScrollingText = disableScrollingText,
                        artist = artist,
                        artistIds = artistIds,
                    )

                if (playerInfoType == PlayerInfoType.Essential)
                    InfoAlbumAndArtistEssential(
                        binder = binder,
                        navController = navController,
                        media = media,
                        title = title,
                        albumId = albumId,
                        mediaId = mediaId,
                        likedAt = likedAt,
                        onCollapse = onCollapse,
                        disableScrollingText = disableScrollingText,
                        artist = artist,
                        artistIds = artistIds,
                    )

                Spacer(
                    modifier = Modifier
                        .height(25.dp)
                )

                if (!playerSwapControlsWithTimeline) {
                    GetSeekBar(
                        position = position,
                        duration = duration,
                        media = media,
                        mediaId = mediaId
                    )
                    Spacer(
                        modifier = Modifier
                            .weight(0.4f)
                    )
                    GetControls(
                        binder = binder,
                        position = position,
                        shouldBePlaying = shouldBePlaying,
                        likedAt = likedAt,
                        mediaId = mediaId,
                        onBlurScaleChange = onBlurScaleChange
                    )
                    Spacer(
                        modifier = Modifier
                            .weight(0.5f)
                    )
                } else {
                    GetControls(
                        binder = binder,
                        position = position,
                        shouldBePlaying = shouldBePlaying,
                        likedAt = likedAt,
                        mediaId = mediaId,
                        onBlurScaleChange = onBlurScaleChange
                    )
                    Spacer(
                        modifier = Modifier
                            .weight(0.5f)
                    )
                    GetSeekBar(
                        position = position,
                        duration = duration,
                        media = media,
                        mediaId = mediaId
                    )
                    Spacer(
                        modifier = Modifier
                            .weight(0.4f)
                    )
                }

            }

    }
    if (isLandscape)
        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Bottom,
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = playerTimelineSize.size.dp)
        ) {

            if (playerInfoType == PlayerInfoType.Modern)
                InfoAlbumAndArtistModern(
                    binder = binder,
                    navController = navController,
                    media = media,
                    title = title,
                    albumId = albumId,
                    mediaId = mediaId,
                    likedAt = likedAt,
                    onCollapse = onCollapse,
                    disableScrollingText = disableScrollingText,
                    artist = artist,
                    artistIds = artistIds,
                )

            if (playerInfoType == PlayerInfoType.Essential)
                InfoAlbumAndArtistEssential(
                    binder = binder,
                    navController = navController,
                    media = media,
                    title = title,
                    albumId = albumId,
                    mediaId = mediaId,
                    likedAt = likedAt,
                    onCollapse = onCollapse,
                    disableScrollingText = disableScrollingText,
                    artist = artist,
                    artistIds = artistIds,
                )

            Spacer(
                modifier = Modifier
                    .height(if (expandedlandscape) 10.dp else 25.dp)
            )

            if (!playerSwapControlsWithTimeline) {
                GetSeekBar(
                    position = position,
                    duration = duration,
                    media = media,
                    mediaId = mediaId
                )
                Spacer(
                    modifier = Modifier
                        .animateContentSize()
                        .conditional(!expandedlandscape) { weight(0.4f) }
                        .conditional(expandedlandscape) { height(15.dp) }
                )
                GetControls(
                    binder = binder,
                    position = position,
                    shouldBePlaying = shouldBePlaying,
                    likedAt = likedAt,
                    mediaId = mediaId,
                    onBlurScaleChange = onBlurScaleChange
                )
                Spacer(
                    modifier = Modifier
                        .animateContentSize()
                        .conditional(!expandedlandscape) { weight(0.5f) }
                        .conditional(expandedlandscape) { height(15.dp) }
                )
            } else {
                GetControls(
                    binder = binder,
                    position = position,
                    shouldBePlaying = shouldBePlaying,
                    likedAt = likedAt,
                    mediaId = mediaId,
                    onBlurScaleChange = onBlurScaleChange
                )
                Spacer(
                    modifier = Modifier
                        .animateContentSize()
                        .conditional(!expandedlandscape) { weight(0.5f) }
                        .conditional(expandedlandscape) { height(15.dp) }
                )
                GetSeekBar(
                    position = position,
                    duration = duration,
                    media = media,
                    mediaId = mediaId
                )
                Spacer(
                    modifier = Modifier
                        .animateContentSize()
                        .conditional(!expandedlandscape) { weight(0.4f) }
                        .conditional(expandedlandscape) { height(15.dp) }
                )
            }
        }
}

fun Modifier.bounceClick() = composed {
    var buttonState by remember { mutableStateOf(ButtonState.Idle) }
    var buttonzoomout by rememberPreference(buttonzoomoutKey,false)
    val scale by animateFloatAsState(if ((buttonState == ButtonState.Pressed) && (buttonzoomout)) 0.8f else 1f)

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .pointerInput(buttonState) {
            awaitPointerEventScope {
                buttonState = if (buttonState == ButtonState.Pressed) {
                    waitForUpOrCancellation()
                    ButtonState.Idle
                } else {
                    awaitFirstDown(false)
                    ButtonState.Pressed
                }
            }
        }
}

fun Modifier.conditional(condition : Boolean, modifier : Modifier.() -> Modifier) : Modifier {
    return if (condition) {
        then(modifier(Modifier))
    } else {
        this
    }
}


/*
@ExperimentalTextApi
@ExperimentalAnimationApi
@UnstableApi
@Composable
private fun PlayerMenu(
    binder: PlayerService.Binder,
    mediaItem: MediaItem,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    val activityResultLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { }

    BaseMediaItemMenu(
        mediaItem = mediaItem,
        onStartRadio = {
            binder.stopRadio()
            binder.player.seamlessPlay(mediaItem)
            binder.setupRadio(NavigationEndpoint.Endpoint.Watch(videoId = mediaItem.mediaId))
        },
        onGoToEqualizer = {
            try {
                activityResultLauncher.launch(
                    Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL).apply {
                        putExtra(AudioEffect.EXTRA_AUDIO_SESSION, binder.player.audioSessionId)
                        putExtra(AudioEffect.EXTRA_PACKAGE_NAME, context.packageName)
                        putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC)
                    }
                )
            } catch (e: ActivityNotFoundException) {
                context.toast("Couldn't find an application to equalize audio")
            }
        },
        onShowSleepTimer = {},
        onDismiss = onDismiss
    )
}

@Composable
private fun Duration(
    position: Float,
    duration: Long,
) {
    val typography = LocalAppearance.current.typography
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        BasicText(
            text = formatAsDuration(position.toLong()),
            style = typography.xxs.semiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        if (duration != C.TIME_UNSET) {
            BasicText(
                text = formatAsDuration(duration),
                style = typography.xxs.semiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
*/