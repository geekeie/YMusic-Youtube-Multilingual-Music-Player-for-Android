package com.peecock.ymusic.ui.screens.home

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
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
import com.peecock.ymusic.enums.PopupType
import com.peecock.ymusic.enums.SortOrder
import com.peecock.ymusic.enums.UiType
import com.peecock.ymusic.models.Playlist
import com.peecock.ymusic.models.PlaylistPreview
import com.peecock.ymusic.models.Song
import com.peecock.ymusic.models.SongPlaylistMap
import com.peecock.ymusic.query
import com.peecock.ymusic.transaction
import com.peecock.ymusic.ui.components.LocalMenuState
import com.peecock.ymusic.ui.components.themed.FloatingActionsContainerWithScrollToTop
import com.peecock.ymusic.ui.components.themed.HeaderIconButton
import com.peecock.ymusic.ui.components.themed.HeaderWithIcon
import com.peecock.ymusic.ui.components.themed.IconButton
import com.peecock.ymusic.ui.components.themed.InputTextDialog
import com.peecock.ymusic.ui.components.themed.Menu
import com.peecock.ymusic.ui.components.themed.MenuEntry
import com.peecock.ymusic.ui.components.themed.MultiFloatingActionsContainer
import com.peecock.ymusic.ui.components.themed.SmartMessage
import com.peecock.ymusic.ui.components.themed.SortMenu
import com.peecock.ymusic.ui.components.themed.Title
import com.peecock.ymusic.ui.items.PlaylistItem
import com.peecock.ymusic.ui.styling.Dimensions
import com.peecock.ymusic.ui.styling.LocalAppearance
import com.peecock.ymusic.ui.styling.favoritesIcon
import com.peecock.ymusic.ui.styling.px
import com.peecock.ymusic.utils.CheckMonthlyPlaylist
import com.peecock.ymusic.utils.MONTHLY_PREFIX
import com.peecock.ymusic.utils.MaxTopPlaylistItemsKey
import com.peecock.ymusic.utils.UiTypeKey
import com.peecock.ymusic.utils.enableCreateMonthlyPlaylistsKey
import com.peecock.ymusic.utils.libraryItemSizeKey
import com.peecock.ymusic.utils.navigationBarPositionKey
import com.peecock.ymusic.utils.playlistSortByKey
import com.peecock.ymusic.utils.playlistSortOrderKey
import com.peecock.ymusic.utils.rememberPreference
import com.peecock.ymusic.utils.semiBold
import com.peecock.ymusic.utils.showBuiltinPlaylistsKey
import com.peecock.ymusic.utils.showCachedPlaylistKey
import com.peecock.ymusic.utils.showDownloadedPlaylistKey
import com.peecock.ymusic.utils.showFavoritesPlaylistKey
import com.peecock.ymusic.utils.showFloatingIconKey
import com.peecock.ymusic.utils.showMonthlyPlaylistInLibraryKey
import com.peecock.ymusic.utils.showMonthlyPlaylistsKey
import com.peecock.ymusic.utils.showMyTopPlaylistKey
import com.peecock.ymusic.utils.showOnDevicePlaylistKey
import com.peecock.ymusic.utils.showPinnedPlaylistsKey
import com.peecock.ymusic.utils.showPlaylistsGeneralKey
import com.peecock.ymusic.utils.showPlaylistsKey
import com.peecock.ymusic.utils.showPlaylistsListKey
import com.peecock.ymusic.utils.showSearchTabKey


