package com.peecock.ymusic.ui.screens.home

import android.annotation.SuppressLint
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download
import androidx.navigation.NavController
import com.peecock.compose.persist.persistList
import com.peecock.ymusic.Database
import com.peecock.ymusic.LocalPlayerServiceBinder
import com.peecock.ymusic.R
import com.peecock.ymusic.enums.MaxSongs
import com.peecock.ymusic.enums.NavRoutes
import com.peecock.ymusic.enums.NavigationBarPosition
import com.peecock.ymusic.enums.PopupType
import com.peecock.ymusic.enums.SongSortBy
import com.peecock.ymusic.enums.SortOrder
import com.peecock.ymusic.enums.ThumbnailRoundness
import com.peecock.ymusic.enums.UiType
import com.peecock.ymusic.models.Song
import com.peecock.ymusic.models.SongPlaylistMap
import com.peecock.ymusic.query
import com.peecock.ymusic.service.LOCAL_KEY_PREFIX
import com.peecock.ymusic.service.isLocal
import com.peecock.ymusic.ui.components.LocalMenuState
import com.peecock.ymusic.ui.components.themed.ConfirmationDialog
import com.peecock.ymusic.ui.components.themed.FloatingActionsContainerWithScrollToTop
import com.peecock.ymusic.ui.components.themed.HeaderIconButton
import com.peecock.ymusic.ui.components.themed.HeaderInfo
import com.peecock.ymusic.ui.components.themed.HeaderWithIcon
import com.peecock.ymusic.ui.components.themed.IconButton
import com.peecock.ymusic.ui.components.themed.InHistoryMediaItemMenu
import com.peecock.ymusic.ui.components.themed.MultiFloatingActionsContainer
import com.peecock.ymusic.ui.components.themed.PlaylistsItemMenu
import com.peecock.ymusic.ui.components.themed.SmartMessage
import com.peecock.ymusic.ui.components.themed.SortMenu
import com.peecock.ymusic.ui.items.SongItem
import com.peecock.ymusic.ui.styling.Dimensions
import com.peecock.ymusic.ui.styling.LocalAppearance
import com.peecock.ymusic.ui.styling.favoritesIcon
import com.peecock.ymusic.ui.styling.onOverlay
import com.peecock.ymusic.ui.styling.overlay
import com.peecock.ymusic.ui.styling.px
import com.peecock.ymusic.utils.UiTypeKey
import com.peecock.ymusic.utils.asMediaItem
import com.peecock.ymusic.utils.center
import com.peecock.ymusic.utils.color
import com.peecock.ymusic.utils.downloadedStateMedia
import com.peecock.ymusic.utils.forcePlayAtIndex
import com.peecock.ymusic.utils.forcePlayFromBeginning
import com.peecock.ymusic.utils.getDownloadState
import com.peecock.ymusic.utils.includeLocalSongsKey
import com.peecock.ymusic.utils.manageDownload
import com.peecock.ymusic.utils.maxSongsInQueueKey
import com.peecock.ymusic.utils.navigationBarPositionKey
import com.peecock.ymusic.utils.rememberPreference
import com.peecock.ymusic.utils.secondary
import com.peecock.ymusic.utils.semiBold
import com.peecock.ymusic.utils.showFloatingIconKey
import com.peecock.ymusic.utils.showSearchTabKey
import com.peecock.ymusic.utils.songSortByKey
import com.peecock.ymusic.utils.songSortOrderKey
import com.peecock.ymusic.utils.thumbnailRoundnessKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber


