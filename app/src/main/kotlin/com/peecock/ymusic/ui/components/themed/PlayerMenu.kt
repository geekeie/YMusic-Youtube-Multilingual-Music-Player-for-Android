package com.peecock.ymusic.ui.components.themed

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import com.peecock.innertube.models.NavigationEndpoint
import com.peecock.ymusic.Database
import com.peecock.ymusic.R
import com.peecock.ymusic.enums.MenuStyle
import com.peecock.ymusic.query
import com.peecock.ymusic.service.PlayerService
import com.peecock.ymusic.utils.menuStyleKey
import com.peecock.ymusic.utils.rememberEqualizerLauncher
import com.peecock.ymusic.utils.rememberPreference
import com.peecock.ymusic.utils.seamlessPlay

@ExperimentalTextApi
@ExperimentalAnimationApi
@UnstableApi
@Composable
fun PlayerMenu(
    navController: NavController,
    binder: PlayerService.Binder,
    mediaItem: MediaItem,
    onDismiss: () -> Unit,
    onClosePlayer: () -> Unit,
    ) {

    val menuStyle by rememberPreference(
        menuStyleKey,
        MenuStyle.List
    )

    val context = LocalContext.current

    val launchEqualizer by rememberEqualizerLauncher(audioSessionId = { binder?.player?.audioSessionId })

    val activityResultLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { }

    var isHiding by remember {
        mutableStateOf(false)
    }

    if (isHiding) {
        ConfirmationDialog(
            text = stringResource(R.string.hidesong),
            onDismiss = { isHiding = false },
            onConfirm = {
                onDismiss()
                query {
                    binder.cache.removeResource(mediaItem.mediaId)
                    Database.resetTotalPlayTimeMs(mediaItem.mediaId)
                    /*
                    if (binder.player.hasNextMediaItem()) {
                        binder.player.forceSeekToNext()
                        binder.player.removeMediaItem(binder.player.currentMediaItemIndex - 1)
                    }
                    if (binder.player.hasPreviousMediaItem()) {
                        binder.player.forceSeekToPrevious()
                        binder.player.removeMediaItem(binder.player.currentMediaItemIndex + 1)
                    }
                     */
                }
            }
        )
    }


    if (menuStyle == MenuStyle.Grid) {
        BaseMediaItemGridMenu(
            navController = navController,
            mediaItem = mediaItem,
            onDismiss = onDismiss,
            onStartRadio = {
                binder.stopRadio()
                binder.player.seamlessPlay(mediaItem)
                binder.setupRadio(NavigationEndpoint.Endpoint.Watch(videoId = mediaItem.mediaId))
            },
            onGoToEqualizer = launchEqualizer,
            /*
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
                    SmartMessage(context.resources.getString(R.string.info_not_find_application_audio), type = PopupType.Warning, context = context)
                }
            },
             */
            onHideFromDatabase = { isHiding = true },
            onClosePlayer = onClosePlayer
        )
    } else {
        BaseMediaItemMenu(
            navController = navController,
            mediaItem = mediaItem,
            onStartRadio = {
                binder.stopRadio()
                binder.player.seamlessPlay(mediaItem)
                binder.setupRadio(NavigationEndpoint.Endpoint.Watch(videoId = mediaItem.mediaId))
            },
            onGoToEqualizer = launchEqualizer,
            onShowSleepTimer = {},
            onHideFromDatabase = { isHiding = true },
            onDismiss = onDismiss,
            onClosePlayer = onClosePlayer
        )
    }

}


@ExperimentalTextApi
@ExperimentalAnimationApi
@UnstableApi
@Composable
fun MiniPlayerMenu(
    navController: NavController,
    binder: PlayerService.Binder,
    mediaItem: MediaItem,
    onDismiss: () -> Unit,
    onClosePlayer: () -> Unit,
) {

    val menuStyle by rememberPreference(
        menuStyleKey,
        MenuStyle.List
    )

    if (menuStyle == MenuStyle.Grid) {
        MiniMediaItemGridMenu(
            navController = navController,
            mediaItem = mediaItem,
            onGoToPlaylist = {
                onClosePlayer()
            },
            onDismiss = onDismiss
        )
    } else {
        MiniMediaItemMenu(
            navController = navController,
            mediaItem = mediaItem,
            onGoToPlaylist = {
                onClosePlayer()
            },
            onDismiss = onDismiss
        )
    }

}
