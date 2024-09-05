package com.peecock.ymusic.ui.screens.album

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download
import androidx.navigation.NavController
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Size
import com.peecock.compose.persist.persist
import com.peecock.compose.persist.persistList
import com.peecock.ymusic.Database
import com.peecock.ymusic.LocalPlayerServiceBinder
import com.peecock.ymusic.R
import com.peecock.ymusic.enums.PopupType
import com.peecock.ymusic.enums.UiType
import com.peecock.ymusic.models.Album
import com.peecock.ymusic.models.Info
import com.peecock.ymusic.models.Playlist
import com.peecock.ymusic.models.Song
import com.peecock.ymusic.models.SongPlaylistMap
import com.peecock.ymusic.query
import com.peecock.ymusic.service.isLocal
import com.peecock.ymusic.transaction
import com.peecock.ymusic.ui.components.LocalMenuState
import com.peecock.ymusic.ui.components.ShimmerHost
import com.peecock.ymusic.ui.components.SwipeablePlaylistItem
import com.peecock.ymusic.ui.components.themed.AlbumsItemMenu
import com.peecock.ymusic.ui.components.themed.ConfirmationDialog
import com.peecock.ymusic.ui.components.themed.HeaderIconButton
import com.peecock.ymusic.ui.components.themed.HeaderWithIcon
import com.peecock.ymusic.ui.components.themed.InputTextDialog
import com.peecock.ymusic.ui.components.themed.LayoutWithAdaptiveThumbnail
import com.peecock.ymusic.ui.components.themed.MultiFloatingActionsContainer
import com.peecock.ymusic.ui.components.themed.NonQueuedMediaItemMenu
import com.peecock.ymusic.ui.components.themed.NowPlayingShow
import com.peecock.ymusic.ui.components.themed.SelectorDialog
import com.peecock.ymusic.ui.components.themed.SmartMessage
import com.peecock.ymusic.ui.items.AlbumItemPlaceholder
import com.peecock.ymusic.ui.items.SongItem
import com.peecock.ymusic.ui.items.SongItemPlaceholder
import com.peecock.ymusic.ui.screens.home.MODIFIED_PREFIX
import com.peecock.ymusic.ui.styling.Dimensions
import com.peecock.ymusic.ui.styling.LocalAppearance
import com.peecock.ymusic.utils.UiTypeKey
import com.peecock.ymusic.utils.addNext
import com.peecock.ymusic.utils.asMediaItem
import com.peecock.ymusic.utils.center
import com.peecock.ymusic.utils.color
import com.peecock.ymusic.utils.downloadedStateMedia
import com.peecock.ymusic.utils.durationTextToMillis
import com.peecock.ymusic.utils.enqueue
import com.peecock.ymusic.utils.forcePlayAtIndex
import com.peecock.ymusic.utils.forcePlayFromBeginning
import com.peecock.ymusic.utils.formatAsTime
import com.peecock.ymusic.utils.getDownloadState
import com.peecock.ymusic.utils.isLandscape
import com.peecock.ymusic.utils.manageDownload
import com.peecock.ymusic.utils.medium
import com.peecock.ymusic.utils.rememberPreference
import com.peecock.ymusic.utils.semiBold
import com.peecock.ymusic.utils.showFloatingIconKey
import java.text.SimpleDateFormat
import java.util.Date

