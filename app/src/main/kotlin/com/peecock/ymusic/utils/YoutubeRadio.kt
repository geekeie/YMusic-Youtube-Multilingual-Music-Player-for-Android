package com.peecock.ymusic.utils

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import com.peecock.innertube.Innertube
import com.peecock.innertube.models.bodies.ContinuationBody
import com.peecock.innertube.models.bodies.NextBody
import com.peecock.innertube.requests.nextPage
import com.peecock.ymusic.Database
import com.peecock.ymusic.R
import com.peecock.ymusic.enums.PopupType
import com.peecock.ymusic.ui.components.themed.SmartMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class YouTubeRadio(
    private val videoId: String? = null,
    private var playlistId: String? = null,
    private var playlistSetVideoId: String? = null,
    private var parameters: String? = null,
    private val isDiscoverEnabled: Boolean = false,
    private val context: Context
) {
    private var nextContinuation: String? = null

    @OptIn(UnstableApi::class) suspend fun process(): List<MediaItem> {
        var mediaItems: List<MediaItem>? = null

        nextContinuation = withContext(Dispatchers.IO) {
            val continuation = nextContinuation

            if (continuation == null) {
               Innertube.nextPage(
                    NextBody(
                        videoId = videoId,
                        playlistId = playlistId,
                        params = parameters,
                        playlistSetVideoId = playlistSetVideoId
                    )
                )?.map { nextResult ->
                    playlistId = nextResult.playlistId
                    parameters = nextResult.params
                    playlistSetVideoId = nextResult.playlistSetVideoId

                    nextResult.itemsPage
                }
            } else {
                Innertube.nextPage(ContinuationBody(continuation = continuation))
            }?.getOrNull()?.let { songsPage ->
                mediaItems = songsPage.items?.map(Innertube.SongItem::asMediaItem)
                songsPage.continuation?.takeUnless { nextContinuation == it }
            }

        }

        if (isDiscoverEnabled) {
            var listMediaItems = mutableListOf<MediaItem>()
            withContext(Dispatchers.IO) {
                mediaItems?.forEach {
                    val songInPlaylist = Database.songUsedInPlaylists(it.mediaId)
                    val songIsLiked = Database.songliked(it.mediaId)
                    if (songInPlaylist == 0 && songIsLiked == 0) {
                        listMediaItems.add(it)
                    }
                }
            }

            SmartMessage(context.resources.getString(R.string.discover_has_been_applied_to_radio).format(
                mediaItems?.size?.minus(listMediaItems.size) ?: 0
            ), PopupType.Success, context = context)

            mediaItems = listMediaItems
        }

        return mediaItems ?: emptyList()
    }
}
