package com.peecock.ymusic.ui.items

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.peecock.innertube.Innertube
import com.peecock.ymusic.Database
import com.peecock.ymusic.R
import com.peecock.ymusic.models.PlaylistPreview
import com.peecock.ymusic.ui.components.themed.TextPlaceholder
import com.peecock.ymusic.ui.screens.home.PINNED_PREFIX
import com.peecock.ymusic.ui.screens.home.PIPED_PREFIX
import com.peecock.ymusic.ui.styling.LocalAppearance
import com.peecock.ymusic.ui.styling.onOverlay
import com.peecock.ymusic.ui.styling.overlay
import com.peecock.ymusic.ui.styling.shimmer
import com.peecock.ymusic.utils.MONTHLY_PREFIX
import com.peecock.ymusic.utils.color
import com.peecock.ymusic.utils.getTitleMonthlyPlaylist
import com.peecock.ymusic.utils.medium
import com.peecock.ymusic.utils.secondary
import com.peecock.ymusic.utils.semiBold
import com.peecock.ymusic.utils.thumbnail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import timber.log.Timber

@Composable
fun PlaylistItem(
    @DrawableRes icon: Int,
    colorTint: Color,
    name: String?,
    songCount: Int?,
    thumbnailSizeDp: Dp,
    modifier: Modifier = Modifier,
    alternative: Boolean = false,
    showName: Boolean = true,
    iconSize: Dp = 34.dp
) {
    PlaylistItem(
        thumbnailContent = {
            Image(
                painter = painterResource(icon),
                contentDescription = null,
                colorFilter = ColorFilter.tint(colorTint),
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(iconSize)
            )
        },
        songCount = songCount,
        name = name,
        channelName = null,
        thumbnailSizeDp = thumbnailSizeDp,
        modifier = modifier,
        alternative = alternative,
        showName = showName
    )
}

@Composable
fun PlaylistItem(
    playlist: PlaylistPreview,
    thumbnailSizePx: Int,
    thumbnailSizeDp: Dp,
    modifier: Modifier = Modifier,
    alternative: Boolean = false,
    showName: Boolean = true
) {
    val thumbnails by remember {
        Database.playlistThumbnailUrls(playlist.playlist.id).distinctUntilChanged().map {
            it.map { url ->
                url.thumbnail(thumbnailSizePx / 2)
            }
        }
    }.collectAsState(initial = emptyList(), context = Dispatchers.IO)

    PlaylistItem(
        thumbnailContent = {
            if (thumbnails.toSet().size == 1) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(thumbnails.first())
                        .setHeader("User-Agent", "Mozilla/5.0")
                        .build(), //thumbnails.first().thumbnail(thumbnailSizePx),
                    onError = {error ->
                        Timber.e("Failed AsyncImage in PlaylistItem ${error.result.throwable.stackTraceToString()}")
                    },
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    //modifier = it KOTLIN 2
                )
            } else {
                Box(
                    modifier = Modifier // KOTLIN 2
                        .fillMaxSize()
                ) {
                    listOf(
                        Alignment.TopStart,
                        Alignment.TopEnd,
                        Alignment.BottomStart,
                        Alignment.BottomEnd
                    ).forEachIndexed { index, alignment ->
                        val thumbnail = thumbnails.getOrNull(index)
                        if (thumbnail != null)
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(thumbnail)
                                    .setHeader("User-Agent", "Mozilla/5.0")
                                    .build(),
                                onError = {error ->
                                    Timber.e("Failed AsyncImage 1 in PlaylistItem ${error.result.throwable.stackTraceToString()}")
                                },
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .align(alignment)
                                    .size(thumbnailSizeDp / 2)
                            )
                    }
                }
            }
        },
        songCount = playlist.songCount,
        name = playlist.playlist.name,
        channelName = null,
        thumbnailSizeDp = thumbnailSizeDp,
        modifier = modifier,
        alternative = alternative,
        showName = showName
    )
}

@Composable
fun PlaylistItem(
    playlist: Innertube.PlaylistItem,
    thumbnailSizePx: Int,
    thumbnailSizeDp: Dp,
    modifier: Modifier = Modifier,
    alternative: Boolean = false,
    showSongsCount: Boolean = true
) {
    PlaylistItem(
        thumbnailUrl = playlist.thumbnail?.url,
        songCount = playlist.songCount,
        showSongsCount = showSongsCount,
        name = playlist.info?.name,
        channelName = playlist.channel?.name,
        thumbnailSizePx = thumbnailSizePx,
        thumbnailSizeDp = thumbnailSizeDp,
        modifier = modifier,
        alternative = alternative
    )
}

