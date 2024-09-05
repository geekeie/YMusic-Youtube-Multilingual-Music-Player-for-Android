package com.peecock.ymusic.ui.components.themed

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import coil.compose.AsyncImage
import com.peecock.compose.persist.persistList
import com.peecock.ymusic.Database
import com.peecock.ymusic.models.PlaylistPreview
import com.peecock.ymusic.models.Song
import com.peecock.ymusic.ui.items.PlaylistItem
import com.peecock.ymusic.utils.thumbnail

@Composable
fun Playlist(
    playlist: PlaylistPreview,
    thumbnailSizePx: Int,
    thumbnailSizeDp: Dp,
    modifier: Modifier = Modifier,
    alternative: Boolean = false,
    showName: Boolean = true
) {
    var songs by persistList<Song>("playlist${playlist.playlist.id}/songsThumbnails")
    LaunchedEffect(playlist.playlist.id) {
        Database.songsPlaylistTop4Positions(playlist.playlist.id).collect{ songs = it }
    }
    val thumbnails = songs
        .takeWhile { it.thumbnailUrl?.isNotEmpty() ?: false }
        .take(4)
        .map { it.thumbnailUrl.thumbnail(thumbnailSizePx / 2) }


    PlaylistItem(
        thumbnailContent = {
            if (thumbnails.toSet().size == 1) {
                AsyncImage(
                    model = thumbnails.first().thumbnail(thumbnailSizePx),
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
                        AsyncImage(
                            model = thumbnails.getOrNull(index),
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