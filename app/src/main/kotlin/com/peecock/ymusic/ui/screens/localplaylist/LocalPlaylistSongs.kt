package com.peecock.ymusic.ui.screens.localplaylist

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
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download
import androidx.navigation.NavController
import com.github.doyaaaaaken.kotlincsv.client.KotlinCsvExperimental
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import com.peecock.compose.persist.persist
import com.peecock.compose.persist.persistList
import com.peecock.compose.reordering.draggedItem
import com.peecock.compose.reordering.rememberReorderingState
import com.peecock.compose.reordering.reorder
import com.peecock.innertube.Innertube
import com.peecock.innertube.models.bodies.BrowseBody
import com.peecock.innertube.models.bodies.NextBody
import com.peecock.innertube.requests.playlistPage
import com.peecock.innertube.requests.relatedSongs
import com.peecock.ymusic.Database
import com.peecock.ymusic.LocalPlayerServiceBinder
import com.peecock.ymusic.R
import com.peecock.ymusic.enums.MaxSongs
import com.peecock.ymusic.enums.NavRoutes
import com.peecock.ymusic.enums.NavigationBarPosition
import com.peecock.ymusic.enums.PlaylistSongSortBy
import com.peecock.ymusic.enums.PopupType
import com.peecock.ymusic.enums.RecommendationsNumber
import com.peecock.ymusic.enums.SortOrder
import com.peecock.ymusic.enums.ThumbnailRoundness
import com.peecock.ymusic.enums.UiType
import com.peecock.ymusic.models.Playlist
import com.peecock.ymusic.models.PlaylistPreview
import com.peecock.ymusic.models.Song
import com.peecock.ymusic.models.SongEntity
import com.peecock.ymusic.models.SongPlaylistMap
import com.peecock.ymusic.query
import com.peecock.ymusic.service.isLocal
import com.peecock.ymusic.transaction
import com.peecock.ymusic.ui.components.LocalMenuState
import com.peecock.ymusic.ui.components.SwipeableQueueItem
import com.peecock.ymusic.ui.components.themed.ConfirmationDialog
import com.peecock.ymusic.ui.components.themed.FloatingActionsContainerWithScrollToTop
import com.peecock.ymusic.ui.components.themed.HeaderIconButton
import com.peecock.ymusic.ui.components.themed.HeaderWithIcon
import com.peecock.ymusic.ui.components.themed.IconButton
import com.peecock.ymusic.ui.components.themed.IconInfo
import com.peecock.ymusic.ui.components.themed.InPlaylistMediaItemMenu
import com.peecock.ymusic.ui.components.themed.InputTextDialog
import com.peecock.ymusic.ui.components.themed.NowPlayingShow
import com.peecock.ymusic.ui.components.themed.Playlist
import com.peecock.ymusic.ui.components.themed.PlaylistsItemMenu
import com.peecock.ymusic.ui.components.themed.SmartMessage
import com.peecock.ymusic.ui.components.themed.SortMenu
import com.peecock.ymusic.ui.items.SongItem
import com.peecock.ymusic.ui.screens.home.PINNED_PREFIX
import com.peecock.ymusic.ui.screens.home.PIPED_PREFIX
import com.peecock.ymusic.ui.styling.Dimensions
import com.peecock.ymusic.ui.styling.LocalAppearance
import com.peecock.ymusic.ui.styling.favoritesIcon
import com.peecock.ymusic.ui.styling.onOverlay
import com.peecock.ymusic.ui.styling.overlay
import com.peecock.ymusic.ui.styling.px
import com.peecock.ymusic.utils.MONTHLY_PREFIX
import com.peecock.ymusic.utils.UiTypeKey
import com.peecock.ymusic.utils.addNext
import com.peecock.ymusic.utils.addToPipedPlaylist
import com.peecock.ymusic.utils.asMediaItem
import com.peecock.ymusic.utils.autosyncKey
import com.peecock.ymusic.utils.center
import com.peecock.ymusic.utils.cleanPrefix
import com.peecock.ymusic.utils.color
import com.peecock.ymusic.utils.completed
import com.peecock.ymusic.utils.deletePipedPlaylist
import com.peecock.ymusic.utils.downloadedStateMedia
import com.peecock.ymusic.utils.durationTextToMillis
import com.peecock.ymusic.utils.enqueue
import com.peecock.ymusic.utils.forcePlay
import com.peecock.ymusic.utils.forcePlayAtIndex
import com.peecock.ymusic.utils.forcePlayFromBeginning
import com.peecock.ymusic.utils.formatAsTime
import com.peecock.ymusic.utils.getDownloadState
import com.peecock.ymusic.utils.getPipedSession
import com.peecock.ymusic.utils.getTitleMonthlyPlaylist
import com.peecock.ymusic.utils.isLandscape
import com.peecock.ymusic.utils.isPipedEnabledKey
import com.peecock.ymusic.utils.isRecommendationEnabledKey
import com.peecock.ymusic.utils.manageDownload
import com.peecock.ymusic.utils.maxSongsInQueueKey
import com.peecock.ymusic.utils.navigationBarPositionKey
import com.peecock.ymusic.utils.playlistSongSortByKey
import com.peecock.ymusic.utils.recommendationsNumberKey
import com.peecock.ymusic.utils.rememberPreference
import com.peecock.ymusic.utils.removeFromPipedPlaylist
import com.peecock.ymusic.utils.renamePipedPlaylist
import com.peecock.ymusic.utils.reorderInQueueEnabledKey
import com.peecock.ymusic.utils.secondary
import com.peecock.ymusic.utils.semiBold
import com.peecock.ymusic.utils.showFloatingIconKey
import com.peecock.ymusic.utils.songSortOrderKey
import com.peecock.ymusic.utils.syncSongsInPipedPlaylist
import com.peecock.ymusic.utils.thumbnailRoundnessKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.UUID


