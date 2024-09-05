package com.peecock.ymusic.ui.screens.player

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.media.audiofx.AudioEffect
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.DraggableState
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.neverEqualPolicy
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.paint
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.LinearGradientShader
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.input.pointer.util.addPointerInputChange
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.peecock.ymusic.Database
import com.peecock.ymusic.LocalPlayerServiceBinder
import com.peecock.ymusic.R
import com.peecock.ymusic.enums.BackgroundProgress
import com.peecock.ymusic.enums.ColorPaletteMode
import com.peecock.ymusic.enums.DurationInSeconds
import com.peecock.ymusic.enums.NavRoutes
import com.peecock.ymusic.enums.PlayerBackgroundColors
import com.peecock.ymusic.enums.PlayerThumbnailSize
import com.peecock.ymusic.enums.PopupType
import com.peecock.ymusic.enums.UiType
import com.peecock.ymusic.models.Info
import com.peecock.ymusic.models.Song
import com.peecock.ymusic.models.ui.toUiMedia
import com.peecock.ymusic.query
import com.peecock.ymusic.ui.components.LocalMenuState
import com.peecock.ymusic.ui.components.rememberBottomSheetState
import com.peecock.ymusic.ui.components.themed.CircularSlider
import com.peecock.ymusic.ui.components.themed.ConfirmationDialog
import com.peecock.ymusic.ui.components.themed.DefaultDialog
import com.peecock.ymusic.ui.components.themed.DownloadStateIconButton
import com.peecock.ymusic.ui.components.themed.IconButton
import com.peecock.ymusic.ui.components.themed.MiniPlayerMenu
import com.peecock.ymusic.ui.components.themed.PlayerMenu
import com.peecock.ymusic.ui.components.themed.SecondaryTextButton
import com.peecock.ymusic.ui.components.themed.SmartMessage
import com.peecock.ymusic.ui.components.themed.animateBrushRotation
import com.peecock.ymusic.ui.styling.Dimensions
import com.peecock.ymusic.ui.styling.LocalAppearance
import com.peecock.ymusic.ui.styling.collapsedPlayerProgressBar
import com.peecock.ymusic.ui.styling.dynamicColorPaletteOf
import com.peecock.ymusic.ui.styling.favoritesOverlay
import com.peecock.ymusic.ui.styling.px
import com.peecock.ymusic.utils.BlurTransformation
import com.peecock.ymusic.utils.DisposableListener
import com.peecock.ymusic.utils.UiTypeKey
import com.peecock.ymusic.utils.actionspacedevenlyKey
import com.peecock.ymusic.utils.audioFadeIn
import com.peecock.ymusic.utils.audioFadeOut
import com.peecock.ymusic.utils.backgroundProgressKey
import com.peecock.ymusic.utils.colorPaletteModeKey
import com.peecock.ymusic.utils.currentWindow
import com.peecock.ymusic.utils.disableClosingPlayerSwipingDownKey
import com.peecock.ymusic.utils.disablePlayerHorizontalSwipeKey
import com.peecock.ymusic.utils.downloadedStateMedia
import com.peecock.ymusic.utils.durationTextToMillis
import com.peecock.ymusic.utils.effectRotationKey
import com.peecock.ymusic.utils.expandedplayerKey
import com.peecock.ymusic.utils.expandedplayertoggleKey
import com.peecock.ymusic.utils.forceSeekToNext
import com.peecock.ymusic.utils.formatAsDuration
import com.peecock.ymusic.utils.formatAsTime
import com.peecock.ymusic.utils.getBitmapFromUrl
import com.peecock.ymusic.utils.getDownloadState
import com.peecock.ymusic.utils.isLandscape
import com.peecock.ymusic.utils.manageDownload
import com.peecock.ymusic.utils.playbackFadeDurationKey
import com.peecock.ymusic.utils.playerBackgroundColorsKey
import com.peecock.ymusic.utils.playerThumbnailSizeKey
import com.peecock.ymusic.utils.positionAndDurationState
import com.peecock.ymusic.utils.rememberPreference
import com.peecock.ymusic.utils.semiBold
import com.peecock.ymusic.utils.shouldBePlaying
import com.peecock.ymusic.utils.showButtonPlayerAddToPlaylistKey
import com.peecock.ymusic.utils.showButtonPlayerArrowKey
import com.peecock.ymusic.utils.showButtonPlayerDownloadKey
import com.peecock.ymusic.utils.showButtonPlayerLoopKey
import com.peecock.ymusic.utils.showButtonPlayerLyricsKey
import com.peecock.ymusic.utils.showButtonPlayerMenuKey
import com.peecock.ymusic.utils.showButtonPlayerShuffleKey
import com.peecock.ymusic.utils.showButtonPlayerSleepTimerKey
import com.peecock.ymusic.utils.showButtonPlayerSystemEqualizerKey
import com.peecock.ymusic.utils.showNextSongsInPlayerKey
import com.peecock.ymusic.utils.showTotalTimeQueueKey
import com.peecock.ymusic.utils.shuffleQueue
import com.peecock.ymusic.utils.thumbnail
import com.peecock.ymusic.utils.thumbnailTapEnabledKey
import com.peecock.ymusic.utils.trackLoopEnabledKey
import com.peecock.ymusic.utils.windows
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.RoundingMode
import kotlin.math.absoluteValue
import com.peecock.ymusic.utils.showlyricsthumbnailKey
import com.peecock.ymusic.utils.showthumbnailKey
import com.peecock.ymusic.utils.visualizerEnabledKey


