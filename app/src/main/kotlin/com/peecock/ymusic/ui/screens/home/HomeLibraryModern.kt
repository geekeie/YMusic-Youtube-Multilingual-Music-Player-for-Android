package com.peecock.ymusic.ui.screens.home

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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.peecock.compose.persist.persistList
import com.peecock.ymusic.Database
import com.peecock.ymusic.LocalPlayerAwareWindowInsets
import com.peecock.ymusic.R
import com.peecock.ymusic.enums.BuiltInPlaylist
import com.peecock.ymusic.enums.LibraryItemSize
import com.peecock.ymusic.enums.MaxTopPlaylistItems
import com.peecock.ymusic.enums.NavigationBarPosition
import com.peecock.ymusic.enums.PlaylistSortBy
import com.peecock.ymusic.enums.PlaylistsType
import com.peecock.ymusic.enums.PopupType
import com.peecock.ymusic.enums.SortOrder
import com.peecock.ymusic.enums.ThumbnailRoundness
import com.peecock.ymusic.enums.UiType
import com.peecock.ymusic.models.Playlist
import com.peecock.ymusic.models.PlaylistPreview
import com.peecock.ymusic.models.Song
import com.peecock.ymusic.models.SongPlaylistMap
import com.peecock.ymusic.query
import com.peecock.ymusic.transaction
import com.peecock.ymusic.ui.components.ButtonsRow
import com.peecock.ymusic.ui.components.LocalMenuState
import com.peecock.ymusic.ui.components.themed.FloatingActionsContainerWithScrollToTop
import com.peecock.ymusic.ui.components.themed.HeaderIconButton
import com.peecock.ymusic.ui.components.themed.HeaderInfo
import com.peecock.ymusic.ui.components.themed.HeaderWithIcon
import com.peecock.ymusic.ui.components.themed.IconButton
import com.peecock.ymusic.ui.components.themed.InputTextDialog
import com.peecock.ymusic.ui.components.themed.Menu
import com.peecock.ymusic.ui.components.themed.MenuEntry
import com.peecock.ymusic.ui.components.themed.MultiFloatingActionsContainer
import com.peecock.ymusic.ui.components.themed.SmartMessage
import com.peecock.ymusic.ui.components.themed.SortMenu
import com.peecock.ymusic.ui.components.themed.TitleSection
import com.peecock.ymusic.ui.items.PlaylistItem
import com.peecock.ymusic.ui.styling.Dimensions
import com.peecock.ymusic.ui.styling.LocalAppearance
import com.peecock.ymusic.ui.styling.favoritesIcon
import com.peecock.ymusic.ui.styling.px
import com.peecock.ymusic.utils.CheckMonthlyPlaylist
import com.peecock.ymusic.utils.ImportPipedPlaylists
import com.peecock.ymusic.utils.MONTHLY_PREFIX
import com.peecock.ymusic.utils.MaxTopPlaylistItemsKey
import com.peecock.ymusic.utils.UiTypeKey
import com.peecock.ymusic.utils.autosyncKey
import com.peecock.ymusic.utils.createPipedPlaylist
import com.peecock.ymusic.utils.enableCreateMonthlyPlaylistsKey
import com.peecock.ymusic.utils.getPipedSession
import com.peecock.ymusic.utils.isPipedEnabledKey
import com.peecock.ymusic.utils.libraryItemSizeKey
import com.peecock.ymusic.utils.navigationBarPositionKey
import com.peecock.ymusic.utils.pipedApiTokenKey
import com.peecock.ymusic.utils.playlistSortByKey
import com.peecock.ymusic.utils.playlistTypeKey
import com.peecock.ymusic.utils.rememberEncryptedPreference
import com.peecock.ymusic.utils.rememberPreference
import com.peecock.ymusic.utils.secondary
import com.peecock.ymusic.utils.semiBold
import com.peecock.ymusic.utils.showFloatingIconKey
import com.peecock.ymusic.utils.showMonthlyPlaylistsKey
import com.peecock.ymusic.utils.showPinnedPlaylistsKey
import com.peecock.ymusic.utils.showPipedPlaylistsKey
import com.peecock.ymusic.utils.showSearchTabKey
import com.peecock.ymusic.utils.thumbnailRoundnessKey


const val PIPED_PREFIX = "piped:"

