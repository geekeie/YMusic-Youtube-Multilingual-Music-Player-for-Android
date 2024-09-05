package com.peecock.ymusic.utils


import android.annotation.SuppressLint
import android.content.Context
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import com.peecock.ymusic.R
import com.peecock.ymusic.enums.DurationInMinutes
import com.peecock.ymusic.ui.components.themed.SmartMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber


val Player.currentWindow: Timeline.Window?
    get() = if (mediaItemCount == 0) null else currentTimeline.getWindow(currentMediaItemIndex, Timeline.Window())

val Timeline.mediaItems: List<MediaItem>
    get() = List(windowCount) {
        getWindow(it, Timeline.Window()).mediaItem
    }

inline val Timeline.windows: List<Timeline.Window>
    get() = List(windowCount) {
        getWindow(it, Timeline.Window())
    }

val Player.shouldBePlaying: Boolean
    get() = !(playbackState == Player.STATE_ENDED || !playWhenReady)

fun Player.seamlessPlay(mediaItem: MediaItem) {
    if (mediaItem.mediaId == currentMediaItem?.mediaId) {
        if (currentMediaItemIndex > 0) removeMediaItems(0, currentMediaItemIndex)
        if (currentMediaItemIndex < mediaItemCount - 1) removeMediaItems(currentMediaItemIndex + 1, mediaItemCount)
    } else {
        forcePlay(mediaItem)
    }
}

fun Player.shuffleQueue() {
    val mediaItems = currentTimeline.mediaItems.toMutableList().apply { removeAt(currentMediaItemIndex) }
    if (currentMediaItemIndex > 0) removeMediaItems(0, currentMediaItemIndex)
    if (currentMediaItemIndex < mediaItemCount - 1) removeMediaItems(currentMediaItemIndex + 1, mediaItemCount)
    addMediaItems(mediaItems.shuffled())
}

@SuppressLint("Range")
@UnstableApi
fun Player.playAtMedia(mediaItems: List<MediaItem>, mediaId: String) {
    Log.d("mediaItem-playAtMedia","${mediaItems.size}")
    if (mediaItems.isEmpty()) return
    val itemIndex = findMediaItemIndexById(mediaId)

    Log.d("mediaItem-playAtMedia",itemIndex.toString())
    setMediaItems(mediaItems, itemIndex, C.TIME_UNSET)
    playWhenReady = true
    prepare()
}

fun Player.forcePlay(mediaItem: MediaItem) {
    setMediaItem(mediaItem, true)
    playWhenReady = true
    prepare()
}
@SuppressLint("Range")
@UnstableApi
fun Player.forcePlayAtIndex(mediaItems: List<MediaItem>, mediaItemIndex: Int) {
    if (mediaItems.isEmpty()) return

    setMediaItems(mediaItems, mediaItemIndex, C.TIME_UNSET)
    playWhenReady = true
    prepare()
}
@UnstableApi
fun Player.forcePlayFromBeginning(mediaItems: List<MediaItem>) =
    forcePlayAtIndex(mediaItems, 0)

fun Player.forceSeekToPrevious() {
    if (hasPreviousMediaItem() || currentPosition > maxSeekToPreviousPosition) {
        seekToPrevious()
    } else if (mediaItemCount > 0) {
        seekTo(mediaItemCount - 1, C.TIME_UNSET)
    }
}

fun Player.forceSeekToNext() =
    if (hasNextMediaItem()) seekToNext() else seekTo(0, C.TIME_UNSET)

@UnstableApi
fun Player.addNext(mediaItem: MediaItem, context: Context? = null) {
    if (context != null && excludeMediaItem(mediaItem, context)) return

    val itemIndex = findMediaItemIndexById(mediaItem.mediaId)
    if (itemIndex >= 0) removeMediaItem(itemIndex)

    if (playbackState == Player.STATE_IDLE || playbackState == Player.STATE_ENDED) {
        forcePlay(mediaItem)
    } else {
        addMediaItem(currentMediaItemIndex + 1, mediaItem)
    }
}