@ExperimentalTextApi
@SuppressLint("SuspiciousIndentation", "RememberReturnType")
@ExperimentalFoundationApi
@ExperimentalAnimationApi
@UnstableApi
@Composable
fun Player(
    navController: NavController,
    layoutState: PlayerSheetState, //BottomSheetState,
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(
        topStart = 12.dp,
        topEnd = 12.dp
    ),
    onDismiss: () -> Unit,
) {
    val menuState = LocalMenuState.current

    //val localSheetState = LocalPlayerSheetState.current
    //println("mediaItem localsheetstate collapsed ${localSheetState.isCollapsed}")

    val uiType by rememberPreference(UiTypeKey, UiType.RiMusic)

    val effectRotationEnabled by rememberPreference(effectRotationKey, true)

    val playerThumbnailSize by rememberPreference(
        playerThumbnailSizeKey,
        PlayerThumbnailSize.Biggest
    )

    var disablePlayerHorizontalSwipe by rememberPreference(disablePlayerHorizontalSwipeKey, false)
    var showlyricsthumbnail by rememberPreference(showlyricsthumbnailKey, false)
    val (colorPalette, typography, thumbnailShape) = LocalAppearance.current

    val binder = LocalPlayerServiceBinder.current

    binder?.player ?: return

    var nullableMediaItem by remember {
        mutableStateOf(binder.player.currentMediaItem, neverEqualPolicy())
    }

    var shouldBePlaying by remember {
        mutableStateOf(binder.player.shouldBePlaying)
    }

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

    val visualizerEnabled by rememberPreference(visualizerEnabledKey, false)

    val playbackFadeDuration by rememberPreference(
        playbackFadeDurationKey,
        DurationInSeconds.Disabled
    )

    val context = LocalContext.current

    binder.player.DisposableListener {
        object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                nullableMediaItem = mediaItem
                //println("mediaItem onMediaItemTransition")
                if (playbackFadeDuration != DurationInSeconds.Disabled) {
                    binder.player.volume = 0f
                    //println("mediaItem volume startFadeIn initial volume ${binder.player.volume}")
                    audioFadeIn(binder.player, playbackFadeDuration.seconds, context)
                }
            }

            override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                shouldBePlaying = binder.player.shouldBePlaying
                //println("mediaItem onPlayWhenReadyChanged $playWhenReady")
                //if (playbackFadeDuration != DurationInSeconds.Disabled) {
                //}
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                shouldBePlaying = binder.player.shouldBePlaying
                //println("mediaItem onPlaybackStateChanged")
            }
        }
    }

    val mediaItem = nullableMediaItem ?: return

    val positionAndDuration by binder.player.positionAndDurationState()
    var timeRemaining by remember { mutableIntStateOf(0) }
    timeRemaining = positionAndDuration.second.toInt() - positionAndDuration.first.toInt()
    //println("mediaItem timeRemaining $timeRemaining")

    if (playbackFadeDuration != DurationInSeconds.Disabled) {
        val songProgressFloat =
            ((positionAndDuration.first.toFloat() * 100) / positionAndDuration.second.absoluteValue)
                .toBigDecimal().setScale(2, RoundingMode.UP).toDouble()
        //val songProgressInt = songProgressFloat.toInt()
        if (songProgressFloat in playbackFadeDuration.fadeOutRange && binder.player.shouldBePlaying) {
            //if (timeRemaining in playbackFadeDuration.fadeOutRange) {
            //println("mediaItem volume startFadeOut $fadeInOut")
            audioFadeOut(binder.player, playbackFadeDuration.seconds, context)
            //fadeInOut = true
            //startFadeOut(binder, playbackFadeDuration.seconds)
            //fade = !fade
        }


        /*
        if (songProgressFloat in playbackFadeDuration.fadeInRange && binder.player.shouldBePlaying) {
            //binder.player.volume = 0f
            println("mediaItem volume startFadeIn")
            audioFadeIn(binder.player, playbackFadeDuration.seconds, context)
            //fadeInOut = false
            //startFadeIn(binder, playbackFadeDuration.seconds)
            //fade = !fade
        }
         */

        //println("mediaItem positionAndDuration $positionAndDuration % ${(positionAndDuration.first.toInt()*100) / positionAndDuration.second.toInt()}")
        //println("mediaItem progress float $songProgressFloat playbackFadeDuration ${playbackFadeDuration} $fadeInOut")
    }


    val windowInsets = WindowInsets.systemBars

    val horizontalBottomPaddingValues = windowInsets
        .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom).asPaddingValues()

    var albumInfo by remember {
        mutableStateOf(mediaItem.mediaMetadata.extras?.getString("albumId")?.let { albumId ->
            Info(albumId, null)
        })
    }

    var artistsInfo by remember {
        mutableStateOf(
            mediaItem.mediaMetadata.extras?.getStringArrayList("artistNames")?.let { artistNames ->
                mediaItem.mediaMetadata.extras?.getStringArrayList("artistIds")?.let { artistIds ->
                    artistNames.zip(artistIds).map { (authorName, authorId) ->
                        Info(authorId, authorName)
                    }
                }
            }
        )
    }

    LaunchedEffect(mediaItem.mediaId) {
        withContext(Dispatchers.IO) {
            //if (albumInfo == null)
            albumInfo = Database.songAlbumInfo(mediaItem.mediaId)
            //if (artistsInfo == null)
            artistsInfo = Database.songArtistInfo(mediaItem.mediaId)
        }
    }


    val ExistIdsExtras =
        mediaItem.mediaMetadata.extras?.getStringArrayList("artistIds")?.size.toString()
    val ExistAlbumIdExtras = mediaItem.mediaMetadata.extras?.getString("albumId")

    var albumId = albumInfo?.id
    if (albumId == null) albumId = ExistAlbumIdExtras
    //var albumTitle = albumInfo?.name

    var artistIds = arrayListOf<String>()
    var artistNames = arrayListOf<String>()


    artistsInfo?.forEach { (id) -> artistIds = arrayListOf(id) }
    if (ExistIdsExtras.equals(0)
            .not()
    ) mediaItem.mediaMetadata.extras?.getStringArrayList("artistIds")?.toCollection(artistIds)

    artistsInfo?.forEach { (name) -> artistNames = arrayListOf(name) }
    if (ExistIdsExtras.equals(0)
            .not()
    ) mediaItem.mediaMetadata.extras?.getStringArrayList("artistNames")?.toCollection(artistNames)



    if (artistsInfo?.isEmpty() == true && ExistIdsExtras.equals(0).not()) {
        artistsInfo = artistNames.let { artistNames ->
            artistIds.let { artistIds ->
                artistNames.zip(artistIds).map {
                    Info(it.second, it.first)
                }
            }
        }
    }


    /*
    //Log.d("mediaItem_pl_mediaId",mediaItem.mediaId)
    Log.d("mediaItem_player","--- START LOG ARTIST ---")
    Log.d("mediaItem_player","ExistIdsExtras: $ExistIdsExtras")
    Log.d("mediaItem_player","metadata artisIds "+mediaItem.mediaMetadata.extras?.getStringArrayList("artistIds").toString())
    Log.d("mediaItem_player","variable artisIds "+artistIds.toString())
    Log.d("mediaItem_player","variable artisNames pre"+artistNames.toString())
    Log.d("mediaItem_player","variable artistsInfo pre "+artistsInfo.toString())

    //Log.d("mediaItem_pl_artinfo",artistsInfo.toString())
    //Log.d("mediaItem_pl_artId",artistIds.toString())
    Log.d("mediaItem_player","--- START LOG ALBUM ---")
    Log.d("mediaItem_player",ExistAlbumIdExtras.toString())
    Log.d("mediaItem_player","metadata albumId "+mediaItem.mediaMetadata.extras?.getString("albumId").toString())
    Log.d("mediaItem_player","metadata extra "+mediaItem.mediaMetadata.extras?.toString())
    Log.d("mediaItem_player","metadata full "+mediaItem.mediaMetadata.toString())
    //Log.d("mediaItem_pl_extrasArt",mediaItem.mediaMetadata.extras?.getStringArrayList("artistNames").toString())
    //Log.d("mediaItem_pl_extras",mediaItem.mediaMetadata.extras.toString())
    Log.d("mediaItem_player","albumInfo "+albumInfo.toString())
    Log.d("mediaItem_player","albumId "+albumId.toString())

    Log.d("mediaItem_pl","--- END LOG ---")

    */


    var trackLoopEnabled by rememberPreference(trackLoopEnabledKey, defaultValue = false)
    var expandedplayer by rememberPreference(expandedplayerKey, false)


    var likedAt by rememberSaveable {
        mutableStateOf<Long?>(null)
    }
    LaunchedEffect(mediaItem.mediaId) {
        Database.likedAt(mediaItem.mediaId).distinctUntilChanged().collect { likedAt = it }
    }


    var downloadState by remember {
        mutableStateOf(Download.STATE_STOPPED)
    }
    downloadState = getDownloadState(mediaItem.mediaId)