@ExperimentalTextApi
@SuppressLint("SuspiciousIndentation")
@ExperimentalAnimationApi
@ExperimentalFoundationApi
@UnstableApi
@Composable
fun AlbumSongs(
    navController: NavController,
    browseId: String,
    headerContent: @Composable (textButton: (@Composable () -> Unit)?) -> Unit,
    thumbnailContent: @Composable () -> Unit,
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val (colorPalette, typography) = LocalAppearance.current
    val binder = LocalPlayerServiceBinder.current
    val menuState = LocalMenuState.current
    val uiType  by rememberPreference(UiTypeKey, UiType.RiMusic)

    var songs by persistList<Song>("album/$browseId/songs")
    var album by persist<Album?>("album/$browseId")

    LaunchedEffect(Unit) {
        Database.albumSongs(browseId).collect { songs = it }
    }
    LaunchedEffect(Unit) {
        Database.album(browseId).collect { album = it }
    }

    /*
    val playlistPreviews by remember {
        Database.playlistPreviews(PlaylistSortBy.Name, SortOrder.Ascending)
    }.collectAsState(initial = emptyList(), context = Dispatchers.IO)

    var showPlaylistSelectDialog by remember {
        mutableStateOf(false)
    }
     */

    var showConfirmDeleteDownloadDialog by remember {
        mutableStateOf(false)
    }

    var showConfirmDownloadAllDialog by remember {
        mutableStateOf(false)
    }

    val thumbnailSizeDp = Dimensions.thumbnails.song

    val lazyListState = rememberLazyListState()

    val context = LocalContext.current
    var downloadState by remember {
        mutableStateOf(Download.STATE_STOPPED)
    }

    var listMediaItems = remember {
        mutableListOf<MediaItem>()
    }

    var selectItems by remember {
        mutableStateOf(false)
    }

    var showSelectDialog by remember {
        mutableStateOf(false)
    }

    /*
    var showAddPlaylistSelectDialog by remember {
        mutableStateOf(false)
    }
     */

    var showSelectCustomizeAlbumDialog by remember {
        mutableStateOf(false)
    }
    var showDialogChangeAlbumTitle by remember {
        mutableStateOf(false)
    }
    var showDialogChangeAlbumAuthors by remember {
        mutableStateOf(false)
    }
    var showDialogChangeAlbumCover by remember {
        mutableStateOf(false)
    }
    var isCreatingNewPlaylist by rememberSaveable {
        mutableStateOf(false)
    }
    var totalPlayTimes = 0L
    songs.forEach {
        totalPlayTimes += it.durationText?.let { it1 ->
            durationTextToMillis(it1) }?.toLong() ?: 0
    }
    var position by remember {
        mutableIntStateOf(0)
    }

    var scrollToNowPlaying by remember {
        mutableStateOf(false)
    }

    var nowPlayingItem by remember {
        mutableStateOf(-1)
    }

    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(LocalContext.current)
            .data(album?.thumbnailUrl)
            .size(Size.ORIGINAL)
            .build()
    )

    var bitmap = remember<Bitmap?> {
        null
    }
    val imageState = painter.state

    val exportLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("image/png")) { uri ->
            if (uri == null) return@rememberLauncherForActivityResult

            context.applicationContext.contentResolver.openOutputStream(uri)
                ?.use { outputStream ->
                    if (imageState is AsyncImagePainter.State.Success) {
                        bitmap = imageState.result.drawable.toBitmap()
                        try {
                            bitmap?.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                            outputStream.flush()
                            outputStream.close()
                        } catch (e: Exception) {
                            e.printStackTrace()
                            SmartMessage(context.resources.getString(R.string.info_error), type = PopupType.Error, context = context)
                        }
                    } else
                        SmartMessage(context.resources.getString(R.string.info_error), type = PopupType.Error, context = context)
                }

        }



    if (showDialogChangeAlbumTitle)
        InputTextDialog(
            onDismiss = { showDialogChangeAlbumTitle = false },
            title = stringResource(R.string.update_title),
            value = album?.title.toString(),
            placeholder = stringResource(R.string.title),
            setValue = {
                if (it.isNotEmpty()) {
                    query {
                        Database.updateAlbumTitle(browseId, it)
                    }
                    //context.toast("Album Saved $it")
                }
            },
            prefix = MODIFIED_PREFIX
        )
    if (showDialogChangeAlbumAuthors)
        InputTextDialog(
            onDismiss = { showDialogChangeAlbumAuthors = false },
            title = stringResource(R.string.update_authors),
            value = album?.authorsText.toString(),
            placeholder = stringResource(R.string.authors),
            setValue = {
                if (it.isNotEmpty()) {
                    query {
                        Database.updateAlbumAuthors(browseId, it)
                    }
                    //context.toast("Album Saved $it")
                }
            }
        )

    if (showDialogChangeAlbumCover)
        InputTextDialog(
            onDismiss = { showDialogChangeAlbumCover = false },
            title = stringResource(R.string.update_cover),
            value = album?.thumbnailUrl.toString(),
            placeholder = stringResource(R.string.cover),
            setValue = {
                if (it.isNotEmpty()) {
                    query {
                        Database.updateAlbumCover(browseId, it)
                    }
                    //context.toast("Album Saved $it")
                }
            }
        )

    if (isCreatingNewPlaylist)
        InputTextDialog(
            onDismiss = { isCreatingNewPlaylist = false },
            title = stringResource(R.string.new_playlist),
            value = "",
            placeholder = stringResource(R.string.new_playlist),
            setValue = {
                if (it.isNotEmpty()) {
                    query {
                        Database.insert(Playlist(name = it))
                    }
                    //context.toast("Song Saved $it")
                }
            }
        )

    if (showConfirmDeleteDownloadDialog) {
        ConfirmationDialog(
            text = stringResource(R.string.do_you_really_want_to_delete_download),
            onDismiss = { showConfirmDeleteDownloadDialog = false },
            onConfirm = {
                showConfirmDeleteDownloadDialog = false
                downloadState = Download.STATE_DOWNLOADING
                if (songs.isNotEmpty() == true)
                    songs.forEach {
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

    if (showConfirmDownloadAllDialog) {
        ConfirmationDialog(
            text = stringResource(R.string.do_you_really_want_to_download_all),
            onDismiss = { showConfirmDownloadAllDialog = false },
            onConfirm = {
                showConfirmDownloadAllDialog = false
                downloadState = Download.STATE_DOWNLOADING
                if (songs.isNotEmpty() == true)
                    songs.forEach {
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

    if (showSelectDialog)
        SelectorDialog(
            title = stringResource(R.string.enqueue),
            onDismiss = { showSelectDialog = false },
            values = listOf(
                Info("a", stringResource(R.string.enqueue_all)),
                Info("s", stringResource(R.string.enqueue_selected))
            ),
            onValueSelected = {
                if (it == "a") {
                    binder?.player?.enqueue(songs.map(Song::asMediaItem))
                } else selectItems = true

                showSelectDialog = false
            }
        )

    LaunchedEffect(scrollToNowPlaying) {
        if (scrollToNowPlaying)
            lazyListState.scrollToItem(nowPlayingItem, 1)
        scrollToNowPlaying = false
    }

    LayoutWithAdaptiveThumbnail(thumbnailContent = thumbnailContent) {
        Box(
            modifier = Modifier
                .background(colorPalette.background0)
                //.fillMaxSize()
                .fillMaxHeight()
                //.fillMaxWidth(if (navigationBarPosition == NavigationBarPosition.Left) 1f else contentWidth)
                .fillMaxWidth()
        ) {

                LazyColumn(
                    state = lazyListState,
                    //contentPadding = LocalPlayerAwareWindowInsets.current
                    //    .only(WindowInsetsSides.Vertical + WindowInsetsSides.End).asPaddingValues(),
                    modifier = Modifier
                        .background(colorPalette.background0)
                        .fillMaxSize()
                ) {
                    item(
                        key="title"
                    ) {
                        HeaderWithIcon(
                            title = album?.title ?: "",
                            iconId = R.drawable.album,
                            enabled = false,
                            showIcon = false,
                            modifier = Modifier,
                            onClick = {}
                        )
                    }
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



                                /*
                            HeaderIconButton(
                                icon = R.drawable.enqueue,
                                enabled = songs.isNotEmpty(),
                                color = if (songs.isNotEmpty()) colorPalette.text else colorPalette.textDisabled,
                                onClick = {
                                    if (!selectItems)
                                    showSelectDialog = true else {
                                        binder?.player?.enqueue(listMediaItems)
                                        listMediaItems.clear()
                                        selectItems = false
                                    }

                                }
                            )
                             */



                                HeaderIconButton(
                                    icon = R.drawable.shuffle,
                                    enabled = songs.isNotEmpty(),
                                    color = if (songs.isNotEmpty()) colorPalette.text else colorPalette.textDisabled,
                                    onClick = {},
                                    modifier = Modifier
                                        .combinedClickable(
                                            onClick = {
                                                if (songs.isNotEmpty()) {
                                                    binder?.stopRadio()
                                                    binder?.player?.forcePlayFromBeginning(
                                                        songs.shuffled().map(Song::asMediaItem)
                                                    )
                                                }
                                            },
                                            onLongClick = {
                                                SmartMessage(context.resources.getString(R.string.info_shuffle), context = context)
                                            }
                                        )
                                )

                                HeaderIconButton(
                                    modifier = Modifier
                                        .padding(horizontal = 5.dp)
                                        .combinedClickable(
                                            onClick = {
                                                nowPlayingItem = -1
                                                scrollToNowPlaying = false
                                                songs
                                                    .forEachIndexed { index, song ->
                                                        if (song.asMediaItem.mediaId == binder?.player?.currentMediaItem?.mediaId)
                                                            nowPlayingItem = index
                                                    }

                                                if (nowPlayingItem > -1)
                                                    scrollToNowPlaying = true
                                            },
                                            onLongClick = {
                                                SmartMessage(context.resources.getString(R.string.info_find_the_song_that_is_playing), context = context)
                                            }
                                        ),
                                    icon = R.drawable.locate,
                                    enabled = songs.isNotEmpty(),
                                    color = if (songs.isNotEmpty()) colorPalette.text else colorPalette.textDisabled,
                                    onClick = {}


                                )


                                HeaderIconButton(
                                    icon = R.drawable.ellipsis_horizontal,
                                    enabled = songs.isNotEmpty(),
                                    color = if (songs.isNotEmpty()) colorPalette.text else colorPalette.textDisabled,
                                    onClick = {
                                        menuState.display {
                                            album?.let {
                                                AlbumsItemMenu(
                                                    onDismiss = menuState::hide,
                                                    onSelectUnselect = {
                                                        selectItems = !selectItems
                                                        if (!selectItems) {
                                                            listMediaItems.clear()
                                                        }
                                                    },
                                                    /*
                                                onSelect = { selectItems = true },
                                                onUncheck = {
                                                    selectItems = false
                                                    listMediaItems.clear()
                                                },
                                                 */
                                                    onChangeAlbumTitle = {
                                                        showDialogChangeAlbumTitle = true
                                                    },
                                                    onChangeAlbumAuthors = {
                                                        showDialogChangeAlbumAuthors = true
                                                    },
                                                    onChangeAlbumCover = {
                                                        showDialogChangeAlbumCover = true
                                                    },
                                                    onDownloadAlbumCover = {
                                                        try {
                                                            @SuppressLint("SimpleDateFormat")
                                                            val dateFormat =
                                                                SimpleDateFormat("yyyyMMddHHmmss")
                                                            exportLauncher.launch(
                                                                "ImageCover_${
                                                                    dateFormat.format(
                                                                        Date()
                                                                    )
                                                                }"
                                                            )
                                                        } catch (e: ActivityNotFoundException) {
                                                            SmartMessage("Couldn't find an application to create documents", type = PopupType.Warning, context = context)
                                                        }
                                                    },
                                                    onPlayNext = {
                                                        if (listMediaItems.isEmpty()) {
                                                            binder?.player?.addNext(songs.map(Song::asMediaItem), context)
                                                        } else {
                                                            binder?.player?.addNext(listMediaItems, context)
                                                            listMediaItems.clear()
                                                            selectItems = false
                                                        }
                                                    },
                                                    onEnqueue = {
                                                        if (listMediaItems.isEmpty()) {
                                                            binder?.player?.enqueue(songs.map(Song::asMediaItem), context)
                                                        } else {
                                                            binder?.player?.enqueue(listMediaItems, context)
                                                            listMediaItems.clear()
                                                            selectItems = false
                                                        }
                                                    },
                                                    album = it,
                                                    onAddToPlaylist = { playlistPreview ->
                                                        position =
                                                            playlistPreview.songCount.minus(1) ?: 0
                                                        //Log.d("mediaItem", " maxPos in Playlist $it ${position}")
                                                        if (position > 0) position++ else position =
                                                            0
                                                        //Log.d("mediaItem", "next initial pos ${position}")
                                                        if (listMediaItems.isEmpty()) {
                                                            songs.forEachIndexed { index, song ->
                                                                transaction {
                                                                    Database.insert(song.asMediaItem)
                                                                    Database.insert(
                                                                        SongPlaylistMap(
                                                                            songId = song.asMediaItem.mediaId,
                                                                            playlistId = playlistPreview.playlist.id,
                                                                            position = position + index
                                                                        )
                                                                    )
                                                                }
                                                                //Log.d("mediaItemPos", "added position ${position + index}")
                                                            }
                                                        } else {
                                                            listMediaItems.forEachIndexed { index, song ->
                                                                //Log.d("mediaItemMaxPos", position.toString())
                                                                transaction {
                                                                    Database.insert(song)
                                                                    Database.insert(
                                                                        SongPlaylistMap(
                                                                            songId = song.mediaId,
                                                                            playlistId = playlistPreview.playlist.id,
                                                                            position = position + index
                                                                        )
                                                                    )
                                                                }
                                                                //Log.d("mediaItemPos", "add position $position")
                                                            }
                                                            listMediaItems.clear()
                                                            selectItems = false
                                                        }
                                                    },
                                                )
                                            }
                                        }

                                    }
                                )
                            }

                            if (!isLandscape) {
                                thumbnailContent()
                            }

                            /*
                            album?.title?.let {
                                BasicText(
                                    text = it,
                                    style = typography.xs.semiBold,
                                    maxLines = 1
                                )
                            }
                             */
                            if (album != null) {
                                BasicText(
                                    text = songs.size.toString() + " "
                                            + stringResource(R.string.songs)
                                            + " - " + formatAsTime(totalPlayTimes),
                                    style = typography.xxs.medium,
                                    maxLines = 1,
                                    modifier = Modifier
                                        .padding(all = 5.dp)
                                )
                            } else {
                                BasicText(
                                    text = stringResource(R.string.info_wait_it_may_take_a_few_minutes),
                                    style = typography.xxs.medium,
                                    maxLines = 1
                                )
                            }



                        }
                    }
                        itemsIndexed(
                            items = songs,
                            key = { _, song -> song.id }
                        ) { index, song ->
                            val isLocal by remember { derivedStateOf { song.asMediaItem.isLocal } }
                            downloadState = getDownloadState(song.asMediaItem.mediaId)
                            val isDownloaded =
                                if (!isLocal) downloadedStateMedia(song.asMediaItem.mediaId) else true
                            val checkedState = rememberSaveable { mutableStateOf(false) }
                            SwipeablePlaylistItem(
                                mediaItem = song.asMediaItem,
                                onSwipeToRight = {
                                    binder?.player?.addNext(song.asMediaItem)
                                }
                            ) {
                                SongItem(
                                    title = song.title,
                                    isDownloaded = isDownloaded,
                                    downloadState = downloadState,
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
                                        if (!isLocal)
                                            manageDownload(
                                                context = context,
                                                songId = song.asMediaItem.mediaId,
                                                songTitle = song.asMediaItem.mediaMetadata.title.toString(),
                                                downloadState = isDownloaded
                                            )
                                    },
                                    authors = song.artistsText,
                                    duration = song.durationText,
                                    thumbnailSizeDp = thumbnailSizeDp,
                                    thumbnailContent = {
                                        /*
                                    AsyncImage(
                                        model = song.thumbnailUrl,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .clip(LocalAppearance.current.thumbnailShape)
                                            .fillMaxSize()
                                    )
                                     */

                                        BasicText(
                                            text = "${index + 1}",
                                            style = typography.s.semiBold.center.color(colorPalette.textDisabled),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier
                                                .width(thumbnailSizeDp)
                                                .align(Alignment.Center)
                                        )

                                        if (nowPlayingItem > -1)
                                            NowPlayingShow(song.asMediaItem.mediaId)
                                    },
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
                                                if (!selectItems) {
                                                    binder?.stopRadio()
                                                    binder?.player?.forcePlayAtIndex(
                                                        songs.map(Song::asMediaItem),
                                                        index
                                                    )
                                                } else checkedState.value = !checkedState.value
                                            }
                                        ),
                                    trailingContent = {
                                        if (selectItems)
                                            Checkbox(
                                                checked = checkedState.value,
                                                onCheckedChange = {
                                                    checkedState.value = it
                                                    if (it) listMediaItems.add(song.asMediaItem) else
                                                        listMediaItems.remove(song.asMediaItem)
                                                },
                                                colors = CheckboxDefaults.colors(
                                                    checkedColor = colorPalette.accent,
                                                    uncheckedColor = colorPalette.text
                                                ),
                                                modifier = Modifier
                                                    .scale(0.7f)
                                            )
                                        else checkedState.value = false
                                    },
                                    mediaId = song.asMediaItem.mediaId
                                )
                            }
                        }

                    item(key = "bottom"){
                        Spacer(modifier = Modifier.height(Dimensions.bottomSpacer))
                    }

                    if (songs.isEmpty()) {
                        item(key = "loading") {
                            ShimmerHost(
                                modifier = Modifier
                                    .fillParentMaxSize()
                            ) {
                                repeat(1) {
                                    AlbumItemPlaceholder(thumbnailSizeDp = Dimensions.thumbnails.album)
                                }
                                repeat(4) {
                                    SongItemPlaceholder(thumbnailSizeDp = Dimensions.thumbnails.song)
                                }
                            }
                        }
                    }
                }


            val showFloatingIcon by rememberPreference(showFloatingIconKey, false)
            if(uiType == UiType.ViMusic || showFloatingIcon)
                MultiFloatingActionsContainer(
                    iconId = R.drawable.shuffle,
                    onClick = {
                        if (songs.isNotEmpty()) {
                            binder?.stopRadio()
                            binder?.player?.forcePlayFromBeginning(
                                songs.shuffled().map(Song::asMediaItem)
                            )
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
                        if (songs.isNotEmpty()) {
                            binder?.stopRadio()
                            binder?.player?.forcePlayFromBeginning(
                                songs.shuffled().map(Song::asMediaItem)
                            )
                        }
                    }
                )

                 */





        }
    }
}
