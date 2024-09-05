package com.peecock.ymusic.ui.screens.statistics

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.peecock.compose.persist.persistList
import com.peecock.ymusic.Database
import com.peecock.ymusic.LocalPlayerAwareWindowInsets
import com.peecock.ymusic.LocalPlayerServiceBinder
import com.peecock.ymusic.R
import com.peecock.ymusic.enums.MaxStatisticsItems
import com.peecock.ymusic.enums.NavRoutes
import com.peecock.ymusic.enums.NavigationBarPosition
import com.peecock.ymusic.enums.StatisticsType
import com.peecock.ymusic.enums.ThumbnailRoundness
import com.peecock.ymusic.models.Album
import com.peecock.ymusic.models.Artist
import com.peecock.ymusic.models.PlaylistPreview
import com.peecock.ymusic.models.Song
import com.peecock.ymusic.ui.components.LocalMenuState
import com.peecock.ymusic.ui.components.themed.HeaderWithIcon
import com.peecock.ymusic.ui.components.themed.NonQueuedMediaItemMenu
import com.peecock.ymusic.ui.items.AlbumItem
import com.peecock.ymusic.ui.items.ArtistItem
import com.peecock.ymusic.ui.items.PlaylistItem
import com.peecock.ymusic.ui.items.SongItem
import com.peecock.ymusic.ui.screens.settings.SettingsEntry
import com.peecock.ymusic.ui.styling.Dimensions
import com.peecock.ymusic.ui.styling.LocalAppearance
import com.peecock.ymusic.ui.styling.px
import com.peecock.ymusic.ui.styling.shimmer
import com.peecock.ymusic.utils.UpdateYoutubeAlbum
import com.peecock.ymusic.utils.UpdateYoutubeArtist
import com.peecock.ymusic.utils.asMediaItem
import com.peecock.ymusic.utils.downloadedStateMedia
import com.peecock.ymusic.utils.durationTextToMillis
import com.peecock.ymusic.utils.forcePlayAtIndex
import com.peecock.ymusic.utils.formatAsTime
import com.peecock.ymusic.utils.getDownloadState
import com.peecock.ymusic.utils.isLandscape
import com.peecock.ymusic.utils.manageDownload
import com.peecock.ymusic.utils.maxStatisticsItemsKey
import com.peecock.ymusic.utils.navigationBarPositionKey
import com.peecock.ymusic.utils.rememberPreference
import com.peecock.ymusic.utils.semiBold
import com.peecock.ymusic.utils.showStatsListeningTimeKey
import com.peecock.ymusic.utils.thumbnailRoundnessKey
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days

