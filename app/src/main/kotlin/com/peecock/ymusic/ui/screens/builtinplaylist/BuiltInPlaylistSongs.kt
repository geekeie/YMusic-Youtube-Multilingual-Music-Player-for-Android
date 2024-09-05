package com.peecock.ymusic.ui.screens.builtinplaylist

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
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
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
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
import coil.compose.AsyncImage
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import com.peecock.compose.persist.persist
import com.peecock.compose.persist.persistList
import com.peecock.innertube.Innertube
import com.peecock.innertube.models.bodies.NextBody
import com.peecock.innertube.requests.relatedSongs
import com.peecock.ymusic.Database
import com.peecock.ymusic.LocalPlayerServiceBinder
import com.peecock.ymusic.R
import com.peecock.ymusic.enums.BuiltInPlaylist
import com.peecock.ymusic.enums.MaxSongs
import com.peecock.ymusic.enums.MaxTopPlaylistItems
import com.peecock.ymusic.enums.NavRoutes
import com.peecock.ymusic.enums.NavigationBarPosition
import com.peecock.ymusic.enums.PopupType
import com.peecock.ymusic.enums.RecommendationsNumber
import com.peecock.ymusic.enums.SongSortBy
import com.peecock.ymusic.enums.SortOrder
import com.peecock.ymusic.enums.ThumbnailRoundness
import com.peecock.ymusic.models.Song
import com.peecock.ymusic.models.SongPlaylistMap
import com.peecock.ymusic.query
import com.peecock.ymusic.service.isLocal
import com.peecock.ymusic.transaction
import com.peecock.ymusic.ui.components.LocalMenuState
import com.peecock.ymusic.ui.components.themed.ConfirmationDialog
import com.peecock.ymusic.ui.components.themed.FloatingActionsContainerWithScrollToTop
import com.peecock.ymusic.ui.components.themed.HeaderIconButton
import com.peecock.ymusic.ui.components.themed.HeaderWithIcon
import com.peecock.ymusic.ui.components.themed.IconButton
import com.peecock.ymusic.ui.components.themed.IconInfo
import com.peecock.ymusic.ui.components.themed.InHistoryMediaItemMenu
import com.peecock.ymusic.ui.components.themed.InputTextDialog
import com.peecock.ymusic.ui.components.themed.NonQueuedMediaItemMenuLibrary
import com.peecock.ymusic.ui.items.PlaylistItem
import com.peecock.ymusic.ui.items.SongItem
import com.peecock.ymusic.ui.styling.Dimensions
import com.peecock.ymusic.ui.styling.LocalAppearance
import com.peecock.ymusic.ui.styling.onOverlay
import com.peecock.ymusic.ui.styling.overlay
import com.peecock.ymusic.ui.styling.px
import com.peecock.ymusic.utils.asMediaItem
import com.peecock.ymusic.utils.center
import com.peecock.ymusic.utils.color
import com.peecock.ymusic.utils.downloadedStateMedia
import com.peecock.ymusic.utils.durationTextToMillis
import com.peecock.ymusic.utils.enqueue
import com.peecock.ymusic.utils.forcePlay
import com.peecock.ymusic.utils.forcePlayAtIndex
import com.peecock.ymusic.utils.forcePlayFromBeginning
import com.peecock.ymusic.utils.formatAsTime
import com.peecock.ymusic.utils.getDownloadState
import com.peecock.ymusic.utils.isRecommendationEnabledKey
import com.peecock.ymusic.utils.manageDownload
import com.peecock.ymusic.utils.recommendationsNumberKey
import com.peecock.ymusic.utils.rememberPreference
import com.peecock.ymusic.utils.secondary
import com.peecock.ymusic.utils.semiBold
import com.peecock.ymusic.utils.songSortByKey
import com.peecock.ymusic.utils.songSortOrderKey
import com.peecock.ymusic.utils.thumbnail
import com.peecock.ymusic.utils.thumbnailRoundnessKey
import com.peecock.ymusic.ui.components.themed.NowPlayingShow
import com.peecock.ymusic.ui.components.themed.PlaylistsItemMenu
import com.peecock.ymusic.ui.components.themed.SmartMessage
import com.peecock.ymusic.ui.components.themed.SortMenu
import com.peecock.ymusic.ui.styling.favoritesIcon
import com.peecock.ymusic.utils.MaxTopPlaylistItemsKey
import com.peecock.ymusic.utils.addNext
import com.peecock.ymusic.utils.autoShuffleKey
import com.peecock.ymusic.utils.isLandscape
import com.peecock.ymusic.utils.maxSongsInQueueKey
import com.peecock.ymusic.utils.navigationBarPositionKey
import com.peecock.ymusic.utils.showSearchTabKey
import java.text.SimpleDateFormat
import java.util.Date