@Composable
fun PlaylistItem(
    thumbnailUrl: String?,
    songCount: Int?,
    name: String?,
    channelName: String?,
    thumbnailSizePx: Int,
    thumbnailSizeDp: Dp,
    modifier: Modifier = Modifier,
    alternative: Boolean = false,
    showSongsCount: Boolean = true
) {
    PlaylistItem(
        thumbnailContent = {
            AsyncImage(
                model = thumbnailUrl?.thumbnail(thumbnailSizePx),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                //modifier = it KOTLIN 2
            )
        },
        songCount = songCount,
        showSongsCount = showSongsCount,
        name = name,
        channelName = channelName,
        thumbnailSizeDp = thumbnailSizeDp,
        modifier = modifier,
        alternative = alternative,
    )
}

@Composable
fun PlaylistItem(
    thumbnailContent: @Composable BoxScope.(
        //modifier: Modifier
            ) -> Unit,
    songCount: Int?,
    name: String?,
    channelName: String?,
    thumbnailSizeDp: Dp,
    modifier: Modifier = Modifier,
    alternative: Boolean = false,
    showName: Boolean = true,
    showSongsCount: Boolean = true
) {
    val (colorPalette, typography, thumbnailShape) = LocalAppearance.current

    ItemContainer(
        alternative = alternative,
        thumbnailSizeDp = thumbnailSizeDp,
        modifier = modifier
    ) { //centeredModifier ->
        Box(
            modifier = Modifier // KOTLIN 2
                .clip(thumbnailShape)
                .background(color = colorPalette.background4)
                .requiredSize(thumbnailSizeDp)
        ) {
            thumbnailContent(
                /*
                modifier = Modifier
                    .fillMaxSize()

                 */
            )

            name?.let {
                if (it.startsWith(PIPED_PREFIX,0,true)) {
                    Image(
                        painter = painterResource(R.drawable.piped_logo),
                        colorFilter = ColorFilter.tint(colorPalette.red),
                        modifier = Modifier
                            .size(40.dp)
                            .padding(all = 5.dp),
                        contentDescription = "Background Image",
                        contentScale = ContentScale.Fit
                    )
                }
                if (it.startsWith(PINNED_PREFIX,0,true)) {
                    Image(
                        painter = painterResource(R.drawable.pin),
                        colorFilter = ColorFilter.tint(colorPalette.accent),
                        modifier = Modifier
                            .size(40.dp)
                            .padding(all = 5.dp),
                        contentDescription = "Background Image",
                        contentScale = ContentScale.Fit
                    )
                }
                if (it.startsWith(MONTHLY_PREFIX,0,true)) {
                    Image(
                        painter = painterResource(R.drawable.stat_month),
                        colorFilter = ColorFilter.tint(colorPalette.accent),
                        modifier = Modifier
                            .size(40.dp)
                            .padding(all = 5.dp),
                        contentDescription = "Background Image",
                        contentScale = ContentScale.Fit
                    )
                }
            }


            if (showSongsCount)
                songCount?.let {
                    BasicText(
                        text = "$songCount",
                        style = typography.xxs.medium.color(colorPalette.onOverlay),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .padding(all = 4.dp)
                            .background(color = colorPalette.overlay, shape = RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 6.dp)
                            .align(Alignment.BottomEnd)
                    )
                }

        }


        ItemInfoContainer(
            horizontalAlignment = if (alternative && channelName == null) Alignment.CenterHorizontally else Alignment.Start,
            modifier = Modifier
                .fillMaxSize()
        ) {
            if (showName)
                if (name != null) {
                    BasicText(
                        //text = name.substringAfter(PINNED_PREFIX) ?: "",
                        text = if (name.startsWith(PINNED_PREFIX,0,true))
                            name.substringAfter(PINNED_PREFIX) else
                            if (name.startsWith(MONTHLY_PREFIX,0,true))
                                getTitleMonthlyPlaylist(name.substringAfter(MONTHLY_PREFIX)) else
                            if (name.startsWith(PIPED_PREFIX,0,true))
                            name.substringAfter(PIPED_PREFIX) else name,
                        style = typography.xs.semiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .basicMarquee(iterations = Int.MAX_VALUE)
                    )
                }

            channelName?.let {
                BasicText(
                    text = channelName,
                    style = typography.xs.semiBold.secondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .basicMarquee(iterations = Int.MAX_VALUE)
                )
            }
        }
    }
}

@Composable
fun PlaylistItemPlaceholder(
    thumbnailSizeDp: Dp,
    modifier: Modifier = Modifier,
    alternative: Boolean = false,
) {
    val (colorPalette, _, thumbnailShape) = LocalAppearance.current

    ItemContainer(
        alternative = alternative,
        thumbnailSizeDp = thumbnailSizeDp,
        modifier = modifier
    ) {
        Spacer(
            modifier = Modifier
                .background(color = colorPalette.shimmer, shape = thumbnailShape)
                .size(thumbnailSizeDp)
        )

        ItemInfoContainer(
            horizontalAlignment = if (alternative) Alignment.CenterHorizontally else Alignment.Start,
        ) {
            TextPlaceholder()
            TextPlaceholder()
        }
    }
}
