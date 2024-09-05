package com.peecock.ymusic.ui.screens.home

import android.annotation.SuppressLint
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download
import androidx.navigation.NavController
import com.peecock.compose.persist.persist
import com.peecock.compose.persist.persistList
import com.peecock.innertube.Innertube
import com.peecock.innertube.models.NavigationEndpoint
import com.peecock.innertube.models.bodies.NextBody
import com.peecock.innertube.requests.discoverPage
import com.peecock.innertube.requests.relatedPage
import com.peecock.ymusic.Database
import com.peecock.ymusic.LocalPlayerAwareWindowInsets
import com.peecock.ymusic.LocalPlayerServiceBinder
import com.peecock.ymusic.R
import com.peecock.ymusic.enums.NavRoutes
import com.peecock.ymusic.enums.NavigationBarPosition
import com.peecock.ymusic.enums.PlayEventsType
import com.peecock.ymusic.enums.UiType
import com.peecock.ymusic.models.Artist
import com.peecock.ymusic.models.PlaylistPreview
import com.peecock.ymusic.models.Song
import com.peecock.ymusic.query
import com.peecock.ymusic.service.DownloadUtil
import com.peecock.ymusic.service.isLocal
import com.peecock.ymusic.ui.components.LocalMenuState
import com.peecock.ymusic.ui.components.PullToRefreshBox
import com.peecock.ymusic.ui.components.themed.HeaderWithIcon
import com.peecock.ymusic.ui.components.themed.MultiFloatingActionsContainer
import com.peecock.ymusic.ui.components.themed.NonQueuedMediaItemMenu
import com.peecock.ymusic.ui.components.themed.Title
import com.peecock.ymusic.ui.items.AlbumItem
import com.peecock.ymusic.ui.items.ArtistItem
import com.peecock.ymusic.ui.items.PlaylistItem
import com.peecock.ymusic.ui.items.SongItem
import com.peecock.ymusic.ui.styling.Dimensions
import com.peecock.ymusic.ui.styling.LocalAppearance
import com.peecock.ymusic.ui.styling.px
import com.peecock.ymusic.utils.UiTypeKey
import com.peecock.ymusic.utils.asMediaItem
import com.peecock.ymusic.utils.center
import com.peecock.ymusic.utils.downloadedStateMedia
import com.peecock.ymusic.utils.forcePlay
import com.peecock.ymusic.utils.getDownloadState
import com.peecock.ymusic.utils.isLandscape
import com.peecock.ymusic.utils.manageDownload
import com.peecock.ymusic.utils.navigationBarPositionKey
import com.peecock.ymusic.utils.playEventsTypeKey
import com.peecock.ymusic.utils.rememberPreference
import com.peecock.ymusic.utils.secondary
import com.peecock.ymusic.utils.semiBold
import com.peecock.ymusic.utils.showFloatingIconKey
import com.peecock.ymusic.utils.showMonthlyPlaylistInQuickPicksKey
import com.peecock.ymusic.utils.showNewAlbumsArtistsKey
import com.peecock.ymusic.utils.showNewAlbumsKey
import com.peecock.ymusic.utils.showPlaylistMightLikeKey
import com.peecock.ymusic.utils.showRelatedAlbumsKey
import com.peecock.ymusic.utils.showSearchTabKey
import com.peecock.ymusic.utils.showSimilarArtistsKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import timber.log.Timber


