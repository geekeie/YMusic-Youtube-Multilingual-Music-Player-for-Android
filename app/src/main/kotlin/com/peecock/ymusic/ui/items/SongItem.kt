package com.peecock.ymusic.ui.items

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadService
import coil.compose.AsyncImage
import com.peecock.innertube.Innertube
import com.peecock.ymusic.Database
import com.peecock.ymusic.R
import com.peecock.ymusic.models.Song
import com.peecock.ymusic.service.MyDownloadService
import com.peecock.ymusic.ui.components.themed.HeaderIconButton
import com.peecock.ymusic.ui.components.themed.IconButton
import com.peecock.ymusic.ui.components.themed.SmartMessage
import com.peecock.ymusic.ui.components.themed.TextPlaceholder
import com.peecock.ymusic.ui.styling.LocalAppearance
import com.peecock.ymusic.ui.styling.favoritesIcon
import com.peecock.ymusic.ui.styling.shimmer
import com.peecock.ymusic.utils.cleanPrefix
import com.peecock.ymusic.utils.medium
import com.peecock.ymusic.utils.playlistindicatorKey
import com.peecock.ymusic.utils.rememberPreference
import com.peecock.ymusic.utils.secondary
import com.peecock.ymusic.utils.semiBold
import com.peecock.ymusic.utils.thumbnail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

const val EXPLICIT_PREFIX = "e:"

@UnstableApi
@Composable
fun SongItem(
    song: Innertube.SongItem,
    thumbnailSizePx: Int,
    thumbnailSizeDp: Dp,
    modifier: Modifier = Modifier,
    isDownloaded: Boolean,
    onDownloadClick: () -> Unit,
    downloadState: Int,
    thumbnailContent: (@Composable BoxScope.() -> Unit)? = null
) {
    SongItem(
        thumbnailUrl = song.thumbnail?.size(thumbnailSizePx),
        title = song.info?.name,
        authors = song.authors?.joinToString("") { it.name ?: "" },
        duration = song.durationText,
        thumbnailSizeDp = thumbnailSizeDp,
        modifier = modifier,
        isDownloaded = isDownloaded,
        onDownloadClick = onDownloadClick,
        downloadState = downloadState,
        isExplicit = song.explicit,
        mediaId = song.key,
        onThumbnailContent = thumbnailContent
    )
}

