package com.peecock.ymusic.ui.screens.home

import android.annotation.SuppressLint
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import com.peecock.compose.persist.persist
import com.peecock.ymusic.Database
import com.peecock.ymusic.LocalPlayerServiceBinder
import com.peecock.ymusic.R
import com.peecock.ymusic.enums.AlbumSortBy
import com.peecock.ymusic.enums.LibraryItemSize
import com.peecock.ymusic.enums.NavigationBarPosition
import com.peecock.ymusic.enums.SortOrder
import com.peecock.ymusic.enums.UiType
import com.peecock.ymusic.models.Album
import com.peecock.ymusic.models.Song
import com.peecock.ymusic.models.SongPlaylistMap
import com.peecock.ymusic.query
import com.peecock.ymusic.transaction
import com.peecock.ymusic.ui.components.LocalMenuState
import com.peecock.ymusic.ui.components.themed.AlbumsItemMenu
import com.peecock.ymusic.ui.components.themed.FloatingActionsContainerWithScrollToTop
import com.peecock.ymusic.ui.components.themed.HeaderIconButton
import com.peecock.ymusic.ui.components.themed.HeaderInfo
import com.peecock.ymusic.ui.components.themed.HeaderWithIcon
import com.peecock.ymusic.ui.components.themed.InputTextDialog
import com.peecock.ymusic.ui.components.themed.Menu
import com.peecock.ymusic.ui.components.themed.MenuEntry
import com.peecock.ymusic.ui.components.themed.MultiFloatingActionsContainer
import com.peecock.ymusic.ui.items.AlbumItem
import com.peecock.ymusic.ui.styling.Dimensions
import com.peecock.ymusic.ui.styling.LocalAppearance
import com.peecock.ymusic.ui.styling.px
import com.peecock.ymusic.utils.UiTypeKey
import com.peecock.ymusic.utils.addNext
import com.peecock.ymusic.utils.albumSortByKey
import com.peecock.ymusic.utils.albumSortOrderKey
import com.peecock.ymusic.utils.albumsItemSizeKey
import com.peecock.ymusic.utils.asMediaItem
import com.peecock.ymusic.utils.enqueue
import com.peecock.ymusic.utils.navigationBarPositionKey
import com.peecock.ymusic.utils.rememberPreference
import com.peecock.ymusic.utils.showFloatingIconKey
import com.peecock.ymusic.utils.showSearchTabKey
import kotlin.random.Random