@ExperimentalMaterialApi
@ExperimentalTextApi
@SuppressLint("SuspiciousIndentation")
@ExperimentalFoundationApi
@ExperimentalAnimationApi
@ExperimentalComposeUiApi
@UnstableApi
@Composable
fun QuickPicks(
    navController: NavController,
    onAlbumClick: (String) -> Unit,
    onArtistClick: (String) -> Unit,
    onPlaylistClick: (String) -> Unit,
    onSearchClick: () -> Unit,
    onMoodClick: (mood: Innertube.Mood.Item) -> Unit,
    onSettingsClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onStatisticsClick: () -> Unit
) {
    val (colorPalette, typography) = LocalAppearance.current
    val binder = LocalPlayerServiceBinder.current
    val menuState = LocalMenuState.current
    val windowInsets = LocalPlayerAwareWindowInsets.current
    val uiType  by rememberPreference(UiTypeKey, UiType.RiMusic)
    val playEventType  by rememberPreference(playEventsTypeKey, PlayEventsType.MostPlayed)

    var trending by persist<Song?>("home/trending")

    var relatedPageResult by persist<Result<Innertube.RelatedPage?>?>(tag = "home/relatedPageResult")
    var related by persist<Innertube.RelatedPage?>(tag = "home/relatedPage")

    var discoverPage by persist<Result<Innertube.DiscoverPage>>("home/discoveryAlbums")

    //var discoverPageAlbums by persist<Result<Innertube.DiscoverPageAlbums>>("home/discoveryAlbums")

    var preferitesArtists by persistList<Artist>("home/artists")

    //val localMonthlyPlaylists = monthlyPLaylists()
    var localMonthlyPlaylists by persistList<PlaylistPreview>("home/monthlyPlaylists")
    LaunchedEffect(Unit) {
        Database.monthlyPlaylistsPreview("").collect{ localMonthlyPlaylists = it }
    }

    var downloadState by remember {
        mutableStateOf(Download.STATE_STOPPED)
    }

    val context = LocalContext.current


    val showRelatedAlbums by rememberPreference(showRelatedAlbumsKey, true)
    val showSimilarArtists by rememberPreference(showSimilarArtistsKey, true)
    val showNewAlbumsArtists by rememberPreference(showNewAlbumsArtistsKey, true)
    val showPlaylistMightLike by rememberPreference(showPlaylistMightLikeKey, true)
    val showNewAlbums by rememberPreference(showNewAlbumsKey, true)
    val showMonthlyPlaylistInQuickPicks by rememberPreference(showMonthlyPlaylistInQuickPicksKey, true)

    val navigationBarPosition by rememberPreference(navigationBarPositionKey, NavigationBarPosition.Bottom)

    val refreshScope = rememberCoroutineScope()

    suspend fun loadData() {
        runCatching {
            refreshScope.launch(Dispatchers.IO) {
                when (playEventType) {
                    PlayEventsType.MostPlayed ->
                        Database.trendingReal().distinctUntilChanged().collect { songs ->
                            val song = songs.firstOrNull()
                            if (relatedPageResult == null || trending?.id != song?.id) {
                                relatedPageResult = Innertube.relatedPage(
                                    NextBody(
                                        videoId = (song?.id ?: "HZnNt9nnEhw")
                                    )
                                )
                            }
                            trending = song
                        }

                    PlayEventsType.LastPlayed, PlayEventsType.CasualPlayed -> {
                        val numSongs = if (playEventType == PlayEventsType.LastPlayed) 3 else 100
                        Database.lastPlayed(numSongs).distinctUntilChanged().collect { songs ->
                            val song = if (playEventType == PlayEventsType.LastPlayed) songs.firstOrNull()
                            else songs.shuffled().firstOrNull()
                            if (relatedPageResult == null || trending?.id != song?.id) {
                                relatedPageResult =
                                    Innertube.relatedPage(
                                        NextBody(
                                            videoId = (song?.id ?: "HZnNt9nnEhw")
                                        )
                                    )
                            }
                            trending = song
                        }
                    }

                }
            }

            discoverPage = Innertube.discoverPage()

        }.onFailure {
            Timber.e(it.message)
        }
    }

    LaunchedEffect(Unit) {
        loadData()
    }

    var refreshing by remember { mutableStateOf(false) }

    fun refresh() {
        if (refreshing) return
        refreshScope.launch(Dispatchers.IO) {
            refreshing = true
            loadData()
            delay(500)
            refreshing = false
        }
    }

    /*
    LaunchedEffect(Unit) {
        //discoverPageAlbums = Innertube.discoverPageNewAlbums()
        discoverPage = Innertube.discoverPage()
    }
     */

    LaunchedEffect(Unit) {
        Database.preferitesArtistsByName().collect { preferitesArtists = it }
    }

    val songThumbnailSizeDp = Dimensions.thumbnails.song
    val songThumbnailSizePx = songThumbnailSizeDp.px
    val albumThumbnailSizeDp = 108.dp
    val albumThumbnailSizePx = albumThumbnailSizeDp.px
    val artistThumbnailSizeDp = 92.dp
    val artistThumbnailSizePx = artistThumbnailSizeDp.px
    val playlistThumbnailSizeDp = 108.dp
    val playlistThumbnailSizePx = playlistThumbnailSizeDp.px

    val scrollState = rememberScrollState()
    val quickPicksLazyGridState = rememberLazyGridState()
    val moodAngGenresLazyGridState = rememberLazyGridState()

    val endPaddingValues = windowInsets.only(WindowInsetsSides.End).asPaddingValues()

    val sectionTextModifier = Modifier
        .padding(horizontal = 16.dp)
        .padding(top = 24.dp, bottom = 8.dp)
        .padding(endPaddingValues)

    val showSearchTab by rememberPreference(showSearchTabKey, false)

    //val showActionsBar by rememberPreference(showActionsBarKey, true)

    val downloadedSongs = remember {
        DownloadUtil.downloads.value.filter {
            it.value.state == Download.STATE_COMPLETED
        }.keys.toList()
    }
    var cachedSongs = remember {
        binder?.cache?.keys?.toMutableList()
    }
    cachedSongs?.addAll(downloadedSongs)



    PullToRefreshBox(
        refreshing = refreshing,
        onRefresh = { refresh() }
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth(
                    if (navigationBarPosition == NavigationBarPosition.Left ||
                        navigationBarPosition == NavigationBarPosition.Top ||
                        navigationBarPosition == NavigationBarPosition.Bottom
                    ) 1f
                    else Dimensions.contentWidthRightBar
                )

        ) {
            val quickPicksLazyGridItemWidthFactor =
                if (isLandscape && maxWidth * 0.475f >= 320.dp) {
                    0.475f
                } else {
                    0.9f
                }
            val itemInHorizontalGridWidth = maxWidth * quickPicksLazyGridItemWidthFactor

            val moodItemWidthFactor =
                if (isLandscape && maxWidth * 0.475f >= 320.dp) 0.475f else 0.9f
            val itemWidth = maxWidth * moodItemWidthFactor

            Column(
                modifier = Modifier
                    .background(colorPalette.background0)
                    .fillMaxHeight()
                    .verticalScroll(scrollState)
                    /*
                    .padding(
                        windowInsets
                            .only(WindowInsetsSides.Vertical)
                            .asPaddingValues()
                    )
                     */
            ) {

                //if (uiType == UiType.ViMusic)
                    HeaderWithIcon(
                        title = stringResource(R.string.quick_picks),
                        iconId = R.drawable.search,
                        enabled = true,
                        showIcon = !showSearchTab,
                        modifier = Modifier,
                        onClick = onSearchClick
                    )


                /*
                if (showActionsBar)
                    Row(
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(all = 10.dp)
                    ) {
                        ButtonWithTitle(
                            title = stringResource(R.string.history),
                            icon = R.drawable.history,
                            onClick = onHistoryClick,
                            modifier = Modifier.weight(1f)
                        )


                        /*
                        ButtonWithTitle(
                            title = "Settings", //stringResource(R.string.settings),
                            icon = R.drawable.settings,
                            onClick = onSettingsClick,
                            modifier = Modifier.weight(1f)
                        )
                         */

                        Image(
                            painter = painterResource(R.drawable.app_icon),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(colorPalette.favoritesIcon),
                            modifier = Modifier
                                .size(48.dp)
                        )

                        ButtonWithTitle(
                            title = stringResource(R.string.statistics),
                            icon = R.drawable.stats_chart,
                            onClick = onStatisticsClick,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    */

                BasicText(
                    text = stringResource(R.string.tips),
                    style = typography.m.semiBold,
                    modifier = sectionTextModifier
                )
                BasicText(
                    text = when (playEventType) {
                        PlayEventsType.MostPlayed -> stringResource(R.string.by_most_played_song)
                        PlayEventsType.LastPlayed -> stringResource(R.string.by_last_played_song)
                        PlayEventsType.CasualPlayed -> stringResource(R.string.by_casual_played_song)
                    },
                    style = typography.xxs.secondary,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 8.dp)
                )

                //relatedPageResult?.getOrNull()?.let { related ->
                related = relatedPageResult?.getOrNull()

                    LazyHorizontalGrid(
                        state = quickPicksLazyGridState,
                        rows = GridCells.Fixed(if(related != null) 3 else 1),
                        flingBehavior = ScrollableDefaults.flingBehavior(),
                        contentPadding = endPaddingValues,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(if (related != null) Dimensions.itemsVerticalPadding * 3 * 9 else Dimensions.itemsVerticalPadding * 9)
                        //.height((songThumbnailSizeDp + Dimensions.itemsVerticalPadding * 2) * 4)
                    ) {
                        trending?.let { song ->
                            item {
                                val isLocal by remember { derivedStateOf { song.asMediaItem.isLocal } }
                                downloadState = getDownloadState(song.asMediaItem.mediaId)
                                val isDownloaded =
                                    if (!isLocal) downloadedStateMedia(song.asMediaItem.mediaId) else true

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
                                                songId = song.id,
                                                songTitle = song.title,
                                                downloadState = isDownloaded
                                            )

                                    },
                                    downloadState = downloadState,
                                    thumbnailSizePx = songThumbnailSizePx,
                                    thumbnailSizeDp = songThumbnailSizeDp,
                                    trailingContent = {
                                        Image(
                                            painter = painterResource(R.drawable.star),
                                            contentDescription = null,
                                            colorFilter = ColorFilter.tint(colorPalette.accent),
                                            modifier = Modifier
                                                .size(16.dp)
                                        )
                                    },
                                    modifier = Modifier
                                        .combinedClickable(
                                            onLongClick = {
                                                menuState.display {
                                                    NonQueuedMediaItemMenu(
                                                        navController = navController,
                                                        onDismiss = menuState::hide,
                                                        mediaItem = song.asMediaItem,
                                                        onRemoveFromQuickPicks = {
                                                            query {
                                                                Database.clearEventsFor(song.id)
                                                            }
                                                        },

                                                        onDownload = {
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
                                                            manageDownload(
                                                                context = context,
                                                                songId = song.id,
                                                                songTitle = song.title,
                                                                downloadState = isDownloaded
                                                            )
                                                        }

                                                    )
                                                }
                                            },
                                            onClick = {
                                                val mediaItem = song.asMediaItem
                                                binder?.stopRadio()
                                                binder?.player?.forcePlay(mediaItem)
                                                binder?.setupRadio(
                                                    NavigationEndpoint.Endpoint.Watch(videoId = mediaItem.mediaId)
                                                )
                                            }
                                        )
                                        .animateItemPlacement()
                                        .width(itemInHorizontalGridWidth)
                                )
                            }
                        }

                        if (related != null) {
                            items(
                                items = related?.songs?.filter {
                                    if (cachedSongs != null) {
                                        if (cachedSongs.indexOf(it.asMediaItem.mediaId) < 0) true else false
                                    } else true
                                }
                                ?.dropLast(if (trending == null) 0 else 1)
                                ?: emptyList(),
                                key = Innertube.SongItem::key
                            ) { song ->
                                val isLocal by remember { derivedStateOf { song.asMediaItem.isLocal } }
                                downloadState = getDownloadState(song.asMediaItem.mediaId)
                                val isDownloaded =
                                    if (!isLocal) downloadedStateMedia(song.asMediaItem.mediaId) else true

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
                                                        onDownload = {
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

                                                        )
                                                }
                                            },
                                            onClick = {
                                                val mediaItem = song.asMediaItem
                                                binder?.stopRadio()
                                                binder?.player?.forcePlay(mediaItem)
                                                binder?.setupRadio(
                                                    NavigationEndpoint.Endpoint.Watch(videoId = mediaItem.mediaId)
                                                )
                                            }
                                        )
                                        .animateItemPlacement()
                                        .width(itemInHorizontalGridWidth)
                                )
                            }
                        }
                    }

                    if (related == null) {
                        BasicText(
                            text = stringResource(R.string.sorry_tips_are_not_available),
                            style = typography.s.semiBold.center,
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(all = 16.dp)
                        )
                    }


                    discoverPage?.getOrNull()?.let { page ->
                        var newReleaseAlbumsFiltered by persistList<Innertube.AlbumItem>("discovery/newalbumsartist")
                        page.newReleaseAlbums.forEach { album ->
                            preferitesArtists.forEach { artist ->
                                if (artist.name == album.authors?.first()?.name) {
                                    newReleaseAlbumsFiltered += album
                                }
                            }
                        }

                        if (showNewAlbumsArtists)
                            if (newReleaseAlbumsFiltered.isNotEmpty() && preferitesArtists.isNotEmpty()) {

                                BasicText(
                                    text = stringResource(R.string.new_albums_of_your_artists),
                                    style = typography.m.semiBold,
                                    modifier = sectionTextModifier
                                )

                                LazyRow(contentPadding = endPaddingValues) {
                                    items(
                                        items = newReleaseAlbumsFiltered.distinct(),
                                        key = { it.key }) {
                                        AlbumItem(
                                            album = it,
                                            thumbnailSizePx = albumThumbnailSizePx,
                                            thumbnailSizeDp = albumThumbnailSizeDp,
                                            alternative = true,
                                            modifier = Modifier.clickable(onClick = {
                                                onAlbumClick(it.key)
                                            })
                                        )
                                    }
                                }

                            }

                        if (showNewAlbums) {
                            /*
                            BasicText(
                                text = stringResource(R.string.new_albums),
                                style = typography.m.semiBold,
                                modifier = sectionTextModifier
                            )
                             */
                            Title(
                                title = stringResource(R.string.new_albums),
                                onClick = { navController.navigate(NavRoutes.newAlbums.name) },
                                //modifier = Modifier.fillMaxWidth(0.7f)
                            )

                            LazyRow(contentPadding = endPaddingValues) {
                                items(items = page.newReleaseAlbums.distinct(), key = { it.key }) {
                                    AlbumItem(
                                        album = it,
                                        thumbnailSizePx = albumThumbnailSizePx,
                                        thumbnailSizeDp = albumThumbnailSizeDp,
                                        alternative = true,
                                        modifier = Modifier.clickable(onClick = {
                                            onAlbumClick(it.key)
                                        })
                                    )
                                }
                            }
                        }
                    }

                    if (showRelatedAlbums)
                        related?.albums?.let { albums ->
                            BasicText(
                                text = stringResource(R.string.related_albums),
                                style = typography.m.semiBold,
                                modifier = sectionTextModifier
                            )

                            LazyRow(contentPadding = endPaddingValues) {
                                items(
                                    items = albums,
                                    key = Innertube.AlbumItem::key
                                ) { album ->
                                    AlbumItem(
                                        album = album,
                                        thumbnailSizePx = albumThumbnailSizePx,
                                        thumbnailSizeDp = albumThumbnailSizeDp,
                                        alternative = true,
                                        modifier = Modifier
                                            .clickable(onClick = { onAlbumClick(album.key) })
                                    )
                                }
                            }
                        }

                    if (showSimilarArtists)
                        related?.artists?.let { artists ->
                            BasicText(
                                text = stringResource(R.string.similar_artists),
                                style = typography.m.semiBold,
                                modifier = sectionTextModifier
                            )

                            LazyRow(contentPadding = endPaddingValues) {
                                items(
                                    items = artists,
                                    key = Innertube.ArtistItem::key,
                                ) { artist ->
                                    ArtistItem(
                                        artist = artist,
                                        thumbnailSizePx = artistThumbnailSizePx,
                                        thumbnailSizeDp = artistThumbnailSizeDp,
                                        alternative = true,
                                        modifier = Modifier
                                            .clickable(onClick = { onArtistClick(artist.key) })
                                    )
                                }
                            }
                        }

                    if (showPlaylistMightLike)
                        related?.playlists?.let { playlists ->
                            BasicText(
                                text = stringResource(R.string.playlists_you_might_like),
                                style = typography.m.semiBold,
                                modifier = Modifier
                                    .padding(horizontal = 16.dp)
                                    .padding(top = 24.dp, bottom = 8.dp)
                            )

                            LazyRow(contentPadding = endPaddingValues) {
                                items(
                                    items = playlists,
                                    key = Innertube.PlaylistItem::key,
                                ) { playlist ->
                                    PlaylistItem(
                                        playlist = playlist,
                                        thumbnailSizePx = playlistThumbnailSizePx,
                                        thumbnailSizeDp = playlistThumbnailSizeDp,
                                        alternative = true,
                                        showSongsCount = false,
                                        modifier = Modifier
                                            .clickable(onClick = { onPlaylistClick(playlist.key) })
                                    )
                                }
                            }
                        }

                    discoverPage?.getOrNull()?.let { page ->
                        if (page.moods.isNotEmpty()) {

                            /*
                            BasicText(
                                text = stringResource(R.string.moods_and_genres),
                                style = typography.m.semiBold,
                                modifier = sectionTextModifier
                            )
                             */
                            Title(
                                title = stringResource(R.string.moods_and_genres),
                                onClick = { navController.navigate(NavRoutes.moodsPage.name) },
                                //modifier = Modifier.fillMaxWidth(0.7f)
                            )

                            LazyHorizontalGrid(
                                state = moodAngGenresLazyGridState,
                                rows = GridCells.Fixed(4),
                                flingBehavior = ScrollableDefaults.flingBehavior(),
                                //flingBehavior = rememberSnapFlingBehavior(snapLayoutInfoProvider),
                                contentPadding = endPaddingValues,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    //.height((thumbnailSizeDp + Dimensions.itemsVerticalPadding * 8) * 8)
                                    .height(Dimensions.itemsVerticalPadding * 4 * 8)
                            ) {
                                items(
                                    items = page.moods.sortedBy { it.title },
                                    key = { it.endpoint.params ?: it.title }
                                ) {
                                    MoodItem(
                                        mood = it,
                                        onClick = { it.endpoint.browseId?.let { _ -> onMoodClick(it) } },
                                        modifier = Modifier
                                            .width(itemWidth)
                                            .padding(4.dp)
                                    )
                                }
                            }

                        }
                    }

                if (showMonthlyPlaylistInQuickPicks)
                    localMonthlyPlaylists.let { playlists ->
                        BasicText(
                            text = stringResource(R.string.monthly_playlists),
                            style = typography.m.semiBold,
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .padding(top = 24.dp, bottom = 8.dp)
                        )

                        LazyRow(contentPadding = endPaddingValues) {
                            items(
                                items = playlists,
                                key = {it.playlist.id }
                            ) { playlist ->
                                PlaylistItem(
                                    playlist = playlist,
                                    thumbnailSizeDp = playlistThumbnailSizeDp,
                                    thumbnailSizePx = playlistThumbnailSizePx,
                                    alternative = true,
                                    modifier = Modifier
                                        .clickable(onClick = { navController.navigate(route = "${NavRoutes.localPlaylist.name}/${playlist.playlist.id}") })
                                        .animateItemPlacement()
                                        .fillMaxSize()
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(Dimensions.bottomSpacer))


                //} ?:

                relatedPageResult?.exceptionOrNull()?.let {
                    BasicText(
                        text = stringResource(R.string.page_not_been_loaded),
                        style = typography.s.secondary.center,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(all = 16.dp)
                    )
                }

                /*
                if (related == null)
                    ShimmerHost {
                        repeat(3) {
                            SongItemPlaceholder(
                                thumbnailSizeDp = songThumbnailSizeDp,
                            )
                        }

                        TextPlaceholder(modifier = sectionTextModifier)

                        Row {
                            repeat(2) {
                                AlbumItemPlaceholder(
                                    thumbnailSizeDp = albumThumbnailSizeDp,
                                    alternative = true
                                )
                            }
                        }

                        TextPlaceholder(modifier = sectionTextModifier)

                        Row {
                            repeat(2) {
                                ArtistItemPlaceholder(
                                    thumbnailSizeDp = albumThumbnailSizeDp,
                                    alternative = true
                                )
                            }
                        }

                        TextPlaceholder(modifier = sectionTextModifier)

                        Row {
                            repeat(2) {
                                PlaylistItemPlaceholder(
                                    thumbnailSizeDp = albumThumbnailSizeDp,
                                    alternative = true
                                )
                            }
                        }
                    }
                 */
            }


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
                    scrollState = scrollState,
                    iconId = R.drawable.search,
                    onClick = onSearchClick
                )
                 */

        }

        /*
        PullRefreshIndicator(
            refreshing, refreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
         */
    }
}


