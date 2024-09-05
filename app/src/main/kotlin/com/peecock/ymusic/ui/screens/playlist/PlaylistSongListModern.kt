package com.peecock.ymusic.ui.screens.playlist

import android.annotation.SuppressLint
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.peecock.compose.persist.persist
import com.peecock.compose.persist.persistList
import com.peecock.innertube.Innertube
import com.peecock.innertube.models.NavigationEndpoint
import com.peecock.innertube.models.bodies.BrowseBody
import com.peecock.innertube.requests.playlistPage
import com.peecock.ymusic.Database
import com.peecock.ymusic.LocalPlayerServiceBinder
import com.peecock.ymusic.R
import com.peecock.ymusic.enums.NavRoutes
import com.peecock.ymusic.enums.NavigationBarPosition
import com.peecock.ymusic.enums.PopupType
import com.peecock.ymusic.enums.ThumbnailRoundness
import com.peecock.ymusic.enums.UiType
import com.peecock.ymusic.models.Playlist
import com.peecock.ymusic.models.Song
import com.peecock.ymusic.models.SongPlaylistMap
import com.peecock.ymusic.query
import com.peecock.ymusic.service.isLocal
import com.peecock.ymusic.transaction
import com.peecock.ymusic.ui.components.LocalMenuState
import com.peecock.ymusic.ui.components.ShimmerHost
import com.peecock.ymusic.ui.components.SwipeablePlaylistItem
import com.peecock.ymusic.ui.components.themed.AutoResizeText
import com.peecock.ymusic.ui.components.themed.FloatingActionsContainerWithScrollToTop
import com.peecock.ymusic.ui.components.themed.FontSizeRange
import com.peecock.ymusic.ui.components.themed.HeaderIconButton
import com.peecock.ymusic.ui.components.themed.IconButton
import com.peecock.ymusic.ui.components.themed.InputTextDialog
import com.peecock.ymusic.ui.components.themed.LayoutWithAdaptiveThumbnail
import com.peecock.ymusic.ui.components.themed.NonQueuedMediaItemMenu
import com.peecock.ymusic.ui.components.themed.PlaylistsItemMenu
import com.peecock.ymusic.ui.components.themed.SmartMessage
import com.peecock.ymusic.ui.components.themed.adaptiveThumbnailContent
import com.peecock.ymusic.ui.items.AlbumItemPlaceholder
import com.peecock.ymusic.ui.items.SongItem
import com.peecock.ymusic.ui.items.SongItemPlaceholder
import com.peecock.ymusic.ui.styling.Dimensions
import com.peecock.ymusic.ui.styling.LocalAppearance
import com.peecock.ymusic.ui.styling.favoritesIcon
import com.peecock.ymusic.ui.styling.px
import com.peecock.ymusic.utils.UiTypeKey
import com.peecock.ymusic.utils.addNext
import com.peecock.ymusic.utils.asMediaItem
import com.peecock.ymusic.utils.asSong
import com.peecock.ymusic.utils.completed
import com.peecock.ymusic.utils.downloadedStateMedia
import com.peecock.ymusic.utils.durationTextToMillis
import com.peecock.ymusic.utils.enqueue
import com.peecock.ymusic.utils.fadingEdge
import com.peecock.ymusic.utils.forcePlayAtIndex
import com.peecock.ymusic.utils.forcePlayFromBeginning
import com.peecock.ymusic.utils.formatAsTime
import com.peecock.ymusic.utils.getDownloadState
import com.peecock.ymusic.utils.isLandscape
import com.peecock.ymusic.utils.manageDownload
import com.peecock.ymusic.utils.medium
import com.peecock.ymusic.utils.navigationBarPositionKey
import com.peecock.ymusic.utils.rememberPreference
import com.peecock.ymusic.utils.resize
import com.peecock.ymusic.utils.secondary
import com.peecock.ymusic.utils.semiBold
import com.peecock.ymusic.utils.showFloatingIconKey
import com.peecock.ymusic.utils.thumbnailRoundnessKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber


@ExperimentalTextApi
@SuppressLint("SuspiciousIndentation")
@ExperimentalFoundationApi
@ExperimentalAnimationApi
@ExperimentalComposeUiApi
@UnstableApi
@Composable
fun PlaylistSongListModern(
    navController: NavController,
    browseId: String,
    params: String?,
    maxDepth: Int?,
) {
    val (colorPalette, typography) = LocalAppearance.current
    val binder = LocalPlayerServiceBinder.current
    val context = LocalContext.current
    val menuState = LocalMenuState.current
    val uiType  by rememberPreference(UiTypeKey, UiType.RiMusic)

    var playlistPage by persist<Innertube.PlaylistOrAlbumPage?>("playlist/$browseId/playlistPage")
    var playlistSongs by persistList<Innertube.SongItem>("playlist/$browseId/songs")

    var filter: String? by rememberSaveable { mutableStateOf(null) }
    val hapticFeedback = LocalHapticFeedback.current

    LaunchedEffect(Unit, filter) {
        if (playlistPage != null && playlistPage?.songsPage?.continuation == null) return@LaunchedEffect

        playlistPage = withContext(Dispatchers.IO) {
            Innertube.playlistPage(BrowseBody(browseId = browseId))?.completed()?.getOrNull()
        }

        playlistSongs = playlistPage?.songsPage?.items!!
        /*
        playlistPage = withContext(Dispatchers.IO) {
            Innertube
                .playlistPage(BrowseBody(browseId = browseId, params = params))
                ?.completed()
                ?.getOrNull()
        }
         */
        //Log.d("mediaPlaylist", "${playlistPage?.title} songs ${playlistPage?.songsPage?.items?.size} continuation ${playlistPage?.songsPage?.continuation}")

/*
                playlistPage = withContext(Dispatchers.IO) {
                    Innertube.playlistPage(BrowseBody(browseId = browseId, params = params))
                        ?.completed(maxDepth = maxDepth ?: Int.MAX_VALUE)?.getOrNull()
                }
 */


/*
        playlistPage = withContext(Dispatchers.IO) {
            Innertube.playlistPage(BrowseBody(browseId = browseId))?.completed()?.getOrNull()
        }
*/
    }

    var filterCharSequence: CharSequence
    filterCharSequence = filter.toString()
    //Log.d("mediaItemFilter", "<${filter}>  <${filterCharSequence}>")
    if (!filter.isNullOrBlank()) {
        playlistPage?.songsPage?.items =
            playlistPage?.songsPage?.items?.filter { songItem ->
                songItem.asMediaItem.mediaMetadata.title?.contains(
                    filterCharSequence,
                    true
                ) ?: false
                        || songItem.asMediaItem.mediaMetadata.artist?.contains(
                    filterCharSequence,
                    true
                ) ?: false
                        || songItem.asMediaItem.mediaMetadata.albumTitle?.contains(
                    filterCharSequence,
                    true
                ) ?: false
            }
    } else playlistPage?.songsPage?.items = playlistSongs


    var searching by rememberSaveable { mutableStateOf(false) }

    val songThumbnailSizeDp = Dimensions.thumbnails.song
    val songThumbnailSizePx = songThumbnailSizeDp.px

    var isImportingPlaylist by rememberSaveable {
        mutableStateOf(false)
    }

    var downloadState by remember {
        mutableStateOf(Download.STATE_STOPPED)
    }

    var thumbnailRoundness by rememberPreference(
        thumbnailRoundnessKey,
        ThumbnailRoundness.Heavy
    )
/*
    var showAddPlaylistSelectDialog by remember {
        mutableStateOf(false)
    }

    val playlistPreviews by remember {
        Database.playlistPreviews(PlaylistSortBy.Name, SortOrder.Ascending)
    }.collectAsState(initial = emptyList(), context = Dispatchers.IO)

    var showPlaylistSelectDialog by remember {
        mutableStateOf(false)
    }
 */

    var totalPlayTimes = 0L
    playlistPage?.songsPage?.items?.forEach {
        totalPlayTimes += it.durationText?.let { it1 ->
            durationTextToMillis(it1) }?.toLong() ?: 0
    }

    if (isImportingPlaylist) {
        InputTextDialog(
            onDismiss = { isImportingPlaylist = false },
            title = stringResource(R.string.enter_the_playlist_name),
            value = playlistPage?.title ?: "",
            placeholder = "https://........",
            setValue = { text ->
                query {
                    transaction {
                        val playlistId = Database.insert(Playlist(name = text, browseId = browseId))

                        playlistPage?.songsPage?.items
                            ?.map(Innertube.SongItem::asMediaItem)
                            ?.onEach(Database::insert)
                            ?.mapIndexed { index, mediaItem ->
                                SongPlaylistMap(
                                    songId = mediaItem.mediaId,
                                    playlistId = playlistId,
                                    position = index
                                )
                            }?.let(Database::insertSongPlaylistMaps)
                    }
                }
                SmartMessage(context.resources.getString(R.string.done), PopupType.Success, context = context)
            }
        )
    }

    var position by remember {
        mutableIntStateOf(0)
    }

    val thumbnailContent = adaptiveThumbnailContent(playlistPage == null, playlistPage?.thumbnail?.url)

    val lazyListState = rememberLazyListState()

    val navigationBarPosition by rememberPreference(navigationBarPositionKey, NavigationBarPosition.Bottom)

    val coroutineScope = rememberCoroutineScope()

    LayoutWithAdaptiveThumbnail(thumbnailContent = thumbnailContent) {
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
                state = lazyListState,
                //contentPadding = LocalPlayerAwareWindowInsets.current
                //.only(WindowInsetsSides.Vertical + WindowInsetsSides.End).asPaddingValues(),
                modifier = Modifier
                    .background(colorPalette.background0)
                    .fillMaxSize()
            ) {

                item(
                    key = "header"
                ) {

                    val modifierArt = if (isLandscape) Modifier.fillMaxWidth() else Modifier.fillMaxWidth().aspectRatio(4f / 3)

                    Box(
                        modifier = modifierArt
                    ) {
                        if (playlistPage != null) {
                            if(!isLandscape)
                                AsyncImage(
                                    model = playlistPage!!.thumbnail?.url?.resize(1200, 900),
                                    contentDescription = "loading...",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .align(Alignment.Center)
                                        .fadingEdge(
                                            top = WindowInsets.systemBars
                                                .asPaddingValues()
                                                .calculateTopPadding() + Dimensions.fadeSpacingTop,
                                            bottom = Dimensions.fadeSpacingBottom
                                        )
                                )

                            AutoResizeText(
                                text = playlistPage?.title ?: "",
                                style = typography.l.semiBold,
                                fontSizeRange = FontSizeRange(32.sp, 38.sp),
                                fontWeight = typography.l.semiBold.fontWeight,
                                fontFamily = typography.l.semiBold.fontFamily,
                                color = typography.l.semiBold.color,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(horizontal = 30.dp)
                                    .padding(bottom = 20.dp)
                            )

                            BasicText(
                                text = playlistPage!!.songsPage?.items?.size.toString() + " "
                                        + stringResource(R.string.songs)
                                        + " - " + formatAsTime(totalPlayTimes),
                                style = typography.xs.medium,
                                maxLines = 1,
                                modifier = Modifier
                                    //.padding(top = 10.dp)
                                    .align(Alignment.BottomCenter)
                            )


                            HeaderIconButton(
                                icon = R.drawable.share_social,
                                color = colorPalette.text,
                                iconSize = 24.dp,
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(top = 5.dp, end= 5.dp),
                                onClick = {
                                    (playlistPage?.url ?: "https://music.youtube.com/playlist?list=${browseId.removePrefix("VL")}").let { url ->
                                        val sendIntent = Intent().apply {
                                            action = Intent.ACTION_SEND
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_TEXT, url)
                                        }

                                        context.startActivity(Intent.createChooser(sendIntent, null))
                                    }
                                }
                            )

                        } else {
                            Column(
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(4f / 3)
                            ) {
                                ShimmerHost {
                                    AlbumItemPlaceholder(
                                        thumbnailSizeDp = 200.dp,
                                        alternative = true
                                    )
                                    BasicText(
                                        text = stringResource(R.string.info_wait_it_may_take_a_few_minutes),
                                        style = typography.xs.medium,
                                        maxLines = 1,
                                        modifier = Modifier
                                            //.padding(top = 10.dp)

                                    )
                                }
                            }
                        }
                    }

                }

                item(
                    key = "actions",
                    contentType = 0
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(top = 10.dp)
                            .fillMaxWidth()
                    ) {

                        //if (!isLandscape) thumbnailContent()

                        if (playlistPage != null) {

                            //actionsContent()

                            HeaderIconButton(
                                onClick = { searching = !searching },
                                icon = R.drawable.search_circle,
                                color = colorPalette.text,
                                iconSize = 24.dp,
                                modifier = Modifier
                                    .padding(horizontal = 5.dp)
                            )

                            HeaderIconButton(
                                icon = R.drawable.downloaded,
                                color = colorPalette.text,
                                onClick = {},
                                modifier = Modifier
                                    .padding(horizontal = 5.dp)
                                    .combinedClickable(
                                        onClick = {
                                            downloadState = Download.STATE_DOWNLOADING
                                            if (playlistPage?.songsPage?.items?.isNotEmpty() == true)
                                                playlistPage?.songsPage?.items?.forEach {
                                                    binder?.cache?.removeResource(it.asMediaItem.mediaId)
                                                    query {
                                                        Database.insert(
                                                            Song(
                                                                id = it.asMediaItem.mediaId,
                                                                title = it.asMediaItem.mediaMetadata.title.toString(),
                                                                artistsText = it.asMediaItem.mediaMetadata.artist.toString(),
                                                                thumbnailUrl = it.thumbnail?.url,
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
                                    .padding(horizontal = 5.dp)
                                    .combinedClickable(
                                        onClick = {
                                            downloadState = Download.STATE_DOWNLOADING
                                            if (playlistPage?.songsPage?.items?.isNotEmpty() == true)
                                                playlistPage?.songsPage?.items?.forEach {
                                                    binder?.cache?.removeResource(it.asMediaItem.mediaId)
                                                    manageDownload(
                                                        context = context,
                                                        songId = it.asMediaItem.mediaId,
                                                        songTitle = it.asMediaItem.mediaMetadata.title.toString(),
                                                        downloadState = true
                                                    )
                                                }
                                        },
                                        onLongClick = {
                                            SmartMessage(context.resources.getString(R.string.info_remove_all_downloaded_songs), context = context)
                                        }
                                    )
                            )



                            HeaderIconButton(
                                icon = R.drawable.enqueue,
                                enabled = playlistPage?.songsPage?.items?.isNotEmpty() == true,
                                color =  if (playlistPage?.songsPage?.items?.isNotEmpty() == true) colorPalette.text else colorPalette.textDisabled,
                                onClick = {},
                                modifier = Modifier
                                    .padding(horizontal = 5.dp)
                                    .combinedClickable(
                                        onClick = {
                                            playlistPage?.songsPage?.items?.map(Innertube.SongItem::asMediaItem)?.let { mediaItems ->
                                                binder?.player?.enqueue(mediaItems, context)
                                            }
                                        },
                                        onLongClick = {
                                            SmartMessage(context.resources.getString(R.string.info_enqueue_songs), context = context)
                                        }
                                    )
                            )

                            HeaderIconButton(
                                icon = R.drawable.shuffle,
                                enabled = playlistPage?.songsPage?.items?.isNotEmpty() == true,
                                color = if (playlistPage?.songsPage?.items?.isNotEmpty() ==true) colorPalette.text else colorPalette.textDisabled,
                                onClick = {},
                                modifier = Modifier
                                    .padding(horizontal = 5.dp)
                                    .combinedClickable(
                                        onClick = {
                                            if (playlistPage?.songsPage?.items?.isNotEmpty() == true) {
                                                binder?.stopRadio()
                                                playlistPage?.songsPage?.items?.shuffled()?.map(Innertube.SongItem::asMediaItem)
                                                    ?.let {
                                                        binder?.player?.forcePlayFromBeginning(
                                                            it
                                                        )
                                                    }
                                            }
                                        },
                                        onLongClick = {
                                            SmartMessage(context.resources.getString(R.string.info_shuffle), context = context)
                                        }
                                    )
                            )

                            HeaderIconButton(
                                icon = R.drawable.radio,
                                enabled = playlistPage?.songsPage?.items?.isNotEmpty() == true,
                                color = colorPalette.text,
                                onClick = {},
                                modifier = Modifier
                                    .padding(horizontal = 5.dp)
                                    .combinedClickable(
                                        onClick = {
                                            if (binder != null) {
                                                binder.stopRadio()
                                                binder.playRadio(
                                                    NavigationEndpoint.Endpoint.Watch( videoId =
                                                        if (binder.player.currentMediaItem?.mediaId != null)
                                                            binder.player.currentMediaItem?.mediaId
                                                        else playlistPage?.songsPage?.items?.first()?.asMediaItem?.mediaId
                                                    )
                                                )
                                            }

                                        },
                                        onLongClick = {
                                            SmartMessage(context.resources.getString(R.string.info_start_radio), context = context)
                                        }
                                    )
                            )


                            HeaderIconButton(
                                icon = R.drawable.add_in_playlist,
                                color = colorPalette.text,
                                onClick = {},
                                modifier = Modifier
                                    .padding(horizontal = 5.dp)
                                    .combinedClickable(
                                        onClick = {
                                            menuState.display {
                                                PlaylistsItemMenu(
                                                    navController = navController,
                                                    modifier = Modifier.fillMaxHeight(0.4f),
                                                    onDismiss = menuState::hide,
                                                    onImportOnlinePlaylist = {
                                                        isImportingPlaylist = true
                                                    },

                                                    onAddToPlaylist = { playlistPreview ->
                                                        position =
                                                            playlistPreview.songCount.minus(1) ?: 0
                                                        if (position > 0) position++ else position = 0

                                                        playlistPage!!.songsPage?.items?.forEachIndexed { index, song ->
                                                            runCatching {
                                                                 coroutineScope.launch(Dispatchers.IO) {
                                                                    Database.insert(song.asSong)
                                                                    Database.insert(
                                                                        SongPlaylistMap(
                                                                            songId = song.asMediaItem.mediaId,
                                                                            playlistId = playlistPreview.playlist.id,
                                                                            position = position + index
                                                                        )
                                                                    )
                                                                }
                                                            }.onFailure {
                                                                Timber.e("Failed onAddToPlaylist in PlaylistSongListModern  ${it.stackTraceToString()}")
                                                            }
                                                        }
                                                        CoroutineScope(Dispatchers.Main).launch {
                                                            SmartMessage(context.resources.getString(R.string.done), type = PopupType.Success, context = context)
                                                        }
                                                    },

                                                    onGoToPlaylist = {
                                                        navController.navigate("${NavRoutes.localPlaylist.name}/$it")
                                                    }


                                                )
                                            }
                                        },
                                        onLongClick = {
                                            SmartMessage(context.resources.getString(R.string.info_add_in_playlist), context = context)
                                        }
                                    )
                            )


                            /*
                            HeaderIconButton(
                                icon = R.drawable.share_social,
                                color = colorPalette.text,
                                onClick = {
                                    (playlistPage?.url ?: "https://music.youtube.com/playlist?list=${browseId.removePrefix("VL")}").let { url ->
                                        val sendIntent = Intent().apply {
                                            action = Intent.ACTION_SEND
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_TEXT, url)
                                        }

                                        context.startActivity(Intent.createChooser(sendIntent, null))
                                    }
                                }
                            )
                             */

                        } else {
                            BasicText(
                                text = stringResource(R.string.info_wait_it_may_take_a_few_minutes),
                                style = typography.xxs.medium,
                                maxLines = 1
                            )
                        }
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

                itemsIndexed(items = playlistPage?.songsPage?.items ?: emptyList()) { index, song ->

                    SwipeablePlaylistItem(
                        mediaItem = song.asMediaItem,
                        onSwipeToRight = {
                            binder?.player?.addNext(song.asMediaItem)
                        }
                    ) {
                        val isLocal by remember { derivedStateOf { song.asMediaItem.isLocal } }
                        downloadState = getDownloadState(song.asMediaItem.mediaId)
                        val isDownloaded = if (!isLocal) downloadedStateMedia(song.asMediaItem.mediaId) else true
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
                                            thumbnailUrl = song.thumbnail?.url,
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
                            thumbnailSizePx = songThumbnailSizePx,
                            thumbnailSizeDp = songThumbnailSizeDp,
                            modifier = Modifier
                                .combinedClickable(
                                    onLongClick = {
                                        menuState.display {
                                            NonQueuedMediaItemMenu(
                                                navController = navController,
                                                onDismiss = menuState::hide,
                                                mediaItem = song.asMediaItem,
                                            )
                                        };
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                    },
                                    onClick = {
                                        searching = false
                                        filter = null
                                        playlistPage?.songsPage?.items?.map(Innertube.SongItem::asMediaItem)
                                            ?.let { mediaItems ->
                                                binder?.stopRadio()
                                                binder?.player?.forcePlayAtIndex(mediaItems, index)
                                            }
                                    }
                                )
                        )
                    }
                }

                item(
                    key = "footer",
                    contentType = 0,
                ) {
                    Spacer(modifier = Modifier.height(Dimensions.bottomSpacer))
                }

                if (playlistPage == null) {
                    item(key = "loading") {
                        ShimmerHost(
                            modifier = Modifier
                                .fillParentMaxSize()
                        ) {
                            repeat(4) {
                                SongItemPlaceholder(thumbnailSizeDp = songThumbnailSizeDp)
                            }
                        }
                    }
                }
            }

            val showFloatingIcon by rememberPreference(showFloatingIconKey, false)
            if(uiType == UiType.ViMusic || showFloatingIcon)
            FloatingActionsContainerWithScrollToTop(
                lazyListState = lazyListState,
                iconId = R.drawable.shuffle,
                onClick = {
                    playlistPage?.songsPage?.items?.let { songs ->
                        if (songs.isNotEmpty()) {
                            binder?.stopRadio()
                            binder?.player?.forcePlayFromBeginning(
                                songs.shuffled().map(Innertube.SongItem::asMediaItem)
                            )
                        }
                    }
                }
            )


        }
    }
}