@ExperimentalTextApi
@SuppressLint("SuspiciousIndentation", "StateFlowValueCalledInComposition")
@ExperimentalFoundationApi
@ExperimentalAnimationApi
@ExperimentalComposeUiApi
@UnstableApi
@Composable
fun BuiltInPlaylistSongs(
    navController: NavController,
    builtInPlaylist: BuiltInPlaylist,
    onSearchClick: () -> Unit
) {
    val context = LocalContext.current
    val (colorPalette, typography, thumbnailShape) = LocalAppearance.current
    val binder = LocalPlayerServiceBinder.current
    val menuState = LocalMenuState.current

    var songs by persistList<Song>("${builtInPlaylist.name}/songs")

    var sortBy by rememberPreference(songSortByKey, SongSortBy.DateAdded)
    var sortOrder by rememberPreference(songSortOrderKey, SortOrder.Descending)
    var autoShuffle by rememberPreference(autoShuffleKey, false)

    var filter: String? by rememberSaveable { mutableStateOf(null) }

    var downloadState by remember {
        mutableStateOf(Download.STATE_STOPPED)
    }

    var showConfirmDeleteDownloadDialog by remember {
        mutableStateOf(false)
    }
    var showConfirmDownloadAllDialog by remember {
        mutableStateOf(false)
    }

    val maxTopPlaylistItems by rememberPreference(
        MaxTopPlaylistItemsKey,
        MaxTopPlaylistItems.`10`
    )

    var cleanCacheOfflineSongs by remember {
        mutableStateOf(false)
    }
    var reloadSongs by remember {
        mutableStateOf(false)
    }

    if (cleanCacheOfflineSongs) {
        ConfirmationDialog(
            text = stringResource(R.string.do_you_really_want_to_delete_cache),
            onDismiss = {
                cleanCacheOfflineSongs = false
            },
            onConfirm = {
                binder?.cache?.keys?.forEach { song ->
                    binder.cache.removeResource(song)
                }
                reloadSongs = !reloadSongs
            }
        )

    }

    if (cleanCacheOfflineSongs) {
        ConfirmationDialog(
            text = stringResource(R.string.do_you_really_want_to_delete_cache),
            onDismiss = {
                cleanCacheOfflineSongs = false
            },
            onConfirm = {
                binder?.cache?.keys?.forEach { song ->
                    binder.cache.removeResource(song)
                }
                reloadSongs = !reloadSongs
            }
        )

    }

/*
     LaunchedEffect(Unit, sortBy, sortOrder, filter, reloadSongs, autoShuffle) {
         when (builtInPlaylist) {

             BuiltInPlaylist.Downloaded -> {
                 val downloads = DownloadUtil.downloads.value
                 Database.listAllSongsAsFlow()
                     .map {
                         it.filter {song ->
                            downloads[song.id]?.state == Download.STATE_COMPLETED
                         }
                     }
             }

             BuiltInPlaylist.Favorites, BuiltInPlaylist.OnDevice, BuiltInPlaylist.All -> Database
                 .songsFavorites(sortBy, sortOrder)


             BuiltInPlaylist.Offline -> {
                 Database.songsOffline(sortBy, sortOrder)
                 /*
                 Database
                 .songsOffline(sortBy, sortOrder)
                 .flowOn(Dispatchers.IO)
                 .map { songs ->
                     songs.filter { song ->
                         song.contentLength?.let {
                             binder?.cache?.isCached(song.song.id, 0, song.contentLength)
                         } ?: false
                     }.map(SongWithContentLength::song)
                 }

              */
             }


             BuiltInPlaylist.Top ->
                 Database.trending(maxTopPlaylistItems.number.toInt())

         }.collect { songs =
             if (autoShuffle && builtInPlaylist == BuiltInPlaylist.Favorites)
                 it.shuffled()
             else it
         }
    }
    */

    val thumbnailSizeDp = Dimensions.thumbnails.song
    val thumbnailSizePx = thumbnailSizeDp.px
    val playlistThumbnailSizeDp = Dimensions.thumbnails.playlist
    val playlistThumbnailSizePx = playlistThumbnailSizeDp.px
    val thumbnails = songs
        .takeWhile { it.thumbnailUrl?.isNotEmpty() ?: false }
        .take(4)
        .map { it.thumbnailUrl.thumbnail(playlistThumbnailSizePx / 2) }


    if (builtInPlaylist == BuiltInPlaylist.Downloaded) {
        when (sortOrder) {
            SortOrder.Ascending -> {
                when (sortBy) {
                    SongSortBy.Title, SongSortBy.AlbumName -> songs = songs.sortedBy { it.title }
                    SongSortBy.PlayTime -> songs = songs.sortedBy { it.totalPlayTimeMs }
                    SongSortBy.Duration -> songs = songs.sortedBy { it.durationText }
                    SongSortBy.Artist -> songs = songs.sortedBy { it.artistsText }
                    SongSortBy.DatePlayed -> {}
                    SongSortBy.DateLiked -> songs = songs.sortedBy { it.likedAt }
                    SongSortBy.DateAdded -> {}
                }
            }
            SortOrder.Descending -> {
                when (sortBy) {
                    SongSortBy.Title, SongSortBy.AlbumName -> songs = songs.sortedByDescending { it.title }
                    SongSortBy.PlayTime -> songs = songs.sortedByDescending { it.totalPlayTimeMs }
                    SongSortBy.Duration -> songs = songs.sortedByDescending { it.durationText }
                    SongSortBy.Artist -> songs = songs.sortedByDescending { it.artistsText }
                    SongSortBy.DatePlayed -> {}
                    SongSortBy.DateLiked -> songs = songs.sortedByDescending { it.likedAt }
                    SongSortBy.DateAdded -> {}
                }
            }
        }

    }

    if (builtInPlaylist == BuiltInPlaylist.Favorites) {
        songs.shuffled()
    }



    var filterCharSequence: CharSequence
    filterCharSequence = filter.toString()
    //Log.d("mediaItemFilter", "<${filter}>  <${filterCharSequence}>")
    if (!filter.isNullOrBlank())
    songs = songs
        .filter {
            it.title.contains(filterCharSequence,true)
            || it.artistsText?.contains(filterCharSequence,true) ?: false
        }

    var searching by rememberSaveable { mutableStateOf(false) }

    val sortOrderIconRotation by animateFloatAsState(
        targetValue = if (sortOrder == SortOrder.Ascending) 0f else 180f,
        animationSpec = tween(durationMillis = 400, easing = LinearEasing), label = ""
    )

    var thumbnailRoundness by rememberPreference(
        thumbnailRoundnessKey,
        ThumbnailRoundness.Heavy
    )

    val lazyListState = rememberLazyListState()

    var totalPlayTimes = 0L
    songs.forEach {
        totalPlayTimes += it.durationText?.let { it1 ->
            durationTextToMillis(it1)
        }?.toLong() ?: 0
    }

    //**** SMART RECOMMENDATION
    val recommendationsNumber by rememberPreference(recommendationsNumberKey,   RecommendationsNumber.`5`)
    var isRecommendationEnabled by rememberPreference(isRecommendationEnabledKey, false)
    var relatedSongsRecommendationResult by persist<Result<Innertube.RelatedSongs?>?>(tag = "home/relatedSongsResult")
    var songBaseRecommendation by persist<Song?>("home/songBaseRecommendation")
    var positionsRecommendationList = arrayListOf<Int>()
    if (isRecommendationEnabled) {
        LaunchedEffect(Unit,isRecommendationEnabled) {
                val song = songs.shuffled().firstOrNull()
                if (relatedSongsRecommendationResult == null || songBaseRecommendation?.id != song?.id) {
                    relatedSongsRecommendationResult =
                        Innertube.relatedSongs(NextBody(videoId = (song?.id ?: "HZnNt9nnEhw")))
                }
        }


        if (relatedSongsRecommendationResult != null) {
            for (index in 0..recommendationsNumber.number) {
                positionsRecommendationList.add((0..songs.size).random())
            }
        }
        //Log.d("mediaItem","positionsList "+positionsRecommendationList.toString())
        //**** SMART RECOMMENDATION
    }

    var scrollToNowPlaying by remember {
        mutableStateOf(false)
    }

    var nowPlayingItem by remember {
        mutableStateOf(-1)
    }

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
                            songs.forEach {
                                writeRow(
                                    "",
                                    plistName,
                                    it.id,
                                    it.title,
                                    it.artistsText,
                                    it.durationText,
                                    it.thumbnailUrl
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

    val navigationBarPosition by rememberPreference(navigationBarPositionKey, NavigationBarPosition.Bottom)
    val showSearchTab by rememberPreference(showSearchTabKey, false)
    val maxSongsInQueue  by rememberPreference(maxSongsInQueueKey, MaxSongs.`500`)

    Box (
        modifier = Modifier
            .background(colorPalette.background0)
            //.fillMaxSize()
            .fillMaxHeight()
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
            //contentPadding = LocalPlayerAwareWindowInsets.current
            //    .only(WindowInsetsSides.Vertical + WindowInsetsSides.End).asPaddingValues(),
            modifier = Modifier
                .background(colorPalette.background0)
                .fillMaxSize()
        ) {
            item(
                key = "header",
                contentType = 0
            ) {

                HeaderWithIcon(
                    title = when (builtInPlaylist) {
                        BuiltInPlaylist.All -> stringResource(R.string.songs)
                        BuiltInPlaylist.OnDevice -> context.resources.getString(R.string.on_device)
                        BuiltInPlaylist.Favorites -> stringResource(R.string.favorites)
                        BuiltInPlaylist.Downloaded -> stringResource(R.string.downloaded)
                        BuiltInPlaylist.Offline -> stringResource(R.string.cached)
                        BuiltInPlaylist.Top -> stringResource(R.string.my_playlist_top).format(maxTopPlaylistItems.number)
                    },
                    iconId = R.drawable.search,
                    enabled = true,
                    showIcon = !showSearchTab,
                    modifier = Modifier,
                    onClick = onSearchClick
                )

                Row(
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        //.background(colorPalette.background4)
                        .fillMaxSize(0.99F)
                        .background(
                            color = colorPalette.background1,
                            shape = thumbnailRoundness.shape()
                        )
                ) {

                    if (songs.isEmpty())
                    PlaylistItem(
                        icon = when (builtInPlaylist) {
                            BuiltInPlaylist.All -> R.drawable.musical_notes
                            BuiltInPlaylist.OnDevice -> R.drawable.musical_notes
                            BuiltInPlaylist.Favorites -> R.drawable.heart
                            BuiltInPlaylist.Downloaded -> R.drawable.downloaded
                            BuiltInPlaylist.Offline -> R.drawable.sync
                            BuiltInPlaylist.Top -> R.drawable.trending
                        },
                        colorTint = colorPalette.favoritesIcon,
                        name = when (builtInPlaylist) {
                            BuiltInPlaylist.All -> context.resources.getString(R.string.songs)
                            BuiltInPlaylist.OnDevice -> context.resources.getString(R.string.on_device)
                            BuiltInPlaylist.Favorites -> stringResource(R.string.favorites)
                            BuiltInPlaylist.Downloaded -> stringResource(R.string.downloaded)
                            BuiltInPlaylist.Offline -> stringResource(R.string.cached)
                            BuiltInPlaylist.Top -> stringResource(R.string.playlist_top)
                        },
                        songCount = null,
                        thumbnailSizeDp = playlistThumbnailSizeDp,
                        alternative = false,
                        modifier = Modifier
                            .padding(top = 14.dp)
                    )

                    if (songs.isNotEmpty())
                    PlaylistItem(
                        thumbnailContent = {
                            if (thumbnails.toSet().size == 1) {
                                AsyncImage(
                                    model = thumbnails.first().thumbnail(playlistThumbnailSizePx),
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
                                                .size(playlistThumbnailSizeDp / 2)
                                        )
                                    }
                                }
                            }
                        },
                        songCount = null,
                        name = "",
                        channelName = null,
                        thumbnailSizeDp = playlistThumbnailSizeDp,
                        alternative = true,
                        showName = false,
                        modifier = Modifier
                            .padding(top = 14.dp)
                    )


                    Column (
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.Start,
                        modifier = Modifier
                            .padding(end = 10.dp)
                            .fillMaxWidth( if (isLandscape) 0.90f else 0.80f )
                        //.border(BorderStroke(1.dp, Color.White))
                    ) {
                        Spacer(modifier = Modifier.height(10.dp))
                        IconInfo(
                            title = songs.size.toString(),
                            icon = painterResource(R.drawable.musical_notes)
                        )
                        Spacer(modifier = Modifier.height(5.dp))
                        IconInfo(
                            title = formatAsTime(totalPlayTimes),
                            icon = painterResource(R.drawable.time)
                        )
                        if (isRecommendationEnabled) {
                            Spacer(modifier = Modifier.height(5.dp))
                            IconInfo(
                                title = positionsRecommendationList.distinct().size.toString(),
                                icon = painterResource(R.drawable.smart_shuffle)
                            )
                        }
                        Spacer(modifier = Modifier.height(30.dp))
                    }

                    Column (
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        HeaderIconButton(
                            icon = R.drawable.smart_shuffle,
                            enabled = true,
                            color = if (isRecommendationEnabled) colorPalette.text else colorPalette.textDisabled,
                            onClick = {},
                            modifier = Modifier
                                .combinedClickable(
                                    onClick = {
                                        isRecommendationEnabled = !isRecommendationEnabled
                                    },
                                    onLongClick = {
                                        SmartMessage(context.resources.getString(R.string.info_smart_recommendation), context = context)
                                    }
                                )
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        HeaderIconButton(
                            icon = R.drawable.shuffle,
                            enabled = songs.isNotEmpty(),
                            color = if (songs.isNotEmpty()) colorPalette.text else colorPalette.textDisabled,
                            onClick = {},
                                modifier = Modifier
                                    .combinedClickable(
                                        onClick = {
                                            if (songs.isNotEmpty()) {
                                                val itemsLimited =
                                                    if (songs.size > maxSongsInQueue.number) songs.shuffled()
                                                        .take(maxSongsInQueue.number.toInt()) else songs
                                                binder?.stopRadio()
                                                binder?.player?.forcePlayFromBeginning(
                                                    itemsLimited.shuffled().map(Song::asMediaItem)
                                                )
                                            }
                                        },
                                        onLongClick = {
                                            SmartMessage(context.resources.getString(R.string.info_shuffle), context = context)
                                        }
                                    )
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        HeaderIconButton(
                            modifier = Modifier.padding(horizontal = 5.dp),
                            onClick = { searching = !searching },
                            icon = R.drawable.search_circle,
                            color = colorPalette.text,
                            iconSize = 24.dp
                        )
                    }

                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(horizontal = 10.dp)
                        .fillMaxWidth()
                ) {

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
                                        SmartMessage(context.resources.getString(R.string.info_download_all_songs), context = context)
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
                                isRecommendationEnabled = false
                                downloadState = Download.STATE_DOWNLOADING
                                if (songs.isNotEmpty() == true)
                                    songs.forEach {
                                        binder?.cache?.removeResource(it.asMediaItem.mediaId)
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

                    }

                    /*
                    HeaderIconButton(
                        icon = R.drawable.enqueue,
                        enabled = songs.isNotEmpty(),
                        color = if (songs.isNotEmpty()) colorPalette.text else colorPalette.textDisabled,
                        onClick = {
                            binder?.player?.enqueue(songs.map(Song::asMediaItem))
                        }
                    )
                     */

                    /*
                    HeaderIconButton(
                        icon = R.drawable.smart_shuffle,
                        enabled = true,
                        color = if (isRecommendationEnabled) colorPalette.text else colorPalette.textDisabled,
                        onClick = {
                            isRecommendationEnabled = !isRecommendationEnabled
                        }
                    )

                     */


                    if (builtInPlaylist == BuiltInPlaylist.Offline)
                        HeaderIconButton(
                            icon = R.drawable.trash,
                            enabled = true,
                            color = if (songs.isNotEmpty()) colorPalette.text else colorPalette.textDisabled,
                            onClick = {},
                            modifier = Modifier
                                .combinedClickable(
                                    onClick = {
                                        cleanCacheOfflineSongs = true
                                    },
                                    onLongClick = {
                                        SmartMessage(context.resources.getString(R.string.info_clean_cached_congs), context = context)
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


                    /*
                    HeaderIconButton(
                        icon = R.drawable.shuffle,
                        enabled = songs.isNotEmpty(),
                        color = if (songs.isNotEmpty()) colorPalette.text else colorPalette.textDisabled,
                        onClick = {
                            if (songs.isNotEmpty()) {
                                val itemsLimited = if (songs.size > maxSongsInQueue.number)  songs.shuffled().take(maxSongsInQueue.number.toInt()) else songs
                                binder?.stopRadio()
                                binder?.player?.forcePlayFromBeginning(
                                    itemsLimited.shuffled().map(Song::asMediaItem)
                                )
                            }
                        }
                    )

                     */

                    HeaderIconButton(
                        modifier = Modifier.padding(horizontal = 5.dp)
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
                    LaunchedEffect(scrollToNowPlaying) {
                        if (scrollToNowPlaying)
                            lazyListState.scrollToItem(nowPlayingItem, 1)
                        scrollToNowPlaying = false
                    }

                    HeaderIconButton(
                        icon = R.drawable.ellipsis_horizontal,
                        color = if (songs.isNotEmpty() == true) colorPalette.text else colorPalette.textDisabled,
                        enabled = songs.isNotEmpty() == true,
                        modifier = Modifier
                            .padding(end = 4.dp),
                        onClick = {
                            menuState.display {
                                PlaylistsItemMenu(
                                    navController = navController,
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
                                    onAddToPlaylist = { playlistPreview ->
                                        position =
                                            playlistPreview.songCount.minus(1)
                                        //Log.d("mediaItem", " maxPos in Playlist $it ${position}")
                                        if (position > 0) position++ else position = 0
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
                                    onExport = {
                                        isExporting = true
                                    },
                                    onGoToPlaylist = {
                                        navController.navigate("${NavRoutes.localPlaylist.name}/$it")
                                    }
                                )
                            }
                        }
                    )


                }

                Spacer(modifier = Modifier.height(10.dp))

                Row (
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(horizontal = 10.dp)
                        .fillMaxWidth()
                ) {

                    if ( builtInPlaylist == BuiltInPlaylist.Favorites ||
                        builtInPlaylist == BuiltInPlaylist.Offline ||
                        builtInPlaylist == BuiltInPlaylist.Downloaded )  {
                        HeaderIconButton(
                            icon = R.drawable.arrow_up,
                            color = colorPalette.text,
                            onClick = { sortOrder = !sortOrder },
                            modifier = Modifier
                                .graphicsLayer { rotationZ = sortOrderIconRotation }
                        )

                        BasicText(
                            text = when (sortBy) {
                                SongSortBy.Title, SongSortBy.AlbumName -> stringResource(R.string.sort_title)
                                SongSortBy.DatePlayed -> stringResource(R.string.sort_date_played)
                                SongSortBy.PlayTime -> stringResource(R.string.sort_listening_time)
                                SongSortBy.DateAdded -> stringResource(R.string.sort_date_added)
                                SongSortBy.DateLiked -> stringResource(R.string.sort_date_liked)
                                SongSortBy.Artist -> stringResource(R.string.sort_artist)
                                SongSortBy.Duration -> stringResource(R.string.sort_duration)
                            },
                            style = typography.xs.semiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                //.fillMaxWidth(0.7f)
                                .clickable {
                                    menuState.display {
                                        SortMenu(
                                            title = stringResource(R.string.sorting_order),
                                            onDismiss = menuState::hide,
                                            onTitle = { sortBy = SongSortBy.Title },
                                            onDatePlayed = { sortBy = SongSortBy.DatePlayed },
                                            onDateAdded = { sortBy = SongSortBy.DateAdded },
                                            onPlayTime = { sortBy = SongSortBy.PlayTime },
                                            onDateLiked = { sortBy = SongSortBy.DateLiked },
                                            onArtist = { sortBy = SongSortBy.Artist },
                                            onDuration = { sortBy = SongSortBy.Duration }
                                        )
                                    }
                                    //showSortTypeSelectDialog = true
                                }
                        )

                    }


                    Spacer(
                        modifier = Modifier
                            .width(30.dp)
                    )


                }

                Row (
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier
                        .padding(all = 10.dp)
                        .fillMaxWidth()
                ) {
                    AnimatedVisibility(visible = searching) {
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
                                            style = typography.xs.semiBold.secondary.copy(color = colorPalette.textDisabled),
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

            }

            itemsIndexed(
                items = songs,
                key = { _, song -> song.id },
                contentType = { _, song -> song },
            ) { index, song ->

                if (index in positionsRecommendationList.distinct()) {
                    val songRecommended = relatedSongsRecommendationResult?.getOrNull()?.songs?.shuffled()
                        ?.lastOrNull()
                    val duration = songRecommended?.durationText
                    songRecommended?.asMediaItem?.let {
                        SongItem(
                            song = it,
                            duration = duration,
                            isRecommended = true,
                            thumbnailSizeDp = thumbnailSizeDp,
                            thumbnailSizePx = thumbnailSizePx,
                            isDownloaded = false,
                            onDownloadClick = {},
                            downloadState = Download.STATE_STOPPED,
                            trailingContent = {},
                            onThumbnailContent = {},
                            modifier = Modifier
                                .clickable {
                                    binder?.stopRadio()
                                    binder?.player?.forcePlay(it)
                                }

                        )
                    }
                }

                //BehindMotionSwipe(
                //    content = {
                        val isLocal by remember { derivedStateOf { song.asMediaItem.isLocal } }
                        downloadState = getDownloadState(song.asMediaItem.mediaId)
                        val isDownloaded =
                            if (!isLocal) downloadedStateMedia(song.asMediaItem.mediaId) else true
                        val checkedState = rememberSaveable { mutableStateOf(false) }
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

                                if (!isLocal)
                                    manageDownload(
                                        context = context,
                                        songId = song.asMediaItem.mediaId,
                                        songTitle = song.asMediaItem.mediaMetadata.title.toString(),
                                        downloadState = isDownloaded
                                    )
                            },
                            downloadState = downloadState,
                            thumbnailSizeDp = thumbnailSizeDp,
                            thumbnailSizePx = thumbnailSizePx,
                            onThumbnailContent = {
                                if (sortBy == SongSortBy.PlayTime) {
                                    BasicText(
                                        text = song.formattedTotalPlayTime,
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
                                    NowPlayingShow(song.asMediaItem.mediaId)

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
                            modifier = Modifier
                                .combinedClickable(
                                    onLongClick = {
                                        menuState.display {
                                            when (builtInPlaylist) {
                                                BuiltInPlaylist.Favorites,
                                                BuiltInPlaylist.Downloaded,
                                                BuiltInPlaylist.Top -> NonQueuedMediaItemMenuLibrary(
                                                    navController = navController,
                                                    mediaItem = song.asMediaItem,
                                                    onDismiss = menuState::hide
                                                )

                                                BuiltInPlaylist.Offline -> InHistoryMediaItemMenu(
                                                    navController = navController,
                                                    song = song,
                                                    onDismiss = menuState::hide
                                                )

                                                BuiltInPlaylist.OnDevice, BuiltInPlaylist.All -> {}
                                            }
                                        }
                                    },
                                    onClick = {
                                        if (!selectItems) {
                                            searching = false
                                            filter = null
                                            val itemsLimited =
                                                if (songs.size > maxSongsInQueue.number) songs.take(
                                                    maxSongsInQueue.number.toInt()
                                                ) else songs
                                            binder?.stopRadio()
                                            binder?.player?.forcePlayAtIndex(
                                                itemsLimited.map(Song::asMediaItem),
                                                index
                                            )
                                        } else checkedState.value = !checkedState.value
                                    }
                                )
                                .background(color = colorPalette.background0)
                                .animateItemPlacement()
                        )
                    /*
                    },
                    leftActionsContent = {
                            LeftAction(
                                icon = R.drawable.play_skip_forward,
                                backgroundColor = Color.Transparent, //colorPalette.background4,
                                onClick = {
                                    //binder?.player?.enqueue( song.asMediaItem )
                                    binder?.player?.addNext( song.asMediaItem )
                                }
                            )
                    },
                    rightActionsContent = {

                            var likedAt by remember {
                                mutableStateOf<Long?>(null)
                            }
                            LaunchedEffect(Unit, song.asMediaItem.mediaId) {
                                Database.likedAt(song.asMediaItem.mediaId).collect { likedAt = it }
                            }

                            RightActions(
                                iconAction1 = if (likedAt == null) R.drawable.heart_outline else R.drawable.heart,
                                backgroundColorAction1 = Color.Transparent, //colorPalette.background4,
                                onClickAction1 = {
                                    songToggleLike(song)
                                },
                                iconAction2 = R.drawable.trash,
                                backgroundColorAction2 = Color.Transparent, //colorPalette.iconButtonPlayer,
                                enableAction2 = builtInPlaylist == BuiltInPlaylist.Offline,
                                onClickAction2 = {
                                    if (binder != null) {
                                        when (builtInPlaylist) {
                                            BuiltInPlaylist.Offline ->
                                                binder.cache.removeResource(song.asMediaItem.mediaId)
                                            BuiltInPlaylist.Favorites, BuiltInPlaylist.OnDevice, BuiltInPlaylist.All -> {}
                                            BuiltInPlaylist.Downloaded -> {/*
                                                binder.downloadCache.removeResource(song.asMediaItem.mediaId)
                                                manageDownload(
                                                    context = context,
                                                    songId = song.asMediaItem.mediaId,
                                                    songTitle = song.asMediaItem.mediaMetadata.title.toString(),
                                                    downloadState = false
                                                )*/
                                            }
                                            BuiltInPlaylist.Top -> {}
                                        }
                                    }
                                }
                            )


                    },
                    onHorizontalSwipeWhenActionDisabled = {}
                )
                */
            }

            item(
                key = "footer",
                contentType = 0,
            ) {
                Spacer(modifier = Modifier.height(Dimensions.bottomSpacer))
            }

            }

        FloatingActionsContainerWithScrollToTop(lazyListState = lazyListState)

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