@UnstableApi
@Composable
fun SongItem(
    song: MediaItem,
    thumbnailSizeDp: Dp,
    thumbnailSizePx: Int,
    modifier: Modifier = Modifier,
    onThumbnailContent: (@Composable BoxScope.() -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    isDownloaded: Boolean,
    onDownloadClick: () -> Unit,
    downloadState: Int,
    isRecommended: Boolean = false,
    duration: String? = ""
) {
    SongItem(
        thumbnailUrl = song.mediaMetadata.artworkUri.thumbnail(thumbnailSizePx)?.toString(),
        title = song.mediaMetadata.title.toString(),
        authors = song.mediaMetadata.artist.toString(),
        duration = duration?.ifBlank { song.mediaMetadata.extras?.getString("durationText") },
        thumbnailSizeDp = thumbnailSizeDp,
        onThumbnailContent = onThumbnailContent,
        trailingContent = trailingContent,
        modifier = modifier,
        isDownloaded = isDownloaded,
        onDownloadClick = onDownloadClick,
        downloadState = downloadState,
        isRecommended = isRecommended,
        mediaId = song.mediaId
    )
}

@UnstableApi
@Composable
fun SongItem(
    song: Song,
    thumbnailSizePx: Int,
    thumbnailSizeDp: Dp,
    modifier: Modifier = Modifier,
    onThumbnailContent: (@Composable BoxScope.() -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    isDownloaded: Boolean,
    onDownloadClick: () -> Unit,
    downloadState: Int
) {
    SongItem(
        thumbnailUrl = song.thumbnailUrl?.thumbnail(thumbnailSizePx),
        totalPlayTimeMs = song.totalPlayTimeMs,
        title = song.title,
        authors = song.artistsText,
        duration = song.durationText,
        thumbnailSizeDp = thumbnailSizeDp,
        onThumbnailContent = onThumbnailContent,
        trailingContent = trailingContent,
        modifier = modifier,
        isDownloaded = isDownloaded,
        onDownloadClick = onDownloadClick,
        downloadState = downloadState,
        mediaId = song.id
    )
}

@UnstableApi
@Composable
fun SongItem(
    thumbnailUrl: String?,
    totalPlayTimeMs: Long? = 0,
    title: String?,
    authors: String?,
    duration: String?,
    thumbnailSizeDp: Dp,
    modifier: Modifier = Modifier,
    onThumbnailContent: (@Composable BoxScope.() -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    isDownloaded: Boolean,
    onDownloadClick: () -> Unit,
    downloadState: Int,
    isRecommended: Boolean = false,
    isExplicit: Boolean = false,
    mediaId: String
) {
    SongItem(
        title = title,
        totalPlayTimeMs = totalPlayTimeMs,
        authors = authors,
        duration = duration,
        thumbnailSizeDp = thumbnailSizeDp,
        thumbnailContent = {
            AsyncImage(
                model = thumbnailUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .clip(LocalAppearance.current.thumbnailShape)
                    .fillMaxSize()
            )

            onThumbnailContent?.invoke(this)
        },
        modifier = modifier,
        trailingContent = trailingContent,
        isDownloaded = isDownloaded,
        onDownloadClick = onDownloadClick,
        downloadState = downloadState,
        isRecommended = isRecommended,
        isExplicit = isExplicit,
        mediaId = mediaId
    )
}

@Composable
fun SongItem(
    thumbnailContent: @Composable BoxScope.() -> Unit,
    title: String?,
    authors: String?,
    duration: String?,
    thumbnailSizeDp: Dp,
    modifier: Modifier = Modifier,
    trailingContent: @Composable (() -> Unit)? = null,
    isDownloaded: Boolean,
    onDownloadClick: () -> Unit
) {
    val (colorPalette, typography) = LocalAppearance.current

    ItemContainer(
        alternative = false,
        thumbnailSizeDp = thumbnailSizeDp,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(thumbnailSizeDp)
        ) {
            thumbnailContent()
        }

        ItemInfoContainer {
            trailingContent?.let {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    BasicText(
                        text = title ?: "",
                        style = typography.xs.semiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f)
                            .basicMarquee(iterations = Int.MAX_VALUE)
                    )

                    it()
                }
            } ?: BasicText(
                text = title ?: "",
                style = typography.xs.semiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .basicMarquee(iterations = Int.MAX_VALUE)
            )


            Row(verticalAlignment = Alignment.CenterVertically) {

                IconButton(
                    onClick = onDownloadClick,
                    icon = if (isDownloaded) R.drawable.downloaded else R.drawable.download,
                    color = if (isDownloaded) colorPalette.text else colorPalette.textDisabled,
                    modifier = Modifier
                        .size(16.dp)
                )

                Spacer(modifier = Modifier.padding(horizontal = 2.dp))

                BasicText(
                    text = authors ?: "",
                    style = typography.xs.semiBold.secondary,
                    maxLines = 1,
                    overflow = TextOverflow.Clip,
                    modifier = Modifier
                        .weight(1f)
                        .basicMarquee(iterations = Int.MAX_VALUE)
                )

                duration?.let {
                    BasicText(
                        text = duration,
                        style = typography.xxs.secondary.medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@UnstableApi
@Composable
fun SongItem(
    thumbnailContent: @Composable BoxScope.() -> Unit,
    totalPlayTimeMs: Long? = 0,
    title: String?,
    authors: String?,
    duration: String?,
    thumbnailSizeDp: Dp,
    modifier: Modifier = Modifier,
    trailingContent: @Composable (() -> Unit)? = null,
    isDownloaded: Boolean,
    onDownloadClick: () -> Unit,
    downloadState: Int,
    isRecommended: Boolean = false,
    isExplicit: Boolean = false,
    mediaId: String
) {
    val (colorPalette, typography) = LocalAppearance.current
    var songPlaylist by remember {
        mutableStateOf(0)
    }
    LaunchedEffect(Unit, mediaId) {
        withContext(Dispatchers.IO) {
            songPlaylist = Database.songUsedInPlaylists(mediaId)
        }
    }

    var playlistindicator by rememberPreference(playlistindicatorKey,false)
    val context = LocalContext.current

    ItemContainer(
        alternative = false,
        thumbnailSizeDp = thumbnailSizeDp,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(thumbnailSizeDp)
        ) {
            thumbnailContent()


            var likedAt by remember {
                mutableStateOf<Long?>(null)
            }
            LaunchedEffect(Unit, mediaId) {
                Database.likedAt(mediaId).collect { likedAt = it }
            }
            if (likedAt != null)
                HeaderIconButton(
                    onClick = {},
                    icon = R.drawable.heart,
                    color = colorPalette.favoritesIcon,
                    iconSize = 12.dp,
                    modifier = Modifier
                        //.padding(start = 4.dp)
                        .align(Alignment.BottomStart)
                        .absoluteOffset(-8.dp,0.dp)

                )
            /*
            if (totalPlayTimeMs != null) {
                if (totalPlayTimeMs <= 0 ) {
                    HeaderIconButton(
                        onClick = {},
                        icon = R.drawable.noteslashed,
                        color = colorPalette.favoritesIcon,
                        iconSize = 12.dp,
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .align(Alignment.BottomStart)
                    )
                }
            }
             */

            /*
            BasicText(
                text = totalPlayTimeMs.toString() ?: "",
                style = typography.xs.semiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(all = 16.dp)
            )
             */
        }

        ItemInfoContainer {
            trailingContent?.let {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isRecommended)
                        IconButton(
                            icon = R.drawable.smart_shuffle,
                            color = colorPalette.accent,
                            enabled = true,
                            onClick = {},
                            modifier = Modifier
                                .size(18.dp)
                        )

                    if (playlistindicator && (songPlaylist > 0)) {
                        IconButton(
                            icon = R.drawable.add_in_playlist,
                            color = colorPalette.text,
                            enabled = true,
                            onClick = {},
                            modifier = Modifier
                                .size(14.dp)
                                .background(colorPalette.accent, CircleShape)
                                .padding(all = 3.dp)
                                .combinedClickable(onClick = {}, onLongClick = {
                                    SmartMessage(
                                        context.resources.getString(R.string.playlistindicatorinfo2),
                                        context = context
                                    )
                                })
                        )
                        Spacer(modifier = Modifier.padding(horizontal = 3.dp))
                    }

                    if (isExplicit || title?.startsWith(EXPLICIT_PREFIX) == true)
                        IconButton(
                            icon = R.drawable.explicit,
                            color = colorPalette.text,
                            enabled = true,
                            onClick = {},
                            modifier = Modifier
                                .size(18.dp)
                        )

                    BasicText(
                        text = cleanPrefix(title ?: ""),
                        style = typography.xs.semiBold,
                        /*
                        style = TextStyle(
                            color = if (isRecommended) colorPalette.accent else colorPalette.text,
                            fontStyle = typography.xs.semiBold.fontStyle,
                            fontSize = typography.xs.semiBold.fontSize
                        ),
                         */
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f)
                            .basicMarquee(iterations = Int.MAX_VALUE)
                    )

                    /*
                    if (playlistindicator && (songPlaylist > 0)) {
                        IconButton(
                            icon = R.drawable.add_in_playlist,
                            color = colorPalette.text,
                            enabled = true,
                            onClick = {},
                            modifier = Modifier
                                .size(18.dp)
                                .background(colorPalette.accent, CircleShape)
                                .padding(all = 3.dp)
                                .combinedClickable(onClick = {}, onLongClick = {
                                    SmartMessage(context.resources.getString(R.string.playlistindicatorinfo2), context = context)
                                })
                        )
                    }
                     */

                    it()
                }
            } ?: Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isRecommended)
                        IconButton(
                            icon = R.drawable.smart_shuffle,
                            color = colorPalette.accent,
                            enabled = true,
                            onClick = {},
                            modifier = Modifier
                                .size(18.dp)
                        )

                    if (isExplicit || title?.startsWith(EXPLICIT_PREFIX) == true)
                        IconButton(
                            icon = R.drawable.explicit,
                            color = colorPalette.text,
                            enabled = true,
                            onClick = {},
                            modifier = Modifier
                                .size(18.dp)
                        )
                    BasicText(
                        text = cleanPrefix(title ?: ""),
                        style = typography.xs.semiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .basicMarquee(iterations = Int.MAX_VALUE)
                            .weight(1f)
                    )
                if (playlistindicator && (songPlaylist > 0)) {
                    IconButton(
                        icon = R.drawable.add_in_playlist,
                        color = colorPalette.text,
                        enabled = true,
                        onClick = {},
                        modifier = Modifier
                            .size(18.dp)
                            .background(colorPalette.accent, CircleShape)
                            .padding(all = 3.dp)
                            .combinedClickable(onClick = {}, onLongClick = {
                                SmartMessage(context.resources.getString(R.string.playlistindicatorinfo2), context = context)
                            })
                    )
                }
            }


            Row(verticalAlignment = Alignment.CenterVertically) {

                //Log.d("downloadState",downloadState.toString())

                /*
                if ((downloadState == Download.STATE_DOWNLOADING
                            || downloadState == Download.STATE_QUEUED
                            || downloadState == Download.STATE_RESTARTING
                        )
                    && !isDownloaded) {
                    val context = LocalContext.current
                    IconButton(
                        onClick = {
                            DownloadService.sendRemoveDownload(
                                context,
                                MyDownloadService::class.java,
                                mediaId,
                                false
                            )
                        },
                        icon = R.drawable.download_progress,
                        color = colorPalette.text,
                        modifier = Modifier
                            .size(16.dp)
                    )
                    /*
                    CircularProgressIndicator(
                        strokeWidth = 2.dp,
                        color = colorPalette.text,
                        modifier = Modifier
                            .size(16.dp)
                            .clickable {
                                DownloadService.sendRemoveDownload(
                                        context,
                                        MyDownloadService::class.java,
                                        mediaId,
                                        false
                                    )
                            }
                    )
                     */
                } else {
                   IconButton(
                        onClick = onDownloadClick,
                        icon = if (isDownloaded) R.drawable.downloaded else R.drawable.download,
                        color = if (isDownloaded) colorPalette.text else colorPalette.textDisabled,
                        modifier = Modifier
                            .size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.padding(horizontal = 2.dp))
                */

                BasicText(
                    text = authors ?: "",
                    style = typography.xs.semiBold.secondary,
                    maxLines = 1,
                    overflow = TextOverflow.Clip,
                    modifier = Modifier
                        .weight(1f)
                        .basicMarquee(iterations = Int.MAX_VALUE)
                )

                duration?.let {
                    BasicText(
                        text = duration,
                        style = typography.xxs.secondary.medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .padding(top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.padding(horizontal = 4.dp))

                if ((downloadState == Download.STATE_DOWNLOADING
                            || downloadState == Download.STATE_QUEUED
                            || downloadState == Download.STATE_RESTARTING
                            )
                    && !isDownloaded) {
                    val context = LocalContext.current
                    IconButton(
                        onClick = {
                            DownloadService.sendRemoveDownload(
                                context,
                                MyDownloadService::class.java,
                                mediaId,
                                false
                            )
                        },
                        icon = R.drawable.download_progress,
                        color = colorPalette.text,
                        modifier = Modifier
                            .size(20.dp)
                    )
                } else {
                    IconButton(
                        onClick = onDownloadClick,
                        icon = if (isDownloaded) R.drawable.downloaded else R.drawable.download,
                        color = if (isDownloaded) colorPalette.text else colorPalette.textDisabled,
                        modifier = Modifier
                            .size(20.dp)
                    )
                }

            }
        }
    }
}


@Composable
fun SongItemPlaceholder(
    thumbnailSizeDp: Dp,
    modifier: Modifier = Modifier
) {
    val (colorPalette, _, thumbnailShape) = LocalAppearance.current

    ItemContainer(
        alternative = false,
        thumbnailSizeDp =thumbnailSizeDp,
        modifier = modifier
    ) {
        Spacer(
            modifier = Modifier
                .background(color = colorPalette.shimmer, shape = thumbnailShape)
                .size(thumbnailSizeDp)
        )

        ItemInfoContainer {
            TextPlaceholder()
            TextPlaceholder()
        }
    }
}