//    val isLocal by remember { derivedStateOf { mediaItem.isLocal } }

    var isDownloaded by rememberSaveable { mutableStateOf(false) }
    isDownloaded = downloadedStateMedia(mediaItem.mediaId)

    val showButtonPlayerAddToPlaylist by rememberPreference(showButtonPlayerAddToPlaylistKey, true)
    val showButtonPlayerArrow by rememberPreference(showButtonPlayerArrowKey, false)
    val showButtonPlayerDownload by rememberPreference(showButtonPlayerDownloadKey, true)
    val showButtonPlayerLoop by rememberPreference(showButtonPlayerLoopKey, true)
    val showButtonPlayerLyrics by rememberPreference(showButtonPlayerLyricsKey, true)
    val expandedplayertoggle by rememberPreference(expandedplayertoggleKey, true)
    val showButtonPlayerShuffle by rememberPreference(showButtonPlayerShuffleKey, true)
    val showButtonPlayerSleepTimer by rememberPreference(showButtonPlayerSleepTimerKey, false)
    val showButtonPlayerMenu by rememberPreference(showButtonPlayerMenuKey, false)
    val showButtonPlayerSystemEqualizer by rememberPreference(
        showButtonPlayerSystemEqualizerKey,
        false
    )
    val disableClosingPlayerSwipingDown by rememberPreference(
        disableClosingPlayerSwipingDownKey,
        true
    )
    val showTotalTimeQueue by rememberPreference(showTotalTimeQueueKey, true)
    val backgroundProgress by rememberPreference(
        backgroundProgressKey,
        BackgroundProgress.MiniPlayer
    )
    /*
    val playlistPreviews by remember {
        Database.playlistPreviews(PlaylistSortBy.Name, SortOrder.Ascending)
    }.collectAsState(initial = emptyList(), context = Dispatchers.IO)


    var showPlaylistSelectDialog by remember {
        mutableStateOf(false)
    }
     */

    var isShowingSleepTimerDialog by remember {
        mutableStateOf(false)
    }

    val sleepTimerMillisLeft by (binder?.sleepTimerMillisLeft
        ?: flowOf(null))
        .collectAsState(initial = null)

    var showCircularSlider by remember {
        mutableStateOf(false)
    }
    var actionspacedevenly by rememberPreference(actionspacedevenlyKey, false)

    if (isShowingSleepTimerDialog) {
        if (sleepTimerMillisLeft != null) {
            ConfirmationDialog(
                text = stringResource(R.string.stop_sleep_timer),
                cancelText = stringResource(R.string.no),
                confirmText = stringResource(R.string.stop),
                onDismiss = { isShowingSleepTimerDialog = false },
                onConfirm = {
                    binder.cancelSleepTimer()
                    //onDismiss()
                }
            )
        } else {
            DefaultDialog(
                onDismiss = { isShowingSleepTimerDialog = false }
            ) {
                var amount by remember {
                    mutableStateOf(1)
                }

                BasicText(
                    text = stringResource(R.string.set_sleep_timer),
                    style = typography.s.semiBold,
                    modifier = Modifier
                        .padding(vertical = 8.dp, horizontal = 24.dp)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(
                        space = 16.dp,
                        alignment = Alignment.CenterHorizontally
                    ),
                    modifier = Modifier
                        .padding(vertical = 10.dp)
                ) {
                    if (!showCircularSlider) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .alpha(if (amount <= 1) 0.5f else 1f)
                                .clip(CircleShape)
                                .clickable(enabled = amount > 1) { amount-- }
                                .size(48.dp)
                                .background(colorPalette.background0)
                        ) {
                            BasicText(
                                text = "-",
                                style = typography.xs.semiBold
                            )
                        }

                        Box(contentAlignment = Alignment.Center) {
                            BasicText(
                                text = stringResource(
                                    R.string.left,
                                    formatAsDuration(amount * 5 * 60 * 1000L)
                                ),
                                style = typography.s.semiBold,
                                modifier = Modifier
                                    .clickable {
                                        showCircularSlider = !showCircularSlider
                                    }
                            )
                        }

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .alpha(if (amount >= 60) 0.5f else 1f)
                                .clip(CircleShape)
                                .clickable(enabled = amount < 60) { amount++ }
                                .size(48.dp)
                                .background(colorPalette.background0)
                        ) {
                            BasicText(
                                text = "+",
                                style = typography.xs.semiBold
                            )
                        }

                    } else {
                        CircularSlider(
                            stroke = 40f,
                            thumbColor = colorPalette.accent,
                            text = formatAsDuration(amount * 5 * 60 * 1000L),
                            modifier = Modifier
                                .size(300.dp),
                            onChange = {
                                amount = (it * 120).toInt()
                            }
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier
                        .padding(bottom = 20.dp)
                        .fillMaxWidth()
                ) {
                    SecondaryTextButton(
                        text = stringResource(R.string.set_to) + " "
                                + formatAsDuration(timeRemaining.toLong())
                                + " " + stringResource(R.string.end_of_song),
                        onClick = {
                            binder?.startSleepTimer(timeRemaining.toLong())
                            isShowingSleepTimerDialog = false
                        }
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {

                    IconButton(
                        onClick = { showCircularSlider = !showCircularSlider },
                        icon = R.drawable.time,
                        color = colorPalette.text
                    )
                    IconButton(
                        onClick = { isShowingSleepTimerDialog = false },
                        icon = R.drawable.close,
                        color = colorPalette.text
                    )
                    IconButton(
                        enabled = amount > 0,
                        onClick = {
                            binder?.startSleepTimer(amount * 5 * 60 * 1000L)
                            isShowingSleepTimerDialog = false
                        },
                        icon = R.drawable.checkmark,
                        color = colorPalette.accent
                    )
                }
            }
        }
    }

    var position by remember {
        mutableIntStateOf(0)
    }

    var dynamicColorPalette by remember { mutableStateOf(colorPalette) }
    val colorPaletteMode by rememberPreference(colorPaletteModeKey, ColorPaletteMode.Dark)
    val playerBackgroundColors by rememberPreference(
        playerBackgroundColorsKey,
        PlayerBackgroundColors.BlurredCoverColor
    )
    val isGradientBackgroundEnabled =
        playerBackgroundColors == PlayerBackgroundColors.ThemeColorGradient ||
                playerBackgroundColors == PlayerBackgroundColors.CoverColorGradient ||
                playerBackgroundColors == PlayerBackgroundColors.FluidThemeColorGradient ||
                playerBackgroundColors == PlayerBackgroundColors.FluidCoverColorGradient

    if (playerBackgroundColors == PlayerBackgroundColors.CoverColorGradient ||
        playerBackgroundColors == PlayerBackgroundColors.CoverColor ||
        playerBackgroundColors == PlayerBackgroundColors.FluidCoverColorGradient
    ) {
        val isSystemDarkMode = isSystemInDarkTheme()
        LaunchedEffect(mediaItem.mediaId) {
            dynamicColorPalette = dynamicColorPaletteOf(
                getBitmapFromUrl(
                    context,
                    binder.player.currentWindow?.mediaItem?.mediaMetadata?.artworkUri.toString()
                ),
                isSystemDarkMode,
                colorPaletteMode == ColorPaletteMode.PitchBlack
            ) ?: colorPalette
        }
    }

    /*  */
    var sizeShader by remember { mutableStateOf(Size.Zero) }

    val shaderA = LinearGradientShader(
        Offset(sizeShader.width / 2f, 0f),
        Offset(sizeShader.width / 2f, sizeShader.height),
        listOf(
            dynamicColorPalette.background2,
            colorPalette.background2,
        ),
        listOf(0f, 1f)
    )

    val shaderB = LinearGradientShader(
        Offset(sizeShader.width / 2f, 0f),
        Offset(sizeShader.width / 2f, sizeShader.height),
        listOf(
            colorPalette.background1,
            dynamicColorPalette.accent,
        ),
        listOf(0f, 1f)
    )

    val shaderMask = LinearGradientShader(
        Offset(sizeShader.width / 2f, 0f),
        Offset(sizeShader.width / 2f, sizeShader.height),
        listOf(
            //Color.White,
            colorPalette.background2,
            Color.Transparent,
        ),
        listOf(0f, 1f)
    )

    val brushA by animateBrushRotation(shaderA, sizeShader, 20_000, true)
    val brushB by animateBrushRotation(shaderB, sizeShader, 12_000, false)
    val brushMask by animateBrushRotation(shaderMask, sizeShader, 15_000, true)
    /*  */

    val (thumbnailSizeDp, thumbnailSizePx) = Dimensions.thumbnails.player.song.let {
        it to (it - 64.dp).px
    }

    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(LocalContext.current)
            .data(
                mediaItem.mediaMetadata.artworkUri.thumbnail(
                    thumbnailSizePx
                )
            )
            .size(coil.size.Size.ORIGINAL)
            .transformations(
                listOf(
                    BlurTransformation(
                        scale = 0.5f,
                        radius = 25
                    )
                )
            )
            .build()
    )

    //val imageState = painter.state

    /*
    OnGlobalRoute {
        layoutState.collapseSoft()
    }
     */

    //val onGoToHome = homeRoute::global

    PlayerSheet(
        state = layoutState,
        modifier = modifier,
        onCollapse = onDismiss,
        disableDismiss = disableClosingPlayerSwipingDown,
        onDismiss = {
            //if (disableClosingPlayerSwipingDown) {
            //val playerBottomSheetMinHeight = 70.dp
            //layoutState.snapTo(playerBottomSheetMinHeight)
            //} else {
            binder.stopRadio()
            binder.player.clearMediaItems()
            //}
        },
        collapsedContent = {
            /*
            var deltaX by remember { mutableStateOf(0f) }
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top,
                modifier = Modifier
                    .background(colorPalette.background1)
                    //.clip(shape)
                    .fillMaxSize()
                    //.padding(horizontalBottomPaddingValues) //change bottom navigation
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
                        /*
                        drawLine(
                            color = colorPalette.textDisabled,
                            start = Offset(x = 0f, y = 1.dp.toPx()),
                            end = Offset(x = size.maxDimension, y = 1.dp.toPx()),
                            strokeWidth = 2.dp.toPx()
                        )

                        val progress =
                            positionAndDuration.first.toFloat() / positionAndDuration.second.absoluteValue

                        drawLine(
                            color = colorPalette.collapsedPlayerProgressBar,
                            start = Offset(x = 0f, y = 1.dp.toPx()),
                            end = Offset(x = size.width * progress, y = 1.dp.toPx()),
                            strokeWidth = 60.dp.toPx()
                        )

                        drawLine(
                            color = colorPalette.collapsedPlayerProgressBar,
                            start = Offset(x = 0f, y = 1.dp.toPx()),
                            end = Offset(x = size.width * progress, y = 1.dp.toPx()),
                            strokeWidth = 2.dp.toPx()
                        )
                        drawCircle(
                            color = colorPalette.collapsedPlayerProgressBar,
                            radius = 3.dp.toPx(),
                            center = Offset(x = size.width * progress, y = 1.dp.toPx())
                        )
                         */
                    }
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onHorizontalDrag = { change, dragAmount ->
                                deltaX = dragAmount
                            },
                            onDragStart = {
                                //Log.d("mediaItemGesture","ondragStart offset ${it}")
                            },
                            onDragEnd = {
                                if (!disablePlayerHorizontalSwipe) {
                                    if (deltaX > 0) {
                                        binder.player.seekToPrevious()
                                    } else {
                                        binder.player.forceSeekToNext()
                                    }

                                }

                            }

                        )
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
                    /* minimized player */
                    BasicText(
                        text = mediaItem.mediaMetadata.title?.toString() ?: "",
                        style = typography.xxs.semiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )

                    BasicText(
                        text = mediaItem.mediaMetadata.artist?.toString() ?: "",
                        style = typography.xxs.semiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
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
                    IconButton(
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
                                    binder.player.pause()
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

                    IconButton(
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
                }

                Spacer(
                    modifier = Modifier
                        .width(2.dp)
                )
            }
            */
        }
    ) {


        val windows by remember {
            mutableStateOf(binder.player.currentTimeline.windows)
        }

        var queuedSongs by remember {
            mutableStateOf<List<Song>>(emptyList())
        }

        LaunchedEffect(Unit, mediaItem.mediaId, windows) {
            Database.getSongsList(
                windows.map {
                    it.mediaItem.mediaId
                }
            ).collect { queuedSongs = it }
        }

        var totalPlayTimes = 0L
        queuedSongs.forEach {
            totalPlayTimes += it.durationText?.let { it1 ->
                durationTextToMillis(it1)
            }?.toLong() ?: 0
        }


        var isShowingLyrics by rememberSaveable {
            mutableStateOf(false)
        }

        var isShowingStatsForNerds by rememberSaveable {
            mutableStateOf(false)
        }

        var isShowingEqualizer by remember {
            mutableStateOf(false)
        }

        val thumbnailTapEnabled by rememberPreference(thumbnailTapEnabledKey, false)
        val showNextSongsInPlayer by rememberPreference(showNextSongsInPlayerKey, false)

        val playerBottomHeight = if (showNextSongsInPlayer) 80.dp else 50.dp
        //val playerBottomHeight = 0.dp
        /*
        val playerBottomSheetState = rememberBottomSheetState(
            playerBottomHeight + horizontalBottomPaddingValues.calculateBottomPadding(),
            layoutState.expandedBound
        )
         */
        val playerSheetState = rememberPlayerSheetState(
            playerBottomHeight + horizontalBottomPaddingValues.calculateBottomPadding(),
            layoutState.expandedBound
        )
        val queueSheetState = rememberBottomSheetState(
            playerBottomHeight + horizontalBottomPaddingValues.calculateBottomPadding(),
            layoutState.expandedBound
        )

        val lyricsBottomSheetState = rememberBottomSheetState(
            0.dp + horizontalBottomPaddingValues.calculateBottomPadding(),
            layoutState.expandedBound
        )


        var containerModifier = Modifier
            /*
            .padding(
                windowInsets
                    .only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
                    .asPaddingValues()
            )
            */
            .padding(bottom = playerSheetState.collapsedBound)

        if (!isGradientBackgroundEnabled) {
            if (playerBackgroundColors == PlayerBackgroundColors.BlurredCoverColor) {
                containerModifier = containerModifier
                    .background(dynamicColorPalette.background1)
                    .paint(
                        painter = painter,
                        contentScale = ContentScale.Crop,
                        sizeToIntrinsics = false
                    )
            } else {
                containerModifier = containerModifier
                    .background(
                        dynamicColorPalette.background1
                        //colorPalette.background1
                    )
            }
        } else {
            when (playerBackgroundColors) {
                PlayerBackgroundColors.FluidThemeColorGradient,
                PlayerBackgroundColors.FluidCoverColorGradient -> {
                    containerModifier = containerModifier
                        .onSizeChanged {
                            sizeShader = Size(it.width.toFloat(), it.height.toFloat())
                        }
                        .drawBehind {
                            drawRect(brush = brushA)
                            drawRect(brush = brushMask, blendMode = BlendMode.DstOut)
                            drawRect(brush = brushB, blendMode = BlendMode.DstAtop)
                        }
                }

                else -> {
                    containerModifier = containerModifier
                        .background(
                            Brush.verticalGradient(
                                0.5f to dynamicColorPalette.background2,
                                1.0f to colorPalette.background2,
                                //0.0f to colorPalette.background0,
                                //1.0f to colorPalette.background2,
                                startY = 0.0f,
                                endY = 1500.0f
                            )
                        )

                }
            }

        }
        var showthumbnail by rememberPreference(showthumbnailKey, false)
        val thumbnailContent: @Composable (
            //modifier: Modifier
        ) -> Unit = { //modifier -> KOTLIN 2
            var deltaX by remember { mutableStateOf(0f) }
            //var direction by remember { mutableIntStateOf(-1)}
            Thumbnail(
                thumbnailTapEnabledKey = thumbnailTapEnabled,
                isShowingLyrics = isShowingLyrics,
                onShowLyrics = { isShowingLyrics = it },
                isShowingStatsForNerds = isShowingStatsForNerds,
                onShowStatsForNerds = { isShowingStatsForNerds = it },
                isShowingVisualizer = isShowingEqualizer,
                onShowEqualizer = { isShowingEqualizer = it },
                showthumbnail = showthumbnail,
                onMaximize = { lyricsBottomSheetState.expandSoft() },
                onDoubleTap = {
                    val currentMediaItem = binder.player.currentMediaItem
                    query {
                        if (Database.like(
                                mediaItem.mediaId,
                                if (likedAt == null) System.currentTimeMillis() else null
                            ) == 0
                        ) {
                            currentMediaItem
                                ?.takeIf { it.mediaId == mediaItem.mediaId }
                                ?.let {
                                    Database.insert(currentMediaItem, Song::toggleLike)
                                }
                        }
                    }
                    if (effectRotationEnabled) isRotated = !isRotated
                },
                modifier = modifier
                    .nestedScroll(layoutState.preUpPostDownNestedScrollConnection)
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onHorizontalDrag = { change, dragAmount ->
                                deltaX = dragAmount
                            },
                            onDragStart = {
                                //Log.d("mediaItemGesture","ondragStart offset ${it}")
                            },
                            onDragEnd = {
                                if (!disablePlayerHorizontalSwipe) {
                                    if (deltaX > 0) {
                                        binder.player.seekToPreviousMediaItem()
                                        //binder.player.forceSeekToPrevious()
                                        //Log.d("mediaItem","Swipe to LEFT")
                                    } else {
                                        binder.player.forceSeekToNext()
                                        //Log.d("mediaItem","Swipe to RIGHT")
                                    }

                                }

                            }

                        )
                    }

                /* **** */
                /*
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDrag = { change, dragAmount ->
                            change.consume()
                            val (x, y) = dragAmount
                            if(abs(x) > abs(y)){
                                when {
                                    x > 0 -> {
                                        //right
                                        direction = 0
                                    }
                                    x < 0 -> {
                                        // left
                                        direction = 1
                                    }
                                }
                            } else {
                                when {
                                    y > 0 -> {
                                        // down
                                        direction = 2
                                    }
                                    y < 0 -> {
                                        // up
                                        direction = 3
                                    }
                                }
                            }

                        },
                        onDragEnd = {
                            when (direction) {
                                0 -> {
                                    //right swipe code here
                                    if (!disablePlayerHorizontalSwipe)
                                        binder.player.seekToPreviousMediaItem()
                                    //Log.d("mediaItem","Swipe to RIGHT")
                                }
                                1 -> {
                                    // left swipe code here
                                    if (!disablePlayerHorizontalSwipe)
                                        binder.player.forceSeekToNext()
                                    //Log.d("mediaItem","Swipe to LEFT")
                                }

                                2 -> {
                                    // down swipe code here
                                    layoutState.collapseSoft()
                                    //Log.d("mediaItem","Swipe to DOWN")
                                }
                                3 -> {
                                    // up swipe code here
                                    playerBottomSheetState.expandSoft()
                                    //Log.d("mediaItem","Swipe to UP")
                                }

                            }

                        }
                    )
                }
            */
                /* **** */
            )
        }


        val controlsContent: @Composable (
            //modifier: Modifier KOTLIN 2
        ) -> Unit = { //modifier ->
            Controls(
                navController = navController,
                onCollapse = onDismiss,
                expandedplayer = expandedplayer,
                layoutState = layoutState,
                media = mediaItem.toUiMedia(positionAndDuration.second),
                mediaId = mediaItem.mediaId,
                title = mediaItem.mediaMetadata.title?.toString(),
                artist = mediaItem.mediaMetadata.artist?.toString(),
                artistIds = artistsInfo,
                albumId = albumId,
                shouldBePlaying = shouldBePlaying,
                position = positionAndDuration.first,
                duration = positionAndDuration.second,
                modifier = modifier,
                onBlurScaleChange = {}
            )
        }

        if (isLandscape) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = containerModifier
                    .padding(top = 20.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(0.66f)
                        .padding(bottom = 10.dp)
                ) {

                    thumbnailContent(
                        /*
                        modifier = Modifier
                            .padding(horizontal = 10.dp)

                         */
                    )
                }

                controlsContent(
                    /*
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .fillMaxHeight()
                        .weight(1f)

                     */
                )
            }
        } else {
            /*
            var offsetX by remember { mutableStateOf(0f) }
            var deltaX by remember { mutableStateOf(0f) }
            var direction by remember { mutableStateOf(-1)}
             */

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = containerModifier
                    //.padding(top = 10.dp)
                    .drawBehind {
                        if (backgroundProgress == BackgroundProgress.Both || backgroundProgress == BackgroundProgress.Player) {
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

                /*
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { change, dragAmount ->
                            deltaX = dragAmount
                        },
                        onDragStart = {
                            //Log.d("mediaItemGesture","ondragStart offset ${it}")
                        },
                        onDragEnd = {
                            if (!disablePlayerHorizontalSwipe) {
                                if (deltaX > 0) {
                                    binder.player.seekToPreviousMediaItem()
                                    //binder.player.forceSeekToPrevious()
                                    Log.d("mediaItem","Swipe to LEFT")
                                }
                                else {
                                    binder.player.forceSeekToNext()
                                    Log.d("mediaItem","Swipe to RIGHT")
                                }

                            }

                        }

                    )
                }
                 */

            ) {


                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .padding(
                            windowInsets
                                .only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
                                .asPaddingValues()
                        )
                        .fillMaxWidth(0.9f)
                        .height(30.dp)
                ) {

                    if (uiType != UiType.ViMusic) {
                        Image(
                            painter = painterResource(R.drawable.chevron_down),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(colorPalette.collapsedPlayerProgressBar),
                            modifier = Modifier
                                .clickable {
                                    onDismiss()
                                    layoutState.collapseSoft()
                                }
                                .rotate(rotationAngle)
                                //.padding(10.dp)
                                .size(24.dp)

                        )
                        /*
                        IconButton(
                            icon = R.drawable.chevron_down,
                            color = colorPalette.text,
                            enabled = true,
                            onClick = {
                                layoutState.collapseSoft()
                            },
                            modifier = Modifier
                                //.padding(horizontal = 15.dp)
                                .size(24.dp)
                        )
                         */


                        Image(
                            painter = painterResource(R.drawable.app_icon),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(colorPalette.collapsedPlayerProgressBar),
                            modifier = Modifier
                                .clickable {
                                    //onGoToHome()
                                    navController.navigate(NavRoutes.home.name)
                                    onDismiss()
                                    layoutState.collapseSoft()
                                }
                                .rotate(rotationAngle)
                                //.padding(10.dp)
                                .size(24.dp)

                        )

                        if (!showButtonPlayerMenu)
                            Image(
                                painter = painterResource(R.drawable.ellipsis_vertical),
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(colorPalette.collapsedPlayerProgressBar),
                                modifier = Modifier
                                    .clickable {
                                        menuState.display {
                                            PlayerMenu(
                                                navController = navController,
                                                onDismiss = menuState::hide,
                                                mediaItem = mediaItem,
                                                binder = binder,
                                                onClosePlayer = {
                                                    onDismiss()
                                                    layoutState.collapseSoft()
                                                }
                                            )
                                        }
                                    }
                                    .rotate(rotationAngle)
                                    //.padding(10.dp)
                                    .size(24.dp)

                            )

                    }
                }

                Spacer(
                    modifier = Modifier
                        .height(5.dp)
                )

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        //.weight(0.5f)
                        .fillMaxHeight(0.55f)
                ) {
                    thumbnailContent(
                        /*
                        modifier = Modifier
                            .clip(thumbnailShape)
                            .padding(
                                horizontal = playerThumbnailSize.size.dp,
                                vertical = 4.dp,
                            )

                         */
                    )
                }


                if (showTotalTimeQueue)
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                    ) {
                        Image(
                            painter = painterResource(R.drawable.time),
                            colorFilter = ColorFilter.tint(colorPalette.accent),
                            modifier = Modifier
                                .size(20.dp)
                                .padding(horizontal = 5.dp),
                            contentDescription = "Background Image",
                            contentScale = ContentScale.Fit
                        )
                        BasicText(
                            text = " ${formatAsTime(totalPlayTimes)}",
                            style = typography.xxs.semiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }


                Spacer(
                    modifier = Modifier
                        .height(10.dp)
                )



                controlsContent(
                    /*
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .fillMaxWidth()

                     */
                )
            }
        }


        Queue(
            navController = navController,
            layoutState = queueSheetState,
            content = {

                val context = LocalContext.current

                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxHeight()


                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = if (actionspacedevenly) Arrangement.SpaceEvenly else Arrangement.SpaceBetween, //Arrangement.SpaceEvenly,
                        modifier = Modifier
                            //.align(Alignment.Center)
                            //.padding(horizontal = 12.dp)
                            //.fillMaxWidth()
                            .fillMaxSize()
                            //.fillMaxHeight(0.5f)
                            .drawBehind {
                                if (backgroundProgress == BackgroundProgress.Both || backgroundProgress == BackgroundProgress.Player) {
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
                        //if (uiType != UiType.ViMusic) {

                        if (showButtonPlayerDownload)
                            DownloadStateIconButton(
                                icon = if (isDownloaded) R.drawable.downloaded else R.drawable.download,
                                color = if (isDownloaded) colorPalette.text else colorPalette.textDisabled,
                                downloadState = downloadState,
                                onClick = {
                                    //if (!isLocal)
                                    manageDownload(
                                        context = context,
                                        songId = mediaItem.mediaId,
                                        songTitle = mediaItem.mediaMetadata.title.toString(),
                                        downloadState = isDownloaded
                                    )
                                },
                                modifier = Modifier
                                    .padding(start = 12.dp)
                                    .size(24.dp)
                            )

                        /*
                        if (showPlaylistSelectDialog) {
                            SelectorDialog(
                                title = stringResource(R.string.playlists),
                                onDismiss = { showPlaylistSelectDialog = false },
                                showItemsIcon = true,
                                values = playlistPreviews.map {
                                    Info(
                                        it.playlist.id.toString(),
                                        "${it.playlist.name} \n ${it.songCount} ${stringResource(R.string.songs)}"
                                    )
                                },
                                onValueSelected = {
                                    //Log.d("mediaItem", it)
                                    var position = 0
                                    query {
                                        position =
                                            Database.getSongMaxPositionToPlaylist(it.toLong())
                                    }
                                    if (position > 0) position++
                                    //Log.d("mediaItemMaxPos", position.toString())
                                    transaction {
                                        Database.insert(mediaItem)
                                        Database.insert(
                                            SongPlaylistMap(
                                                songId = mediaItem.mediaId,
                                                playlistId = it.toLong(),
                                                position = position
                                            )
                                        )
                                    }
                                    //Log.d("mediaItemPos", "add position $position")
                                    showPlaylistSelectDialog = false
                                }
                            )
                        }
                        */

                        if (showButtonPlayerAddToPlaylist)
                            IconButton(
                                icon = R.drawable.add_in_playlist,
                                color = colorPalette.text,
                                onClick = {
                                    menuState.display {
                                        MiniPlayerMenu(
                                            navController = navController,
                                            onDismiss = menuState::hide,
                                            mediaItem = mediaItem,
                                            binder = binder,
                                            onClosePlayer = {
                                                onDismiss()
                                                layoutState.collapseSoft()
                                            }
                                        )
                                        /*
                                        PlaylistsItemMenu(
                                            navController = navController,
                                            modifier = Modifier.fillMaxHeight(0.6f),
                                            onDismiss = menuState::hide,
                                            onAddToPlaylist = { playlistPreview ->
                                                position =
                                                    playlistPreview.songCount.minus(1) ?: 0
                                                if (position > 0) position++ else position = 0
                                                transaction {
                                                    Database.insert(mediaItem)
                                                    Database.insert(
                                                        SongPlaylistMap(
                                                            songId = mediaItem.mediaId,
                                                            playlistId = playlistPreview.playlist.id,
                                                            position = position + 1
                                                        )
                                                    )
                                                }
                                            },
                                            onGoToPlaylist = {
                                                navController.navigate("${NavRoutes.localPlaylist.name}/$it")
                                                layoutState.collapseSoft()
                                            }
                                        )
                                        */
                                    }
                                },
                                modifier = Modifier
                                    //.padding(horizontal = 4.dp)
                                    .size(24.dp)
                            )



                        if (showButtonPlayerLoop)
                            IconButton(
                                icon = R.drawable.repeat,
                                color = if (trackLoopEnabled) colorPalette.text else colorPalette.textDisabled,
                                onClick = {
                                    trackLoopEnabled = !trackLoopEnabled
                                    if (effectRotationEnabled) isRotated = !isRotated
                                },
                                modifier = Modifier
                                    //.padding(horizontal = 4.dp)
                                    .size(24.dp)
                            )

                        if (showButtonPlayerShuffle)
                            IconButton(
                                icon = R.drawable.shuffle,
                                color = colorPalette.text,
                                enabled = true,
                                onClick = {
                                    binder?.player?.shuffleQueue()
                                    binder.player.forceSeekToNext()
                                },
                                modifier = Modifier
                                    .size(24.dp),
                            )

                        if (showButtonPlayerLyrics)
                            IconButton(
                                icon = R.drawable.song_lyrics,
                                color = if (isShowingLyrics) colorPalette.text else colorPalette.textDisabled,
                                enabled = true,
                                onClick = {
                                    if (isShowingEqualizer) isShowingEqualizer = !isShowingEqualizer
                                    isShowingLyrics = !isShowingLyrics
                                },
                                modifier = Modifier
                                    .size(24.dp),
                            )

                        if (expandedplayertoggle)
                            IconButton(
                                icon = R.drawable.minmax,
                                color = if (expandedplayer) colorPalette.text else colorPalette.textDisabled,
                                enabled = true,
                                onClick = {
                                    expandedplayer = !expandedplayer
                                },
                                modifier = Modifier
                                    .size(20.dp),
                            )


                        if (visualizerEnabled)
                            IconButton(
                                icon = R.drawable.sound_effect,
                                color = if (isShowingEqualizer) colorPalette.text else colorPalette.textDisabled,
                                enabled = true,
                                onClick = {
                                    if (isShowingLyrics) isShowingLyrics = !isShowingLyrics
                                    isShowingEqualizer = !isShowingEqualizer
                                },
                                modifier = Modifier
                                    .size(24.dp)
                            )


                        if (showButtonPlayerSleepTimer)
                            IconButton(
                                icon = R.drawable.sleep,
                                color = if (sleepTimerMillisLeft != null) colorPalette.text else colorPalette.textDisabled,
                                enabled = true,
                                onClick = {
                                    isShowingSleepTimerDialog = true
                                },
                                modifier = Modifier
                                    .size(24.dp),
                            )

                        if (showButtonPlayerSystemEqualizer) {
                            val activityResultLauncher =
                                rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { }

                            IconButton(
                                icon = R.drawable.equalizer,
                                color = colorPalette.text,
                                enabled = true,
                                onClick = {
                                    try {
                                        activityResultLauncher.launch(
                                            Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL).apply {
                                                putExtra(
                                                    AudioEffect.EXTRA_AUDIO_SESSION,
                                                    binder.player.audioSessionId
                                                )
                                                putExtra(
                                                    AudioEffect.EXTRA_PACKAGE_NAME,
                                                    context.packageName
                                                )
                                                putExtra(
                                                    AudioEffect.EXTRA_CONTENT_TYPE,
                                                    AudioEffect.CONTENT_TYPE_MUSIC
                                                )
                                            }
                                        )
                                    } catch (e: ActivityNotFoundException) {
                                        SmartMessage(
                                            context.resources.getString(R.string.info_not_find_application_audio),
                                            type = PopupType.Warning, context = context
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .size(20.dp),
                            )
                        }

                        if (showButtonPlayerArrow)
                            IconButton(
                                icon = R.drawable.chevron_up,
                                color = colorPalette.text,
                                enabled = true,
                                onClick = {
                                    queueSheetState.expandSoft()
                                },
                                modifier = Modifier
                                    .padding(end = 12.dp)
                                    .size(24.dp),
                            )

                        if (showButtonPlayerMenu && !isLandscape)
                            IconButton(
                                icon = R.drawable.ellipsis_vertical,
                                color = colorPalette.text,
                                onClick = {
                                    menuState.display {
                                        PlayerMenu(
                                            navController = navController,
                                            onDismiss = menuState::hide,
                                            mediaItem = mediaItem,
                                            binder = binder,
                                            onClosePlayer = {
                                                onDismiss()
                                                layoutState.collapseSoft()
                                            }

                                        )
                                    }
                                },
                                modifier = Modifier
                                    .padding(end = 12.dp)
                                    .size(24.dp)
                            )


                        if (isLandscape) {
                            IconButton(
                                icon = R.drawable.ellipsis_horizontal,
                                color = colorPalette.text,
                                onClick = {
                                    menuState.display {
                                        PlayerMenu(
                                            navController = navController,
                                            onDismiss = menuState::hide,
                                            mediaItem = mediaItem,
                                            binder = binder,
                                            onClosePlayer = {
                                                onDismiss()
                                                layoutState.collapseSoft()
                                            }
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .padding(end = 12.dp)
                                    .size(24.dp)
                            )
                        }
                    }

                    if (showNextSongsInPlayer) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .padding(horizontal = 12.dp)
                                .fillMaxWidth()
                        ) {
                            val nextMediaItemIndex = binder.player.nextMediaItemIndex
                            val nextMediaItem = if (binder.player.hasNextMediaItem())
                                binder.player.getMediaItemAt(binder.player.nextMediaItemIndex)
                            else MediaItem.EMPTY
                            val nextNextMediaItem = try {
                                binder.player.getMediaItemAt(nextMediaItemIndex + 1)
                            } catch (e: Exception) {
                                MediaItem.EMPTY
                            }

                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .height(Dimensions.collapsedPlayer)
                            ) {
                                AsyncImage(
                                    model = nextMediaItem.mediaMetadata.artworkUri.thumbnail(
                                        Dimensions.thumbnails.song.px / 2
                                    ),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .padding(end = 5.dp)
                                        .clip(thumbnailShape)
                                        .size(30.dp)
                                )
                            }
                            Column(
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .height(Dimensions.collapsedPlayer)
                                    .weight(1f)
                            ) {

                                BasicText(
                                    text = nextMediaItem.mediaMetadata.title?.toString() ?: "",
                                    style = typography.xxxs.semiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )

                                BasicText(
                                    text = nextMediaItem.mediaMetadata.artist?.toString() ?: "",
                                    style = typography.xxxs.semiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }

                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .height(Dimensions.collapsedPlayer)
                            ) {
                                AsyncImage(
                                    model = nextNextMediaItem.mediaMetadata.artworkUri.thumbnail(
                                        Dimensions.thumbnails.song.px / 2
                                    ),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .padding(end = 5.dp)
                                        .clip(thumbnailShape)
                                        .size(30.dp)
                                )
                            }
                            Column(
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .height(Dimensions.collapsedPlayer)
                                    .weight(1f)
                            ) {

                                BasicText(
                                    text = nextNextMediaItem.mediaMetadata.title?.toString() ?: "",
                                    style = typography.xxxs.semiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )

                                BasicText(
                                    text = nextNextMediaItem.mediaMetadata.artist?.toString() ?: "",
                                    style = typography.xxxs.semiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                }
            },
            backgroundColorProvider = { colorPalette.background2 },
            modifier = Modifier
                .align(Alignment.BottomCenter),
            shape = shape
        )


        FullLyricsSheet(
            layoutState = lyricsBottomSheetState,
            content = {},
            backgroundColorProvider = { colorPalette.background2 },
            onMaximize = { lyricsBottomSheetState.collapseSoft() },
            onRefresh = {
                lyricsBottomSheetState.collapse(tween(50))
                lyricsBottomSheetState.expand(tween(50))
            }
        )

    }

}


@UnstableApi
@Composable
fun PlayerSheet(
    state: PlayerSheetState,
    disableVerticalDrag: Boolean? = false,
    disableDismiss: Boolean? = false,
    modifier: Modifier = Modifier,
    onDismiss: (() -> Unit)? = null,
    onCollapse: (() -> Unit)? = null,
    collapsedContent: @Composable BoxScope.() -> Unit,
    content: @Composable BoxScope.() -> Unit,
) {
    val density = LocalDensity.current
    val windowsInsets = WindowInsets.systemBars
    val bottomDp = with(density) { windowsInsets.getBottom(density).toDp() }
    Box(
        modifier = modifier
            //change bottom navigation
            .offset {
                val y =
                    (state.expandedBound - state.value)
                        //(state.expandedBound - state.value - (Dimensions.collapsedPlayer + bottomDp)) //bottom navigation
                        .roundToPx()
                        .coerceAtLeast(0)
                IntOffset(x = 0, y = y)
            }

            .pointerInput(state) {
                val velocityTracker = VelocityTracker()

                detectVerticalDragGestures(
                    onVerticalDrag = { change, dragAmount ->
                        if (disableVerticalDrag == false) {
                            velocityTracker.addPointerInputChange(change)
                            //if (disableDismiss == false)
                            state.dispatchRawDelta(dragAmount)
                        }
                    },
                    onDragCancel = {
                        velocityTracker.resetTracking()
                        state.snapTo(state.collapsedBound)
                    },
                    onDragEnd = {
                        if (disableVerticalDrag == false) {
                            val velocity = -velocityTracker.calculateVelocity().y
                            velocityTracker.resetTracking()
                            state.performFling(
                                velocity,
                                if (disableDismiss == false) onDismiss else null
                            )
                            onCollapse?.invoke()
                        }
                    }
                )
            }
            .fillMaxSize()
    ) {
        if (!state.isCollapsed) {
            BackHandler(onBack = state::collapseSoft)
            content()
        }

        if (!state.isExpanded && (onDismiss == null || !state.isDismissed)) {
            Box(
                modifier = Modifier
                    .graphicsLayer {
                        alpha = 1f - (state.progress * 16).coerceAtMost(1f)
                    }
                    .clickable(onClick = {
                        if (disableVerticalDrag == false) state.expandSoft()
                    })
                    .fillMaxWidth()
                    .height(state.collapsedBound),
                content = collapsedContent
            )
        }
    }
}

@Stable
class PlayerSheetState(
    draggableState: DraggableState,
    private val coroutineScope: CoroutineScope,
    private val animatable: Animatable<Dp, AnimationVector1D>,
    private val onAnchorChanged: (Int) -> Unit,
    val collapsedBound: Dp,
) : DraggableState by draggableState {
    val dismissedBound: Dp
        get() = animatable.lowerBound!!

    val expandedBound: Dp
        get() = animatable.upperBound!!

    val value by animatable.asState()

    val isDismissed by derivedStateOf {
        value == animatable.lowerBound!!
    }

    val isCollapsed by derivedStateOf {
        value == collapsedBound
    }

    val isExpanded by derivedStateOf {
        value == animatable.upperBound
    }

    val progress by derivedStateOf {
        1f - (animatable.upperBound!! - animatable.value) / (animatable.upperBound!! - collapsedBound)
    }

    fun collapse(animationSpec: AnimationSpec<Dp>) {
        onAnchorChanged(collapsedAnchor)
        coroutineScope.launch {
            animatable.animateTo(collapsedBound, animationSpec)
        }
    }

    fun expand(animationSpec: AnimationSpec<Dp>) {
        onAnchorChanged(expandedAnchor)
        coroutineScope.launch {
            animatable.animateTo(animatable.upperBound!!, animationSpec)
        }
    }

    private fun collapse() {
        collapse(SpringSpec())
    }

    private fun expand() {
        expand(SpringSpec())
    }

    fun collapseSoft() {
        collapse(tween(300))
    }

    fun expandSoft() {
        expand(tween(300))
    }

    fun dismiss() {
        onAnchorChanged(dismissedAnchor)
        coroutineScope.launch {
            animatable.animateTo(animatable.lowerBound!!)
        }
    }

    fun snapTo(value: Dp) {
        coroutineScope.launch {
            animatable.snapTo(value)
        }
    }

    fun performFling(velocity: Float, onDismiss: (() -> Unit)?) {
        if (velocity > 250) {
            expand()
        } else if (velocity < -250) {
            if (value < collapsedBound && onDismiss != null) {
                dismiss()
                onDismiss.invoke()
            } else {
                collapse()
            }
        } else {
            val l0 = dismissedBound
            val l1 = (collapsedBound - dismissedBound) / 2
            val l2 = (expandedBound - collapsedBound) / 2
            val l3 = expandedBound

            when (value) {
                in l0..l1 -> {
                    if (onDismiss != null) {
                        dismiss()
                        onDismiss.invoke()
                    } else {
                        collapse()
                    }
                }

                in l1..l2 -> collapse()
                in l2..l3 -> expand()
                else -> Unit
            }
        }
    }

    val preUpPostDownNestedScrollConnection
        get() = object : NestedScrollConnection {
            var isTopReached = false

            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (isExpanded && available.y < 0) {
                    isTopReached = false
                }

                return if (isTopReached && available.y < 0 && source == NestedScrollSource.Drag) {
                    dispatchRawDelta(available.y)
                    available
                } else {
                    Offset.Zero
                }
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                if (!isTopReached) {
                    isTopReached = consumed.y == 0f && available.y > 0
                }

                return if (isTopReached && source == NestedScrollSource.Drag) {
                    dispatchRawDelta(available.y)
                    available
                } else {
                    Offset.Zero
                }
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                return if (isTopReached) {
                    val velocity = -available.y
                    performFling(velocity, null)

                    available
                } else {
                    Velocity.Zero
                }
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                isTopReached = false
                return Velocity.Zero
            }
        }
}

const val expandedAnchor = 2
const val collapsedAnchor = 1
const val dismissedAnchor = 0

@Composable
fun rememberPlayerSheetState(
    dismissedBound: Dp,
    expandedBound: Dp,
    collapsedBound: Dp = dismissedBound,
    initialAnchor: Int = dismissedAnchor
): PlayerSheetState {
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()

    var previousAnchor by rememberSaveable {
        mutableStateOf(initialAnchor)
    }

    return remember(dismissedBound, expandedBound, collapsedBound, coroutineScope) {
        val initialValue = when (previousAnchor) {
            expandedAnchor -> expandedBound
            collapsedAnchor -> collapsedBound
            dismissedAnchor -> dismissedBound
            else -> error("Unknown PlayerSheet anchor")
        }

        val animatable = Animatable(initialValue, Dp.VectorConverter).also {
            it.updateBounds(dismissedBound.coerceAtMost(expandedBound), expandedBound)
        }

        PlayerSheetState(
            draggableState = DraggableState { delta ->
                coroutineScope.launch {
                    animatable.snapTo(animatable.value - with(density) { delta.toDp() })
                }
            },
            onAnchorChanged = { previousAnchor = it },
            coroutineScope = coroutineScope,
            animatable = animatable,
            collapsedBound = collapsedBound
        )
    }
}

