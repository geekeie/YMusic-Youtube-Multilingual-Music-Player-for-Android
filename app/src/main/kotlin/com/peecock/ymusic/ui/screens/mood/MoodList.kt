package com.peecock.ymusic.ui.screens.mood

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.valentinilk.shimmer.shimmer
import com.peecock.compose.persist.persist
import com.peecock.innertube.Innertube
import com.peecock.innertube.models.bodies.BrowseBodyWithLocale
import com.peecock.innertube.requests.BrowseResult
import com.peecock.innertube.requests.browse
import com.peecock.ymusic.LocalPlayerAwareWindowInsets
import com.peecock.ymusic.R
import com.peecock.ymusic.enums.NavRoutes
import com.peecock.ymusic.enums.NavigationBarPosition
import com.peecock.ymusic.models.Mood
import com.peecock.ymusic.ui.components.ShimmerHost
import com.peecock.ymusic.ui.components.themed.HeaderPlaceholder
import com.peecock.ymusic.ui.components.themed.HeaderWithIcon
import com.peecock.ymusic.ui.components.themed.TextPlaceholder
import com.peecock.ymusic.ui.items.AlbumItem
import com.peecock.ymusic.ui.items.AlbumItemPlaceholder
import com.peecock.ymusic.ui.items.ArtistItem
import com.peecock.ymusic.ui.items.PlaylistItem
import com.peecock.ymusic.ui.styling.Dimensions
import com.peecock.ymusic.ui.styling.LocalAppearance
import com.peecock.ymusic.ui.styling.px
import com.peecock.ymusic.utils.center
import com.peecock.ymusic.utils.navigationBarPositionKey
import com.peecock.ymusic.utils.rememberPreference
import com.peecock.ymusic.utils.secondary
import com.peecock.ymusic.utils.semiBold

internal const val defaultBrowseId = "FEmusic_moods_and_genres_category"

@ExperimentalFoundationApi
@ExperimentalAnimationApi
@Composable
fun MoodList(
    navController: NavController,
    mood: Mood
) {
    val (colorPalette, typography) = LocalAppearance.current
    val windowInsets = LocalPlayerAwareWindowInsets.current

    val browseId = mood.browseId ?: defaultBrowseId
    var moodPage by persist<Result<BrowseResult>>("playlist/$browseId${mood.params?.let { "/$it" } ?: ""}")

    LaunchedEffect(Unit) {
        moodPage = Innertube.browse(BrowseBodyWithLocale(browseId = browseId, params = mood.params))
    }

    val thumbnailSizeDp = Dimensions.thumbnails.album
    val thumbnailSizePx = thumbnailSizeDp.px

    val lazyListState = rememberLazyListState()

    val endPaddingValues = windowInsets.only(WindowInsetsSides.End).asPaddingValues()

    val sectionTextModifier = Modifier
        .padding(horizontal = 16.dp)
        .padding(top = 24.dp, bottom = 8.dp)
        .padding(endPaddingValues)

    val navigationBarPosition by rememberPreference(navigationBarPositionKey, NavigationBarPosition.Bottom)

    Column (
        modifier = Modifier
            .background(colorPalette.background0)
            //.fillMaxSize()
            .fillMaxHeight()
            .fillMaxWidth(if (navigationBarPosition == NavigationBarPosition.Left ||
                navigationBarPosition == NavigationBarPosition.Top ||
                navigationBarPosition == NavigationBarPosition.Bottom) 1f
            else Dimensions.contentWidthRightBar)
    ) {
        moodPage?.getOrNull()?.let { moodResult ->
            LazyColumn(
                state = lazyListState,
                //contentPadding = LocalPlayerAwareWindowInsets.current
                //    .only(WindowInsetsSides.Vertical + WindowInsetsSides.End).asPaddingValues(),
                modifier = Modifier
                    .background(colorPalette.background0)
                    .fillMaxSize()
            ) {
                item(
                    key = "header",
                    contentType = 0
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        HeaderWithIcon(
                            title = mood.name,
                            iconId = R.drawable.globe,
                            enabled = true,
                            showIcon = true,
                            modifier = Modifier,
                            onClick = {}
                        )
                    }
                }

                moodResult.items.forEach { item ->
                    item {
                        BasicText(
                            text = item.title,
                            style = typography.m.semiBold,
                            modifier = sectionTextModifier
                        )
                    }
                    item {
                        LazyRow {
                            items(items = item.items, key = { it.key }) { childItem ->
                                if (childItem.key == defaultBrowseId) return@items
                                when (childItem) {
                                    is Innertube.AlbumItem -> AlbumItem(
                                        album = childItem,
                                        thumbnailSizePx = thumbnailSizePx,
                                        thumbnailSizeDp = thumbnailSizeDp,
                                        alternative = true,
                                        modifier = Modifier.clickable {
                                            childItem.info?.endpoint?.browseId?.let {
                                                //albumRoute.global(it)
                                                navController.navigate(route = "${NavRoutes.album.name}/$it")
                                            }
                                        }
                                    )

                                    is Innertube.ArtistItem -> ArtistItem(
                                        artist = childItem,
                                        thumbnailSizePx = thumbnailSizePx,
                                        thumbnailSizeDp = thumbnailSizeDp,
                                        alternative = true,
                                        modifier = Modifier.clickable {
                                            childItem.info?.endpoint?.browseId?.let {
                                                //artistRoute.global(it)
                                                navController.navigate(route = "${NavRoutes.artist.name}/$it")
                                            }
                                        }
                                    )

                                    is Innertube.PlaylistItem -> PlaylistItem(
                                        playlist = childItem,
                                        thumbnailSizePx = thumbnailSizePx,
                                        thumbnailSizeDp = thumbnailSizeDp,
                                        alternative = true,
                                        modifier = Modifier.clickable {
                                            childItem.info?.endpoint?.let { endpoint ->
                                                /*
                                                playlistRoute.global(
                                                    p0 = endpoint.browseId,
                                                    p1 = endpoint.params,
                                                    p2 = childItem.songCount?.let { it / 100 }
                                                )
                                                 */
                                                navController.navigate(route = "${NavRoutes.playlist.name}/${endpoint.browseId}")
                                            }
                                            /*
                                            childItem.info?.endpoint?.browseId?.let {
                                                playlistRoute.global(
                                                    it,
                                                    null

                                                )
                                            }
                                             */
                                        }
                                    )

                                    else -> {}
                                }
                            }
                        }
                    }
                }

                item(key = "bottom") {
                    Spacer(modifier = Modifier.height(Dimensions.bottomSpacer))
                }

            }
        } ?: moodPage?.exceptionOrNull()?.let {
            BasicText(
                text = stringResource(R.string.page_not_been_loaded),
                style = typography.s.secondary.center,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(all = 16.dp)
            )
        } ?: ShimmerHost {
            HeaderPlaceholder(modifier = Modifier.shimmer())
            repeat(4) {
                TextPlaceholder(modifier = sectionTextModifier)
                Row {
                    repeat(6) {
                        AlbumItemPlaceholder(
                            thumbnailSizeDp = thumbnailSizeDp,
                            alternative = true
                        )
                    }
                }
            }
        }
    }
}