@UnstableApi
fun Player.addNext(mediaItems: List<MediaItem>, context: Context? = null) {
    val filteredMediaItems = if (context != null) excludeMediaItems(mediaItems, context)
    else mediaItems

    filteredMediaItems.forEach { mediaItem ->
        val itemIndex = findMediaItemIndexById(mediaItem.mediaId)
        if (itemIndex >= 0) removeMediaItem(itemIndex)
    }

    if (playbackState == Player.STATE_IDLE || playbackState == Player.STATE_ENDED) {
        forcePlay(filteredMediaItems.first())
    } else {
        addMediaItems(currentMediaItemIndex + 1, filteredMediaItems)
    }

}


fun Player.enqueue(mediaItem: MediaItem, context: Context? = null) {
     if (context != null && excludeMediaItem(mediaItem, context)) return

    if (playbackState == Player.STATE_IDLE || playbackState == Player.STATE_ENDED) {
        forcePlay(mediaItem)
    } else {
        addMediaItem(mediaItemCount, mediaItem)
    }
}


@UnstableApi
fun Player.enqueue(mediaItems: List<MediaItem>, context: Context? = null) {
    val filteredMediaItems = if (context != null) excludeMediaItems(mediaItems, context)
    else mediaItems

    if (playbackState == Player.STATE_IDLE || playbackState == Player.STATE_ENDED) {
        //forcePlayFromBeginning(mediaItems)
        forcePlayFromBeginning(filteredMediaItems)
    } else {
        //addMediaItems(mediaItemCount, mediaItems)
        addMediaItems(mediaItemCount, filteredMediaItems)
    }
}

fun Player.findNextMediaItemById(mediaId: String): MediaItem? {
    for (i in currentMediaItemIndex until mediaItemCount) {
        if (getMediaItemAt(i).mediaId == mediaId) {
            return getMediaItemAt(i)
        }
    }
    return null
}

fun Player.findMediaItemIndexById(mediaId: String): Int {
    for (i in currentMediaItemIndex until mediaItemCount) {
        if (getMediaItemAt(i).mediaId == mediaId) {
            return i
        }
    }
    return -1
}

fun Player.excludeMediaItems(mediaItems: List<MediaItem>, context: Context): List<MediaItem> {
    var filteredMediaItems = mediaItems
    runCatching {
        val preferences = context.preferences
        val excludeSongWithDurationLimit =
            preferences.getEnum(excludeSongsWithDurationLimitKey, DurationInMinutes.Disabled)

        if (excludeSongWithDurationLimit != DurationInMinutes.Disabled) {
            filteredMediaItems = mediaItems.filter {
                it.mediaMetadata.extras?.getString("durationText")?.let { it1 ->
                    durationTextToMillis(it1)
                }!! < excludeSongWithDurationLimit.minutesInMilliSeconds
            }

            val excludedSongs = mediaItems.size - filteredMediaItems.size
            if (excludedSongs > 0)
                CoroutineScope(Dispatchers.Main).launch {
                        SmartMessage(context.resources.getString(R.string.message_excluded_s_songs).format(excludedSongs), context = context)
                }
        }
    }.onFailure {
        Timber.e(it.message)
    }

    return filteredMediaItems
}
fun Player.excludeMediaItem(mediaItem: MediaItem, context: Context): Boolean {
    runCatching {
        val preferences = context.preferences
        val excludeSongWithDurationLimit =
            preferences.getEnum(excludeSongsWithDurationLimitKey, DurationInMinutes.Disabled)
        if (excludeSongWithDurationLimit != DurationInMinutes.Disabled) {
            val excludedSong = mediaItem.mediaMetadata.extras?.getString("durationText")?.let { it1 ->
                    durationTextToMillis(it1)
                }!! <= excludeSongWithDurationLimit.minutesInMilliSeconds

            if (excludedSong)
                CoroutineScope(Dispatchers.Main).launch {
                    SmartMessage(context.resources.getString(R.string.message_excluded_s_songs).format(1), context = context)
                }

            return excludedSong
        }
    }.onFailure {
        //it.printStackTrace()
        Timber.e(it.message)
        return false
    }

    return false

}