@ExperimentalTextApi
@SuppressLint("SuspiciousIndentation")
@ExperimentalFoundationApi
@ExperimentalAnimationApi
@ExperimentalComposeUiApi
@UnstableApi
@Composable
fun HomeSongs(
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

    var items by persistList<Song>("home/songs")

    /*
    var filterDownloaded by remember {
        mutableStateOf(false)
    }
     */

    var filter: String? by rememberSaveable { mutableStateOf(null) }

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

    LaunchedEffect(sortBy, sortOrder, filter, showHiddenSongs, includeLocalSongs) {
            Database.songs(sortBy, sortOrder, showHiddenSongs).collect { items = it.map { it.song }}
    }

    if (!includeLocalSongs)
        items = items
            .filter {
                !it.id.startsWith(LOCAL_KEY_PREFIX)
            }

    var filterCharSequence: CharSequence
    filterCharSequence = filter.toString()
    //Log.d("mediaItemFilter", "<${filter}>  <${filterCharSequence}>")
    if (!filter.isNullOrBlank())
        items = items
            .filter {
                it.title.contains(filterCharSequence,true) ?: false
                        || it.artistsText?.contains(filterCharSequence,true) ?: false
            }

    var searching by rememberSaveable { mutableStateOf(false) }

    val sortOrderIconRotation by animateFloatAsState(
        targetValue = if (sortOrder == SortOrder.Ascending) 0f else 180f,
        animationSpec = tween(durationMillis = 400, easing = LinearEasing), label = ""
    )


    val navigationBarPosition by rememberPreference(navigationBarPositionKey, NavigationBarPosition.Bottom)

    val lazyListState = rememberLazyListState()

    val showSearchTab by rememberPreference(showSearchTabKey, false)
    val maxSongsInQueue  by rememberPreference(maxSongsInQueueKey, MaxSongs.`500`)

    var position by remember {
        mutableIntStateOf(0)
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
            //contentPadding = LocalPlayerAwareWindowInsets.current
            //    .only(WindowInsetsSides.Vertical + WindowInsetsSides.End).asPaddingValues(),
        ) {
            item(
                key = "header",
                contentType = 0
            ) {

                HeaderWithIcon(
                    title = stringResource(R.string.songs),
                    iconId = R.drawable.search,
                    enabled = true,
                    showIcon = !showSearchTab,
                    modifier = Modifier,
                    onClick = onSearchClick
                )

                Row (
                    horizontalArrangement = Arrangement.SpaceBetween, //.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .fillMaxSize()
                ) {

                    HeaderInfo(
                        title = "${items.size}",
                        icon = painterResource(R.drawable.musical_notes),
                        spacer = 20
                    )

                    HeaderIconButton(
                        onClick = { searching = !searching },
                        icon = R.drawable.search_circle,
                        color = colorPalette.text,
                        iconSize = 24.dp,
                        modifier = Modifier
                            .padding(horizontal = 5.dp)
                    )
                    HeaderIconButton(
                        onClick = {},
                        icon = if (showHiddenSongs == 0) R.drawable.eye_off else R.drawable.eye,
                        color = colorPalette.text,
                        //iconSize = 22.dp,
                        modifier = Modifier
                            .padding(horizontal = 5.dp)
                            .combinedClickable(
                                onClick = { showHiddenSongs = if (showHiddenSongs == 0) -1 else 0 },
                                onLongClick = {
                                    SmartMessage(context.resources.getString(R.string.info_show_hide_hidden_songs), context = context)
                                }
                            )
                    )

                    HeaderIconButton(
                        icon = R.drawable.shuffle,
                        enabled = items.isNotEmpty(),
                        color = if (items.isNotEmpty()) colorPalette.text else colorPalette.textDisabled,
                        onClick = {},
                        modifier = Modifier
                            .padding(horizontal = 5.dp)
                            .combinedClickable(
                                onClick = {
                                    if (items.isNotEmpty()) {
                                        val itemsLimited =
                                            if (items.size > maxSongsInQueue.number) items
                                                .shuffled()
                                                .take(maxSongsInQueue.number.toInt()) else items
                                        binder?.stopRadio()
                                        binder?.player?.forcePlayFromBeginning(
                                            itemsLimited
                                                .shuffled()
                                                .map(Song::asMediaItem)
                                        )
                                    }
                                },
                                onLongClick = {
                                    SmartMessage(context.resources.getString(R.string.info_shuffle), context = context)
                                }
                            )
                    )

                    HeaderIconButton(
                        onClick = {  },
                        icon = R.drawable.devices,
                        color = if (includeLocalSongs) colorPalette.text else colorPalette.textDisabled,
                        //iconSize = 22.dp,
                        modifier = Modifier
                            .padding(horizontal = 5.dp)
                            .combinedClickable(
                                onClick = {
                                    includeLocalSongs = !includeLocalSongs
                                },
                                onLongClick = {
                                    SmartMessage(context.resources.getString(R.string.info_includes_excludes_songs_on_the_device), context = context)
                                }
                            )
                    )

                    HeaderIconButton(
                        icon = R.drawable.ellipsis_horizontal,
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
                                            onAddToPlaylist = { playlistPreview ->
                                                position =
                                                    playlistPreview.songCount.minus(1) ?: 0
                                                if (position > 0) position++ else position = 0

                                                items.forEachIndexed { index, song ->
                                                    runCatching {
                                                        Database.insert(song.asMediaItem)
                                                        Database.insert(
                                                            SongPlaylistMap(
                                                                songId = song.asMediaItem.mediaId,
                                                                playlistId = playlistPreview.playlist.id,
                                                                position = position + index
                                                            )
                                                        )
                                                    }.onFailure {
                                                        Timber.e(it.message)
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

                    Spacer(
                        modifier = Modifier
                            .weight(0.3f)
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
                            .clickable {
                                menuState.display{
                                    SortMenu(
                                        title = stringResource(R.string.sorting_order),
                                        onDismiss = menuState::hide,
                                        onDatePlayed = { sortBy = SongSortBy.DatePlayed },
                                        onTitle = { sortBy = SongSortBy.Title },
                                        onDateAdded = { sortBy = SongSortBy.DateAdded },
                                        onPlayTime = { sortBy = SongSortBy.PlayTime },
                                        onDateLiked = { sortBy = SongSortBy.DateLiked },
                                        onArtist = { sortBy = SongSortBy.Artist },
                                        onDuration = { sortBy = SongSortBy.Duration }
                                    )
                                }

                            }
                    )


                        HeaderIconButton(
                            icon = R.drawable.arrow_up,
                            color = colorPalette.text,
                            onClick = { sortOrder = !sortOrder },
                            modifier = Modifier
                                .graphicsLayer { rotationZ = sortOrderIconRotation }
                        )

                }

                /*        */
                Row (
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier
                        //.requiredHeight(30.dp)
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
                    /*
                    else {
                        HeaderIconButton(
                            onClick = { searching = true },
                            icon = R.drawable.search_circle,
                            color = colorPalette.text,
                            iconSize = 24.dp
                        )
                    }

                     */
                }
                /*        */
            }



            itemsIndexed(
                items = items,
                key = { _, song -> song.id }
            ) { index, song ->

                var isHiding by remember {
                    mutableStateOf(false)
                }

                if (isHiding) {
                    ConfirmationDialog(
                        text = stringResource(R.string.hidesong),
                        onDismiss = { isHiding = false },
                        onConfirm = {
                            query {
                                menuState.hide()
                                binder?.cache?.removeResource(song.id)
                                binder?.downloadCache?.removeResource(song.id)
                                Database.incrementTotalPlayTimeMs(song.id, -song.totalPlayTimeMs)
                            }
                        }
                    )
                }

                val isLocal by remember { derivedStateOf { song.asMediaItem.isLocal } }
                downloadState = getDownloadState(song.asMediaItem.mediaId)
                val isDownloaded = if (!isLocal) downloadedStateMedia(song.asMediaItem.mediaId) else true
                SongItem(
                    song = song,
                    isDownloaded =  isDownloaded,
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
                            songId = song.id,
                            songTitle = song.title,
                            downloadState = isDownloaded
                        )
                    },
                    downloadState = downloadState,
                    thumbnailSizePx = thumbnailSizePx,
                    thumbnailSizeDp = thumbnailSizeDp,
                    onThumbnailContent = if (sortBy == SongSortBy.PlayTime) ({
                        BasicText(
                            text = song.formattedTotalPlayTime,
                            style = typography.xxs.semiBold.center.color(colorPalette.onOverlay),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, colorPalette.overlay)
                                    ),
                                    shape = thumbnailShape
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .align(Alignment.BottomCenter)
                        )
                    }) else null,
                    modifier = Modifier
                        .combinedClickable(
                            onLongClick = {
                                menuState.display {
                                    InHistoryMediaItemMenu(
                                        navController = navController,
                                        song = song,
                                        onDismiss = menuState::hide,
                                        onHideFromDatabase = { isHiding = true }
                                    )
                                }
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
                                    itemsLimited.map(Song::asMediaItem),
                                    index
                                )
                            }
                        )
                        .animateItemPlacement()
                )
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
