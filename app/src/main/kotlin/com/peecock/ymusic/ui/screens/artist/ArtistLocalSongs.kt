package com.peecock.ymusic.ui.screens.artist

import android.annotation.SuppressLint
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download
import androidx.navigation.NavController
import com.peecock.compose.persist.persist
import com.peecock.ymusic.Database
import com.peecock.ymusic.LocalPlayerServiceBinder
import com.peecock.ymusic.R
import com.peecock.ymusic.enums.NavigationBarPosition
import com.peecock.ymusic.enums.UiType
import com.peecock.ymusic.models.Song
import com.peecock.ymusic.query
import com.peecock.ymusic.ui.components.LocalMenuState
import com.peecock.ymusic.ui.components.ShimmerHost
import com.peecock.ymusic.ui.components.themed.ConfirmationDialog
import com.peecock.ymusic.ui.components.themed.HeaderIconButton
import com.peecock.ymusic.ui.components.themed.LayoutWithAdaptiveThumbnail
import com.peecock.ymusic.ui.components.themed.MultiFloatingActionsContainer
import com.peecock.ymusic.ui.components.themed.NonQueuedMediaItemMenu
import com.peecock.ymusic.ui.components.themed.SmartMessage
import com.peecock.ymusic.ui.items.SongItem
import com.peecock.ymusic.ui.items.SongItemPlaceholder
import com.peecock.ymusic.ui.styling.Dimensions
import com.peecock.ymusic.ui.styling.LocalAppearance
import com.peecock.ymusic.ui.styling.px
import com.peecock.ymusic.utils.UiTypeKey
import com.peecock.ymusic.utils.asMediaItem
import com.peecock.ymusic.utils.downloadedStateMedia
import com.peecock.ymusic.utils.enqueue
import com.peecock.ymusic.utils.forcePlayAtIndex
import com.peecock.ymusic.utils.forcePlayFromBeginning
import com.peecock.ymusic.utils.getDownloadState
import com.peecock.ymusic.utils.manageDownload
import com.peecock.ymusic.utils.navigationBarPositionKey
import com.peecock.ymusic.utils.rememberPreference
import com.peecock.ymusic.utils.showFloatingIconKey

