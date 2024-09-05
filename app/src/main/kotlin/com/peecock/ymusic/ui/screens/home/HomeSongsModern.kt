package com.peecock.ymusic.ui.screens.home

import android.Manifest
import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download
import androidx.navigation.NavController
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import com.peecock.compose.persist.persistList
import com.peecock.ymusic.Database
import com.peecock.ymusic.LocalPlayerServiceBinder
import com.peecock.ymusic.R
import com.peecock.ymusic.enums.BuiltInPlaylist
import com.peecock.ymusic.enums.DurationInMinutes
import com.peecock.ymusic.enums.MaxSongs
import com.peecock.ymusic.enums.MaxTopPlaylistItems
import com.peecock.ymusic.enums.NavigationBarPosition
import com.peecock.ymusic.enums.OnDeviceFolderSortBy
import com.peecock.ymusic.enums.OnDeviceSongSortBy
import com.peecock.ymusic.enums.PopupType
import com.peecock.ymusic.enums.SongSortBy
import com.peecock.ymusic.enums.SortOrder
import com.peecock.ymusic.enums.ThumbnailRoundness
import com.peecock.ymusic.enums.TopPlaylistPeriod
import com.peecock.ymusic.enums.UiType
import com.peecock.ymusic.models.Folder
import com.peecock.ymusic.models.OnDeviceSong
import com.peecock.ymusic.models.Song
import com.peecock.ymusic.models.SongEntity
import com.peecock.ymusic.models.SongPlaylistMap
import com.peecock.ymusic.query
import com.peecock.ymusic.service.DownloadUtil
import com.peecock.ymusic.service.LOCAL_KEY_PREFIX
import com.peecock.ymusic.service.isLocal
import com.peecock.ymusic.ui.components.ButtonsRow
import com.peecock.ymusic.ui.components.LocalMenuState
import com.peecock.ymusic.ui.components.SwipeablePlaylistItem
import com.peecock.ymusic.ui.components.themed.ConfirmationDialog
import com.peecock.ymusic.ui.components.themed.FloatingActionsContainerWithScrollToTop
import com.peecock.ymusic.ui.components.themed.FolderItemMenu
import com.peecock.ymusic.ui.components.themed.HeaderIconButton
import com.peecock.ymusic.ui.components.themed.HeaderInfo
import com.peecock.ymusic.ui.components.themed.HeaderWithIcon
import com.peecock.ymusic.ui.components.themed.IconButton
import com.peecock.ymusic.ui.components.themed.InHistoryMediaItemMenu
import com.peecock.ymusic.ui.components.themed.InputTextDialog
import com.peecock.ymusic.ui.components.themed.MultiFloatingActionsContainer
import com.peecock.ymusic.ui.components.themed.NowPlayingShow
import com.peecock.ymusic.ui.components.themed.PeriodMenu
import com.peecock.ymusic.ui.components.themed.PlaylistsItemMenu
import com.peecock.ymusic.ui.components.themed.SecondaryTextButton
import com.peecock.ymusic.ui.components.themed.SmartMessage
import com.peecock.ymusic.ui.components.themed.SortMenu
import com.peecock.ymusic.ui.components.themed.TitleSection
import com.peecock.ymusic.ui.items.FolderItem
import com.peecock.ymusic.ui.items.SongItem
import com.peecock.ymusic.ui.screens.ondevice.musicFilesAsFlow
import com.peecock.ymusic.ui.styling.Dimensions
import com.peecock.ymusic.ui.styling.LocalAppearance
import com.peecock.ymusic.ui.styling.favoritesIcon
import com.peecock.ymusic.ui.styling.onOverlay
import com.peecock.ymusic.ui.styling.overlay
import com.peecock.ymusic.ui.styling.px
import com.peecock.ymusic.utils.MaxTopPlaylistItemsKey
import com.peecock.ymusic.utils.OnDeviceOrganize
import com.peecock.ymusic.utils.UiTypeKey
import com.peecock.ymusic.utils.addNext
import com.peecock.ymusic.utils.asMediaItem
import com.peecock.ymusic.utils.autoShuffleKey
import com.peecock.ymusic.utils.builtInPlaylistKey
import com.peecock.ymusic.utils.center
import com.peecock.ymusic.utils.color
import com.peecock.ymusic.utils.defaultFolderKey
import com.peecock.ymusic.utils.downloadedStateMedia
import com.peecock.ymusic.utils.durationTextToMillis
import com.peecock.ymusic.utils.enqueue
import com.peecock.ymusic.utils.excludeSongsWithDurationLimitKey
import com.peecock.ymusic.utils.forcePlayAtIndex
import com.peecock.ymusic.utils.forcePlayFromBeginning
import com.peecock.ymusic.utils.getDownloadState
import com.peecock.ymusic.utils.hasPermission
import com.peecock.ymusic.utils.includeLocalSongsKey
import com.peecock.ymusic.utils.isCompositionLaunched
import com.peecock.ymusic.utils.manageDownload
import com.peecock.ymusic.utils.maxSongsInQueueKey
import com.peecock.ymusic.utils.navigationBarPositionKey
import com.peecock.ymusic.utils.onDeviceFolderSortByKey
import com.peecock.ymusic.utils.onDeviceSongSortByKey
import com.peecock.ymusic.utils.rememberPreference
import com.peecock.ymusic.utils.secondary
import com.peecock.ymusic.utils.semiBold
import com.peecock.ymusic.utils.showCachedPlaylistKey
import com.peecock.ymusic.utils.showDownloadedPlaylistKey
import com.peecock.ymusic.utils.showFavoritesPlaylistKey
import com.peecock.ymusic.utils.showFloatingIconKey
import com.peecock.ymusic.utils.showFoldersOnDeviceKey
import com.peecock.ymusic.utils.showMyTopPlaylistKey
import com.peecock.ymusic.utils.showOnDevicePlaylistKey
import com.peecock.ymusic.utils.showSearchTabKey
import com.peecock.ymusic.utils.songSortByKey
import com.peecock.ymusic.utils.songSortOrderKey
import com.peecock.ymusic.utils.thumbnailRoundnessKey
import com.peecock.ymusic.utils.topPlaylistPeriodKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.time.Duration


