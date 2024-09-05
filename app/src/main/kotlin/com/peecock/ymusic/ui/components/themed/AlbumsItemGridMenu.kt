package com.peecock.ymusic.ui.components.themed

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import com.peecock.ymusic.Database
import com.peecock.ymusic.R
import com.peecock.ymusic.enums.PlaylistSortBy
import com.peecock.ymusic.enums.SortOrder
import com.peecock.ymusic.models.Album
import com.peecock.ymusic.models.Playlist
import com.peecock.ymusic.models.PlaylistPreview
import com.peecock.ymusic.transaction
import com.peecock.ymusic.ui.items.AlbumItem
import com.peecock.ymusic.ui.screens.home.PINNED_PREFIX
import com.peecock.ymusic.ui.styling.Dimensions
import com.peecock.ymusic.ui.styling.LocalAppearance
import com.peecock.ymusic.ui.styling.px
import com.peecock.ymusic.utils.MONTHLY_PREFIX
import com.peecock.ymusic.utils.playlistSortByKey
import com.peecock.ymusic.utils.playlistSortOrderKey
import com.peecock.ymusic.utils.rememberPreference
import com.peecock.ymusic.utils.semiBold
import kotlinx.coroutines.Dispatchers

@ExperimentalTextApi
@SuppressLint("SuspiciousIndentation")
@UnstableApi
@ExperimentalAnimationApi
@Composable
fun AlbumsItemGridMenu(
    onDismiss: () -> Unit,
    onSelectUnselect: (() -> Unit)? = null,
    onSelect: (() -> Unit)? = null,
    onUncheck: (() -> Unit)? = null,
    onChangeAlbumTitle: (() -> Unit)? = null,
    onChangeAlbumAuthors: (() -> Unit)? = null,
    onChangeAlbumCover: (() -> Unit)? = null,
    onDownloadAlbumCover: (() -> Unit)? = null,
    album: Album,
    modifier: Modifier = Modifier,
    onPlayNext: (() -> Unit)? = null,
    onEnqueue: (() -> Unit)? = null,
    onAddToPlaylist: ((PlaylistPreview) -> Unit)? = null,

) {
    val (colorPalette, typography) = LocalAppearance.current
    val density = LocalDensity.current

    var isViewingPlaylists by remember {
        mutableStateOf(false)
    }

    var height by remember {
        mutableStateOf(0.dp)
    }

    val thumbnailSizeDp = Dimensions.thumbnails.song + 20.dp
    val thumbnailSizePx = thumbnailSizeDp.px

        AnimatedContent(
            targetState = isViewingPlaylists,
            transitionSpec = {
                val animationSpec = tween<IntOffset>(400)
                val slideDirection =
                    if (targetState) AnimatedContentTransitionScope.SlideDirection.Left
                    else AnimatedContentTransitionScope.SlideDirection.Right

                slideIntoContainer(slideDirection, animationSpec) togetherWith
                        slideOutOfContainer(slideDirection, animationSpec)
            }, label = ""
        ) { currentIsViewingPlaylists ->
            if (currentIsViewingPlaylists) {
                val sortBy by rememberPreference(playlistSortByKey, PlaylistSortBy.DateAdded)
                val sortOrder by rememberPreference(playlistSortOrderKey, SortOrder.Descending)
                val playlistPreviews by remember {
                    Database.playlistPreviews(sortBy, sortOrder)
                }.collectAsState(initial = emptyList(), context = Dispatchers.IO)

                val pinnedPlaylists = playlistPreviews.filter {
                    it.playlist.name.startsWith(PINNED_PREFIX, 0, true)
                }

                val unpinnedPlaylists = playlistPreviews.filter {
                    !it.playlist.name.startsWith(PINNED_PREFIX, 0, true) &&
                    !it.playlist.name.startsWith(MONTHLY_PREFIX, 0, true) //&&
                    //!it.playlist.name.startsWith(PIPED_PREFIX, 0, true)
                }

                var isCreatingNewPlaylist by rememberSaveable {
                    mutableStateOf(false)
                }

                if (isCreatingNewPlaylist && onAddToPlaylist != null) {
                    InputTextDialog(
                        onDismiss = { isCreatingNewPlaylist = false },
                        title = stringResource(R.string.enter_the_playlist_name),
                        value = "",
                        placeholder = stringResource(R.string.enter_the_playlist_name),
                        setValue = { text ->
                            onDismiss()
                            transaction {
                                val playlistId = Database.insert(Playlist(name = text))
                                onAddToPlaylist(
                                    PlaylistPreview(
                                        Playlist(
                                            id = playlistId,
                                            name = text
                                        ), 0
                                    )
                                )
                            }
                        }
                    )

                }

                BackHandler {
                    isViewingPlaylists = false
                }

                Menu(
                    modifier = modifier
                        //.requiredHeight(height)
                        .fillMaxHeight(0.5f)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .fillMaxWidth()
                    ) {
                        IconButton(
                            onClick = { isViewingPlaylists = false },
                            icon = R.drawable.chevron_back,
                            color = colorPalette.textSecondary,
                            modifier = Modifier
                                .padding(all = 4.dp)
                                .size(20.dp)
                        )

                        if (onAddToPlaylist != null) {
                            SecondaryTextButton(
                                text = stringResource(R.string.new_playlist),
                                onClick = { isCreatingNewPlaylist = true },
                                alternative = true
                            )
                        }
                    }

                    if (pinnedPlaylists.isNotEmpty()) {
                        BasicText(
                            text = stringResource(R.string.pinned_playlists),
                            style = typography.m.semiBold,
                            modifier = modifier.padding(start = 20.dp, top = 5.dp)
                        )

                        onAddToPlaylist?.let { onAddToPlaylist ->
                            pinnedPlaylists.forEach { playlistPreview ->
                                MenuEntry(
                                    icon = R.drawable.add_in_playlist,
                                    text = playlistPreview.playlist.name.substringAfter(
                                        PINNED_PREFIX
                                    ),
                                    secondaryText = "${playlistPreview.songCount} " + stringResource(
                                        R.string.songs
                                    ),
                                    onClick = {
                                        onDismiss()
                                        onAddToPlaylist(
                                            PlaylistPreview(
                                                playlistPreview.playlist,
                                                playlistPreview.songCount
                                            )
                                        )
                                    }
                                )
                            }
                        }
                    }

                    if (unpinnedPlaylists.isNotEmpty()) {
                        BasicText(
                            text = stringResource(R.string.playlists),
                            style = typography.m.semiBold,
                            modifier = modifier.padding(start = 20.dp, top = 5.dp)
                        )

                        onAddToPlaylist?.let { onAddToPlaylist ->
                            unpinnedPlaylists.forEach { playlistPreview ->
                                MenuEntry(
                                    icon = R.drawable.add_in_playlist,
                                    text = playlistPreview.playlist.name.substringAfter(
                                        PINNED_PREFIX
                                    ),
                                    secondaryText = "${playlistPreview.songCount} " + stringResource(
                                        R.string.songs
                                    ),
                                    onClick = {
                                        onDismiss()
                                        onAddToPlaylist(
                                            PlaylistPreview(
                                                playlistPreview.playlist,
                                                playlistPreview.songCount
                                            )
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            } else {
                val selectText = "${stringResource(R.string.item_select)}/${stringResource(R.string.item_deselect)}"
                GridMenu(
                    modifier = modifier
                        .onPlaced { height = with(density) { it.size.height.toDp() } },
                    contentPadding = PaddingValues(
                        start = 8.dp,
                        top = 8.dp,
                        end = 8.dp,
                        bottom = 8.dp + WindowInsets.systemBars.asPaddingValues()
                            .calculateBottomPadding()
                    ),
                    topContent = {
                        AlbumItem(
                            album = album,
                            thumbnailSizePx = thumbnailSizePx,
                            thumbnailSizeDp = thumbnailSizeDp,
                            yearCentered = false
                        )
                    }
                ) {

                    onSelectUnselect?.let { onSelectUnselect ->
                        GridMenuItem(
                            icon = R.drawable.checked,
                            title = R.string.item_select,
                            titleString = selectText,
                            colorIcon = colorPalette.text,
                            colorText = colorPalette.text,
                            onClick = {
                                onDismiss()
                                onSelectUnselect()
                            }
                        )
                    }

                    onChangeAlbumTitle?.let {
                        GridMenuItem(
                            icon = R.drawable.title_edit,
                            title = R.string.update_title,
                            colorIcon = colorPalette.text,
                            colorText = colorPalette.text,
                            onClick = {
                                onDismiss()
                                onChangeAlbumTitle()
                            }
                        )
                    }

                    onChangeAlbumAuthors?.let {
                        GridMenuItem(
                            icon = R.drawable.artists_edit,
                            title = R.string.update_authors,
                            colorIcon = colorPalette.text,
                            colorText = colorPalette.text,
                            onClick = {
                                onDismiss()
                                onChangeAlbumAuthors()
                            }
                        )
                    }

                    onChangeAlbumCover?.let {
                        GridMenuItem(
                            icon = R.drawable.cover_edit,
                            title = R.string.update_cover,
                            colorIcon = colorPalette.text,
                            colorText = colorPalette.text,
                            onClick = {
                                onDismiss()
                                onChangeAlbumCover()
                            }
                        )
                    }

                    onDownloadAlbumCover?.let {
                        GridMenuItem(
                            icon = R.drawable.download_cover,
                            title = R.string.download_cover,
                            colorIcon = colorPalette.text,
                            colorText = colorPalette.text,
                            onClick = {
                                onDismiss()
                                onDownloadAlbumCover()
                            }
                        )
                    }

                    onPlayNext?.let { onPlayNext ->
                        GridMenuItem(
                            icon = R.drawable.play_skip_forward,
                            title = R.string.play_next,
                            colorIcon = colorPalette.text,
                            colorText = colorPalette.text,
                            onClick = {
                                onDismiss()
                                onPlayNext()
                            }
                        )
                    }

                    onEnqueue?.let {
                        GridMenuItem(
                            icon = R.drawable.enqueue,
                            title = R.string.enqueue,
                            colorIcon = colorPalette.text,
                            colorText = colorPalette.text,
                            onClick = {
                                onDismiss()
                                onEnqueue()
                            }
                        )
                    }

                    onAddToPlaylist?.let { onAddToPlaylist ->
                        GridMenuItem(
                            icon = R.drawable.add_in_playlist,
                            title = R.string.add_to_playlist,
                            colorIcon = colorPalette.text,
                            colorText = colorPalette.text,
                            onClick = {
                                isViewingPlaylists = true
                            }
                        )
                    }

                }
            }
        }

}