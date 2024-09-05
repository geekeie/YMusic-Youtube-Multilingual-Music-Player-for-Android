package com.peecock.ymusic.ui.components.themed

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.peecock.ymusic.LocalPlayerServiceBinder
import com.peecock.ymusic.R
import com.peecock.ymusic.ui.components.MusicBars
import com.peecock.ymusic.ui.styling.Dimensions
import com.peecock.ymusic.ui.styling.LocalAppearance
import com.peecock.ymusic.ui.styling.onOverlay
import com.peecock.ymusic.utils.shouldBePlaying

@Composable
fun MusicBarsShow (
    mediaId: String
) {
    val binder = LocalPlayerServiceBinder.current
    val player = binder?.player

    val shouldBePlaying by remember {
        mutableStateOf(binder?.player?.shouldBePlaying)
    }

    val (colorPalette, typography, thumbnailShape) = LocalAppearance.current

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .background(
                color = Color.Black.copy(alpha = 0.25f),
                shape = thumbnailShape
            )
            .size(Dimensions.thumbnails.song)
    ) {
        if (shouldBePlaying == true && player?.currentMediaItem?.mediaId == mediaId) {
            MusicBars(
                color = colorPalette.onOverlay,
                modifier = Modifier
                    .height(24.dp)
            )
        }
        if (shouldBePlaying == false && player?.currentMediaItem?.mediaId == mediaId) {
            Image(
                painter = painterResource(R.drawable.play),
                contentDescription = null,
                colorFilter = ColorFilter.tint(colorPalette.onOverlay),
                modifier = Modifier
                    .size(24.dp)
            )
        }
    }

}