@ExperimentalMaterialApi
@SuppressLint("SuspiciousIndentation")
@ExperimentalAnimationApi
@ExperimentalFoundationApi
@Composable
fun HomeLibrary(
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
    val uiType  by rememberPreference(UiTypeKey, UiType.RiMusic)

    var isCreatingANewPlaylist by rememberSaveable {
        mutableStateOf(false)
    }

/*
    var exoPlayerDiskCacheMaxSize by rememberPreference(
        exoPlayerDiskCacheMaxSizeKey,
        ExoPlayerDiskCacheMaxSize.`32MB`
    )

    var exoPlayerDiskDownloadCacheMaxSize by rememberPreference(
        exoPlayerDiskDownloadCacheMaxSizeKey,
        ExoPlayerDiskDownloadCacheMaxSize.`2GB`
    )

 */

    if (isCreatingANewPlaylist) {
        InputTextDialog(
            onDismiss = { isCreatingANewPlaylist = false },
            title = stringResource(R.string.enter_the_playlist_name),
            value = "",
            placeholder = stringResource(R.string.enter_the_playlist_name),
            setValue = { text ->
                query {
                    Database.insert(Playlist(name = text))
                }
            }
        )
    }

    var sortBy by rememberPreference(playlistSortByKey, PlaylistSortBy.DateAdded)
    var sortOrder by rememberPreference(playlistSortOrderKey, SortOrder.Descending)

    var items by persistList<PlaylistPreview>("home/playlists")

    LaunchedEffect(sortBy, sortOrder) {
        Database.playlistPreviews(sortBy, sortOrder).collect { items = it }
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
    val context = LocalContext.current
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

    val navigationBarPosition by rememberPreference(navigationBarPositionKey, NavigationBarPosition.Bottom)

    val lazyGridState = rememberLazyGridState()

    val showSearchTab by rememberPreference(showSearchTabKey, false)

    val showFavoritesPlaylist by rememberPreference(showFavoritesPlaylistKey, true)
    val showCachedPlaylist by rememberPreference(showCachedPlaylistKey, true)
    val showMyTopPlaylist by rememberPreference(showMyTopPlaylistKey, true)
    val showDownloadedPlaylist by rememberPreference(showDownloadedPlaylistKey, true)
    val showOnDevicePlaylist by rememberPreference(showOnDevicePlaylistKey, true)
    val showPlaylists by rememberPreference(showPlaylistsKey, true)

    var showBuiltinPlaylists by rememberPreference(showBuiltinPlaylistsKey, true)
    var showPinnedPlaylists by rememberPreference(showPinnedPlaylistsKey, true)
    var showPlaylistsList by rememberPreference(showPlaylistsListKey, true)
    var showPlaylistsGeneral by rememberPreference(showPlaylistsGeneralKey, true)
    var showMonthlyPlaylists by rememberPreference(showMonthlyPlaylistsKey, true)
    val enableCreateMonthlyPlaylists by rememberPreference(enableCreateMonthlyPlaylistsKey, true)
    val showMonthlyPlaylistInLibrary by rememberPreference(showMonthlyPlaylistInLibraryKey, true)

    //println("mediaItem ${getCalculatedMonths(0)} ${getCalculatedMonths(1)}")
    if (enableCreateMonthlyPlaylists)
        CheckMonthlyPlaylist()

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
            ///columns = GridCells.Adaptive(Dimensions.thumbnails.song * 2 + Dimensions.itemsVerticalPadding * 2),
            //contentPadding = LocalPlayerAwareWindowInsets.current
            //    .only(WindowInsetsSides.Vertical + WindowInsetsSides.End).asPaddingValues(),
            /*
            verticalArrangement = Arrangement.spacedBy(Dimensions.itemsVerticalPadding * 2),
            horizontalArrangement = Arrangement.spacedBy(
                space = Dimensions.itemsVerticalPadding * 2,
                alignment = Alignment.CenterHorizontally
            ),
             */
            modifier = Modifier
                .fillMaxSize()
                .background(colorPalette.background0)
        ) {
            item(key = "header", contentType = 0, span = { GridItemSpan(maxLineSpan) }) {

                HeaderWithIcon(
                    title = stringResource(R.string.library),
                    iconId = R.drawable.search,
                    enabled = true,
                    showIcon = !showSearchTab,
                    modifier = Modifier,
                    onClick = onSearchClick
                )
            }

            item(key = "itemSize", contentType = 0, span = { GridItemSpan(maxLineSpan) }) {

                Row(
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .padding(top = 10.dp, bottom = 16.dp)
                        .fillMaxWidth()

                ) {

                    /*
                    Spacer(
                        modifier = Modifier
                            .weight(1f)
                    )
                     */

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
                    /*
                    BasicText(
                        text = "${stringResource(R.string.item_size)} ${when (itemSize) {
                            LibraryItemSize.Small.size -> stringResource(R.string.small)
                            LibraryItemSize.Medium.size -> stringResource(R.string.medium)
                            LibraryItemSize.Big.size -> stringResource(R.string.big)
                            else -> stringResource(R.string.small)
                        }}",
                        style = typography.xs.semiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .clickable {
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
                            }
                    )*/
                }
            }


            item(key = "titleBuiltinPlaylists", contentType = 0, span = { GridItemSpan(maxLineSpan) }) {
                Title(
                    title = stringResource(R.string.library_collections),
                    onClick = { showBuiltinPlaylists = !showBuiltinPlaylists },
                    icon = if (showBuiltinPlaylists) R.drawable.arrow_down else R.drawable.arrow_forward,
                    modifier = Modifier
                        .background(colorPalette.background2)
                )
            }

            if(showBuiltinPlaylists) {
                if (showFavoritesPlaylist)
                    item(key = "favorites") {
                        PlaylistItem(
                            icon = R.drawable.heart,
                            iconSize = 48.dp,
                            colorTint = colorPalette.favoritesIcon,
                            name = stringResource(R.string.favorites),
                            songCount = null,
                            thumbnailSizeDp = thumbnailSizeDp,
                            alternative = true,
                            modifier = Modifier
                                .clip(thumbnailShape)
                                .clickable(onClick = { onBuiltInPlaylist(BuiltInPlaylist.Favorites) })
                                .animateItemPlacement()

                        )
                    }

                if (showCachedPlaylist)
                    item(key = "offline") {
                        PlaylistItem(
                            icon = R.drawable.sync,
                            iconSize = 48.dp,
                            colorTint = colorPalette.favoritesIcon,
                            name = stringResource(R.string.cached),
                            songCount = null,
                            thumbnailSizeDp = thumbnailSizeDp,
                            alternative = true,
                            modifier = Modifier
                                .clip(thumbnailShape)
                                .clickable(onClick = { onBuiltInPlaylist(BuiltInPlaylist.Offline) })
                                .animateItemPlacement()
                        )
                    }

                if (showDownloadedPlaylist)
                    item(key = "downloaded") {
                        PlaylistItem(
                            icon = R.drawable.downloaded,
                            iconSize = 48.dp,
                            colorTint = colorPalette.favoritesIcon,
                            name = stringResource(R.string.downloaded),
                            songCount = null,
                            thumbnailSizeDp = thumbnailSizeDp,
                            alternative = true,
                            modifier = Modifier
                                .clip(thumbnailShape)
                                .clickable(onClick = { onBuiltInPlaylist(BuiltInPlaylist.Downloaded) })
                                .animateItemPlacement()
                        )
                    }

                if (showMyTopPlaylist)
                    item(key = "top") {
                        PlaylistItem(
                            icon = R.drawable.trending,
                            iconSize = 48.dp,
                            colorTint = colorPalette.favoritesIcon,
                            name = String.format(stringResource(R.string.my_playlist_top),maxTopPlaylistItems.number),
                            songCount = null,
                            thumbnailSizeDp = thumbnailSizeDp,
                            alternative = true,
                            modifier = Modifier
                                .clip(thumbnailShape)
                                .clickable(onClick = { onBuiltInPlaylist(BuiltInPlaylist.Top) })
                                .animateItemPlacement()
                        )
                    }

                if (showOnDevicePlaylist)
                    item(key = "ondevice") {
                        PlaylistItem(
                            icon = R.drawable.musical_notes,
                            iconSize = 48.dp,
                            colorTint = colorPalette.favoritesIcon,
                            name = stringResource(R.string.on_device),
                            songCount = null,
                            thumbnailSizeDp = thumbnailSizeDp,
                            alternative = true,
                            modifier = Modifier
                                .clip(thumbnailShape)
                                .clickable(onClick = { onDeviceListSongsClick() })
                                .animateItemPlacement()
                        )
                    }
            }
            /*    */


            if (showPlaylists) {

                if (showMonthlyPlaylistInLibrary) {
                    item(
                        key = "headerMonthlyPlaylists",
                        contentType = 0,
                        span = { GridItemSpan(maxLineSpan) }) {
                        Title(
                            title = stringResource(R.string.monthly_playlists),
                            onClick = { showMonthlyPlaylists = !showMonthlyPlaylists },
                            icon = if (showMonthlyPlaylists) R.drawable.arrow_down else R.drawable.arrow_forward,
                            modifier = Modifier
                                .background(colorPalette.background2)
                        )
                    }

                    if (showMonthlyPlaylists) {
                        items(items = items.filter {
                            it.playlist.name.startsWith(MONTHLY_PREFIX, 0, true)
                        }, key = { "M${it.playlist.id}" }) { playlistPreview ->
                            PlaylistItem(
                                playlist = playlistPreview,
                                thumbnailSizeDp = thumbnailSizeDp,
                                thumbnailSizePx = thumbnailSizePx,
                                alternative = true,
                                modifier = Modifier
                                    .clickable(onClick = { onPlaylistClick(playlistPreview.playlist) })
                                    .animateItemPlacement()
                                    .fillMaxSize()
                            )
                        }
                    }
                }

                item(key = "titlePlaylistsGeneral", contentType = 0, span = { GridItemSpan(maxLineSpan) }) {
                    Title(
                        title = stringResource(R.string.playlists),
                        onClick = { showPlaylistsGeneral = !showPlaylistsGeneral },
                        icon = if (showPlaylistsGeneral) R.drawable.arrow_down else R.drawable.arrow_forward,
                        modifier = Modifier.background(colorPalette.background2)
                    )
                }

            if (showPlaylistsGeneral) {
                item(key = "filter", contentType = 0, span = { GridItemSpan(maxLineSpan) }) {

                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(horizontal = 12.dp)
                            .padding(top = 10.dp, bottom = 16.dp)
                            .fillMaxWidth()

                    ) {

                        /*
                        Spacer(
                            modifier = Modifier
                                .weight(1f)
                        )
                         */


                        BasicText(
                            text = when (sortBy) {
                                PlaylistSortBy.Name -> stringResource(R.string.sort_name)
                                PlaylistSortBy.SongCount -> stringResource(R.string.sort_songs_number)
                                PlaylistSortBy.DateAdded -> stringResource(R.string.sort_date_added)
                                PlaylistSortBy.MostPlayed -> stringResource(R.string.most_played_playlists)
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
                                            onName = { sortBy = PlaylistSortBy.Name },
                                            onSongNumber = { sortBy = PlaylistSortBy.SongCount },
                                            onDateAdded = { sortBy = PlaylistSortBy.DateAdded },
                                            onPlayTime = { sortBy = PlaylistSortBy.MostPlayed },
                                        )
                                    }
                                }
                                .padding(end = 5.dp)
                        )


                        HeaderIconButton(
                            icon = R.drawable.arrow_up,
                            color = colorPalette.text,
                            onClick = { sortOrder = !sortOrder },
                            modifier = Modifier
                                .graphicsLayer { rotationZ = sortOrderIconRotation }
                        )
                    }
                }


                if (items.filter {
                        it.playlist.name.startsWith(PINNED_PREFIX, 0, true)
                    }.isNotEmpty()) {
                    item(
                        key = "headerPinnedPlaylist",
                        contentType = 0,
                        span = { GridItemSpan(maxLineSpan) }) {
                        Title(
                            title = "${stringResource(R.string.pinned_playlists)} (${
                                items.filter {
                                    it.playlist.name.startsWith(PINNED_PREFIX, 0, true)
                                }.size
                            })", onClick = { showPinnedPlaylists = !showPinnedPlaylists },
                            icon = if (showPinnedPlaylists) R.drawable.arrow_down else R.drawable.arrow_forward,
                            modifier = Modifier.background(colorPalette.background1)
                        )
                    }
                    /*
                    item(
                        key = "headerPinnedPlaylist",
                        contentType = 0,
                        span = { GridItemSpan(maxLineSpan) }) {
                        BasicText(
                            text = "${stringResource(R.string.pinned_playlists)} (${
                                items.filter {
                                    it.playlist.name.startsWith(PINNED_PREFIX, 0, true)
                                }.size
                            })",
                            style = typography.m.semiBold,
                            modifier = sectionTextModifier
                        )
                    }
                     */

                    if (showPinnedPlaylists) {
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
                                    .animateItemPlacement()
                                    .fillMaxSize()
                            )
                        }
                    }
                }

                item(
                    key = "headerplaylistList",
                    contentType = 0,
                    span = { GridItemSpan(maxLineSpan) }) {
                    Title(
                        title = "${stringResource(R.string.playlists)} (${
                            items.filter {
                                !it.playlist.name.startsWith(PINNED_PREFIX, 0, true)
                            }.size
                        })", onClick = { showPlaylistsList = !showPlaylistsList },
                        icon = if (showPlaylistsList) R.drawable.arrow_down else R.drawable.arrow_forward,
                        modifier = Modifier.background(colorPalette.background1)
                    )
                }
                if (showPlaylistsList) {
                    item(
                        key = "headerplaylist",
                        contentType = 0,
                        span = { GridItemSpan(maxLineSpan) }) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                        ) {

                            /*
                        BasicText(
                            text = "${stringResource(R.string.playlists)} (${
                                items.filter {
                                    !it.playlist.name.startsWith(PINNED_PREFIX, 0, true)
                                }.size
                            })",
                            style = typography.m.semiBold,
                            //modifier = sectionTextModifier
                            modifier = Modifier
                                .padding(15.dp)
                        )
                         */

                            Spacer(
                                modifier = Modifier
                                    .weight(1f)
                            )

                            IconButton(
                                icon = R.drawable.add_in_playlist,
                                color = colorPalette.text,
                                onClick = { isCreatingANewPlaylist = true },
                                modifier = Modifier
                                    .padding(10.dp)
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
                                    .padding(10.dp)
                                    .size(20.dp)
                            )

                        }

                    }

                    items(items = items.filter {
                        !it.playlist.name.startsWith(PINNED_PREFIX, 0, true) &&
                        !it.playlist.name.startsWith(MONTHLY_PREFIX, 0, true)
                    }, key = { it.playlist.id }) { playlistPreview ->
                        PlaylistItem(
                            playlist = playlistPreview,
                            thumbnailSizeDp = thumbnailSizeDp,
                            thumbnailSizePx = thumbnailSizePx,
                            alternative = true,
                            modifier = Modifier
                                .clickable(onClick = { onPlaylistClick(playlistPreview.playlist) })
                                .animateItemPlacement()
                                .fillMaxSize()
                        )
                    }
                }
            }
                item(
                    key = "footer",
                    contentType = 0,
                    span = { GridItemSpan(maxLineSpan) }
                ) {
                    Spacer(modifier = Modifier.height(Dimensions.bottomSpacer))
                }
            }
        }

        FloatingActionsContainerWithScrollToTop(lazyGridState = lazyGridState)

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
                lazyGridState = lazyGridState,
                iconId = R.drawable.search,
                onClick = onSearchClick
            )
             */





    }
}
