package com.peecock.ymusic.ui.screens.album

import android.annotation.SuppressLint
import android.content.Intent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import com.valentinilk.shimmer.shimmer
import com.peecock.compose.persist.PersistMapCleanup
import com.peecock.compose.persist.persist
import com.peecock.compose.routing.RouteHandler
import com.peecock.innertube.Innertube
import com.peecock.innertube.models.bodies.BrowseBody
import com.peecock.innertube.requests.albumPage
import com.peecock.ymusic.Database
import com.peecock.ymusic.R
import com.peecock.ymusic.enums.NavigationBarPosition
import com.peecock.ymusic.enums.ThumbnailRoundness
import com.peecock.ymusic.models.Album
import com.peecock.ymusic.models.SearchQuery
import com.peecock.ymusic.models.SongAlbumMap
import com.peecock.ymusic.query
import com.peecock.ymusic.ui.components.themed.Header
import com.peecock.ymusic.ui.components.themed.HeaderIconButton
import com.peecock.ymusic.ui.components.themed.HeaderPlaceholder
import com.peecock.ymusic.ui.components.themed.IconButton
import com.peecock.ymusic.ui.components.themed.adaptiveThumbnailContent
import com.peecock.ymusic.ui.items.AlbumItem
import com.peecock.ymusic.ui.items.AlbumItemPlaceholder
import com.peecock.ymusic.ui.screens.albumRoute
import com.peecock.ymusic.ui.screens.globalRoutes
import com.peecock.ymusic.ui.screens.homeRoute
import com.peecock.ymusic.ui.screens.search.SearchScreen
import com.peecock.ymusic.ui.screens.searchResultRoute
import com.peecock.ymusic.ui.screens.searchRoute
import com.peecock.ymusic.ui.screens.searchresult.ItemsPage
import com.peecock.ymusic.ui.screens.searchresult.SearchResultScreen
import com.peecock.ymusic.ui.screens.settings.SettingsScreen
import com.peecock.ymusic.ui.screens.settingsRoute
import com.peecock.ymusic.ui.styling.LocalAppearance
import com.peecock.ymusic.ui.styling.favoritesIcon
import com.peecock.ymusic.ui.styling.px
import com.peecock.ymusic.utils.asMediaItem
import com.peecock.ymusic.utils.navigationBarPositionKey
import com.peecock.ymusic.utils.pauseSearchHistoryKey
import com.peecock.ymusic.utils.preferences
import com.peecock.ymusic.utils.rememberPreference
import com.peecock.ymusic.utils.thumbnailRoundnessKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext

@ExperimentalMaterialApi
@ExperimentalTextApi
@SuppressLint("SuspiciousIndentation", "SimpleDateFormat")
@ExperimentalFoundationApi
@ExperimentalAnimationApi
@ExperimentalComposeUiApi
@UnstableApi
@Composable
fun AlbumScreenWithoutScaffold(
    navController: NavController,
    browseId: String
) {

    //val uriHandler = LocalUriHandler.current
    //val saveableStateHolder = rememberSaveableStateHolder()

    var thumbnailRoundness by rememberPreference(
        thumbnailRoundnessKey,
        ThumbnailRoundness.Heavy
    )

    var tabIndex by rememberSaveable {
        mutableStateOf(0)
    }

    var album by persist<Album?>("album/$browseId/album")
    var albumPage by persist<Innertube.PlaylistOrAlbumPage?>("album/$browseId/albumPage")

    var showAlternativePage by remember {
        mutableStateOf(false)
    }
    var changeShape by remember {
        mutableStateOf(false)
    }
    val navigationBarPosition by rememberPreference(navigationBarPositionKey, NavigationBarPosition.Bottom)

    PersistMapCleanup(tagPrefix = "album/$browseId/")

    LaunchedEffect(Unit) {
        Database
            .album(browseId)
            .combine(snapshotFlow { tabIndex }) { album, tabIndex -> album to tabIndex }
            .collect { (currentAlbum, tabIndex) ->
                album = currentAlbum

                if (albumPage == null && currentAlbum?.timestamp == null) {
                    withContext(Dispatchers.IO) {
                        Innertube.albumPage(BrowseBody(browseId = browseId))
                            ?.onSuccess { currentAlbumPage ->
                                albumPage = currentAlbumPage

                                Database.clearAlbum(browseId)

                                Database.upsert(
                                    Album(
                                        id = browseId,
                                        title = currentAlbumPage?.title,
                                        thumbnailUrl = currentAlbumPage?.thumbnail?.url,
                                        year = currentAlbumPage?.year,
                                        authorsText = currentAlbumPage?.authors
                                            ?.joinToString("") { it.name ?: "" },
                                        shareUrl = currentAlbumPage?.url,
                                        timestamp = System.currentTimeMillis(),
                                        bookmarkedAt = album?.bookmarkedAt
                                    ),
                                    currentAlbumPage
                                        ?.songsPage
                                        ?.items
                                        ?.map(Innertube.SongItem::asMediaItem)
                                        ?.onEach(Database::insert)
                                        ?.mapIndexed { position, mediaItem ->
                                            SongAlbumMap(
                                                songId = mediaItem.mediaId,
                                                albumId = browseId,
                                                position = position
                                            )
                                        } ?: emptyList()
                                )
                            }
                    }

                }
            }
    }


    LaunchedEffect(Unit ) {
        withContext(Dispatchers.IO) {
            Innertube.albumPage(BrowseBody(browseId = browseId))
                ?.onSuccess { currentAlbumPage ->
                    albumPage = currentAlbumPage
                }
            //println("mediaItem albumPage ${albumPage?.otherVersions?.size}")
        }
    }


    RouteHandler(listenToGlobalEmitter = true) {
        globalRoutes()

        settingsRoute {
            SettingsScreen(
                navController = navController,
            )
        }

        searchResultRoute { query ->
            SearchResultScreen(
                navController = navController,
                query = query,
                onSearchAgain = {
                    searchRoute(query)
                }
            )
        }

        searchRoute { initialTextInput ->
            val context = LocalContext.current

            SearchScreen(
                navController = navController,
                initialTextInput = initialTextInput,
                onSearch = { query ->
                    pop()
                    searchResultRoute(query)

                    if (!context.preferences.getBoolean(pauseSearchHistoryKey, false)) {
                        query {
                            Database.insert(SearchQuery(query = query))
                        }
                    }
                },
                onViewPlaylist = {}, //onPlaylistUrl,
                onDismiss = { homeRoute::global }
            )
        }

        host {
            val headerContent: @Composable (textButton: (@Composable () -> Unit)?) -> Unit =
                { textButton ->
                    if (album?.timestamp == null) {
                        HeaderPlaceholder(
                            modifier = Modifier
                                .shimmer()
                        )
                    } else {
                        val (colorPalette) = LocalAppearance.current
                        val context = LocalContext.current

                        Header(title = album?.title ?: "Unknown") {

                            if (navigationBarPosition == NavigationBarPosition.Left
                                || navigationBarPosition == NavigationBarPosition.Top
                                ) {
                                IconButton(
                                    onClick = { pop() },
                                    icon = R.drawable.chevron_back,
                                    color = colorPalette.favoritesIcon,
                                    modifier = Modifier
                                        .size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                            }

                            textButton?.invoke()



                            Spacer(
                                modifier = Modifier
                                    .weight(1f)
                            )
/*
                            HeaderIconButton(
                                icon = R.drawable.image,
                                enabled = album?.thumbnailUrl?.isNotEmpty() == true,
                                color = if (album?.thumbnailUrl?.isNotEmpty() == true) colorPalette.text else colorPalette.textDisabled,
                                onClick = {
                                    if (album?.thumbnailUrl?.isNotEmpty() == true)
                                        uriHandler.openUri(album?.thumbnailUrl.toString())
                                    }
                            )
 */




                            HeaderIconButton(
                                icon = if (album?.bookmarkedAt == null) {
                                    R.drawable.bookmark_outline
                                } else {
                                    R.drawable.bookmark
                                },
                                color = colorPalette.accent,
                                onClick = {
                                    val bookmarkedAt =
                                        if (album?.bookmarkedAt == null) System.currentTimeMillis() else null

                                    query {
                                        album
                                            ?.copy(bookmarkedAt = bookmarkedAt)
                                            ?.let(Database::update)
                                    }
                                }
                            )

                            HeaderIconButton(
                                icon = R.drawable.share_social,
                                color = colorPalette.text,
                                onClick = {
                                    album?.shareUrl?.let { url ->
                                        val sendIntent = Intent().apply {
                                            action = Intent.ACTION_SEND
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_TEXT, url)
                                        }

                                        context.startActivity(
                                            Intent.createChooser(
                                                sendIntent,
                                                null
                                            )
                                        )
                                    }
                                }
                            )
                            if (navigationBarPosition == NavigationBarPosition.Right
                                || navigationBarPosition == NavigationBarPosition.Bottom
                                ) {
                                Spacer(modifier = Modifier.width(10.dp))
                                IconButton(
                                    onClick = { pop() },
                                    icon = R.drawable.chevron_back,
                                    color = colorPalette.favoritesIcon,
                                    modifier = Modifier
                                        .size(24.dp)
                                )
                            }
                        }
                    }
                }

            val thumbnailContent =
                adaptiveThumbnailContent(
                    album?.timestamp == null,
                    album?.thumbnailUrl,
                    showIcon = albumPage?.otherVersions?.isNotEmpty(),
                    onOtherVersionAvailable = {
                        showAlternativePage = !showAlternativePage
                    },
                    //shape = thumbnailRoundness.shape()
                    onClick = { changeShape = !changeShape },
                    shape = if (changeShape) CircleShape else thumbnailRoundness.shape(),
                )


                if(!showAlternativePage) {
                    AlbumSongs(
                        navController = navController,
                        browseId = browseId,
                        headerContent = headerContent,
                        thumbnailContent = thumbnailContent,
                        onSearchClick = { searchRoute("") },
                        onSettingsClick = { settingsRoute() }
                    )
                } else {
                    val thumbnailSizeDp = 108.dp
                    val thumbnailSizePx = thumbnailSizeDp.px
                    ItemsPage(
                        tag = "album/$browseId/alternatives",
                        headerContent = headerContent,
                        initialPlaceholderCount = 1,
                        continuationPlaceholderCount = 1,
                        emptyItemsText = stringResource(R.string.album_no_alternative_version),
                        itemsPageProvider = albumPage?.let {
                            ({
                                Result.success(
                                    Innertube.ItemsPage(
                                        items = albumPage?.otherVersions,
                                        continuation = null
                                    )
                                )
                            })
                        },
                        itemContent = { album ->
                            AlbumItem(
                                album = album,
                                thumbnailSizePx = thumbnailSizePx,
                                thumbnailSizeDp = thumbnailSizeDp,
                                modifier = Modifier
                                    .clickable { albumRoute(album.key) }
                            )
                        },
                        itemPlaceholderContent = {
                            AlbumItemPlaceholder(thumbnailSizeDp = thumbnailSizeDp)
                        }
                    )

                }
        }

    }

}