@ExperimentalMaterialApi
@SuppressLint("SuspiciousIndentation")
@ExperimentalAnimationApi
@ExperimentalFoundationApi
@Composable
fun HomeLibraryModern(
    onBuiltInPlaylist: (BuiltInPlaylist) -> Unit,
    onPlaylistClick: (Playlist) -> Unit,
    onDeviceListSongsClick: () -> Unit,
    onSearchClick: () -> Unit,
    onStatisticsClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val (colorPalette, typography, thumbnailShape) = LocalAppearance.current
    val windowInsets = LocalPlayerAwareWindowInsets.current
    val menuState = LocalMenuState.current
    val uiType by rememberPreference(UiTypeKey, UiType.RiMusic)

    var isCreatingANewPlaylist by rememberSaveable {
        mutableStateOf(false)
    }

    val isPipedEnabled by rememberPreference(isPipedEnabledKey, false)
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val pipedSession = getPipedSession()


    if (isCreatingANewPlaylist) {
        InputTextDialog(
            onDismiss = { isCreatingANewPlaylist = false },
            title = stringResource(R.string.enter_the_playlist_name),
            value = "",
            placeholder = stringResource(R.string.enter_the_playlist_name),
            setValue = { text ->

                if (isPipedEnabled && pipedSession.token.isNotEmpty()) {
                    createPipedPlaylist(
                        context = context,
                        coroutineScope = coroutineScope,
                        pipedSession = pipedSession.toApiSession(),
                        name = text
                    )
                } else {
                    query {
                        Database.insert(Playlist(name = text))
                    }
                }
            }
        )
    }

    ImportPipedPlaylists()

    var sortBy by rememberPreference(playlistSortByKey, PlaylistSortBy.DateAdded)
    var sortOrder by rememberEncryptedPreference(pipedApiTokenKey, SortOrder.Descending)

    var searching by rememberSaveable { mutableStateOf(false) }
    var filter: String? by rememberSaveable { mutableStateOf(null) }

    var items by persistList<PlaylistPreview>("home/playlists")

    LaunchedEffect(sortBy, sortOrder, filter) {
        Database.playlistPreviews(sortBy, sortOrder).collect { items = it }
    }

    var filterCharSequence: CharSequence
    filterCharSequence = filter.toString()
    //Log.d("mediaItemFilter", "<${filter}>  <${filterCharSequence}>")
    if (!filter.isNullOrBlank())
        items = items
            .filter {
                it.playlist.name.contains(filterCharSequence, true) ?: false
            }

    val sortOrderIconRotation by animateFloatAsState(
        targetValue = if (sortOrder == SortOrder.Ascending) 0f else 180f,
        animationSpec = tween(durationMillis = 400, easing = LinearEasing), label = ""
    )

    var itemSize by rememberPreference(libraryItemSizeKey, LibraryItemSize.Small.size)

    val thumbnailSizeDp = itemSize.dp + 24.dp
    val thumbnailSizePx = thumbnailSizeDp.px

    val endPaddingValues = windowInsets.only(WindowInsetsSides.End).asPaddingValues()

    val sectionTextModifier = Modifier
        .padding(horizontal = 16.dp)
        .padding(top = 24.dp, bottom = 8.dp)
        .padding(endPaddingValues)

    var plistId by remember {
        mutableStateOf(0L)
    }

    val importLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri == null) return@rememberLauncherForActivityResult

            //requestPermission(activity, "Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED")

            context.applicationContext.contentResolver.openInputStream(uri)
                ?.use { inputStream ->
                    csvReader().open(inputStream) {
                        readAllWithHeaderAsSequence().forEachIndexed { index, row: Map<String, String> ->
                            println("mediaItem index song ${index}")
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
                                }
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

    val maxTopPlaylistItems by rememberPreference(
        MaxTopPlaylistItemsKey,
        MaxTopPlaylistItems.`10`
    )

    val navigationBarPosition by rememberPreference(
        navigationBarPositionKey,
        NavigationBarPosition.Bottom
    )

    val lazyGridState = rememberLazyGridState()

    val showSearchTab by rememberPreference(showSearchTabKey, false)

    val enableCreateMonthlyPlaylists by rememberPreference(enableCreateMonthlyPlaylistsKey, true)

    //println("mediaItem ${getCalculatedMonths(0)} ${getCalculatedMonths(1)}")
    if (enableCreateMonthlyPlaylists)
        CheckMonthlyPlaylist()

    val showPinnedPlaylists by rememberPreference(showPinnedPlaylistsKey, true)
    val showMonthlyPlaylists by rememberPreference(showMonthlyPlaylistsKey, true)
    val showPipedPlaylists by rememberPreference(showPipedPlaylistsKey, true)
    var playlistType by rememberPreference(playlistTypeKey, PlaylistsType.Playlist)

    var buttonsList = listOf(PlaylistsType.Playlist to stringResource(R.string.playlists))
    if (showPipedPlaylists) buttonsList +=
        PlaylistsType.PipedPlaylist to stringResource(R.string.piped_playlists)
    if (showPinnedPlaylists) buttonsList +=
        PlaylistsType.PinnedPlaylist to stringResource(R.string.pinned_playlists)
    if (showMonthlyPlaylists) buttonsList +=
        PlaylistsType.MonthlyPlaylist to stringResource(R.string.monthly_playlists)

    val thumbnailRoundness by rememberPreference(
        thumbnailRoundnessKey,
        ThumbnailRoundness.Heavy
    )
    var autosync by rememberPreference(autosyncKey, false)

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
        LazyVerticalGrid(
            state = lazyGridState,
            columns = GridCells.Adaptive(itemSize.dp + 24.dp),
            modifier = Modifier
                .fillMaxSize()
                .background(colorPalette.background0)
        ) {
            item(key = "header", contentType = 0, span = { GridItemSpan(maxLineSpan) }) {

                if (uiType == UiType.ViMusic)
                    HeaderWithIcon(
                        title = stringResource(R.string.playlists),
                        iconId = R.drawable.search,
                        enabled = true,
                        showIcon = !showSearchTab,
                        modifier = Modifier,
                        onClick = onSearchClick
                    )
            }

            item(key = "headerNew", contentType = 0, span = { GridItemSpan(maxLineSpan) }) {

                Row(
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .padding(top = 10.dp, bottom = 16.dp)
                        .fillMaxWidth()

                ) {
                    if (uiType == UiType.RiMusic)
                        TitleSection(title = stringResource(R.string.playlists))

                    HeaderInfo(
                        title = "${items.size}",
                        icon = painterResource(R.drawable.playlist)
                    )
                    Spacer(
                        modifier = Modifier
                            .weight(1f)
                    )

                    HeaderIconButton(
                        icon = R.drawable.arrow_up,
                        color = colorPalette.text,
                        onClick = {},
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .graphicsLayer { rotationZ = sortOrderIconRotation }
                            .combinedClickable(
                                onClick = { sortOrder = !sortOrder },
                                onLongClick = {
                                    menuState.display {
                                        SortMenu(
                                            title = stringResource(R.string.sorting_order),
                                            onDismiss = menuState::hide,
                                            onName = { sortBy = PlaylistSortBy.Name },
                                            onSongNumber = {
                                                sortBy = PlaylistSortBy.SongCount
                                            },
                                            onDateAdded = { sortBy = PlaylistSortBy.DateAdded },
                                            onPlayTime = { sortBy = PlaylistSortBy.MostPlayed },
                                        )
                                    }
                                }
                            )
                    )

                    HeaderIconButton(
                        onClick = {},
                        icon = R.drawable.sync,
                        color = if (autosync) colorPalette.text else colorPalette.textDisabled,
                        iconSize = 24.dp,
                        modifier = Modifier
                            .padding(horizontal = 2.dp)
                            .combinedClickable(onClick = {autosync = !autosync},
                                               onLongClick = {
                                                   SmartMessage(context.resources.getString(R.string.autosync), context = context)
                                               }
                            )
                    )

                    HeaderIconButton(
                        onClick = { searching = !searching },
                        icon = R.drawable.search_circle,
                        color = colorPalette.text,
                        iconSize = 24.dp,
                        modifier = Modifier
                            .padding(horizontal = 2.dp)
                    )


                    IconButton(
                        icon = R.drawable.add_in_playlist,
                        color = colorPalette.text,
                        onClick = { isCreatingANewPlaylist = true },
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(20.dp)
                    )
                    IconButton(
                        icon = R.drawable.resource_import,
                        color = colorPalette.text,
                        onClick = {
                            try {
                                importLauncher.launch(
                                    arrayOf(
                                        "text/*"
                                    )
                                )
                            } catch (e: ActivityNotFoundException) {
                                SmartMessage(
                                    context.resources.getString(R.string.info_not_find_app_open_doc),
                                    type = PopupType.Warning, context = context
                                )
                            }
                        },
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(20.dp)
                    )

                    HeaderIconButton(
                        onClick = {
                            menuState.display {
                                Menu {
                                    MenuEntry(
                                        icon = R.drawable.arrow_forward,
                                        text = stringResource(R.string.small),
                                        onClick = {
                                            itemSize = LibraryItemSize.Small.size
                                            menuState.hide()
                                        }
                                    )
                                    MenuEntry(
                                        icon = R.drawable.arrow_forward,
                                        text = stringResource(R.string.medium),
                                        onClick = {
                                            itemSize = LibraryItemSize.Medium.size
                                            menuState.hide()
                                        }
                                    )
                                    MenuEntry(
                                        icon = R.drawable.arrow_forward,
                                        text = stringResource(R.string.big),
                                        onClick = {
                                            itemSize = LibraryItemSize.Big.size
                                            menuState.hide()
                                        }
                                    )
                                }
                            }
                        },
                        icon = R.drawable.resize,
                        color = colorPalette.text
                    )

                }
            }

            if (searching)
                item(
                    key = "headerFilter",
                    contentType = 0,
                    span = { GridItemSpan(maxLineSpan) }
                ) {
                    /*        */
                    Row(
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

            item(
                key = "separator",
                contentType = 0,
                span = { GridItemSpan(maxLineSpan) }) {
                ButtonsRow(
                    chips = buttonsList,
                    currentValue = playlistType,
                    onValueUpdate = { playlistType = it },
                    modifier = Modifier.padding(end = 12.dp)
                )
            }

            if (playlistType == PlaylistsType.Playlist) {
                items(items = items,
                    /*
                    .filter {
                    !it.playlist.name.startsWith(PINNED_PREFIX, 0, true) &&
                            !it.playlist.name.startsWith(MONTHLY_PREFIX, 0, true)
                    }

                     */
                    key = { it.playlist.id }) { playlistPreview ->

                    PlaylistItem(
                        playlist = playlistPreview,
                        thumbnailSizeDp = thumbnailSizeDp,
                        thumbnailSizePx = thumbnailSizePx,
                        alternative = true,
                        modifier = Modifier
                            .clickable(onClick = { onPlaylistClick(playlistPreview.playlist) })
                            .animateItem(fadeInSpec = null, fadeOutSpec = null)
                            .fillMaxSize()
                    )
                }
            }

            if (playlistType == PlaylistsType.PipedPlaylist)
                items(items = items.filter {
                    it.playlist.name.startsWith(PIPED_PREFIX, 0, true)
                }, key = { it.playlist.id }) { playlistPreview ->
                    PlaylistItem(
                        playlist = playlistPreview,
                        thumbnailSizeDp = thumbnailSizeDp,
                        thumbnailSizePx = thumbnailSizePx,
                        alternative = true,
                        modifier = Modifier
                            .clickable(onClick = { onPlaylistClick(playlistPreview.playlist) })
                            .animateItem(fadeInSpec = null, fadeOutSpec = null)
                            .fillMaxSize()
                    )
                }

            if (playlistType == PlaylistsType.PinnedPlaylist)
                 items(items = items.filter {
                     it.playlist.name.startsWith(PINNED_PREFIX, 0, true)
                 }, key = { it.playlist.id }) { playlistPreview ->
                    PlaylistItem(
                        playlist = playlistPreview,
                        thumbnailSizeDp = thumbnailSizeDp,
                        thumbnailSizePx = thumbnailSizePx,
                        alternative = true,
                        modifier = Modifier
                            .clickable(onClick = { onPlaylistClick(playlistPreview.playlist) })
                            .animateItem(fadeInSpec = null, fadeOutSpec = null)
                            .fillMaxSize()
                    )
                }

            if (playlistType == PlaylistsType.MonthlyPlaylist)
                items(items = items.filter {
                    it.playlist.name.startsWith(MONTHLY_PREFIX, 0, true)
                }, key = { it.playlist.id }) { playlistPreview ->
                    PlaylistItem(
                        playlist = playlistPreview,
                        thumbnailSizeDp = thumbnailSizeDp,
                        thumbnailSizePx = thumbnailSizePx,
                        alternative = true,
                        modifier = Modifier
                            .clickable(onClick = { onPlaylistClick(playlistPreview.playlist) })
                            .animateItem(fadeInSpec = null, fadeOutSpec = null)
                            .fillMaxSize()
                    )
                }


                item(
                    key = "footer",
                    contentType = 0,
                    span = { GridItemSpan(maxLineSpan) }
                ) {
                    Spacer(modifier = Modifier.height(Dimensions.bottomSpacer))
                }

        }

        FloatingActionsContainerWithScrollToTop(lazyGridState = lazyGridState)

        val showFloatingIcon by rememberPreference(showFloatingIconKey, false)
        if (uiType == UiType.ViMusic || showFloatingIcon)
            MultiFloatingActionsContainer(
                iconId = R.drawable.search,
                onClick = onSearchClick,
                onClickSettings = onSettingsClick,
                onClickSearch = onSearchClick
            )

        /*
    FloatingActionsContainerWithScrollToTop(
            lazyGridState = lazyGridState,
            iconId = R.drawable.search,
            onClick = onSearchClick
        )
         */


    }
}
