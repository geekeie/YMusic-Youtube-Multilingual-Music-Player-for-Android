package com.peecock.ymusic.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.media3.common.util.UnstableApi
import com.peecock.compose.persist.persist
import com.peecock.innertube.Innertube
import com.peecock.innertube.models.bodies.BrowseBody
import com.peecock.innertube.requests.albumPage
import com.peecock.innertube.requests.artistPage
import com.peecock.ymusic.Database
import com.peecock.ymusic.models.Album
import com.peecock.ymusic.models.Artist
import com.peecock.ymusic.models.SongAlbumMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

@Composable
fun UpdateYoutubeArtist(browseId: String) {

    var artistPage by persist<Innertube.ArtistPage?>("artist/$browseId/artistPage")
    var artist by persist<Artist?>("artist/$browseId/artist")
    val tabIndex by rememberPreference(artistScreenTabIndexKey, defaultValue = 0)

    LaunchedEffect(browseId) {
        Database
            .artist(browseId)
            .combine(snapshotFlow { tabIndex }.map { it != 4 }) { artist, mustFetch -> artist to mustFetch }
            .distinctUntilChanged()
            .collect { (currentArtist, mustFetch) ->
                artist = currentArtist

                if (artistPage == null && (currentArtist?.timestamp == null || mustFetch)) {
                    withContext(Dispatchers.IO) {
                        Innertube.artistPage(BrowseBody(browseId = browseId))
                            ?.onSuccess { currentArtistPage ->
                                artistPage = currentArtistPage

                                Database.upsert(
                                    Artist(
                                        id = browseId,
                                        name = currentArtistPage.name,
                                        thumbnailUrl = currentArtistPage.thumbnail?.url,
                                        timestamp = System.currentTimeMillis(),
                                        bookmarkedAt = currentArtist?.bookmarkedAt
                                    )
                                )
                            }
                    }
                }
            }
    }

}

@UnstableApi
@Composable
fun UpdateYoutubeAlbum (browseId: String) {
    var album by persist<Album?>("album/$browseId/album")
    var albumPage by persist<Innertube.PlaylistOrAlbumPage?>("album/$browseId/albumPage")
    val tabIndex by rememberSaveable {mutableStateOf(0)}
    LaunchedEffect(browseId) {
        Database
            .album(browseId)
            .combine(snapshotFlow { tabIndex }) { album, tabIndex -> album to tabIndex }
            .collect { (currentAlbum, tabIndex) ->
                album = currentAlbum

                if (albumPage == null && (currentAlbum?.timestamp == null || tabIndex == 1)) {
                    withContext(Dispatchers.IO) {
                        Innertube.albumPage(BrowseBody(browseId = browseId))
                            ?.onSuccess { currentAlbumPage ->
                                albumPage = currentAlbumPage

                                Database.clearAlbum(browseId)

                                Database.upsert(
                                    Album(
                                        id = browseId,
                                        title = currentAlbumPage?.title,
                                        thumbnailUrl = currentAlbumPage?.thumbnail?.url,
                                        year = currentAlbumPage?.year,
                                        authorsText = currentAlbumPage?.authors
                                            ?.joinToString("") { it.name ?: "" },
                                        shareUrl = currentAlbumPage?.url,
                                        timestamp = System.currentTimeMillis(),
                                        bookmarkedAt = album?.bookmarkedAt
                                    ),
                                    currentAlbumPage
                                        ?.songsPage
                                        ?.items
                                        ?.map(Innertube.SongItem::asMediaItem)
                                        ?.onEach(Database::insert)
                                        ?.mapIndexed { position, mediaItem ->
                                            SongAlbumMap(
                                                songId = mediaItem.mediaId,
                                                albumId = browseId,
                                                position = position
                                            )
                                        } ?: emptyList()
                                )
                            }
                    }

                }
            }
    }
}