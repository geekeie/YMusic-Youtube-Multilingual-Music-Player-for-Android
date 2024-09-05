package com.peecock.ymusic.utils

import androidx.annotation.OptIn
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.media3.common.util.UnstableApi
import com.peecock.ymusic.enums.PlayerControlsType
import com.peecock.ymusic.enums.PlayerPlayButtonType
import com.peecock.ymusic.service.PlayerService
import com.peecock.ymusic.ui.components.themed.PlaybackParamsDialog
import com.peecock.ymusic.ui.screens.player.components.controls.ControlsEssential
import com.peecock.ymusic.ui.screens.player.components.controls.ControlsModern
import kotlin.math.roundToInt

@OptIn(UnstableApi::class)
@Composable
fun GetControls(
    binder: PlayerService.Binder,
    position: Long,
    shouldBePlaying: Boolean,
    likedAt: Long?,
    mediaId: String,
    onBlurScaleChange: (Float) -> Unit
) {
    val playerControlsType by rememberPreference(playerControlsTypeKey, PlayerControlsType.Modern)
    val playerPlayButtonType by rememberPreference(
        playerPlayButtonTypeKey,
        PlayerPlayButtonType.Rectangular
    )
    var isRotated by rememberSaveable { mutableStateOf(false) }
    val rotationAngle by animateFloatAsState(
        targetValue = if (isRotated) 360F else 0f,
        animationSpec = tween(durationMillis = 200), label = ""
    )
    val isGradientBackgroundEnabled by rememberPreference(isGradientBackgroundEnabledKey, false)

    var playbackSpeed by rememberPreference(playbackSpeedKey, 1f)
    var playbackDuration by rememberPreference(playbackDurationKey, 0f)
    var setPlaybackDuration by remember { mutableStateOf(false) }

    var showSpeedPlayerDialog by rememberSaveable {
        mutableStateOf(false)
    }

    if (showSpeedPlayerDialog) {
        PlaybackParamsDialog(
            onDismiss = { showSpeedPlayerDialog = false },
            speedValue = { playbackSpeed = it },
            pitchValue = {},
            durationValue = {
                playbackDuration = it
                setPlaybackDuration = true
            },
            scaleValue = onBlurScaleChange
        )
    }


        MedleyMode(
            binder = binder,
            seconds = if (playbackDuration < 1f) 0 else playbackDuration.roundToInt()
        )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier
            .fillMaxWidth()
    ) {

        if (playerControlsType == PlayerControlsType.Essential)
            ControlsEssential(
                binder = binder,
                position = position,
                playbackSpeed = playbackSpeed,
                shouldBePlaying = shouldBePlaying,
                likedAt = likedAt,
                mediaId = mediaId,
                playerPlayButtonType = playerPlayButtonType,
                rotationAngle = rotationAngle,
                isGradientBackgroundEnabled = isGradientBackgroundEnabled,
                onShowSpeedPlayerDialog = { showSpeedPlayerDialog = true }
            )

        if (playerControlsType == PlayerControlsType.Modern)
            ControlsModern(
                binder = binder,
                position = position,
                playbackSpeed = playbackSpeed,
                shouldBePlaying = shouldBePlaying,
                playerPlayButtonType = playerPlayButtonType,
                rotationAngle = rotationAngle,
                isGradientBackgroundEnabled = isGradientBackgroundEnabled,
                onShowSpeedPlayerDialog = { showSpeedPlayerDialog = true }
            )
    }
}