@ExperimentalTextApi
@SuppressLint("SuspiciousIndentation")
@ExperimentalFoundationApi
@ExperimentalAnimationApi
@ExperimentalComposeUiApi
@UnstableApi
@Composable
fun HomeSongsModern(
    navController: NavController,
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val (colorPalette, typography, thumbnailShape) = LocalAppearance.current
    val binder = LocalPlayerServiceBinder.current
    val menuState = LocalMenuState.current
    val uiType  by rememberPreference(UiTypeKey, UiType.RiMusic)

    val thumbnailSizeDp = Dimensions.thumbnails.song
    val thumbnailSizePx = thumbnailSizeDp.px

    var sortBy by rememberPreference(songSortByKey, SongSortBy.DateAdded)
    var sortOrder by rememberPreference(songSortOrderKey, SortOrder.Descending)

    var items by persistList<SongEntity>("home/songs")

    //var songsWithAlbum by persistList<SongWithAlbum>("home/songsWithAlbum")

    /*
    var filterDownloaded by remember {
        mutableStateOf(false)
    }
     */

    var filter: String? by rememberSaveable { mutableStateOf(null) }
    var builtInPlaylist by rememberPreference(
        builtInPlaylistKey,
        BuiltInPlaylist.Favorites
    )

    var downloadState by remember {
        mutableStateOf(Download.STATE_STOPPED)
    }

    val context = LocalContext.current

    var thumbnailRoundness by rememberPreference(
        thumbnailRoundnessKey,
        ThumbnailRoundness.Heavy
    )

    var showHiddenSongs by remember {
        mutableStateOf(0)
    }

    var includeLocalSongs by rememberPreference(includeLocalSongsKey, true)
    var autoShuffle by rememberPreference(autoShuffleKey, false)

    val maxTopPlaylistItems by rememberPreference(
        MaxTopPlaylistItemsKey,
        MaxTopPlaylistItems.`10`
    )
    var topPlaylistPeriod by rememberPreference(topPlaylistPeriodKey, TopPlaylistPeriod.PastWeek)

    var scrollToNowPlaying by remember {
        mutableStateOf(false)
    }

    var nowPlayingItem by remember {
        mutableStateOf(-1)
    }

    /************ OnDeviceDev */
    val permission = if (Build.VERSION.SDK_INT >= 33) Manifest.permission.READ_MEDIA_AUDIO
    else Manifest.permission.READ_EXTERNAL_STORAGE

    var relaunchPermission by remember {
        mutableStateOf(false)
    }

    var hasPermission by remember(isCompositionLaunched()) {
        mutableStateOf(context.applicationContext.hasPermission(permission))
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { hasPermission = it }
    )
    val backButtonFolder = Folder(stringResource(R.string.back))
    val showFolders by rememberPreference(showFoldersOnDeviceKey, true)

    var sortByOnDevice by rememberPreference(onDeviceSongSortByKey, OnDeviceSongSortBy.DateAdded)
    var sortByFolderOnDevice by rememberPreference(onDeviceFolderSortByKey, OnDeviceFolderSortBy.Title)
    var sortOrderOnDevice by rememberPreference(songSortOrderKey, SortOrder.Descending)

    val defaultFolder by rememberPreference(defaultFolderKey, "/")

    var songsDevice by remember(sortBy, sortOrder) {
        mutableStateOf<List<OnDeviceSong>>(emptyList())
    }
    var songs: List<SongEntity> = emptyList()
    var folders: List<Folder> = emptyList()
    var filteredSongs = songs
    var filteredFolders = folders
    var currentFolder: Folder? = null;
    var currentFolderPath by remember {
        mutableStateOf(defaultFolder)
    }

    /************ */

    val showFavoritesPlaylist by rememberPreference(showFavoritesPlaylistKey, true)
    val showCachedPlaylist by rememberPreference(showCachedPlaylistKey, true)
    val showMyTopPlaylist by rememberPreference(showMyTopPlaylistKey, true)
    val showDownloadedPlaylist by rememberPreference(showDownloadedPlaylistKey, true)
    val showOnDevicePlaylist by rememberPreference(showOnDevicePlaylistKey, true)

    var buttonsList = listOf(BuiltInPlaylist.All to stringResource(R.string.all))
    if (showFavoritesPlaylist) buttonsList +=
        BuiltInPlaylist.Favorites to stringResource(R.string.favorites)
    //if (showCachedPlaylist) buttonsList +=
    //    BuiltInPlaylist.Offline to stringResource(R.string.cached)
    if (showDownloadedPlaylist) buttonsList +=
        BuiltInPlaylist.Downloaded to stringResource(R.string.downloaded)
    if (showMyTopPlaylist) buttonsList +=
        BuiltInPlaylist.Top to String.format(stringResource(R.string.my_playlist_top),maxTopPlaylistItems.number)
    if (showOnDevicePlaylist) buttonsList +=
        BuiltInPlaylist.OnDevice to stringResource(R.string.on_device)

    val excludeSongWithDurationLimit by rememberPreference(excludeSongsWithDurationLimitKey, DurationInMinutes.Disabled)
    val hapticFeedback = LocalHapticFeedback.current

    when (builtInPlaylist) {
        BuiltInPlaylist.All -> {
            LaunchedEffect(sortBy, sortOrder, filter, showHiddenSongs, includeLocalSongs) {
                //Database.songs(sortBy, sortOrder, showHiddenSongs).collect { items = it }
                Database.songs(sortBy, sortOrder, showHiddenSongs).collect { items = it }

            }
        }
        BuiltInPlaylist.Downloaded, BuiltInPlaylist.Favorites, BuiltInPlaylist.Offline, BuiltInPlaylist.Top -> {

            LaunchedEffect(Unit, builtInPlaylist, sortBy, sortOrder, filter, topPlaylistPeriod) {

                    if (builtInPlaylist == BuiltInPlaylist.Downloaded) {
                        val downloads = DownloadUtil.downloads.value
                        Database.listAllSongsAsFlow()
                            .combine(
                                Database
                                    .songsOffline(sortBy, sortOrder)
                            ){ a, b ->
                                a.filter { song ->
                                    downloads[song.song.id]?.state == Download.STATE_COMPLETED
                                }.union(
                                    b.filter { binder?.isCached(it) ?: false } //.map { it.song }
                                )
                            }
                            .collect {
                                items = it.toList()
                            }

                        /*
                        val downloads = DownloadUtil.downloads.value
                        Database.listAllSongsAsFlow()
                            .map {
                                it.filter { song ->
                                    downloads[song.id]?.state == Download.STATE_COMPLETED
                                }
                            }
                            .collect {
                                items = it
                            }
                         */
                    }

                    if (builtInPlaylist == BuiltInPlaylist.Favorites) {
                        Database.songsFavorites(sortBy, sortOrder)
                            .collect {
                                items =
                                    if (autoShuffle)
                                        it.shuffled()
                                    else it
                            }
                    }

                /*
                if (builtInPlaylist == BuiltInPlaylist.Offline) {
                    Database
                        .songsOffline(sortBy, sortOrder)
                        .map { songs ->
                            songs.filter { binder?.isCached(it) ?: false }.map { it.song }
                        }
                        .collect {
                            items = it
                        }

                    //println("mediaItem offline items: ${items.size} filter ${filter}")
                    /*

                                Database
                                    .songsOffline(sortBy, sortOrder)
                                    .map {
                                        it.filter { song ->
                                            song.contentLength?.let {
                                                withContext(Dispatchers.Main) {
                                                    binder?.cache?.isCached(
                                                        song.song.id,
                                                        0,
                                                        song.contentLength
                                                    )
                                                }
                                            } ?: false
                                        }.map(SongWithContentLength::song)
                                    }
                                    //.flowOn(Dispatchers.IO)
                                    .collect {
                                        items = it
                                    }
                            }

                    */
                }
                */
                if (builtInPlaylist == BuiltInPlaylist.Top) {

                        if (topPlaylistPeriod.duration == Duration.INFINITE) {
                            Database
                                .songsEntityByPlayTimeWithLimitDesc(limit = maxTopPlaylistItems.number.toInt())
                                .collect {
                                    items = it.filter { item ->
                                        if (excludeSongWithDurationLimit == DurationInMinutes.Disabled)
                                            true
                                        else
                                            item.song.durationText?.let { it1 ->
                                                durationTextToMillis(it1)
                                            }!! < excludeSongWithDurationLimit.minutesInMilliSeconds
                                    }
                                }
                        } else {
                            Database
                                .trendingSongEntity(
                                    limit = maxTopPlaylistItems.number.toInt(),
                                    period = topPlaylistPeriod.duration.inWholeMilliseconds
                                )
                                .collect {
                                    items = it.filter { item ->
                                        if (excludeSongWithDurationLimit == DurationInMinutes.Disabled)
                                            true
                                        else
                                            item.song.durationText?.let { it1 ->
                                                durationTextToMillis(it1)
                                            }!! < excludeSongWithDurationLimit.minutesInMilliSeconds
                                    }
                                }
                        }
                }
                /*
                if (builtInPlaylist == BuiltInPlaylist.Top) {
                    Database.trending(maxTopPlaylistItems.number.toInt())
                        //.collect { items = it }
                        .collect {
                            items = it.filter {
                                if (excludeSongWithDurationLimit == DurationInMinutes.Disabled)
                                    true
                                else
                                it.durationText?.let { it1 ->
                                    durationTextToMillis(it1)
                                }!! < excludeSongWithDurationLimit.minutesInMilliSeconds
                            }
                        }

                }
                */


            }
        }
        BuiltInPlaylist.OnDevice -> {
            items = emptyList()
            LaunchedEffect(sortByOnDevice, sortOrderOnDevice) {
                if (hasPermission)
                    context.musicFilesAsFlow(sortByOnDevice, sortOrderOnDevice, context)
                        .collect { songsDevice = it.distinctBy { song -> song.id } }
            }
        }
    }

    //println("mediaItem SongEntity: ${items.size} filter ${filter} $items")

    /********** OnDeviceDev */
    if (builtInPlaylist == BuiltInPlaylist.OnDevice) {
        if (showFolders) {
            val organized = OnDeviceOrganize.organizeSongsIntoFolders(songsDevice)
            currentFolder = OnDeviceOrganize.getFolderByPath(organized, currentFolderPath)
            songs = OnDeviceOrganize.sortSongs(
                sortOrder,
                sortByFolderOnDevice,
                currentFolder?.songs?.map { it.toSongEntity() } ?: emptyList())
            filteredSongs = songs
            folders = currentFolder?.subFolders?.toList() ?: emptyList()
            filteredFolders = folders
        } else {
            songs = songsDevice.map { it.toSongEntity() }
            filteredSongs = songs
        }
    }
    /********** */

    if (!includeLocalSongs && builtInPlaylist == BuiltInPlaylist.All)
        items = items
            .filter {
                !it.song.id.startsWith(LOCAL_KEY_PREFIX)
            }

    if (builtInPlaylist == BuiltInPlaylist.Downloaded) {
        when (sortOrder) {
            SortOrder.Ascending -> {
                when (sortBy) {
                    SongSortBy.Title, SongSortBy.AlbumName -> items = items.sortedBy { it.song.title }
                    SongSortBy.PlayTime -> items = items.sortedBy { it.song.totalPlayTimeMs }
                    SongSortBy.Duration -> items = items.sortedBy { it.song.durationText }
                    SongSortBy.Artist -> items = items.sortedBy { it.song.artistsText }
                    SongSortBy.DatePlayed -> {}
                    SongSortBy.DateLiked -> items = items.sortedBy { it.song.likedAt }
                    SongSortBy.DateAdded -> {}
                    SongSortBy.AlbumName -> items = items.sortedBy { it.albumTitle }
                }
            }
            SortOrder.Descending -> {
                when (sortBy) {
                    SongSortBy.Title, SongSortBy.AlbumName -> items = items.sortedByDescending { it.song.title }
                    SongSortBy.PlayTime -> items = items.sortedByDescending { it.song.totalPlayTimeMs }
                    SongSortBy.Duration -> items = items.sortedByDescending { it.song.durationText }
                    SongSortBy.Artist -> items = items.sortedByDescending { it.song.artistsText }
                    SongSortBy.DatePlayed -> {}
                    SongSortBy.DateLiked -> items = items.sortedByDescending { it.song.likedAt }
                    SongSortBy.DateAdded -> {}
                    SongSortBy.AlbumName -> items = items.sortedByDescending { it.albumTitle }
                }
            }
        }

    }

    var filterCharSequence: CharSequence
    filterCharSequence = filter.toString()
    /******** OnDeviceDev */
    if (builtInPlaylist == BuiltInPlaylist.OnDevice) {
        if (!filter.isNullOrBlank())
            filteredSongs = songs
                .filter {
                    it.song.title.contains(filterCharSequence,true) ?: false
                            || it.song.artistsText?.contains(filterCharSequence,true) ?: false
                            || it.albumTitle?.contains(filterCharSequence,true) ?: false
                }
        if (!filter.isNullOrBlank())
            filteredFolders = folders
                .filter {
                    it.name.contains(filterCharSequence,true)
                }
    } else {
        if (!filter.isNullOrBlank())
            items = items
                .filter {
                    it.song.title.contains(filterCharSequence,true) ?: false
                            || it.song.artistsText?.contains(filterCharSequence,true) ?: false
                            || it.albumTitle?.contains(filterCharSequence,true) ?: false
                }
    }
    /******** */

    var searching by rememberSaveable { mutableStateOf(false) }

    val sortOrderIconRotation by animateFloatAsState(
        targetValue = if (sortOrder == SortOrder.Ascending) 0f else 180f,
        animationSpec = tween(durationMillis = 400, easing = LinearEasing), label = ""
    )


    val navigationBarPosition by rememberPreference(navigationBarPositionKey, NavigationBarPosition.Bottom)

    val lazyListState = rememberLazyListState()

    val showSearchTab by rememberPreference(showSearchTabKey, false)
    val maxSongsInQueue  by rememberPreference(maxSongsInQueueKey, MaxSongs.`500`)

    var listMediaItems = remember {
        mutableListOf<MediaItem>()
    }

    var selectItems by remember {
        mutableStateOf(false)
    }

    var position by remember {
        mutableIntStateOf(0)
    }

    var plistName by remember {
        mutableStateOf("")
    }

    val exportLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
            if (uri == null) return@rememberLauncherForActivityResult

            context.applicationContext.contentResolver.openOutputStream(uri)
                ?.use { outputStream ->
                    csvWriter().open(outputStream){
                        writeRow("PlaylistBrowseId", "PlaylistName", "MediaId", "Title", "Artists", "Duration", "ThumbnailUrl")
                        if (listMediaItems.isEmpty()) {
                            items.forEach {
                                writeRow(
                                    "",
                                    plistName,
                                    it.song.id,
                                    it.song.title,
                                    it.song.artistsText,
                                    it.song.durationText,
                                    it.song.thumbnailUrl
                                )
                            }
                        } else {
                            listMediaItems.forEach {
                                writeRow(
                                    "",
                                    plistName,
                                    it.mediaId,
                                    it.mediaMetadata.title,
                                    it.mediaMetadata.artist,
                                    "",
                                    it.mediaMetadata.artworkUri
                                )
                            }
                        }
                    }
                }

        }

    var isExporting by rememberSaveable {
        mutableStateOf(false)
    }

    if (isExporting) {
        InputTextDialog(
            onDismiss = {
                isExporting = false
            },
            title = stringResource(R.string.enter_the_playlist_name),
            value = when (builtInPlaylist) {
                BuiltInPlaylist.All -> context.resources.getString(R.string.songs)
                BuiltInPlaylist.OnDevice -> context.resources.getString(R.string.on_device)
                BuiltInPlaylist.Favorites -> context.resources.getString(R.string.favorites)
                BuiltInPlaylist.Downloaded -> context.resources.getString(R.string.downloaded)
                BuiltInPlaylist.Offline -> context.resources.getString(R.string.cached)
                BuiltInPlaylist.Top -> context.resources.getString(R.string.playlist_top)
            },
            placeholder = stringResource(R.string.enter_the_playlist_name),
            setValue = { text ->
                plistName = text
                try {
                    @SuppressLint("SimpleDateFormat")
                    val dateFormat = SimpleDateFormat("yyyyMMddHHmmss")
                    exportLauncher.launch("RMPlaylist_${text.take(20)}_${dateFormat.format(
                        Date()
                    )}")
                } catch (e: ActivityNotFoundException) {
                    SmartMessage("Couldn't find an application to create documents",
                        type = PopupType.Warning, context = context)
                }
            }
        )
    }

    var showConfirmDeleteDownloadDialog by remember {
        mutableStateOf(false)
    }
    var showConfirmDownloadAllDialog by remember {
        mutableStateOf(false)
    }



    Box(
        modifier = Modifier
            .background(colorPalette.background0)
            //.fillMaxSize()
            .fillMaxHeight()
            //.fillMaxWidth(if (navigationBarPosition == NavigationBarPosition.Left) 1f else Dimensions.contentWidthRightBar)
            .fillMaxWidth(
                if (navigationBarPosition == NavigationBarPosition.Left ||
                    navigationBarPosition == NavigationBarPosition.Top ||
                    navigationBarPosition == NavigationBarPosition.Bottom
                ) 1f
                else Dimensions.contentWidthRightBar
            )
    ) {
        LazyColumn(
            state = lazyListState,
            modifier = Modifier

        ) {

            stickyHeader {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colorPalette.background0)
                ) {
                    if (uiType == UiType.ViMusic)
                        HeaderWithIcon(
                            title = stringResource(R.string.songs),
                            iconId = R.drawable.search,
                            enabled = true,
                            showIcon = !showSearchTab,
                            modifier = Modifier,
                            onClick = onSearchClick
                        )

                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(all = 12.dp)
                            .fillMaxSize()
                    ) {
                        if (uiType == UiType.RiMusic)
                            TitleSection(title = stringResource(R.string.songs))

                        HeaderInfo(
                            title = if (builtInPlaylist == BuiltInPlaylist.OnDevice) "${filteredSongs.size}" else "${items.size}",
                            icon = painterResource(R.drawable.musical_notes)
                        )
                        Spacer(
                            modifier = Modifier
                                .weight(1f)
                        )
                        if (builtInPlaylist != BuiltInPlaylist.Top) {
                            HeaderIconButton(
                                icon = R.drawable.arrow_up,
                                color = colorPalette.text,
                                onClick = {},
                                modifier = Modifier
                                    .padding(horizontal = 2.dp)
                                    .graphicsLayer {
                                        rotationZ =
                                            sortOrderIconRotation
                                    }
                                    .combinedClickable(
                                        onClick = {
                                            if (builtInPlaylist != BuiltInPlaylist.OnDevice)
                                                sortOrder = !sortOrder
                                            else sortOrderOnDevice = !sortOrderOnDevice
                                        },
                                        onLongClick = {
                                            menuState.display {
                                                when (builtInPlaylist) {
                                                    BuiltInPlaylist.OnDevice -> {
                                                        if (!showFolders)
                                                            SortMenu(
                                                                title = stringResource(R.string.sorting_order),
                                                                onDismiss = menuState::hide,
                                                                onTitle = {
                                                                    sortByOnDevice =
                                                                        OnDeviceSongSortBy.Title
                                                                },
                                                                onDateAdded = {
                                                                    sortByOnDevice =
                                                                        OnDeviceSongSortBy.DateAdded
                                                                },
                                                                onArtist = {
                                                                    sortByOnDevice =
                                                                        OnDeviceSongSortBy.Artist
                                                                },
                                                                onAlbum = {
                                                                    sortByOnDevice =
                                                                        OnDeviceSongSortBy.Album
                                                                },
                                                            )
                                                        else
                                                            SortMenu(
                                                                title = stringResource(R.string.sorting_order),
                                                                onDismiss = menuState::hide,
                                                                onTitle = {
                                                                    sortByFolderOnDevice =
                                                                        OnDeviceFolderSortBy.Title
                                                                },
                                                                onArtist = {
                                                                    sortByFolderOnDevice =
                                                                        OnDeviceFolderSortBy.Artist
                                                                },
                                                                onDuration = {
                                                                    sortByFolderOnDevice =
                                                                        OnDeviceFolderSortBy.Duration
                                                                },
                                                            )
                                                    }

                                                    else -> {
                                                        SortMenu(
                                                            title = stringResource(R.string.sorting_order),
                                                            onDismiss = menuState::hide,
                                                            onTitle = { sortBy = SongSortBy.Title },
                                                            onDatePlayed = {
                                                                sortBy = SongSortBy.DatePlayed
                                                            },
                                                            onDateAdded = {
                                                                sortBy = SongSortBy.DateAdded
                                                            },
                                                            onPlayTime = {
                                                                sortBy = SongSortBy.PlayTime
                                                            },
                                                            onDateLiked = {
                                                                sortBy = SongSortBy.DateLiked
                                                            },
                                                            onArtist = {
                                                                sortBy = SongSortBy.Artist
                                                            },
                                                            onDuration = {
                                                                sortBy = SongSortBy.Duration
                                                            },
                                                            onAlbum = {
                                                                sortBy = SongSortBy.AlbumName
                                                            },
                                                        )
                                                    }
                                                }

                                            }
                                        }
                                    )
                            )
                        }
                        if (builtInPlaylist == BuiltInPlaylist.Top) {
                            HeaderIconButton(
                                icon = R.drawable.stat,
                                color = colorPalette.text,
                                onClick = {},
                                modifier = Modifier
                                    .padding(horizontal = 2.dp)
                                    .clickable(
                                        onClick = {
                                            menuState.display {
                                                PeriodMenu(
                                                    onDismiss = {
                                                        topPlaylistPeriod = it
                                                        menuState.hide()
                                                    }
                                                )
                                            }
                                        }
                                    )
                            )
                        }

                        HeaderIconButton(
                            onClick = { searching = !searching },
                            icon = R.drawable.search_circle,
                            color = colorPalette.text,
                            iconSize = 24.dp,
                            modifier = Modifier
                                .padding(horizontal = 2.dp)
                        )

                        HeaderIconButton(
                            modifier = Modifier
                                .padding(horizontal = 5.dp)
                                .combinedClickable(
                                    onClick = {
                                        nowPlayingItem = -1
                                        scrollToNowPlaying = false
                                        items
                                            .forEachIndexed { index, song ->
                                                if (song.song.asMediaItem.mediaId == binder?.player?.currentMediaItem?.mediaId)
                                                    nowPlayingItem = index
                                            }

                                        if (nowPlayingItem > -1)
                                            scrollToNowPlaying = true
                                    },
                                    onLongClick = {
                                        SmartMessage(
                                            context.resources.getString(R.string.info_find_the_song_that_is_playing),
                                            context = context
                                        )
                                    }
                                ),
                            icon = R.drawable.locate,
                            enabled = songs.isNotEmpty(),
                            color = if (songs.isNotEmpty()) colorPalette.text else colorPalette.textDisabled,
                            onClick = {}
                        )
                        LaunchedEffect(scrollToNowPlaying) {
                            if (scrollToNowPlaying)
                                lazyListState.scrollToItem(nowPlayingItem, 1)
                            scrollToNowPlaying = false
                        }

                        if (builtInPlaylist == BuiltInPlaylist.Favorites) {
                            HeaderIconButton(
                                icon = R.drawable.downloaded,
                                enabled = songs.isNotEmpty(),
                                color = if (songs.isNotEmpty()) colorPalette.text else colorPalette.textDisabled,
                                onClick = {},
                                modifier = Modifier
                                    .combinedClickable(
                                        onClick = {
                                            showConfirmDownloadAllDialog = true
                                        },
                                        onLongClick = {
                                            SmartMessage(
                                                context.resources.getString(R.string.info_download_all_songs),
                                                context = context
                                            )
                                        }
                                    )
                            )
                        }

                        if (showConfirmDownloadAllDialog) {
                            ConfirmationDialog(
                                text = stringResource(R.string.do_you_really_want_to_download_all),
                                onDismiss = { showConfirmDownloadAllDialog = false },
                                onConfirm = {
                                    showConfirmDownloadAllDialog = false
                                    //isRecommendationEnabled = false
                                    downloadState = Download.STATE_DOWNLOADING
                                    if (listMediaItems.isEmpty()) {
                                        if (items.isNotEmpty() == true)
                                            items.forEach {
                                                binder?.cache?.removeResource(it.song.asMediaItem.mediaId)
                                                manageDownload(
                                                    context = context,
                                                    songId = it.song.asMediaItem.mediaId,
                                                    songTitle = it.song.asMediaItem.mediaMetadata.title.toString(),
                                                    downloadState = false
                                                )
                                            }
                                    } else {
                                        listMediaItems.forEach {
                                            binder?.cache?.removeResource(it.mediaId)
                                            manageDownload(
                                                context = context,
                                                songId = it.mediaId,
                                                songTitle = it.mediaMetadata.title.toString(),
                                                downloadState = false
                                            )

                                        }
                                        selectItems = false
                                    }
                                }
                            )
                        }

                        if (builtInPlaylist == BuiltInPlaylist.Favorites || builtInPlaylist == BuiltInPlaylist.Downloaded) {
                            HeaderIconButton(
                                icon = R.drawable.download,
                                enabled = songs.isNotEmpty(),
                                color = if (songs.isNotEmpty()) colorPalette.text else colorPalette.textDisabled,
                                onClick = {},
                                modifier = Modifier
                                    .combinedClickable(
                                        onClick = {
                                            showConfirmDeleteDownloadDialog = true
                                        },
                                        onLongClick = {
                                            SmartMessage(
                                                context.resources.getString(R.string.info_remove_all_downloaded_songs),
                                                context = context
                                            )
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
                                        if (listMediaItems.isEmpty()) {
                                            if (items.isNotEmpty() == true)
                                                items.forEach {
                                                    binder?.cache?.removeResource(it.song.asMediaItem.mediaId)
                                                    manageDownload(
                                                        context = context,
                                                        songId = it.song.asMediaItem.mediaId,
                                                        songTitle = it.song.asMediaItem.mediaMetadata.title.toString(),
                                                        downloadState = true
                                                    )
                                                }
                                        } else {
                                            listMediaItems.forEach {
                                                binder?.cache?.removeResource(it.mediaId)
                                                manageDownload(
                                                    context = context,
                                                    songId = it.mediaId,
                                                    songTitle = it.mediaMetadata.title.toString(),
                                                    downloadState = true
                                                )
                                            }
                                            selectItems = false
                                        }
                                    }
                                )
                            }
                        }

                        if (builtInPlaylist == BuiltInPlaylist.All)
                            HeaderIconButton(
                                onClick = {},
                                icon = if (showHiddenSongs == 0) R.drawable.eye_off else R.drawable.eye,
                                color = colorPalette.text,
                                modifier = Modifier
                                    .padding(horizontal = 2.dp)
                                    .combinedClickable(
                                        onClick = {
                                            showHiddenSongs = if (showHiddenSongs == 0) -1 else 0
                                        },
                                        onLongClick = {
                                            SmartMessage(
                                                context.resources.getString(R.string.info_show_hide_hidden_songs),
                                                context = context
                                            )
                                        }
                                    )
                            )

                        HeaderIconButton(
                            icon = R.drawable.shuffle,
                            enabled = items.isNotEmpty(),
                            color = if (items.isNotEmpty()) colorPalette.text else colorPalette.textDisabled,
                            onClick = {},
                            modifier = Modifier
                                .padding(horizontal = 2.dp)
                                .combinedClickable(
                                    onClick = {
                                        if (builtInPlaylist == BuiltInPlaylist.OnDevice) items =
                                            filteredSongs
                                        if (items.isNotEmpty()) {
                                            val itemsLimited =
                                                if (items.size > maxSongsInQueue.number) items
                                                    .shuffled()
                                                    .take(maxSongsInQueue.number.toInt()) else items
                                            binder?.stopRadio()
                                            binder?.player?.forcePlayFromBeginning(
                                                itemsLimited
                                                    .shuffled()
                                                    .map(SongEntity::asMediaItem)
                                            )
                                        }
                                    },
                                    onLongClick = {
                                        SmartMessage(
                                            context.resources.getString(R.string.info_shuffle),
                                            context = context
                                        )
                                    }
                                )
                        )

                        if (builtInPlaylist == BuiltInPlaylist.Favorites)
                            HeaderIconButton(
                                icon = R.drawable.random,
                                enabled = true,
                                color = if (autoShuffle) colorPalette.text else colorPalette.textDisabled,
                                onClick = {},
                                modifier = Modifier
                                    .combinedClickable(
                                        onClick = {
                                            autoShuffle = !autoShuffle
                                        },
                                        onLongClick = {
                                            SmartMessage("Random sorting", context = context)
                                        }
                                    )
                            )

                        HeaderIconButton(
                            icon = R.drawable.ellipsis_horizontal,
                            color = colorPalette.text,
                            onClick = {
                                menuState.display {
                                    PlaylistsItemMenu(
                                        navController = navController,
                                        modifier = Modifier.fillMaxHeight(0.4f),
                                        onDismiss = menuState::hide,
                                        onSelectUnselect = {
                                            selectItems = !selectItems
                                            if (!selectItems) {
                                                listMediaItems.clear()
                                            }
                                        },
                                        onPlayNext = {
                                            if (builtInPlaylist == BuiltInPlaylist.OnDevice) items =
                                                filteredSongs
                                            if (listMediaItems.isEmpty()) {
                                                binder?.player?.addNext(
                                                    items.map(SongEntity::asMediaItem),
                                                    context
                                                )
                                            } else {
                                                binder?.player?.addNext(listMediaItems, context)
                                                listMediaItems.clear()
                                                selectItems = false
                                            }
                                        },
                                        onEnqueue = {
                                            if (builtInPlaylist == BuiltInPlaylist.OnDevice) items =
                                                filteredSongs
                                            if (listMediaItems.isEmpty()) {
                                                binder?.player?.enqueue(
                                                    items.map(SongEntity::asMediaItem),
                                                    context
                                                )
                                            } else {
                                                binder?.player?.enqueue(listMediaItems, context)
                                                listMediaItems.clear()
                                                selectItems = false
                                            }
                                        },
                                        onAddToPlaylist = { playlistPreview ->
                                            if (builtInPlaylist == BuiltInPlaylist.OnDevice) items =
                                                filteredSongs
                                            position =
                                                playlistPreview.songCount.minus(1) ?: 0
                                            if (position > 0) position++ else position = 0

                                            items.forEachIndexed { index, song ->
                                                runCatching {
                                                    Database.insert(song.song.asMediaItem)
                                                    Database.insert(
                                                        SongPlaylistMap(
                                                            songId = song.song.asMediaItem.mediaId,
                                                            playlistId = playlistPreview.playlist.id,
                                                            position = position + index
                                                        )
                                                    )
                                                }.onFailure {
                                                    Timber.e("Failed addToPlaylist in HomeSongsModern ${it.stackTraceToString()}")
                                                    it.message?.let { it1 ->
                                                        SmartMessage(
                                                            it1,
                                                            PopupType.Error,
                                                            context = context
                                                        )
                                                    }
                                                }
                                            }
                                            CoroutineScope(Dispatchers.Main).launch {
                                                SmartMessage(
                                                    context.resources.getString(R.string.done),
                                                    type = PopupType.Success, context = context
                                                )
                                            }
                                        },
                                        onExport = {
                                            isExporting = true
                                        }
                                    )
                                }
                            },
                            modifier = Modifier
                                .padding(horizontal = 2.dp)
                        )

                    }

                    /*        */

                    AnimatedVisibility(visible = searching) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.Bottom,
                            modifier = Modifier
                                //.requiredHeight(30.dp)
                                .padding(all = 10.dp)
                                .fillMaxWidth()
                        ) {
                            val focusRequester = remember { FocusRequester() }
                            val focusManager = LocalFocusManager.current
                            val keyboardController = LocalSoftwareKeyboardController.current

                            LaunchedEffect(searching) {
                                focusRequester.requestFocus()
                            }

                            BasicTextField(
                                value = filter ?: "",
                                onValueChange = { filter = it },
                                textStyle = typography.xs.semiBold,
                                singleLine = true,
                                maxLines = 1,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = {
                                    if (filter.isNullOrBlank()) filter = ""
                                    focusManager.clearFocus()
                                }),
                                cursorBrush = SolidColor(colorPalette.text),
                                decorationBox = { innerTextField ->
                                    Box(
                                        contentAlignment = Alignment.CenterStart,
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(horizontal = 10.dp)
                                    ) {
                                        IconButton(
                                            onClick = {},
                                            icon = R.drawable.search,
                                            color = colorPalette.favoritesIcon,
                                            modifier = Modifier
                                                .align(Alignment.CenterStart)
                                                .size(16.dp)
                                        )
                                    }
                                    Box(
                                        contentAlignment = Alignment.CenterStart,
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(horizontal = 30.dp)
                                    ) {
                                        androidx.compose.animation.AnimatedVisibility(
                                            visible = filter?.isEmpty() ?: true,
                                            enter = fadeIn(tween(100)),
                                            exit = fadeOut(tween(100)),
                                        ) {
                                            BasicText(
                                                text = stringResource(R.string.search),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                style = typography.xs.semiBold.secondary.copy(color = colorPalette.textDisabled)
                                            )
                                        }

                                        innerTextField()
                                    }
                                },
                                modifier = Modifier
                                    .height(30.dp)
                                    .fillMaxWidth()
                                    .background(
                                        colorPalette.background4,
                                        shape = thumbnailRoundness.shape()
                                    )
                                    .focusRequester(focusRequester)
                                    .onFocusChanged {
                                        if (!it.hasFocus) {
                                            keyboardController?.hide()
                                            if (filter?.isBlank() == true) {
                                                filter = null
                                                searching = false
                                            }
                                        }
                                    }
                            )
                        }

                    }
                    /*        */
                    //}


                    ButtonsRow(
                        chips = buttonsList,
                        currentValue = builtInPlaylist,
                        onValueUpdate = { builtInPlaylist = it },
                        modifier = Modifier.padding(end = 12.dp)
                    )
                }
            }

            if (builtInPlaylist == BuiltInPlaylist.OnDevice) {
                if (!hasPermission) {
                item(
                    key = "OnDeviceSongsPermission"
                ) {
                        LaunchedEffect(Unit, relaunchPermission) { launcher.launch(permission) }

                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(
                                2.dp,
                                Alignment.CenterVertically
                            ),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            BasicText(
                                text = stringResource(R.string.media_permission_required_please_grant),
                                modifier = Modifier.fillMaxWidth(0.75f),
                                style = typography.xs.semiBold
                            )
                            /*
                        Spacer(modifier = Modifier.height(12.dp))
                        SecondaryTextButton(
                            text = stringResource(R.string.grant_permission),
                            onClick = {
                                relaunchPermission = !relaunchPermission
                            }
                        )
                         */
                            Spacer(modifier = Modifier.height(20.dp))
                            SecondaryTextButton(
                                text = stringResource(R.string.open_permission_settings),
                                onClick = {
                                    context.startActivity(
                                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                            setData(
                                                Uri.fromParts(
                                                    "package",
                                                    context.packageName,
                                                    null
                                                )
                                            )
                                        }
                                    )
                                }
                            )

                        }

                    }
                } else {
                    if (showFolders) {
                        if (currentFolderPath != "/") {

                            item {
                                BackHandler(onBack = {
                                    currentFolderPath = currentFolderPath.removeSuffix("/").substringBeforeLast("/") + "/"
                                })
                            }

                            itemsIndexed(items = listOf(backButtonFolder)) { index, folderItem ->
                                FolderItem(
                                    folder = folderItem,
                                    thumbnailSizeDp = thumbnailSizeDp,
                                    icon = R.drawable.chevron_back,
                                    modifier = Modifier
                                        .combinedClickable(
                                            onClick = {
                                                currentFolderPath = currentFolderPath.removeSuffix("/").substringBeforeLast("/") + "/"
                                            }
                                        ),
                                )
                            }
                        }
                        if (currentFolder != null) {
                            itemsIndexed(
                                items = filteredFolders.distinctBy { it.fullPath },
                                key = { _, folder -> folder.fullPath },
                                contentType = { _, folder -> folder }
                            ) { index, folder ->
                                FolderItem(
                                    folder = folder,
                                    thumbnailSizeDp = thumbnailSizeDp,
                                    modifier = Modifier
                                        .combinedClickable(
                                            onLongClick = {
                                                menuState.display {
                                                    FolderItemMenu(
                                                        folder = folder,
                                                        onDismiss = menuState::hide,
                                                        onEnqueue = {
                                                            val allSongs = folder.getAllSongs()
                                                                .map { it.toSong().asMediaItem }
                                                            binder?.player?.enqueue(allSongs, context)
                                                        },
                                                        thumbnailSizeDp = thumbnailSizeDp
                                                    )
                                                };
                                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                            },
                                            onClick = {
                                                currentFolderPath += folder.name + "/"
                                            }
                                        ),
                                )
                            }
                        } else {
                            item {
                                BasicText(
                                    text = stringResource(R.string.folder_was_not_found),
                                    style = typography.xs.semiBold
                                )
                            }
                        }
                    }

                    itemsIndexed(
                        items = filteredSongs.distinctBy { it.song.id },
                        //key = { index, _ -> Random.nextLong().toString() },
                        //contentType = { _, song -> song },
                    //) { index, song ->
                        key = { _, song -> song.song.id }
                    ) { index, song ->
                        SwipeablePlaylistItem(
                            mediaItem = song.asMediaItem,
                            onSwipeToRight = {
                                binder?.player?.addNext(song.asMediaItem)
                            }
                        ) {
                            SongItem(
                                song = song.song,
                                isDownloaded = true,
                                onDownloadClick = {
                                    // not necessary
                                },
                                downloadState = Download.STATE_COMPLETED,
                                thumbnailSizeDp = thumbnailSizeDp,
                                thumbnailSizePx = thumbnailSizePx,
                                onThumbnailContent = {
                                    if (nowPlayingItem > -1)
                                        NowPlayingShow(song.asMediaItem.mediaId)
                                },
                                trailingContent = {
                                    val checkedState = rememberSaveable { mutableStateOf(false) }
                                    if (selectItems)
                                        androidx.compose.material3.Checkbox(
                                            checked = checkedState.value,
                                            onCheckedChange = {
                                                checkedState.value = it
                                                if (it) listMediaItems.add(song.asMediaItem) else
                                                    listMediaItems.remove(song.asMediaItem)
                                            },
                                            colors = androidx.compose.material3.CheckboxDefaults.colors(
                                                checkedColor = colorPalette.accent,
                                                uncheckedColor = colorPalette.text
                                            ),
                                            modifier = Modifier
                                                .scale(0.7f)
                                        )
                                    else checkedState.value = false
                                },
                                modifier = Modifier
                                        .combinedClickable(
                                        onLongClick = {
                                            menuState.display {
                                                InHistoryMediaItemMenu(
                                                    navController = navController,
                                                    song = song.song,
                                                    onDismiss = menuState::hide
                                                )
                                            }
                                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                        },
                                onClick = {
                                    if (!selectItems) {
                                        searching = false
                                        filter = null
                                        binder?.stopRadio()
                                        binder?.player?.forcePlayAtIndex(
                                            filteredSongs.map(SongEntity::asMediaItem),
                                            index
                                        )
                                    }
                                }
                            )
                                    .animateItem(
                                    fadeInSpec = null,
                                    fadeOutSpec = null
                                )
                            )
                        }
                    }
                }
            }

            if (builtInPlaylist != BuiltInPlaylist.OnDevice) {
                itemsIndexed(
                    items = items.distinctBy { it.song.id },
                    key = { _, song -> song.song.id },
                    //contentType = { _, song -> song },
                ) { index, song ->

                    var isHiding by remember {
                        mutableStateOf(false)
                    }

                    var isDeleting by remember {
                        mutableStateOf(false)
                    }

                    if (isHiding) {
                        ConfirmationDialog(
                            text = stringResource(R.string.hidesong),
                            onDismiss = { isHiding = false },
                            onConfirm = {
                                query {
                                    menuState.hide()
                                    binder?.cache?.removeResource(song.song.id)
                                    binder?.downloadCache?.removeResource(song.song.id)
                                    Database.incrementTotalPlayTimeMs(
                                        song.song.id,
                                        -song.song.totalPlayTimeMs
                                    )
                                }
                            }
                        )
                    }

                    if (isDeleting) {
                        ConfirmationDialog(
                            text = stringResource(R.string.delete_song),
                            onDismiss = { isDeleting = false },
                            onConfirm = {
                                query {
                                    menuState.hide()
                                    binder?.cache?.removeResource(song.song.id)
                                    binder?.downloadCache?.removeResource(song.song.id)
                                    Database.delete(song.song)
                                    Database.deleteSongFromPlaylists(song.song.id)
                                }
                                SmartMessage(context.resources.getString(R.string.deleted), context = context)
                            }
                        )
                    }

                    SwipeablePlaylistItem(
                        mediaItem = song.song.asMediaItem,
                        onSwipeToRight = {
                            binder?.player?.addNext(song.song.asMediaItem)
                        }
                    ) {
                        val isLocal by remember { derivedStateOf { song.song.asMediaItem.isLocal } }
                        downloadState = getDownloadState(song.song.asMediaItem.mediaId)
                        val isDownloaded =
                            if (!isLocal) downloadedStateMedia(song.song.asMediaItem.mediaId) else true
                        val checkedState = rememberSaveable { mutableStateOf(false) }
                        SongItem(
                            song = song.song,
                            isDownloaded = isDownloaded,
                            onDownloadClick = {
                                binder?.cache?.removeResource(song.song.asMediaItem.mediaId)
                                query {
                                    Database.insert(
                                        Song(
                                            id = song.song.asMediaItem.mediaId,
                                            title = song.song.asMediaItem.mediaMetadata.title.toString(),
                                            artistsText = song.song.asMediaItem.mediaMetadata.artist.toString(),
                                            thumbnailUrl = song.song.thumbnailUrl,
                                            durationText = null
                                        )
                                    )
                                }
                                if (!isLocal)
                                    manageDownload(
                                        context = context,
                                        songId = song.song.id,
                                        songTitle = song.song.title,
                                        downloadState = isDownloaded
                                    )
                            },
                            downloadState = downloadState,
                            thumbnailSizePx = thumbnailSizePx,
                            thumbnailSizeDp = thumbnailSizeDp,
                            onThumbnailContent = {
                                if (sortBy == SongSortBy.PlayTime) {
                                    BasicText(
                                        text = song.song.formattedTotalPlayTime,
                                        style = typography.xxs.semiBold.center.color(colorPalette.onOverlay),
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                brush = Brush.verticalGradient(
                                                    colors = listOf(
                                                        Color.Transparent,
                                                        colorPalette.overlay
                                                    )
                                                ),
                                                shape = thumbnailShape
                                            )
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                            .align(Alignment.BottomCenter)
                                    )
                                }

                                if (nowPlayingItem > -1)
                                    NowPlayingShow(song.song.asMediaItem.mediaId)

                                if (builtInPlaylist == BuiltInPlaylist.Top)
                                    BasicText(
                                        text = (index + 1).toString(),
                                        style = typography.m.semiBold.center.color(colorPalette.onOverlay),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                brush = Brush.verticalGradient(
                                                    colors = listOf(
                                                        Color.Transparent,
                                                        colorPalette.overlay
                                                    )
                                                ),
                                                shape = thumbnailShape
                                            )
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                            .align(Alignment.Center)
                                    )
                            },
                            trailingContent = {
                                if (selectItems)
                                    Checkbox(
                                        checked = checkedState.value,
                                        onCheckedChange = {
                                            checkedState.value = it
                                            if (it) listMediaItems.add(song.song.asMediaItem) else
                                                listMediaItems.remove(song.song.asMediaItem)
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
                            modifier = Modifier
                                .combinedClickable(
                                    onLongClick = {
                                        menuState.display {
                                            InHistoryMediaItemMenu(
                                                navController = navController,
                                                song = song.song,
                                                onDismiss = menuState::hide,
                                                onHideFromDatabase = { isHiding = true },
                                                onDeleteFromDatabase = { isDeleting = true }
                                            )
                                        }
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                    },
                                    onClick = {
                                        searching = false
                                        filter = null
                                        val itemsLimited =
                                            if (items.size > maxSongsInQueue.number) items.take(
                                                maxSongsInQueue.number.toInt()
                                            ) else items
                                        binder?.stopRadio()
                                        binder?.player?.forcePlayAtIndex(
                                            itemsLimited.map(SongEntity::asMediaItem),
                                            index
                                        )
                                    }
                                )
                                .animateItemPlacement()
                        )
                    }
                }
            }

            item(key = "bottom") {
                Spacer(modifier = Modifier.height(Dimensions.bottomSpacer))
            }

        }

        FloatingActionsContainerWithScrollToTop(lazyListState = lazyListState)

        val showFloatingIcon by rememberPreference(showFloatingIconKey, false)
        if(uiType == UiType.ViMusic || showFloatingIcon)
            MultiFloatingActionsContainer(
                iconId = R.drawable.search,
                onClick = onSearchClick,
                onClickSettings = onSettingsClick,
                onClickSearch = onSearchClick
            )

            /*
        FloatingActionsContainerWithScrollToTop(
                lazyListState = lazyListState,
                iconId = R.drawable.search,
                onClick = onSearchClick
            )

             */





    }
}