@ExperimentalTextApi
@UnstableApi
@SuppressLint("SuspiciousIndentation")
@ExperimentalFoundationApi
@ExperimentalAnimationApi
@Composable
fun HomeAlbums(
    onAlbumClick: (Album) -> Unit,
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val (colorPalette, typography, thumbnailShape) = LocalAppearance.current
    val menuState = LocalMenuState.current
    val binder = LocalPlayerServiceBinder.current
    val uiType  by rememberPreference(UiTypeKey, UiType.RiMusic)

    var sortBy by rememberPreference(albumSortByKey, AlbumSortBy.DateAdded)
    var sortOrder by rememberPreference(albumSortOrderKey, SortOrder.Descending)

    var items by persist<List<Album>>(tag = "home/albums", emptyList())

    LaunchedEffect(sortBy, sortOrder) {
        Database.albums(sortBy, sortOrder).collect { items = it }
    }

    var itemSize by rememberPreference(albumsItemSizeKey, LibraryItemSize.Small.size)
    val thumbnailSizeDp = itemSize.dp + 24.dp
    val thumbnailSizePx = thumbnailSizeDp.px

    val sortOrderIconRotation by animateFloatAsState(
        targetValue = if (sortOrder == SortOrder.Ascending) 0f else 180f,
        animationSpec = tween(durationMillis = 400, easing = LinearEasing), label = ""
    )

    val navigationBarPosition by rememberPreference(navigationBarPositionKey, NavigationBarPosition.Bottom)
/*
    var showSortTypeSelectDialog by remember {
        mutableStateOf(false)
    }

 */

    val lazyListState = rememberLazyListState()

    val showSearchTab by rememberPreference(showSearchTabKey, false)
    //val effectRotationEnabled by rememberPreference(effectRotationKey, true)
    var isRotated by rememberSaveable { mutableStateOf(false) }
    val rotationAngle by animateFloatAsState(
        targetValue = if (isRotated) 360F else 0f,
        animationSpec = tween(durationMillis = 300), label = ""
    )

    val lazyGridState = rememberLazyGridState()




    Box (
        modifier = Modifier
        .background(colorPalette.background0)
        //.fillMaxSize()
        .fillMaxHeight()
        .fillMaxWidth(if (navigationBarPosition == NavigationBarPosition.Left ||
            navigationBarPosition == NavigationBarPosition.Top ||
            navigationBarPosition == NavigationBarPosition.Bottom) 1f
        else Dimensions.contentWidthRightBar)
    ) {


        LazyVerticalGrid(
            state = lazyGridState,
            columns = GridCells.Adaptive(itemSize.dp + 24.dp),
            //contentPadding = LocalPlayerAwareWindowInsets.current.asPaddingValues(),
            modifier = Modifier
                .background(colorPalette.background0)
                .fillMaxSize()
        ) {
            item(
                key = "header",
                contentType = 0,
                span = { GridItemSpan(maxLineSpan) }
            ) {
                HeaderWithIcon(
                    title = stringResource(R.string.albums),
                    iconId = R.drawable.search,
                    enabled = true,
                    showIcon = !showSearchTab,
                    modifier = Modifier,
                    onClick = onSearchClick
                )

            }

            item(
                key = "filter",
                contentType = 0,
                span = { GridItemSpan(maxLineSpan) }
            ) {


                Row (
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .padding(bottom = 20.dp)
                        .fillMaxWidth()
                ){
                    HeaderInfo(
                        title = "${items.size}",
                        icon = painterResource(R.drawable.album),
                        spacer = 0
                    )

                    HeaderIconButton(
                        modifier = Modifier.rotate(rotationAngle),
                        icon = R.drawable.dice,
                        enabled = items.isNotEmpty() ,
                        color = colorPalette.text,
                        onClick = {
                            isRotated = !isRotated
                            //onAlbumClick(items.get((0..<items.size).random()))
                            onAlbumClick(items.get(
                                Random(System.currentTimeMillis()).nextInt(0, items.size-1)
                            ))
                        },
                        iconSize = 16.dp
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

                    Spacer(
                        modifier = Modifier
                            .weight(1f)
                    )


                    /*
                    BasicText(
                        text = when (sortBy) {
                            AlbumSortBy.Title -> stringResource(R.string.sort_title)
                            AlbumSortBy.Year -> stringResource(R.string.sort_year)
                            AlbumSortBy.DateAdded -> stringResource(R.string.sort_date_added)
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
                                        onTitle = { sortBy = AlbumSortBy.Title },
                                        onYear = { sortBy = AlbumSortBy.Year },
                                        onDateAdded = { sortBy = AlbumSortBy.DateAdded },
                                    )
                                }
                                //showSortTypeSelectDialog = true
                            }
                    )

                     */

                    HeaderIconButton(
                        icon = R.drawable.arrow_up,
                        color = colorPalette.text,
                        onClick = { sortOrder = !sortOrder },
                        modifier = Modifier
                            .graphicsLayer { rotationZ = sortOrderIconRotation }
                    )
                }
            }

            items(
                items = items,
                key = Album::id
            ) { album ->
                var songs = remember { listOf<Song>() }
                query {
                    songs = Database.albumSongsList(album.id)
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

                if (showDialogChangeAlbumTitle)
                    InputTextDialog(
                        onDismiss = { showDialogChangeAlbumTitle = false },
                        title = stringResource(R.string.update_title),
                        value = album.title.toString(),
                        placeholder = stringResource(R.string.title),
                        setValue = {
                            if (it.isNotEmpty()) {
                                query {
                                    Database.updateAlbumTitle(album.id, it)
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
                                    Database.updateAlbumAuthors(album.id, it)
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
                                    Database.updateAlbumCover(album.id, it)
                                }
                                //context.toast("Album Saved $it")
                            }
                        }
                    )

                var position by remember {
                    mutableIntStateOf(0)
                }
                val context = LocalContext.current

                AlbumItem(
                    alternative = true,
                    album = album,
                    thumbnailSizePx = thumbnailSizePx,
                    thumbnailSizeDp = thumbnailSizeDp,
                    modifier = Modifier
                        .combinedClickable(

                            onLongClick = {
                                menuState.display {
                                    AlbumsItemMenu(
                                        onDismiss = menuState::hide,
                                        album = album,
                                        onChangeAlbumTitle = {
                                            showDialogChangeAlbumTitle = true
                                        },
                                        onChangeAlbumAuthors = {
                                            showDialogChangeAlbumAuthors = true
                                        },
                                        onChangeAlbumCover = {
                                            showDialogChangeAlbumCover = true
                                        },
                                        onPlayNext = {
                                            println("mediaItem ${songs}")
                                            binder?.player?.addNext(
                                                songs.map(Song::asMediaItem), context
                                            )

                                        },
                                        onEnqueue = {
                                            println("mediaItem ${songs}")
                                            binder?.player?.enqueue(
                                                songs.map(Song::asMediaItem), context
                                            )

                                        },
                                        onAddToPlaylist = { playlistPreview ->
                                            position =
                                                playlistPreview.songCount.minus(1) ?: 0
                                            //Log.d("mediaItem", " maxPos in Playlist $it ${position}")
                                            if (position > 0) position++ else position =
                                                0
                                            //Log.d("mediaItem", "next initial pos ${position}")
                                            //if (listMediaItems.isEmpty()) {
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
                                            //}
                                        }
                                    )
                                }
                            },
                            onClick = {
                                onAlbumClick(album)
                            }
                        )
                        .clip(thumbnailShape)
                        .animateItemPlacement()
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
