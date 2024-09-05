package com.peecock.ymusic.ui.screens.album

import android.annotation.SuppressLint
import android.content.Intent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
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
import com.peecock.ymusic.enums.NavRoutes
import com.peecock.ymusic.enums.ThumbnailRoundness
import com.peecock.ymusic.enums.UiType
import com.peecock.ymusic.models.Album
import com.peecock.ymusic.models.SongAlbumMap
import com.peecock.ymusic.query
import com.peecock.ymusic.ui.components.themed.Header
import com.peecock.ymusic.ui.components.themed.HeaderIconButton
import com.peecock.ymusic.ui.components.themed.HeaderPlaceholder
import com.peecock.ymusic.ui.components.Scaffold
import com.peecock.ymusic.ui.components.themed.adaptiveThumbnailContent
import com.peecock.ymusic.ui.items.AlbumItem
import com.peecock.ymusic.ui.items.AlbumItemPlaceholder
import com.peecock.ymusic.ui.screens.globalRoutes
import com.peecock.ymusic.ui.screens.home.MODIFIED_PREFIX
import com.peecock.ymusic.ui.screens.searchresult.ItemsPage
import com.peecock.ymusic.ui.styling.LocalAppearance
import com.peecock.ymusic.ui.styling.px
import com.peecock.ymusic.utils.UiTypeKey
import com.peecock.ymusic.utils.asMediaItem
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
fun AlbumScreen(
    navController: NavController,
    browseId: String,
    playerEssential: @Composable () -> Unit = {},
) {

    //val uriHandler = LocalUriHandler.current
    val saveableStateHolder = rememberSaveableStateHolder()

    var tabIndex by rememberSaveable {
        mutableStateOf(0)
    }
    val thumbnailRoundness by rememberPreference(
        thumbnailRoundnessKey,
        ThumbnailRoundness.Heavy
    )
    var changeShape by remember {
        mutableStateOf(false)
    }

    var album by persist<Album?>("album/$browseId/album")
    var albumPage by persist<Innertube.PlaylistOrAlbumPage?>("album/$browseId/albumPage")


    PersistMapCleanup(tagPrefix = "album/$browseId/")


    LaunchedEffect(Unit) {
        Database
            .album(browseId)
            .combine(snapshotFlow { tabIndex }) { album, tabIndex -> album to tabIndex }
            .collect { (currentAlbum,
                          // tabIndex
            ) ->
                album = currentAlbum

                if (albumPage == null
                    //&& (currentAlbum?.timestamp == null || tabIndex == 1)
                    ) {

                    withContext(Dispatchers.IO) {
                        Innertube.albumPage(BrowseBody(browseId = browseId))
                            ?.onSuccess { currentAlbumPage ->
                                albumPage = currentAlbumPage

                                Database.upsert(
                                    Album(
                                        id = browseId,
                                        title = if (album?.title?.startsWith(MODIFIED_PREFIX) == true) album?.title else currentAlbumPage?.title,
                                        thumbnailUrl = if (album?.thumbnailUrl?.startsWith(MODIFIED_PREFIX) == true) album?.thumbnailUrl else currentAlbumPage?.thumbnail?.url,
                                        year = currentAlbumPage?.year,
                                        authorsText = if (album?.authorsText?.startsWith(MODIFIED_PREFIX) == true) album?.authorsText else currentAlbumPage?.authors
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
                            /*
                            ?.onFailure {
                                println("mediaItem error home artist ${it.message}")
                            }
                             */
                    }

                }
            }
    }

    /*
    LaunchedEffect(Unit ) {
        withContext(Dispatchers.IO) {
            Innertube.albumPage(BrowseBody(browseId = browseId))
                ?.onSuccess { currentAlbumPage ->
                    albumPage = currentAlbumPage
                }
            //println("mediaItem home albumscreen albumPage des ${albumPage?.description} albumPage ${albumPage?.otherVersions?.size}")
            //println("mediaItem home albumscreen albumPage songPage ${albumPage?.songsPage}")
        }
    }

     */


    RouteHandler(listenToGlobalEmitter = true) {
        globalRoutes()

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

                        Header(
                            //title = album?.title ?: "Unknown"
                            title = "",
                            modifier = Modifier.padding(horizontal = 12.dp)
                        ) {
                            textButton?.invoke()


                            Spacer(
                                modifier = Modifier
                                    .weight(1f)
                            )

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
                        }
                    }
                }

            val thumbnailContent =
                adaptiveThumbnailContent(
                    album?.timestamp == null,
                    album?.thumbnailUrl,
                    showIcon = false, //albumPage?.otherVersions?.isNotEmpty(),
                    onOtherVersionAvailable = {
                        //println("mediaItem Click other version")
                    },
                    //shape = thumbnailRoundness.shape()
                    onClick = { changeShape = !changeShape },
                    shape = if (changeShape) CircleShape else thumbnailRoundness.shape(),
                )

            val uiType  by rememberPreference(UiTypeKey, UiType.RiMusic)

            Scaffold(
                navController = navController,
                playerEssential = playerEssential,
                topIconButtonId = R.drawable.chevron_back,
                showButton1 = if(uiType == UiType.RiMusic) false else true,
                onTopIconButtonClick = pop,
                topIconButton2Id = R.drawable.chevron_back,
                onTopIconButton2Click = pop,
                showButton2 = false,
                tabIndex = tabIndex,
                onTabChanged = { tabIndex = it },
                onHomeClick = {
                    //homeRoute()
                    navController.navigate(NavRoutes.home.name)
                },
                tabColumnContent = { Item ->
                    Item(0,
                        stringResource(R.string.album_and_alternative_versions), R.drawable.album)
                    //Item(1, stringResource(R.string.other_versions), R.drawable.alternative_version)
                }
            ) { currentTabIndex ->
                saveableStateHolder.SaveableStateProvider(key = currentTabIndex) {
                    when (currentTabIndex) {
                        0 -> AlbumDetailsModern(
                            navController = navController,
                            browseId = browseId,
                            albumPage = albumPage,
                            headerContent = headerContent,
                            thumbnailContent = thumbnailContent,
                            onSearchClick = {
                                //searchRoute("")
                                navController.navigate(NavRoutes.search.name)
                            },
                            onSettingsClick = {
                                //settingsRoute()
                                navController.navigate(NavRoutes.settings.name)
                            }
                        )

                        1 -> {
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
                                            .clickable {
                                                //albumRoute(album.key)
                                                navController.navigate(route = "${NavRoutes.album.name}/${album.key}")
                                            }
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
        }
    }
}