@KotlinCsvExperimental
@ExperimentalMaterialApi
@ExperimentalTextApi
@SuppressLint("SuspiciousIndentation")
@ExperimentalAnimationApi
@ExperimentalFoundationApi
@ExperimentalComposeUiApi
@UnstableApi
@Composable
fun LocalPlaylistSongs(
    navController: NavController,
    playlistId: Long,
    onDelete: () -> Unit,
) {
    val (colorPalette, typography, thumbnailShape) = LocalAppearance.current
    val binder = LocalPlayerServiceBinder.current
    val menuState = LocalMenuState.current
    val uiType by rememberPreference(UiTypeKey, UiType.RiMusic)

    var playlistSongs by persistList<SongEntity>("localPlaylist/$playlistId/songs")
    var playlistPreview by persist<PlaylistPreview?>("localPlaylist/playlist")


    var sortBy by rememberPreference(playlistSongSortByKey, PlaylistSongSortBy.Title)
    var sortOrder by rememberPreference(songSortOrderKey, SortOrder.Descending)

    var filter: String? by rememberSaveable { mutableStateOf(null) }

    LaunchedEffect(Unit, filter, sortOrder, sortBy) {
        Database.songsPlaylist(playlistId, sortBy, sortOrder).filterNotNull()
            .collect { playlistSongs = it }
    }

    LaunchedEffect(Unit) {
        Database.singlePlaylistPreview(playlistId).collect { playlistPreview = it }
    }

    //**** SMART RECOMMENDATION
    val recommendationsNumber by rememberPreference(
        recommendationsNumberKey,
        RecommendationsNumber.`5`
    )
    var isRecommendationEnabled by rememberPreference(isRecommendationEnabledKey, false)
    var relatedSongsRecommendationResult by persist<Result<Innertube.RelatedSongs?>?>(tag = "home/relatedSongsResult")
    var songBaseRecommendation by persist<SongEntity?>("home/songBaseRecommendation")
    var positionsRecommendationList = arrayListOf<Int>()
    var autosync by rememberPreference(autosyncKey, false)

    if (isRecommendationEnabled) {
        LaunchedEffect(Unit, isRecommendationEnabled) {
            Database.songsPlaylist(playlistId, sortBy, sortOrder).distinctUntilChanged()
                .collect { songs ->
                    val song = songs.firstOrNull()
                    if (relatedSongsRecommendationResult == null || songBaseRecommendation?.song?.id != song?.song?.id) {
                        relatedSongsRecommendationResult =
                            Innertube.relatedSongs(NextBody(videoId = (song?.song?.id ?: "HZnNt9nnEhw")))
                    }
                    songBaseRecommendation = song
                }
        }
        //relatedSongsRecommendationResult?.getOrNull()?.songs?.toString()?.let { Log.d("mediaItem", "related  $it") }
        //Log.d("mediaItem","related size "+relatedSongsRecommendationResult?.getOrNull()?.songs?.size.toString())
        //val numRelated = relatedSongsResult?.getOrNull()?.songs?.size ?: 0
        //val relatedMax = playlistSongs.size
        if (relatedSongsRecommendationResult != null) {
            for (index in 0..recommendationsNumber.number) {
                positionsRecommendationList.add((0..playlistSongs.size).random())
            }
        }
        //Log.d("mediaItem","positionsList "+positionsRecommendationList.toString())
        //**** SMART RECOMMENDATION
    }

    var filterCharSequence: CharSequence
    filterCharSequence = filter.toString()

    if (!filter.isNullOrBlank())
        playlistSongs =
            playlistSongs.filter { songItem ->
                songItem.song.title.contains(
                    filterCharSequence,
                    true
                ) ?: false
                        || songItem.song.artistsText?.contains(
                    filterCharSequence,
                    true
                ) ?: false
                        || songItem.albumTitle?.contains(
                    filterCharSequence,
                    true
                ) ?: false
            }

    var searching by rememberSaveable { mutableStateOf(false) }

    var totalPlayTimes = 0L
    playlistSongs.forEach {
        totalPlayTimes += it.song.durationText?.let { it1 ->
            durationTextToMillis(it1)
        }?.toLong() ?: 0
    }


    val thumbnailRoundness by rememberPreference(
        thumbnailRoundnessKey,
        ThumbnailRoundness.Heavy
    )

    val sortOrderIconRotation by animateFloatAsState(
        targetValue = if (sortOrder == SortOrder.Ascending) 0f else 180f,
        animationSpec = tween(durationMillis = 400, easing = LinearEasing), label = ""
    )

    val lazyListState = rememberLazyListState()

    val reorderingState = rememberReorderingState(
        lazyListState = lazyListState,
        key = playlistSongs,
        onDragEnd = { fromIndex, toIndex ->
            //Log.d("mediaItem","reoder playlist $playlistId, from $fromIndex, to $toIndex")
            query {
                Database.move(playlistId, fromIndex, toIndex)
            }
        },
        extraItemCount = 1
    )


    var isDeleting by rememberSaveable {
        mutableStateOf(false)
    }

    val isPipedEnabled by rememberPreference(isPipedEnabledKey, false)
    val coroutineScope = rememberCoroutineScope()
    val pipedSession = getPipedSession()
    val context = LocalContext.current


    if (isDeleting) {
        ConfirmationDialog(
            text = stringResource(R.string.delete_playlist),
            onDismiss = { isDeleting = false },
            onConfirm = {
                query {
                    playlistPreview?.playlist?.let(Database::delete)
                }

                if (playlistPreview?.playlist?.name?.startsWith(PIPED_PREFIX) == true && isPipedEnabled && pipedSession.token.isNotEmpty())
                    deletePipedPlaylist(
                        context = context,
                        coroutineScope = coroutineScope,
                        pipedSession = pipedSession.toApiSession(),
                        id = UUID.fromString(playlistPreview?.playlist?.browseId)
                    )


                onDelete()
            }
        )
    }

    var isRenumbering by rememberSaveable {
        mutableStateOf(false)
    }
    if (isRenumbering) {
        ConfirmationDialog(
            text = stringResource(R.string.do_you_really_want_to_renumbering_positions_in_this_playlist),
            onDismiss = { isRenumbering = false },
            onConfirm = {
                query {
                    playlistSongs.forEachIndexed { index, song ->
                        playlistPreview?.playlist?.let {
                            Database.updateSongPosition(it.id, song.song.id, index)
                        }
                    }
                }

            }
        )
    }
    fun sync() {
        playlistPreview?.let { playlistPreview ->
            if (!playlistPreview.playlist.name.startsWith(
                    PIPED_PREFIX,
                    0,
                    true
                )
            ) {
                transaction {
                    runBlocking(Dispatchers.IO) {
                        withContext(Dispatchers.IO) {
                            Innertube.playlistPage(
                                BrowseBody(
                                    browseId = playlistPreview.playlist.browseId
                                        ?: ""
                                )
                            )
                                ?.completed()
                        }
                    }?.getOrNull()?.let { remotePlaylist ->
                        Database.clearPlaylist(playlistId)

                        remotePlaylist.songsPage
                            ?.items
                            ?.map(Innertube.SongItem::asMediaItem)
                            ?.onEach(Database::insert)
                            ?.mapIndexed { position, mediaItem ->
                                SongPlaylistMap(
                                    songId = mediaItem.mediaId,
                                    playlistId = playlistId,
                                    position = position
                                )
                            }?.let(Database::insertSongPlaylistMaps)
                    }
                }
            } else {
                syncSongsInPipedPlaylist(
                    context = context,
                    coroutineScope = coroutineScope,
                    pipedSession = pipedSession.toApiSession(),
                    idPipedPlaylist = UUID.fromString(
                        playlistPreview.playlist.browseId
                    ),
                    playlistId = playlistPreview.playlist.id

                )
            }
        }
    }

    var isReorderDisabled by rememberPreference(reorderInQueueEnabledKey, defaultValue = true)

    val playlistThumbnailSizeDp = Dimensions.thumbnails.playlist
    val playlistThumbnailSizePx = playlistThumbnailSizeDp.px

    val thumbnailSizeDp = Dimensions.thumbnails.song
    val thumbnailSizePx = thumbnailSizeDp.px

    val rippleIndication = ripple(bounded = false)

    var downloadState by remember {
        mutableStateOf(Download.STATE_STOPPED)
    }


    val uriHandler = LocalUriHandler.current

    var showConfirmDeleteDownloadDialog by remember {
        mutableStateOf(false)
    }

    var showConfirmDownloadAllDialog by remember {
        mutableStateOf(false)
    }

    var scrollToNowPlaying by remember {
        mutableStateOf(false)
    }

    var nowPlayingItem by remember {
        mutableStateOf(-1)
    }

    /*
    var showSortTypeSelectDialog by remember {
        mutableStateOf(false)
    }
     */
    /*
        var showAddPlaylistSelectDialog by remember {
            mutableStateOf(false)
        }
        var isCreatingNewPlaylist by rememberSaveable {
            mutableStateOf(false)
        }
        var showPlaylistSelectDialog by remember {
            mutableStateOf(false)
        }
        */
    var listMediaItems = remember {
        mutableListOf<MediaItem>()
    }

    var selectItems by remember {
        mutableStateOf(false)
    }

    var plistId by remember {
        mutableStateOf(0L)
    }
    var plistName by remember {
        mutableStateOf(playlistPreview?.playlist?.name)
    }
    /*
    val playlistPreviews by remember {
        Database.playlistPreviews(PlaylistSortBy.Name, SortOrder.Ascending)
    }.collectAsState(initial = emptyList(), context = Dispatchers.IO)
     */

    var position by remember {
        mutableIntStateOf(0)
    }

    val exportLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
            if (uri == null) return@rememberLauncherForActivityResult

            context.applicationContext.contentResolver.openOutputStream(uri)
                ?.use { outputStream ->
                    csvWriter().open(outputStream) {
                        writeRow(
                            "PlaylistBrowseId",
                            "PlaylistName",
                            "MediaId",
                            "Title",
                            "Artists",
                            "Duration",
                            "ThumbnailUrl"
                        )
                        if (listMediaItems.isEmpty()) {
                            playlistSongs.forEach {
                                writeRow(
                                    playlistPreview?.playlist?.browseId,
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
                                    playlistPreview?.playlist?.browseId,
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


    val importLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri == null) return@rememberLauncherForActivityResult

            context.applicationContext.contentResolver.openInputStream(uri)
                ?.use { inputStream ->
                    csvReader().open(inputStream) {
                        readAllWithHeaderAsSequence().forEachIndexed { index, row: Map<String, String> ->

                            transaction {
                                plistId = row["PlaylistName"]?.let {
                                    Database.playlistExistByName(
                                        it
                                    )
                                } ?: 0L

                                if (plistId == 0L) {
                                    plistId = row["PlaylistName"]?.let {
                                        Database.insert(
                                            Playlist(
                                                name = it,
                                                browseId = row["PlaylistBrowseId"]
                                            )
                                        )
                                    }!!
                                } else {
                                    /**/
                                    if (row["MediaId"] != null && row["Title"] != null) {
                                        val song =
                                            row["MediaId"]?.let {
                                                row["Title"]?.let { it1 ->
                                                    Song(
                                                        id = it,
                                                        title = it1,
                                                        artistsText = row["Artists"],
                                                        durationText = row["Duration"],
                                                        thumbnailUrl = row["ThumbnailUrl"]
                                                    )
                                                }
                                            }
                                        transaction {
                                            if (song != null) {
                                                Database.insert(song)
                                                Database.insert(
                                                    SongPlaylistMap(
                                                        songId = song.id,
                                                        playlistId = plistId,
                                                        position = index
                                                    )
                                                )
                                            }
                                        }


                                    }
                                    /**/
                                }
                            }

                        }
                    }

                }
        }

    var isRenaming by rememberSaveable {
        mutableStateOf(false)
    }
    var isExporting by rememberSaveable {
        mutableStateOf(false)
    }

    if (isRenaming || isExporting) {
        InputTextDialog(
            onDismiss = {
                isRenaming = false
                isExporting = false
            },
            title = stringResource(R.string.enter_the_playlist_name),
            value = playlistPreview?.playlist?.name?.let { cleanPrefix(it) } ?: "",
            placeholder = stringResource(R.string.enter_the_playlist_name),
            setValue = { text ->
                val pipedPlaylist = if (playlistPreview?.playlist?.name?.startsWith(PIPED_PREFIX) == true && isPipedEnabled && pipedSession.token.isNotEmpty())
                    true else false

                if (isRenaming) {
                    query {
                        playlistPreview?.playlist?.copy(name = if (!pipedPlaylist) text else "$PIPED_PREFIX$text")?.let(Database::update)
                    }

                    if (pipedPlaylist)
                        renamePipedPlaylist(
                            context = context,
                            coroutineScope = coroutineScope,
                            pipedSession = pipedSession.toApiSession(),
                            id = UUID.fromString(playlistPreview?.playlist?.browseId),
                            name = "$PIPED_PREFIX$text"
                        )

                }
                if (isExporting) {
                    plistName = text
                    try {
                        @SuppressLint("SimpleDateFormat")
                        val dateFormat = SimpleDateFormat("yyyyMMddHHmmss")
                        exportLauncher.launch("RMPlaylist_${text.take(20)}_${dateFormat.format(Date())}")
                    } catch (e: ActivityNotFoundException) {
                        SmartMessage(
                            context.resources.getString(R.string.info_not_find_app_create_doc),
                            type = PopupType.Warning, context = context
                        )
                    }
                }

            }
        )
    }

    val navigationBarPosition by rememberPreference(
        navigationBarPositionKey,
        NavigationBarPosition.Bottom
    )
    val maxSongsInQueue by rememberPreference(maxSongsInQueueKey, MaxSongs.`500`)

    val playlistNotMonthlyType =
        playlistPreview?.playlist?.name?.startsWith(MONTHLY_PREFIX, 0, true) == false
    val playlistNotPipedType =
        playlistPreview?.playlist?.name?.startsWith(PIPED_PREFIX, 0, true) == false
    val hapticFeedback = LocalHapticFeedback.current


    Box(
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
            state = reorderingState.lazyListState,
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
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {

                    HeaderWithIcon(
                        //title = playlistPreview?.playlist?.name?.substringAfter(PINNED_PREFIX) ?: "Unknown",
                        title = playlistPreview?.playlist?.name?.let { name ->
                            if (name.startsWith(PINNED_PREFIX, 0, true))
                                name.substringAfter(PINNED_PREFIX) else
                                if (name.startsWith(MONTHLY_PREFIX, 0, true))
                                    getTitleMonthlyPlaylist(name.substringAfter(MONTHLY_PREFIX)) else
                                    if (name.startsWith(PIPED_PREFIX, 0, true))
                                        name.substringAfter(PIPED_PREFIX) else name
                            //if (playlistNotMonthlyType) cleanPrefix(it)
                            //else getTitleMonthlyPlaylist(cleanPrefix(it))
                        } ?: "Unknown",
                        iconId = R.drawable.playlist,
                        enabled = true,
                        showIcon = false,
                        modifier = Modifier
                            .padding(bottom = 8.dp),
                        onClick = {}
                    )

                }

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

                    playlistPreview?.let {
                        Playlist(
                            playlist = it,
                            thumbnailSizeDp = playlistThumbnailSizeDp,
                            thumbnailSizePx = playlistThumbnailSizePx,
                            alternative = true,
                            showName = false,
                            modifier = Modifier
                                .padding(top = 14.dp)
                        )
                    }


                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.Start,
                        modifier = Modifier
                            //.fillMaxHeight()
                            .padding(end = 10.dp)
                            .fillMaxWidth(if (isLandscape) 0.90f else 0.80f)
                    ) {
                        Spacer(modifier = Modifier.height(10.dp))
                        IconInfo(
                            title = playlistSongs.size.toString(),
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

                    Column(
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
                            enabled = playlistSongs.isNotEmpty() == true,
                            color = if (playlistSongs.isNotEmpty() == true) colorPalette.text else colorPalette.textDisabled,
                            onClick = {},
                            modifier = Modifier
                                .combinedClickable(
                                    onClick = {
                                        playlistSongs.let { songs ->
                                            if (songs.isNotEmpty()) {
                                                val itemsLimited =
                                                    if (songs.size > maxSongsInQueue.number) songs.shuffled()
                                                        .take(maxSongsInQueue.number.toInt()) else songs
                                                binder?.stopRadio()
                                                binder?.player?.forcePlayFromBeginning(
                                                    itemsLimited.shuffled().map(SongEntity::asMediaItem)
                                                )
                                            }
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

                    if (playlistNotMonthlyType)
                        HeaderIconButton(
                            icon = R.drawable.pin,
                            enabled = playlistSongs.isNotEmpty(),
                            color = if (playlistPreview?.playlist?.name?.startsWith(
                                    PINNED_PREFIX,
                                    0,
                                    true
                                ) == true
                            )
                                colorPalette.text else colorPalette.textDisabled,
                            onClick = {},
                            modifier = Modifier
                                .combinedClickable(
                                    onClick = {
                                        query {
                                            if (playlistPreview?.playlist?.name?.startsWith(
                                                    PINNED_PREFIX,
                                                    0,
                                                    true
                                                ) == true
                                            )
                                                Database.unPinPlaylist(playlistId) else
                                                Database.pinPlaylist(playlistId)
                                        }
                                    },
                                    onLongClick = {
                                        SmartMessage(context.resources.getString(R.string.info_pin_unpin_playlist), context = context)
                                    }
                                )
                        )

                    if (sortBy == PlaylistSongSortBy.Position && sortOrder == SortOrder.Ascending)
                        HeaderIconButton(
                            icon = if (isReorderDisabled) R.drawable.locked else R.drawable.unlocked,
                            enabled = playlistSongs.isNotEmpty() == true,
                            color = if (playlistSongs.isNotEmpty() == true) colorPalette.text else colorPalette.textDisabled,
                            onClick = {},
                            modifier = Modifier
                                .combinedClickable(
                                    onClick = {
                                        if (sortBy == PlaylistSongSortBy.Position && sortOrder == SortOrder.Ascending) {
                                            isReorderDisabled = !isReorderDisabled
                                        } else {
                                            SmartMessage(
                                                context.resources.getString(R.string.info_reorder_is_possible_only_in_ascending_sort),
                                                type = PopupType.Warning, context = context
                                            )
                                        }
                                    },
                                    onLongClick = {
                                        SmartMessage(context.resources.getString(R.string.info_lock_unlock_reorder_songs), context = context)
                                    }
                                )
                        )

                    HeaderIconButton(
                        icon = R.drawable.downloaded,
                        enabled = playlistSongs.isNotEmpty(),
                        color = if (playlistSongs.isNotEmpty()) colorPalette.text else colorPalette.textDisabled,
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
                                isRecommendationEnabled = false
                                downloadState = Download.STATE_DOWNLOADING
                                if (listMediaItems.isEmpty()) {
                                    if (playlistSongs.isNotEmpty() == true)
                                        playlistSongs.forEach {
                                            binder?.cache?.removeResource(it.asMediaItem.mediaId)
                                            query {
                                                Database.insert(
                                                    Song(
                                                        id = it.asMediaItem.mediaId,
                                                        title = it.asMediaItem.mediaMetadata.title.toString(),
                                                        artistsText = it.asMediaItem.mediaMetadata.artist.toString(),
                                                        thumbnailUrl = it.song.thumbnailUrl,
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

                    HeaderIconButton(
                        icon = R.drawable.download,
                        enabled = playlistSongs.isNotEmpty(),
                        color = if (playlistSongs.isNotEmpty()) colorPalette.text else colorPalette.textDisabled,
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
                                if (listMediaItems.isEmpty()) {
                                    if (playlistSongs.isNotEmpty() == true)
                                        playlistSongs.forEach {
                                            binder?.cache?.removeResource(it.asMediaItem.mediaId)
                                            manageDownload(
                                                context = context,
                                                songId = it.asMediaItem.mediaId,
                                                songTitle = it.asMediaItem.mediaMetadata.title.toString(),
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
                                }
                            }
                        )
                    }

                    /*
                    HeaderIconButton(
                        icon = R.drawable.enqueue,
                        enabled = playlistSongs.isNotEmpty(),
                        color = if (playlistSongs.isNotEmpty()) colorPalette.text else colorPalette.textDisabled,
                        onClick = {
                            playlistSongs
                                .map(Song::asMediaItem)
                                .let { mediaItems ->
                                    binder?.player?.enqueue(mediaItems)
                                }
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

                    HeaderIconButton(
                        icon = R.drawable.shuffle,
                        enabled = playlistSongs.isNotEmpty() == true,
                        color = if (playlistSongs.isNotEmpty() == true) colorPalette.text else colorPalette.textDisabled,
                        onClick = {
                            playlistSongs.let { songs ->
                                if (songs.isNotEmpty()) {
                                    val itemsLimited = if (songs.size > maxSongsInQueue.number)  songs.shuffled().take(maxSongsInQueue.number.toInt()) else songs
                                    binder?.stopRadio()
                                    binder?.player?.forcePlayFromBeginning(
                                        itemsLimited.shuffled().map(Song::asMediaItem)
                                    )
                                }
                            }
                        }
                    )
                    */
                    HeaderIconButton(
                        icon = R.drawable.ellipsis_horizontal,
                        color = colorPalette.text, //if (playlistWithSongs?.songs?.isNotEmpty() == true) colorPalette.text else colorPalette.textDisabled,
                        enabled = true, //playlistWithSongs?.songs?.isNotEmpty() == true,
                        modifier = Modifier
                            .padding(end = 4.dp),
                        onClick = {
                            menuState.display {
                                playlistPreview?.let { playlistPreview ->
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
                                        playlist = playlistPreview,
                                        onEnqueue = {
                                            if (listMediaItems.isEmpty()) {
                                                binder?.player?.enqueue(
                                                    playlistSongs.map(SongEntity::asMediaItem),
                                                    context
                                                )
                                            } else {
                                                binder?.player?.enqueue(listMediaItems, context)
                                                listMediaItems.clear()
                                                selectItems = false
                                            }
                                        },
                                        onPlayNext = {
                                            if (listMediaItems.isEmpty()) {
                                                binder?.player?.addNext(
                                                    playlistSongs.map(SongEntity::asMediaItem),
                                                    context
                                                )
                                            } else {
                                                binder?.player?.addNext(listMediaItems, context)
                                                listMediaItems.clear()
                                                selectItems = false
                                            }
                                        },
                                        showOnSyncronize = !playlistPreview.playlist.browseId.isNullOrBlank(),
                                        /*
                                        onSyncronize = {
                                            if (!playlistPreview.playlist.name.startsWith(
                                                    PIPED_PREFIX,
                                                    0,
                                                    true
                                                )
                                            ) {
                                                transaction {
                                                    runBlocking(Dispatchers.IO) {
                                                        withContext(Dispatchers.IO) {
                                                            Innertube.playlistPage(
                                                                BrowseBody(
                                                                    browseId = playlistPreview.playlist.browseId
                                                                        ?: ""
                                                                )
                                                            )
                                                                ?.completed()
                                                        }
                                                    }?.getOrNull()?.let { remotePlaylist ->
                                                        Database.clearPlaylist(playlistId)

                                                        remotePlaylist.songsPage
                                                            ?.items
                                                            ?.map(Innertube.SongItem::asMediaItem)
                                                            ?.onEach(Database::insert)
                                                            ?.mapIndexed { position, mediaItem ->
                                                                SongPlaylistMap(
                                                                    songId = mediaItem.mediaId,
                                                                    playlistId = playlistId,
                                                                    position = position
                                                                )
                                                            }?.let(Database::insertSongPlaylistMaps)
                                                    }
                                                }
                                                //SmartToast(context.resources.getString(R.string.done))
                                                SmartMessage(context.resources.getString(R.string.done), context = context)
                                            } else {
                                                syncSongsInPipedPlaylist(
                                                    context = context,
                                                    coroutineScope = coroutineScope,
                                                    pipedSession = pipedSession.toApiSession(),
                                                    idPipedPlaylist = UUID.fromString(
                                                        playlistPreview.playlist.browseId
                                                    ),
                                                    playlistId = playlistPreview.playlist.id

                                                )
                                                //SmartToast(context.resources.getString(R.string.done))
                                                SmartMessage(context.resources.getString(R.string.done), context = context)
                                            }
                                        },
                                        */
                                        onSyncronize = {sync();SmartMessage(context.resources.getString(R.string.done), context = context) },
                                        onRename = {
                                            if (playlistNotMonthlyType || playlistNotPipedType)
                                                isRenaming = true
                                            else
                                                /*
                                                SmartToast(context.resources.getString(R.string.info_cannot_rename_a_monthly_or_piped_playlist))
                                                 */
                                                SmartMessage(context.resources.getString(R.string.info_cannot_rename_a_monthly_or_piped_playlist), context = context)
                                        },
                                        onAddToPlaylist = { playlistPreview ->
                                            position =
                                                playlistPreview.songCount.minus(1) ?: 0
                                            //Log.d("mediaItem", " maxPos in Playlist $it ${position}")
                                            if (position > 0) position++ else position = 0
                                            //Log.d("mediaItem", "next initial pos ${position}")
                                            if (listMediaItems.isEmpty()) {
                                                playlistSongs.forEachIndexed { index, song ->
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
                                                //println("pipedInfo mediaitemmenu uuid ${playlistPreview.playlist.browseId}")

                                                if (playlistPreview.playlist.name.startsWith(
                                                        PIPED_PREFIX
                                                    ) && isPipedEnabled && pipedSession.token.isNotEmpty()
                                                ) {
                                                    addToPipedPlaylist(
                                                        context = context,
                                                        coroutineScope = coroutineScope,
                                                        pipedSession = pipedSession.toApiSession(),
                                                        id = UUID.fromString(playlistPreview.playlist.browseId),
                                                        videos = listMediaItems.map { it.mediaId }
                                                            .toList()
                                                    )
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
                                                println("pipedInfo mediaitemmenu uuid ${playlistPreview.playlist.browseId}")

                                                if (playlistPreview.playlist.name.startsWith(
                                                        PIPED_PREFIX
                                                    ) && isPipedEnabled && pipedSession.token.isNotEmpty()
                                                )
                                                    addToPipedPlaylist(
                                                        context = context,
                                                        coroutineScope = coroutineScope,
                                                        pipedSession = pipedSession.toApiSession(),
                                                        id = UUID.fromString(playlistPreview.playlist.browseId),
                                                        videos = listMediaItems.map { it.mediaId }
                                                            .toList()
                                                    )
                                                listMediaItems.clear()
                                                selectItems = false
                                            }
                                        },
                                        onRenumberPositions = {
                                            if (playlistNotMonthlyType)
                                                isRenumbering = true
                                            else
                                                /*
                                                SmartToast(context.resources.getString(R.string.info_cannot_renumbering_a_monthly_playlist))
                                                 */
                                                SmartMessage(context.resources.getString(R.string.info_cannot_renumbering_a_monthly_playlist), context = context)
                                        },
                                        onDelete = {
                                            isDeleting = true
                                            /*
                                            if (playlistNotMonthlyType)
                                                isDeleting = true
                                            else
                                                SmartToast(context.resources.getString(R.string.info_cannot_delete_a_monthly_playlist))

                                             */
                                        },
                                        showonListenToYT = !playlistPreview.playlist.browseId.isNullOrBlank(),
                                        onListenToYT = {
                                            binder?.player?.pause()
                                            uriHandler.openUri(
                                                "https://youtube.com/playlist?list=${
                                                    playlistPreview?.playlist?.browseId?.removePrefix(
                                                        "VL"
                                                    )
                                                }"
                                            )
                                        },
                                        onExport = {
                                            isExporting = true
                                        },
                                        onGoToPlaylist = {
                                            navController.navigate("${NavRoutes.localPlaylist.name}/$it")
                                        }
                                        /*
                                        onImport = {
                                            try {
                                                importLauncher.launch(
                                                    arrayOf(
                                                        "text/csv",
                                                        "text/txt"
                                                    )
                                                )
                                            } catch (e: ActivityNotFoundException) {
                                                context.toast("Couldn't find an application to open documents")
                                            }
                                        }
                                        */
                                    )
                                }

                            }
                        }
                    )
                    //}
                }

                if (autosync && playlistPreview?.let { playlistPreview -> !playlistPreview.playlist.browseId.isNullOrBlank()} == true) {sync()}

                Spacer(modifier = Modifier.height(10.dp))

                /*        */
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(horizontal = 10.dp)
                        .fillMaxWidth()
                ) {

                    HeaderIconButton(
                        icon = R.drawable.arrow_up,
                        color = colorPalette.text,
                        onClick = { sortOrder = !sortOrder },
                        modifier = Modifier
                            .graphicsLayer { rotationZ = sortOrderIconRotation }
                    )

                    BasicText(
                        text = when (sortBy) {
                            PlaylistSongSortBy.Album -> stringResource(R.string.sort_album)
                            PlaylistSongSortBy.AlbumYear -> stringResource(R.string.sort_album_year)
                            PlaylistSongSortBy.Position -> stringResource(R.string.sort_position)
                            PlaylistSongSortBy.Title -> stringResource(R.string.sort_title)
                            PlaylistSongSortBy.DatePlayed -> stringResource(R.string.sort_date_played)
                            PlaylistSongSortBy.DateLiked -> stringResource(R.string.sort_date_liked)
                            PlaylistSongSortBy.Artist -> stringResource(R.string.sort_artist)
                            PlaylistSongSortBy.ArtistAndAlbum -> "${stringResource(R.string.sort_artist)}, ${
                                stringResource(
                                    R.string.sort_album
                                )
                            }"

                            PlaylistSongSortBy.PlayTime -> stringResource(R.string.sort_listening_time)
                            PlaylistSongSortBy.Duration -> stringResource(R.string.sort_duration)
                            PlaylistSongSortBy.DateAdded -> stringResource(R.string.sort_date_added)
                        },
                        style = typography.xs.semiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .clickable {
                                menuState.display {
                                    SortMenu(
                                        title = stringResource(R.string.sorting_order),
                                        onDismiss = menuState::hide,
                                        onTitle = { sortBy = PlaylistSongSortBy.Title },
                                        onAlbum = { sortBy = PlaylistSongSortBy.Album },
                                        onAlbumYear = { sortBy = PlaylistSongSortBy.AlbumYear },
                                        onDatePlayed = { sortBy = PlaylistSongSortBy.DatePlayed },
                                        onDateLiked = { sortBy = PlaylistSongSortBy.DateLiked },
                                        onPosition = { sortBy = PlaylistSongSortBy.Position },
                                        onArtist = { sortBy = PlaylistSongSortBy.Artist },
                                        onArtistAndAlbum = {
                                            sortBy = PlaylistSongSortBy.ArtistAndAlbum
                                        },
                                        onPlayTime = { sortBy = PlaylistSongSortBy.PlayTime },
                                        onDuration = { sortBy = PlaylistSongSortBy.Duration },
                                        onDateAdded = { sortBy = PlaylistSongSortBy.DateAdded }
                                    )
                                }

                            }
                    )

                    Row(
                        horizontalArrangement = Arrangement.End, //Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        HeaderIconButton(
                            modifier = Modifier
                                .padding(horizontal = 5.dp)
                                .combinedClickable(
                                    onClick = {
                                        nowPlayingItem = -1
                                        scrollToNowPlaying = false
                                        playlistSongs
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
                            enabled = playlistSongs.isNotEmpty(),
                            color = if (playlistSongs.isNotEmpty()) colorPalette.text else colorPalette.textDisabled,
                            onClick = {}
                        )
                        LaunchedEffect(scrollToNowPlaying) {
                            if (scrollToNowPlaying)
                                lazyListState.scrollToItem(nowPlayingItem, 1)
                            scrollToNowPlaying = false
                        }
                        /*
                        HeaderIconButton(
                            modifier = Modifier.padding(horizontal = 5.dp),
                            onClick = { searching = !searching },
                            icon = R.drawable.search_circle,
                            color = colorPalette.text,
                            iconSize = 24.dp
                        )
                         */
                    }

                }


                Row(
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


            }

            itemsIndexed(
                items = playlistSongs ?: emptyList(),
                key = { _, song -> song.song.id },
                contentType = { _, song -> song },
            ) { index, song ->

                if (index in positionsRecommendationList.distinct()) {
                    val songRecommended =
                        relatedSongsRecommendationResult?.getOrNull()?.songs?.shuffled()
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

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .draggedItem(
                            reorderingState = reorderingState,
                            index = index
                        )
                        .zIndex(10f)
                ) {
                    val isLocal by remember { derivedStateOf { song.asMediaItem.isLocal } }
                    downloadState = getDownloadState(song.asMediaItem.mediaId)
                    val isDownloaded =
                        if (!isLocal) downloadedStateMedia(song.asMediaItem.mediaId) else true
                    val checkedState = rememberSaveable { mutableStateOf(false) }
                    val positionInPlaylist: Int = index
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .zIndex(10f)
                            .align(Alignment.TopEnd)
                            .offset(x = -15.dp)

                    ) {
                        if (!isReorderDisabled && sortBy == PlaylistSongSortBy.Position && sortOrder == SortOrder.Ascending) {
                            IconButton(
                                icon = R.drawable.reorder,
                                color = colorPalette.textDisabled,
                                indication = rippleIndication,
                                onClick = {},
                                modifier = Modifier
                                    .reorder(
                                        reorderingState = reorderingState,
                                        index = index
                                    )
                            )
                        }
                    }

                    SwipeableQueueItem(
                        mediaItem = song.asMediaItem,
                        onSwipeToLeft = {
                            transaction {
                                Database.move(playlistId, positionInPlaylist, Int.MAX_VALUE)
                                Database.delete(SongPlaylistMap(song.song.id, playlistId, Int.MAX_VALUE))
                            }

                            if (playlistPreview?.playlist?.name?.startsWith(PIPED_PREFIX) == true && isPipedEnabled && pipedSession.token.isNotEmpty()) {
                                Timber.d("MediaItemMenu LocalPlaylistSongs onSwipeToLeft browseId ${playlistPreview!!.playlist.browseId}")
                                removeFromPipedPlaylist(
                                    context = context,
                                    coroutineScope = coroutineScope,
                                    pipedSession = pipedSession.toApiSession(),
                                    id = UUID.fromString(playlistPreview?.playlist?.browseId),
                                    positionInPlaylist
                                )
                            }
                            coroutineScope.launch {
                                SmartMessage(
                                    context.resources.getString(R.string.deleted) + " \"" + song.asMediaItem.mediaMetadata.title.toString() + " - " + song.asMediaItem.mediaMetadata.artist.toString() + "\" ",
                                    type = PopupType.Warning, context = context, durationLong = true
                                )
                            }

                        },
                        onSwipeToRight = {
                            binder?.player?.addNext(song.asMediaItem)
                        }
                    ) {
                        SongItem(
                            song = song.song,
                            isDownloaded = isDownloaded,
                            onDownloadClick = {
                                binder?.cache?.removeResource(song.asMediaItem.mediaId)
                                query {
                                    Database.insert(
                                        Song(
                                            id = song.asMediaItem.mediaId,
                                            title = song.asMediaItem.mediaMetadata.title.toString(),
                                            artistsText = song.asMediaItem.mediaMetadata.artist.toString(),
                                            thumbnailUrl = song.song.thumbnailUrl,
                                            durationText = null
                                        )
                                    )
                                }

                                if (!isLocal) {
                                    manageDownload(
                                        context = context,
                                        songId = song.asMediaItem.mediaId,
                                        songTitle = song.asMediaItem.mediaMetadata.title.toString(),
                                        downloadState = isDownloaded
                                    )
                                }
                                //if (isDownloaded) listDownloadedMedia.dropWhile { it.asMediaItem.mediaId == song.asMediaItem.mediaId } else listDownloadedMedia.add(song)
                                //Log.d("mediaItem", "manageDownload click isDownloaded ${isDownloaded} listDownloadedMedia ${listDownloadedMedia.distinct().size}")
                            },
                            downloadState = downloadState,
                            thumbnailSizePx = thumbnailSizePx,
                            thumbnailSizeDp = thumbnailSizeDp,
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

                                /*
                                if (!isReorderDisabled && sortBy == PlaylistSongSortBy.Position && sortOrder == SortOrder.Ascending) {
                                    IconButton(
                                        icon = R.drawable.reorder,
                                        color = colorPalette.textDisabled,
                                        indication = rippleIndication,
                                        onClick = {},
                                        modifier = Modifier
                                            .reorder(
                                                reorderingState = reorderingState,
                                                index = index
                                            )
                                            .size(18.dp)
                                    )
                                }
                                */
                            },
                            onThumbnailContent = {
                                if (sortBy == PlaylistSongSortBy.PlayTime) {
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

                                /*
                                if (sortBy == PlaylistSongSortBy.Position)
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
                                 */

                                if (nowPlayingItem > -1)
                                    NowPlayingShow(song.asMediaItem.mediaId)
                            },
                            modifier = Modifier
                                .combinedClickable(
                                    onLongClick = {
                                        menuState.display {
                                            InPlaylistMediaItemMenu(
                                                navController = navController,
                                                playlist = playlistPreview,
                                                playlistId = playlistId,
                                                positionInPlaylist = index,
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
                                            playlistSongs
                                                .map(SongEntity::asMediaItem)
                                                .let { mediaItems ->
                                                    binder?.stopRadio()
                                                    binder?.player?.forcePlayAtIndex(
                                                        mediaItems,
                                                        index
                                                    )
                                                }
                                        } else checkedState.value = !checkedState.value
                                    }
                                )
                                .draggedItem(reorderingState = reorderingState, index = index)
                                .background(color = colorPalette.background0)
                        )
                    }
                }

            }

            item(
                key = "footer",
                contentType = 0,
            ) {
                Spacer(modifier = Modifier.height(Dimensions.bottomSpacer))
            }
        }

        FloatingActionsContainerWithScrollToTop(lazyListState = lazyListState)

        val showFloatingIcon by rememberPreference(showFloatingIconKey, false)
        if (uiType == UiType.ViMusic || showFloatingIcon)
            FloatingActionsContainerWithScrollToTop(
                lazyListState = lazyListState,
                iconId = R.drawable.shuffle,
                visible = !reorderingState.isDragging,
                onClick = {
                    playlistSongs.let { songs ->
                        if (songs.isNotEmpty()) {
                            binder?.stopRadio()
                            binder?.player?.forcePlayFromBeginning(
                                songs.shuffled().map(SongEntity::asMediaItem)
                            )
                        }
                    }
                }
            )


    }
}