@ExperimentalTextApi
@SuppressLint("SuspiciousIndentation")
@ExperimentalFoundationApi
@ExperimentalAnimationApi
@ExperimentalComposeUiApi
@UnstableApi
@Composable
fun StatisticsPage(
    navController: NavController,
    statisticsType: StatisticsType
) {
    //val onGoToArtist = artistRoute::global
    //val onGoToAlbum = albumRoute::global
    //val onGoToPlaylist = playlistRoute::global
    //val onGoToPlaylist = localPlaylistRoute::global

    val (colorPalette, typography) = LocalAppearance.current
    val binder = LocalPlayerServiceBinder.current
    val menuState = LocalMenuState.current
    val windowInsets = LocalPlayerAwareWindowInsets.current

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

    val endPaddingValues = windowInsets.only(WindowInsetsSides.End).asPaddingValues()

    val sectionTextModifier = Modifier
        .padding(horizontal = 16.dp)
        .padding(top = 24.dp, bottom = 8.dp)
        .padding(endPaddingValues)

    val thumbnailRoundness by rememberPreference(
        thumbnailRoundnessKey,
        ThumbnailRoundness.Heavy
    )

    val showStatsListeningTime by rememberPreference(showStatsListeningTimeKey,   true)

    val context = LocalContext.current

    val thumbnailSizeDp = Dimensions.thumbnails.song
    val thumbnailSize = thumbnailSizeDp.px

    var songs by persistList<Song>("statistics/songs")
    var allSongs by persistList<Song>("statistics/allsongs")
    var artists by persistList<Artist>("statistics/artists")
    var albums by persistList<Album>("statistics/albums")
    var playlists by persistList<PlaylistPreview>("statistics/playlists")



    val now: Long = System.currentTimeMillis()
    //val now: Long = System.currentTimeMillis() / 1000
    //val dateTime = LocalDateTime.now()
    //val today = dateTime.minusDays(1).toEpochSecond(ZoneOffset.UTC)

    /*
    val today = dateTime.minusHours(23).toEpochSecond(ZoneOffset.UTC) * 1000
    val lastWeek = dateTime.minusDays(7).toEpochSecond(ZoneOffset.UTC) * 1000
    val lastMonth = dateTime.minusDays(30).toEpochSecond(ZoneOffset.UTC) * 1000
    val last3Month = dateTime.minusDays(90).toEpochSecond(ZoneOffset.UTC) * 1000
    val last6Month = dateTime.minusDays(180).toEpochSecond(ZoneOffset.UTC) * 1000
    val lastYear = dateTime.minusDays(365).toEpochSecond(ZoneOffset.UTC) * 1000
    val last20Year = dateTime.minusYears(20).toEpochSecond(ZoneOffset.UTC) * 1000
     */

    val today: Duration = 1.days
    val lastWeek: Duration = 7.days
    val lastMonth: Duration = 30.days
    val last3Month: Duration = 90.days
    val last6Month: Duration = 180.days
    val lastYear: Duration = 365.days
    val last50Year: Duration = 18250.days


    val from = when (statisticsType) {
        StatisticsType.Today -> today.inWholeMilliseconds
        StatisticsType.OneWeek -> lastWeek.inWholeMilliseconds
        StatisticsType.OneMonth -> lastMonth.inWholeMilliseconds
        StatisticsType.ThreeMonths -> last3Month.inWholeMilliseconds
        StatisticsType.SixMonths -> last6Month.inWholeMilliseconds
        StatisticsType.OneYear -> lastYear.inWholeMilliseconds
        StatisticsType.All -> last50Year.inWholeMilliseconds
    }

    var maxStatisticsItems by rememberPreference(
        maxStatisticsItemsKey,
        MaxStatisticsItems.`10`
    )

    var totalPlayTimes = 0L
    allSongs.forEach {
        totalPlayTimes += it.durationText?.let { it1 ->
            durationTextToMillis(it1)
        }?.toLong() ?: 0
    }

    if (showStatsListeningTime) {
        LaunchedEffect(Unit) {
            Database.songsMostPlayedByPeriod(from, now).collect { allSongs = it }
        }
    }
    LaunchedEffect(Unit) {
        Database.artistsMostPlayedByPeriod(from, now, maxStatisticsItems.number.toInt()).collect { artists = it }
    }
    LaunchedEffect(Unit) {
        Database.albumsMostPlayedByPeriod(from, now, maxStatisticsItems.number.toInt()).collect { albums = it }
    }
    LaunchedEffect(Unit) {
        Database.playlistsMostPlayedByPeriod(from, now, maxStatisticsItems.number.toInt()).collect { playlists = it }
    }
    LaunchedEffect(Unit) {
        Database.songsMostPlayedByPeriod(from, now, maxStatisticsItems.number).collect { songs = it }
    }

    var downloadState by remember {
        mutableStateOf(Download.STATE_STOPPED)
    }

    val navigationBarPosition by rememberPreference(navigationBarPositionKey, NavigationBarPosition.Bottom)

    BoxWithConstraints {
        val quickPicksLazyGridItemWidthFactor = if (isLandscape && maxWidth * 0.475f >= 320.dp) {
            0.475f
        } else {
            0.9f
        }
/*
        val snapLayoutInfoProvider = remember(quickPicksLazyGridState) {
            SnapLayoutInfoProvider(
                lazyGridState = quickPicksLazyGridState,
                positionInLayout = { layoutSize, itemSize ->
                    (layoutSize * quickPicksLazyGridItemWidthFactor / 2f - itemSize / 2f)
                }
            )
        }
*/
        val itemInHorizontalGridWidth = maxWidth * quickPicksLazyGridItemWidthFactor

        Column(
            modifier = Modifier
                .background(colorPalette.background0)
                //.fillMaxSize()
                .fillMaxHeight()
                .fillMaxWidth(if (navigationBarPosition == NavigationBarPosition.Left ||
                    navigationBarPosition == NavigationBarPosition.Top ||
                    navigationBarPosition == NavigationBarPosition.Bottom) 1f
                else Dimensions.contentWidthRightBar)
                .verticalScroll(scrollState)
                /*
                .padding(
                    windowInsets
                        .only(WindowInsetsSides.Vertical)
                        .asPaddingValues()
                )

                 */
        ) {

            HeaderWithIcon(
                title = when (statisticsType) {
                    StatisticsType.Today -> stringResource(R.string.today)
                    StatisticsType.OneWeek -> stringResource(R.string._1_week)
                    StatisticsType.OneMonth -> stringResource(R.string._1_month)
                    StatisticsType.ThreeMonths -> stringResource(R.string._3_month)
                    StatisticsType.SixMonths -> stringResource(R.string._6_month)
                    StatisticsType.OneYear -> stringResource(R.string._1_year)
                    StatisticsType.All -> stringResource(R.string.all)
                },
                iconId = when (statisticsType) {
                    StatisticsType.Today -> R.drawable.stat_today
                    StatisticsType.OneWeek -> R.drawable.stat_week
                    StatisticsType.OneMonth -> R.drawable.stat_month
                    StatisticsType.ThreeMonths -> R.drawable.stat_3months
                    StatisticsType.SixMonths -> R.drawable.stat_6months
                    StatisticsType.OneYear -> R.drawable.stat_year
                    StatisticsType.All -> R.drawable.calendar_clear
                },
                enabled = true,
                showIcon = true,
                modifier = Modifier,
                onClick = {}
            )

            if (showStatsListeningTime)
            SettingsEntry(
                title = "${allSongs.size} ${stringResource(R.string.statistics_songs_heard)}",
                text = "${formatAsTime(totalPlayTimes)} ${stringResource(R.string.statistics_of_time_taken)}",
                onClick = {},
                trailingContent = {
                    Image(
                        painter = painterResource(R.drawable.musical_notes),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(colorPalette.shimmer),
                        modifier = Modifier
                            .size(34.dp)
                    )
                },
                modifier = Modifier
                    .background(
                        color = colorPalette.background4,
                        shape = thumbnailRoundness.shape()
                    )

            )

            if (allSongs.isNotEmpty())
                BasicText(
                    text = "${maxStatisticsItems} ${stringResource(R.string.most_played_songs)}",
                    style = typography.m.semiBold,
                    modifier = sectionTextModifier
                )

                LazyHorizontalGrid(
                    state = quickPicksLazyGridState,
                    rows = GridCells.Fixed(2),
                    flingBehavior = ScrollableDefaults.flingBehavior(),
                    contentPadding = endPaddingValues,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height((songThumbnailSizeDp + Dimensions.itemsVerticalPadding * 2) * 2)
                ) {

                    items(
                        count = songs.count(),
                        ) {
                        downloadState = getDownloadState(songs.get(it).asMediaItem.mediaId)
                        val isDownloaded = downloadedStateMedia(songs.get(it).asMediaItem.mediaId)
                        SongItem(
                            song = songs.get(it).asMediaItem,
                            isDownloaded = isDownloaded,
                            onDownloadClick = {
                                binder?.cache?.removeResource(songs.get(it).asMediaItem.mediaId)
                                manageDownload(
                                    context = context,
                                    songId = songs.get(it).asMediaItem.mediaId,
                                    songTitle = songs.get(it).asMediaItem.mediaMetadata.title.toString(),
                                    downloadState = isDownloaded
                                )
                            },
                            downloadState = downloadState,
                            thumbnailSizeDp = thumbnailSizeDp,
                            thumbnailSizePx = thumbnailSize,
                            modifier = Modifier
                                .combinedClickable(
                                    onLongClick = {
                                        menuState.display {

                                            //when (builtInPlaylist) {
                                            NonQueuedMediaItemMenu(
                                                navController = navController,
                                                mediaItem = songs.get(it).asMediaItem,
                                                onDismiss = menuState::hide
                                            )
                                            /*
                                                BuiltInPlaylist.Offline -> InHistoryMediaItemMenu(
                                                    song = song,
                                                    onDismiss = menuState::hide
                                                )
                                                */
                                            //}

                                        }
                                    },
                                    onClick = {
                                        binder?.stopRadio()
                                        binder?.player?.forcePlayAtIndex(
                                            songs.map(Song::asMediaItem),
                                            it
                                        )
                                    }
                                )
                                .animateItemPlacement()
                                .width(itemInHorizontalGridWidth)
                        )

                    }

                }

            if (artists.isNotEmpty())
                BasicText(
                    text = "${maxStatisticsItems} ${stringResource(R.string.most_listened_artists)}",
                    style = typography.m.semiBold,
                    modifier = sectionTextModifier
                )

            LazyRow(contentPadding = endPaddingValues) {
                items(
                    count = artists.count()
                ) {

                    if(artists[it].thumbnailUrl.toString() == "null")
                        UpdateYoutubeArtist(artists[it].id)

                    ArtistItem(
                        artist = artists[it],
                        thumbnailSizePx = artistThumbnailSizePx,
                        thumbnailSizeDp = artistThumbnailSizeDp,
                        alternative = true,
                        modifier = Modifier
                            .clickable(onClick = {
                                if (artists[it].id != "") {
                                    //onGoToArtist(artists[it].id)
                                    navController.navigate("${NavRoutes.artist.name}/${artists[it].id}")
                                }
                            })
                    )
                }
            }


            if (albums.isNotEmpty())
                BasicText(
                    text = "${maxStatisticsItems} ${stringResource(R.string.most_albums_listened)}",
                    style = typography.m.semiBold,
                    modifier = sectionTextModifier
                )

            LazyRow(contentPadding = endPaddingValues) {
                items(
                    count = albums.count()
                ) {

                    if(albums[it].thumbnailUrl.toString() == "null")
                        UpdateYoutubeAlbum(albums[it].id)

                    AlbumItem(
                        album = albums[it],
                        thumbnailSizePx = albumThumbnailSizePx,
                        thumbnailSizeDp = albumThumbnailSizeDp,
                        alternative = true,
                        modifier = Modifier
                            .clickable(onClick = {
                                if (albums[it].id != "" )
                                //onGoToAlbum(albums[it].id)
                                    navController.navigate("${NavRoutes.album.name}/${albums[it].id}")
                            })
                    )
                }
            }


            if (playlists.isNotEmpty())
                BasicText(
                    text = "${maxStatisticsItems} ${stringResource(R.string.most_played_playlists)}",
                    style = typography.m.semiBold,
                    modifier = sectionTextModifier
                )

            LazyRow(contentPadding = endPaddingValues) {
                items(
                    count = playlists.count()
                ) {

                    PlaylistItem(
                        playlist = playlists[it],
                        thumbnailSizePx = playlistThumbnailSizePx,
                        thumbnailSizeDp = playlistThumbnailSizeDp,
                        alternative = true,
                        modifier = Modifier
                            .clickable(onClick = {

                               // if (playlists[it].playlist.browseId != "" )
                                    //onGoToPlaylist(playlists[it].playlist.id)
                                navController.navigate("${NavRoutes.playlist.name}/${playlists[it].playlist.id}")
                                 //   onGoToPlaylist(
                                 //       playlists[it].playlist.browseId,
                                 //       null
                                 //   )

                            })
                    )
                }


            }


            Spacer(modifier = Modifier.height(Dimensions.bottomSpacer))

        }
    }
}