@ExperimentalTextApi
@SuppressLint("SuspiciousIndentation")
@ExperimentalFoundationApi
@ExperimentalAnimationApi
@UnstableApi
@Composable
fun ArtistLocalSongs(
    navController: NavController,
    browseId: String,
    headerContent: @Composable (textButton: (@Composable () -> Unit)?) -> Unit,
    thumbnailContent: @Composable () -> Unit,
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val binder = LocalPlayerServiceBinder.current
    val (colorPalette) = LocalAppearance.current
    val menuState = LocalMenuState.current
    val uiType  by rememberPreference(UiTypeKey, UiType.RiMusic)

    var songs by persist<List<Song>?>("artist/$browseId/localSongs")

    var downloadState by remember {
        mutableStateOf(Download.STATE_STOPPED)
    }

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        Database.artistSongs(browseId).collect { songs = it }
/*
        val items = songs?.map { it.id }
        downloader.downloads.collect { downloads ->
            if (items != null) {
                downloadState =
                    if (items.all { downloads[it]?.state == Download.STATE_COMPLETED })
                        Download.STATE_COMPLETED
                    else if (items.all {
                            downloads[it]?.state == Download.STATE_QUEUED
                                    || downloads[it]?.state == Download.STATE_DOWNLOADING
                                    || downloads[it]?.state == Download.STATE_COMPLETED
                        })
                        Download.STATE_DOWNLOADING
                    else
                        Download.STATE_STOPPED
            }
        }

 */

    }

    val songThumbnailSizeDp = Dimensions.thumbnails.song
    val songThumbnailSizePx = songThumbnailSizeDp.px

    val lazyListState = rememberLazyListState()

    var showConfirmDeleteDownloadDialog by remember {
        mutableStateOf(false)
    }

    var showConfirmDownloadAllDialog by remember {
        mutableStateOf(false)
    }

    val navigationBarPosition by rememberPreference(navigationBarPositionKey, NavigationBarPosition.Bottom)

    LayoutWithAdaptiveThumbnail(thumbnailContent = thumbnailContent) {
        Box(
            modifier = Modifier
                .background(colorPalette.background0)
                //.fillMaxSize()
                .fillMaxHeight()
                .fillMaxWidth(if (navigationBarPosition == NavigationBarPosition.Left ||
                    navigationBarPosition == NavigationBarPosition.Top ||
                    navigationBarPosition == NavigationBarPosition.Bottom) 1f
                else Dimensions.contentWidthRightBar)
        ) {
            LazyColumn(
                state = lazyListState,
                //contentPadding = LocalPlayerAwareWindowInsets.current
                //.only(WindowInsetsSides.Vertical + WindowInsetsSides.End).asPaddingValues(),
                modifier = Modifier
                    .background(colorPalette.background0)
                    .fillMaxSize()
            ) {
                item(
                    key = "header",
                    contentType = 0
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        headerContent {

                            HeaderIconButton(
                                icon = R.drawable.downloaded,
                                color = colorPalette.text,
                                onClick = {},
                                modifier = Modifier
                                    .combinedClickable(
                                        onClick = {
                                            showConfirmDownloadAllDialog = true
                                        },
                                        onLongClick = {
                                            SmartMessage(context.resources.getString(R.string.info_download_all_songs), context = context)
                                        }
                                    )
                            )

                            if (showConfirmDownloadAllDialog) {
                                ConfirmationDialog(
                                    text = stringResource(R.string.do_you_really_want_to_download_all),
                                    onDismiss = { showConfirmDownloadAllDialog = false },
                                    onConfirm = {
                                        showConfirmDownloadAllDialog = false
                                        downloadState = Download.STATE_DOWNLOADING
                                        if (songs?.isNotEmpty() == true)
                                            songs?.forEach {
                                                binder?.cache?.removeResource(it.asMediaItem.mediaId)
                                                query {
                                                    Database.insert(
                                                        Song(
                                                            id = it.asMediaItem.mediaId,
                                                            title = it.asMediaItem.mediaMetadata.title.toString(),
                                                            artistsText = it.asMediaItem.mediaMetadata.artist.toString(),
                                                            thumbnailUrl = it.thumbnailUrl,
                                                            durationText = null
                                                        )
                                                    )
                                                }
                                                manageDownload(
                                                    context = context,
                                                    songId = it.asMediaItem.mediaId,
                                                    songTitle = it.asMediaItem.mediaMetadata.title.toString(),
                                                    downloadState = false
                                                )
                                            }
                                    }
                                )
                            }

                            HeaderIconButton(
                                icon = R.drawable.download,
                                color = colorPalette.text,
                                onClick = {},
                                modifier = Modifier
                                    .combinedClickable(
                                        onClick = {
                                            showConfirmDeleteDownloadDialog = true
                                        },
                                        onLongClick = {
                                            SmartMessage(context.resources.getString(R.string.info_remove_all_downloaded_songs), context = context)
                                        }
                                    )
                            )

                            if (showConfirmDeleteDownloadDialog) {
                                ConfirmationDialog(
                                    text = stringResource(R.string.do_you_really_want_to_delete_download),
                                    onDismiss = { showConfirmDeleteDownloadDialog = false },
                                    onConfirm = {
                                        showConfirmDeleteDownloadDialog = false
                                        downloadState = Download.STATE_DOWNLOADING
                                        if (songs?.isNotEmpty() == true)
                                            songs?.forEach {
                                                binder?.cache?.removeResource(it.asMediaItem.mediaId)
                                                manageDownload(
                                                    context = context,
                                                    songId = it.asMediaItem.mediaId,
                                                    songTitle = it.asMediaItem.mediaMetadata.title.toString(),
                                                    downloadState = true
                                                )
                                            }
                                    }
                                )
                            }

                            HeaderIconButton(
                                icon = R.drawable.enqueue,
                                enabled = !songs.isNullOrEmpty(),
                                color = if (!songs.isNullOrEmpty()) colorPalette.text else colorPalette.textDisabled,
                                onClick = {  },
                                modifier = Modifier
                                    .combinedClickable(
                                        onClick = {
                                            binder?.player?.enqueue(songs!!.map(Song::asMediaItem), context)
                                        },
                                        onLongClick = {
                                            SmartMessage(context.resources.getString(R.string.info_enqueue_songs), context = context)
                                        }
                                    )
                            )
                            HeaderIconButton(
                                icon = R.drawable.shuffle,
                                enabled = !songs.isNullOrEmpty(),
                                color = if (!songs.isNullOrEmpty()) colorPalette.text else colorPalette.textDisabled,
                                onClick = {},
                                modifier = Modifier
                                    .combinedClickable(
                                        onClick = {
                                            songs?.let { songs ->
                                                if (songs.isNotEmpty()) {
                                                    binder?.stopRadio()
                                                    binder?.player?.forcePlayFromBeginning(
                                                        songs.shuffled().map(Song::asMediaItem)
                                                    )
                                                }
                                            }
                                        },
                                        onLongClick = {
                                            SmartMessage(context.resources.getString(R.string.info_shuffle), context = context)
                                        }
                                    )
                            )
                        }

                        thumbnailContent()
                    }
                }

                songs?.let { songs ->
                    itemsIndexed(
                        items = songs,
                        key = { _, song -> song.id }
                    ) { index, song ->

                        downloadState = getDownloadState(song.asMediaItem.mediaId)
                        val isDownloaded = downloadedStateMedia(song.asMediaItem.mediaId)
                        SongItem(
                            song = song,
                            isDownloaded = isDownloaded,
                            onDownloadClick = {
                                binder?.cache?.removeResource(song.asMediaItem.mediaId)
                                query {
                                    Database.insert(
                                        Song(
                                            id = song.asMediaItem.mediaId,
                                            title = song.asMediaItem.mediaMetadata.title.toString(),
                                            artistsText = song.asMediaItem.mediaMetadata.artist.toString(),
                                            thumbnailUrl = song.thumbnailUrl,
                                            durationText = null
                                        )
                                    )
                                }

                                manageDownload(
                                    context = context,
                                    songId = song.id,
                                    songTitle = song.title,
                                    downloadState = isDownloaded
                                )
                            },
                            downloadState = downloadState,
                            thumbnailSizeDp = songThumbnailSizeDp,
                            thumbnailSizePx = songThumbnailSizePx,
                            modifier = Modifier
                                .combinedClickable(
                                    onLongClick = {
                                        menuState.display {
                                            NonQueuedMediaItemMenu(
                                                navController = navController,
                                                onDismiss = menuState::hide,
                                                mediaItem = song.asMediaItem,
                                            )
                                        }
                                    },
                                    onClick = {
                                        binder?.stopRadio()
                                        binder?.player?.forcePlayAtIndex(
                                            songs.map(Song::asMediaItem),
                                            index
                                        )
                                    }
                                )
                        )
                    }
                } ?: item(key = "loading") {
                    ShimmerHost {
                        repeat(4) {
                            SongItemPlaceholder(thumbnailSizeDp = Dimensions.thumbnails.song)
                        }
                    }
                }
            }

            val showFloatingIcon by rememberPreference(showFloatingIconKey, false)
            if(uiType == UiType.ViMusic || showFloatingIcon)
                MultiFloatingActionsContainer(
                    iconId = R.drawable.shuffle,
                    onClick = {
                        songs?.let { songs ->
                            if (songs.isNotEmpty()) {
                                binder?.stopRadio()
                                binder?.player?.forcePlayFromBeginning(
                                    songs.shuffled().map(Song::asMediaItem)
                                )
                            }
                        }
                    },
                    onClickSettings = onSettingsClick,
                    onClickSearch = onSearchClick
                )

                /*
                FloatingActionsContainerWithScrollToTop(
                    lazyListState = lazyListState,
                    iconId = R.drawable.shuffle,
                    onClick = {
                        songs?.let { songs ->
                            if (songs.isNotEmpty()) {
                                binder?.stopRadio()
                                binder?.player?.forcePlayFromBeginning(
                                    songs.shuffled().map(Song::asMediaItem)
                                )
                            }
                        }
                    }
                )

                 */





        }
    }
}
