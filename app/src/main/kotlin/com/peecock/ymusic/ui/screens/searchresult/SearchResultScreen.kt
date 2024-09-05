package com.peecock.ymusic.ui.screens.searchresult

import android.annotation.SuppressLint
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download
import androidx.navigation.NavController
import com.peecock.compose.persist.PersistMapCleanup
import com.peecock.compose.persist.persist
import com.peecock.compose.persist.persistMap
import com.peecock.compose.routing.RouteHandler
import com.peecock.innertube.Innertube
import com.peecock.innertube.models.bodies.BrowseBody
import com.peecock.innertube.models.bodies.ContinuationBody
import com.peecock.innertube.models.bodies.SearchBody
import com.peecock.innertube.requests.albumPage
import com.peecock.innertube.requests.searchPage
import com.peecock.innertube.utils.from
import com.peecock.ymusic.Database
import com.peecock.ymusic.LocalPlayerServiceBinder
import com.peecock.ymusic.R
import com.peecock.ymusic.enums.NavRoutes
import com.peecock.ymusic.enums.UiType
import com.peecock.ymusic.models.Album
import com.peecock.ymusic.models.Song
import com.peecock.ymusic.models.SongAlbumMap
import com.peecock.ymusic.query
import com.peecock.ymusic.ui.components.LocalMenuState
import com.peecock.ymusic.ui.components.Scaffold
import com.peecock.ymusic.ui.components.SwipeableAlbumItem
import com.peecock.ymusic.ui.components.SwipeablePlaylistItem
import com.peecock.ymusic.ui.components.themed.Header
import com.peecock.ymusic.ui.components.themed.NonQueuedMediaItemMenu
import com.peecock.ymusic.ui.items.AlbumItem
import com.peecock.ymusic.ui.items.AlbumItemPlaceholder
import com.peecock.ymusic.ui.items.ArtistItem
import com.peecock.ymusic.ui.items.ArtistItemPlaceholder
import com.peecock.ymusic.ui.items.PlaylistItem
import com.peecock.ymusic.ui.items.PlaylistItemPlaceholder
import com.peecock.ymusic.ui.items.SongItem
import com.peecock.ymusic.ui.items.SongItemPlaceholder
import com.peecock.ymusic.ui.items.VideoItem
import com.peecock.ymusic.ui.items.VideoItemPlaceholder
import com.peecock.ymusic.ui.screens.globalRoutes
import com.peecock.ymusic.ui.styling.Dimensions
import com.peecock.ymusic.ui.styling.px
import com.peecock.ymusic.utils.UiTypeKey
import com.peecock.ymusic.utils.addNext
import com.peecock.ymusic.utils.asMediaItem
import com.peecock.ymusic.utils.downloadedStateMedia
import com.peecock.ymusic.utils.enqueue
import com.peecock.ymusic.utils.forcePlay
import com.peecock.ymusic.utils.getDownloadState
import com.peecock.ymusic.utils.manageDownload
import com.peecock.ymusic.utils.rememberPreference
import com.peecock.ymusic.utils.searchResultScreenTabIndexKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@ExperimentalMaterialApi
@ExperimentalTextApi
@SuppressLint("SuspiciousIndentation")
@ExperimentalFoundationApi
@ExperimentalAnimationApi
@ExperimentalComposeUiApi
@UnstableApi
@Composable
fun SearchResultScreen(
    navController: NavController,
    playerEssential: @Composable () -> Unit = {},
    query: String, onSearchAgain: () -> Unit
) {
    val context = LocalContext.current
    val binder = LocalPlayerServiceBinder.current
    val saveableStateHolder = rememberSaveableStateHolder()
    val (tabIndex, onTabIndexChanges) = rememberPreference(searchResultScreenTabIndexKey, 0)


    var downloadState by remember {
        mutableStateOf(Download.STATE_STOPPED)
    }
    val hapticFeedback = LocalHapticFeedback.current

    var loadAlbum by remember {
        mutableStateOf(false)
    }

    PersistMapCleanup(tagPrefix = "searchResults/$query/")

    RouteHandler(listenToGlobalEmitter = true) {
        globalRoutes()

        host {
            val headerContent: @Composable (textButton: (@Composable () -> Unit)?) -> Unit = {
                Header(
                    title = query,
                    modifier = Modifier
                        .pointerInput(Unit) {
                            detectTapGestures {
                                context.persistMap?.keys?.removeAll {
                                    it.startsWith("searchResults/$query/")
                                }
                                onSearchAgain()
                            }
                        }
                )
            }

            val emptyItemsText = stringResource(R.string.no_results_found)
            val uiType by rememberPreference(UiTypeKey, UiType.RiMusic)
            Scaffold(
                navController = navController,
                playerEssential = playerEssential,
                topIconButtonId = R.drawable.chevron_back,
                onTopIconButtonClick = pop,
                showButton1 = if (uiType == UiType.RiMusic) false else true,
                topIconButton2Id = R.drawable.chevron_back,
                onTopIconButton2Click = pop,
                showButton2 = false,
                tabIndex = tabIndex,
                onHomeClick = {
                    navController.navigate(NavRoutes.home.name)
                },
                onTabChanged = onTabIndexChanges,
                tabColumnContent = { Item ->
                    Item(0, stringResource(R.string.songs), R.drawable.musical_notes)
                    Item(1, stringResource(R.string.albums), R.drawable.album)
                    Item(2, stringResource(R.string.artists), R.drawable.artist)
                    Item(3, stringResource(R.string.videos), R.drawable.video)
                    Item(4, stringResource(R.string.playlists), R.drawable.playlist)
                    Item(5, stringResource(R.string.featured), R.drawable.featured_playlist)
                    Item(6, stringResource(R.string.podcasts), R.drawable.podcast)
                }
            ) { tabIndex ->
                saveableStateHolder.SaveableStateProvider(tabIndex) {
                    when (tabIndex) {
                        0 -> {
                            val binder = LocalPlayerServiceBinder.current
                            val menuState = LocalMenuState.current
                            val thumbnailSizeDp = Dimensions.thumbnails.song
                            val thumbnailSizePx = thumbnailSizeDp.px

                            ItemsPage(
                                tag = "searchResults/$query/songs",
                                itemsPageProvider = { continuation ->
                                    if (continuation == null) {
                                        Innertube.searchPage(
                                            body = SearchBody(
                                                query = query,
                                                params = Innertube.SearchFilter.Song.value
                                            ),
                                            fromMusicShelfRendererContent = Innertube.SongItem.Companion::from
                                        )
                                    } else {
                                        Innertube.searchPage(
                                            body = ContinuationBody(continuation = continuation),
                                            fromMusicShelfRendererContent = Innertube.SongItem.Companion::from
                                        )
                                    }
                                },
                                emptyItemsText = emptyItemsText,
                                headerContent = headerContent,
                                itemContent = { song ->
                                    //Log.d("mediaItem",song.toString())

                                    SwipeablePlaylistItem(
                                        mediaItem = song.asMediaItem,
                                        onSwipeToRight = {
                                            binder?.player?.addNext(song.asMediaItem)
                                        }
                                    ) {
                                        downloadState = getDownloadState(song.asMediaItem.mediaId)
                                        val isDownloaded =
                                            downloadedStateMedia(song.asMediaItem.mediaId)
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

                                                manageDownload(
                                                    context = context,
                                                    songId = song.asMediaItem.mediaId,
                                                    songTitle = song.asMediaItem.mediaMetadata.title.toString(),
                                                    downloadState = isDownloaded
                                                )
                                            },
                                            downloadState = downloadState,
                                            thumbnailSizePx = thumbnailSizePx,
                                            thumbnailSizeDp = thumbnailSizeDp,
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
                                                        hapticFeedback.performHapticFeedback(
                                                            HapticFeedbackType.LongPress
                                                        )
                                                    },
                                                    onClick = {
                                                        binder?.stopRadio()
                                                        binder?.player?.forcePlay(song.asMediaItem)
                                                        binder?.setupRadio(song.info?.endpoint)
                                                    }
                                                )
                                        )
                                    }
                                },
                                itemPlaceholderContent = {
                                    SongItemPlaceholder(thumbnailSizeDp = thumbnailSizeDp)
                                }
                            )
                        }

                        1 -> {
                            val thumbnailSizeDp = 108.dp
                            val thumbnailSizePx = thumbnailSizeDp.px

                            ItemsPage(
                                tag = "searchResults/$query/albums",
                                itemsPageProvider = { continuation ->
                                    if (continuation == null) {
                                        Innertube.searchPage(
                                            body = SearchBody(
                                                query = query,
                                                params = Innertube.SearchFilter.Album.value
                                            ),
                                            fromMusicShelfRendererContent = Innertube.AlbumItem::from
                                        )
                                    } else {
                                        Innertube.searchPage(
                                            body = ContinuationBody(continuation = continuation),
                                            fromMusicShelfRendererContent = Innertube.AlbumItem::from
                                        )
                                    }
                                },
                                emptyItemsText = emptyItemsText,
                                headerContent = headerContent,
                                itemContent = { album ->
                                    var albumPage by persist<Innertube.PlaylistOrAlbumPage?>("album/${album.key}/albumPage")
                                    SwipeableAlbumItem(
                                        albumItem = album,
                                        onSwipeToLeft = {
                                            CoroutineScope(Dispatchers.IO).launch {
                                                Database
                                                    .album(album.key)
                                                    .combine(snapshotFlow { tabIndex }) { album, tabIndex -> album to tabIndex }
                                                    .collect { (currentAlbum) ->
                                                        if (albumPage == null)
                                                            withContext(Dispatchers.IO) {
                                                                Innertube.albumPage(
                                                                    BrowseBody(
                                                                        browseId = album.key
                                                                    )
                                                                )
                                                                    ?.onSuccess { currentAlbumPage ->
                                                                        albumPage =
                                                                            currentAlbumPage

                                                                        println("mediaItem success home album songsPage ${currentAlbumPage?.songsPage} description ${currentAlbumPage?.description} year ${currentAlbumPage?.year}")

                                                                        albumPage
                                                                            ?.songsPage
                                                                            ?.items
                                                                            ?.map(
                                                                                Innertube.SongItem::asMediaItem
                                                                            )
                                                                            ?.let { it1 ->
                                                                                withContext(Dispatchers.Main) {
                                                                                    binder?.player?.enqueue(
                                                                                        it1,
                                                                                        context
                                                                                    )
                                                                                }
                                                                            }
                                                                        println("mediaItem success add in queue album songsPage ${albumPage
                                                                            ?.songsPage
                                                                            ?.items?.size}")

                                                                    }
                                                                    ?.onFailure {
                                                                        println("mediaItem error searchResultScreen album ${it.stackTraceToString()}")
                                                                    }

                                                            }

                                                        //}
                                                    }

                                            }

                                        },
                                        onSwipeToRight = {
                                            CoroutineScope(Dispatchers.IO).launch {
                                                Database
                                                    .album(album.key)
                                                    .combine(snapshotFlow { tabIndex }) { album, tabIndex -> album to tabIndex }
                                                    .collect { (currentAlbum) ->
                                                        if (albumPage == null)
                                                            withContext(Dispatchers.IO) {
                                                                Innertube.albumPage(
                                                                    BrowseBody(
                                                                        browseId = album.key
                                                                    )
                                                                )
                                                                    ?.onSuccess { currentAlbumPage ->
                                                                        albumPage =
                                                                            currentAlbumPage

                                                                        println("mediaItem success home album songsPage ${currentAlbumPage?.songsPage} description ${currentAlbumPage?.description} year ${currentAlbumPage?.year}")

                                                                        Database.upsert(
                                                                            Album(
                                                                                id = album.key,
                                                                                title = currentAlbumPage?.title,
                                                                                thumbnailUrl = currentAlbumPage?.thumbnail?.url,
                                                                                year = currentAlbumPage?.year,
                                                                                authorsText = currentAlbumPage?.authors
                                                                                    ?.joinToString(
                                                                                        ""
                                                                                    ) {
                                                                                        it.name
                                                                                            ?: ""
                                                                                    },
                                                                                shareUrl = currentAlbumPage?.url,
                                                                                timestamp = System.currentTimeMillis(),
                                                                                bookmarkedAt = System.currentTimeMillis()
                                                                            ),
                                                                            currentAlbumPage
                                                                                ?.songsPage
                                                                                ?.items
                                                                                ?.map(
                                                                                    Innertube.SongItem::asMediaItem
                                                                                )
                                                                                ?.onEach(
                                                                                    Database::insert
                                                                                )
                                                                                ?.mapIndexed { position, mediaItem ->
                                                                                    SongAlbumMap(
                                                                                        songId = mediaItem.mediaId,
                                                                                        albumId = album.key,
                                                                                        position = position
                                                                                    )
                                                                                }
                                                                                ?: emptyList()
                                                                        )

                                                                    }
                                                                    ?.onFailure {
                                                                        println("mediaItem error searchResultScreen album ${it.stackTraceToString()}")
                                                                    }

                                                            }

                                                        //}
                                                    }
                                            }
                                        }
                                    ) {
                                        AlbumItem(
                                            yearCentered = false,
                                            album = album,
                                            thumbnailSizePx = thumbnailSizePx,
                                            thumbnailSizeDp = thumbnailSizeDp,
                                            modifier = Modifier
                                                .combinedClickable(
                                                    onClick = {
                                                        navController.navigate("${NavRoutes.album.name}/${album.key}")
                                                    },
                                                    onLongClick = {}

                                                )
                                        )
                                    }
                                },
                                itemPlaceholderContent = {
                                    AlbumItemPlaceholder(thumbnailSizeDp = thumbnailSizeDp)
                                }
                            )
                        }

                        2 -> {
                            val thumbnailSizeDp = 64.dp
                            val thumbnailSizePx = thumbnailSizeDp.px

                            ItemsPage(
                                tag = "searchResults/$query/artists",
                                itemsPageProvider = { continuation ->
                                    if (continuation == null) {
                                        Innertube.searchPage(
                                            body = SearchBody(
                                                query = query,
                                                params = Innertube.SearchFilter.Artist.value
                                            ),
                                            fromMusicShelfRendererContent = Innertube.ArtistItem::from
                                        )
                                    } else {
                                        Innertube.searchPage(
                                            body = ContinuationBody(continuation = continuation),
                                            fromMusicShelfRendererContent = Innertube.ArtistItem::from
                                        )
                                    }
                                },
                                emptyItemsText = emptyItemsText,
                                headerContent = headerContent,
                                itemContent = { artist ->
                                    ArtistItem(
                                        artist = artist,
                                        thumbnailSizePx = thumbnailSizePx,
                                        thumbnailSizeDp = thumbnailSizeDp,
                                        modifier = Modifier
                                            .clickable(onClick = {
                                                //artistRoute(artist.key)
                                                navController.navigate("${NavRoutes.artist.name}/${artist.key}")
                                            })
                                    )
                                },
                                itemPlaceholderContent = {
                                    ArtistItemPlaceholder(thumbnailSizeDp = thumbnailSizeDp)
                                }
                            )
                        }

                        3 -> {
                            val binder = LocalPlayerServiceBinder.current
                            val menuState = LocalMenuState.current
                            val thumbnailHeightDp = 72.dp
                            val thumbnailWidthDp = 128.dp

                            ItemsPage(
                                tag = "searchResults/$query/videos",
                                itemsPageProvider = { continuation ->
                                    if (continuation == null) {
                                        Innertube.searchPage(
                                            body = SearchBody(
                                                query = query,
                                                params = Innertube.SearchFilter.Video.value
                                            ),
                                            fromMusicShelfRendererContent = Innertube.VideoItem::from
                                        )
                                    } else {
                                        Innertube.searchPage(
                                            body = ContinuationBody(continuation = continuation),
                                            fromMusicShelfRendererContent = Innertube.VideoItem::from
                                        )
                                    }
                                },
                                emptyItemsText = emptyItemsText,
                                headerContent = headerContent,
                                itemContent = { video ->
                                    SwipeablePlaylistItem(
                                        mediaItem = video.asMediaItem,
                                        onSwipeToRight = {
                                            binder?.player?.addNext(video.asMediaItem)
                                        }
                                    ) {
                                        VideoItem(
                                            video = video,
                                            thumbnailWidthDp = thumbnailWidthDp,
                                            thumbnailHeightDp = thumbnailHeightDp,
                                            modifier = Modifier
                                                .combinedClickable(
                                                    onLongClick = {
                                                        menuState.display {
                                                            NonQueuedMediaItemMenu(
                                                                navController = navController,
                                                                mediaItem = video.asMediaItem,
                                                                onDismiss = menuState::hide
                                                            )
                                                        };
                                                        hapticFeedback.performHapticFeedback(
                                                            HapticFeedbackType.LongPress
                                                        )
                                                    },
                                                    onClick = {
                                                        binder?.stopRadio()
                                                        binder?.player?.forcePlay(video.asMediaItem)
                                                        binder?.setupRadio(video.info?.endpoint)
                                                    }
                                                )
                                        )
                                    }
                                },
                                itemPlaceholderContent = {
                                    VideoItemPlaceholder(
                                        thumbnailHeightDp = thumbnailHeightDp,
                                        thumbnailWidthDp = thumbnailWidthDp
                                    )
                                }
                            )
                        }

                        4, 5 -> {
                            val thumbnailSizeDp = Dimensions.thumbnails.playlist
                            val thumbnailSizePx = thumbnailSizeDp.px
                            //val thumbnailSizeDp = 108.dp
                            //val thumbnailSizePx = thumbnailSizeDp.px

                            ItemsPage(
                                tag = "searchResults/$query/${
                                    when (tabIndex) {
                                        4 -> "playlists"
                                        else -> "featured"
                                    }
                                }",
                                itemsPageProvider = { continuation ->
                                    if (continuation == null) {
                                        val filter = when (tabIndex) {
                                            4 -> Innertube.SearchFilter.CommunityPlaylist
                                            else -> Innertube.SearchFilter.FeaturedPlaylist
                                        }

                                        Innertube.searchPage(
                                            body = SearchBody(query = query, params = filter.value),
                                            fromMusicShelfRendererContent = Innertube.PlaylistItem::from
                                        )
                                    } else {
                                        Innertube.searchPage(
                                            body = ContinuationBody(continuation = continuation),
                                            fromMusicShelfRendererContent = Innertube.PlaylistItem::from
                                        )
                                    }
                                },
                                emptyItemsText = emptyItemsText,
                                headerContent = headerContent,
                                itemContent = { playlist ->
                                    PlaylistItem(
                                        playlist = playlist,
                                        thumbnailSizePx = thumbnailSizePx,
                                        thumbnailSizeDp = thumbnailSizeDp,
                                        showSongsCount = false,
                                        modifier = Modifier
                                            .clickable(onClick = {
                                                //playlistRoute(playlist.key)
                                                navController.navigate("${NavRoutes.playlist.name}/${playlist.key}")
                                            })
                                    )
                                },
                                itemPlaceholderContent = {
                                    PlaylistItemPlaceholder(thumbnailSizeDp = thumbnailSizeDp)
                                }
                            )
                        }

                        6 -> {
                            val thumbnailSizeDp = Dimensions.thumbnails.playlist
                            val thumbnailSizePx = thumbnailSizeDp.px
                            //val thumbnailSizeDp = 108.dp
                            //val thumbnailSizePx = thumbnailSizeDp.px

                            ItemsPage(
                                tag = "searchResults/$query/podcasts",
                                itemsPageProvider = { continuation ->
                                    if (continuation == null) {
                                        val filter = Innertube.SearchFilter.Podcast

                                        Innertube.searchPage(
                                            body = SearchBody(query = query, params = filter.value),
                                            fromMusicShelfRendererContent = Innertube.PlaylistItem::from
                                        )
                                    } else {
                                        Innertube.searchPage(
                                            body = ContinuationBody(continuation = continuation),
                                            fromMusicShelfRendererContent = Innertube.PlaylistItem::from
                                        )
                                    }
                                },
                                emptyItemsText = emptyItemsText,
                                headerContent = headerContent,
                                itemContent = { playlist ->
                                    PlaylistItem(
                                        playlist = playlist,
                                        thumbnailSizePx = thumbnailSizePx,
                                        thumbnailSizeDp = thumbnailSizeDp,
                                        showSongsCount = false,
                                        modifier = Modifier
                                            .clickable(onClick = {
                                                //playlistRoute(playlist.key)
                                                println("mediaItem searchResultScreen playlist key ${playlist.key}")
                                                navController.navigate("${NavRoutes.podcast.name}/${playlist.key}")
                                            })
                                    )
                                },
                                itemPlaceholderContent = {
                                    PlaylistItemPlaceholder(thumbnailSizeDp = thumbnailSizeDp)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
