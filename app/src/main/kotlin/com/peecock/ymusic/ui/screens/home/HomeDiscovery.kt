package com.peecock.ymusic.ui.screens.home

import android.annotation.SuppressLint
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.layout.requiredWidth
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import com.peecock.compose.persist.persist
import com.peecock.compose.persist.persistList
import com.peecock.innertube.Innertube
import com.peecock.innertube.requests.discoverPage
import com.peecock.ymusic.Database
import com.peecock.ymusic.LocalPlayerAwareWindowInsets
import com.peecock.ymusic.R
import com.peecock.ymusic.enums.NavigationBarPosition
import com.peecock.ymusic.enums.ThumbnailRoundness
import com.peecock.ymusic.enums.UiType
import com.peecock.ymusic.models.Artist
import com.peecock.ymusic.ui.components.ShimmerHost
import com.peecock.ymusic.ui.components.themed.FloatingActionsContainerWithScrollToTop
import com.peecock.ymusic.ui.components.themed.HeaderWithIcon
import com.peecock.ymusic.ui.components.themed.TextPlaceholder
import com.peecock.ymusic.ui.items.AlbumItem
import com.peecock.ymusic.ui.items.AlbumItemPlaceholder
import com.peecock.ymusic.ui.styling.Dimensions
import com.peecock.ymusic.ui.styling.LocalAppearance
import com.peecock.ymusic.ui.styling.px
import com.peecock.ymusic.ui.styling.shimmer
import com.peecock.ymusic.utils.UiTypeKey
import com.peecock.ymusic.utils.center
import com.peecock.ymusic.utils.isLandscape
import com.peecock.ymusic.utils.navigationBarPositionKey
import com.peecock.ymusic.utils.rememberPreference
import com.peecock.ymusic.utils.secondary
import com.peecock.ymusic.utils.semiBold
import com.peecock.ymusic.utils.showSearchTabKey
import com.peecock.ymusic.utils.thumbnailRoundnessKey

@ExperimentalMaterialApi
@SuppressLint("SuspiciousIndentation")
@UnstableApi
@OptIn(ExperimentalAnimationApi::class, ExperimentalFoundationApi::class)
@Composable
fun HomeDiscovery(
    onMoodClick: (mood: Innertube.Mood.Item) -> Unit,
    onNewReleaseAlbumClick: (String) -> Unit,
    onSearchClick: () -> Unit
) {
    //val coroutineScope = CoroutineScope(Dispatchers.IO)

    val (colorPalette, typography) = LocalAppearance.current
    val windowInsets = LocalPlayerAwareWindowInsets.current
    val uiType  by rememberPreference(UiTypeKey, UiType.RiMusic)

    val scrollState = rememberScrollState()
    val lazyGridState = rememberLazyGridState()

    val endPaddingValues = windowInsets.only(WindowInsetsSides.End).asPaddingValues()

    val sectionTextModifier = Modifier
        .padding(horizontal = 16.dp)
        .padding(top = 24.dp, bottom = 8.dp)
        .padding(endPaddingValues)

    val thumbnailDp = Dimensions.thumbnails.album
    val thumbnailPx = thumbnailDp.px
    val thumbnailSizeDp = 20.dp
    //val thumbnailSizePx = thumbnailSizeDp.px

    var discoverPage by persist<Result<Innertube.DiscoverPage>>("home/discovery")

    var preferitesArtists by persistList<Artist>("home/artists")

    LaunchedEffect(key1 = Unit) {
        discoverPage = Innertube.discoverPage()
    }
    LaunchedEffect(Unit) {
        Database.preferitesArtistsByName().collect { preferitesArtists = it }
    }

    val navigationBarPosition by rememberPreference(navigationBarPositionKey, NavigationBarPosition.Bottom)

    val showSearchTab by rememberPreference(showSearchTabKey, false)

    //Log.d("mediaItemArtists",preferitesArtists.toString())

    BoxWithConstraints {

        val moodItemWidthFactor = if (isLandscape && maxWidth * 0.475f >= 320.dp) 0.475f else 0.9f
        /*
        val snapLayoutInfoProvider = rememberSnapLayoutInfoProvider(
            lazyGridState = lazyGridState,
            positionInLayout = { layoutSize, itemSize ->
                layoutSize * moodItemWidthFactor / 2f - itemSize / 2f
            }
        )
         */
        val itemWidth = maxWidth * moodItemWidthFactor

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
                .padding(
                    windowInsets
                        .only(WindowInsetsSides.Vertical)
                        .asPaddingValues()
                )
        ) {
            //Header(title = "Discover", modifier = Modifier.padding(endPaddingValues))
            HeaderWithIcon(
                title = stringResource(R.string.discovery),
                iconId = R.drawable.search,
                enabled = true,
                showIcon = !showSearchTab,
                modifier = Modifier,
                onClick = onSearchClick
            )
            discoverPage?.getOrNull()?.let { page ->
                var newReleaseAlbumsFiltered by persistList<Innertube.AlbumItem>("discovery/newalbumsartist")
                page.newReleaseAlbums.forEach { album ->
                    preferitesArtists.forEach { artist ->
                        if (artist.name == album.authors?.first()?.name) {
                            newReleaseAlbumsFiltered += album
                            //Log.d("mediaItem","artst ok")
                        }
                    }
                }

                 if ( newReleaseAlbumsFiltered.distinct().isNotEmpty() && preferitesArtists.isNotEmpty() ) {
                    BasicText(
                        text = stringResource(R.string.new_albums_of_your_artists),
                        style = typography.m.semiBold,
                        modifier = sectionTextModifier
                    )

                    LazyRow(contentPadding = endPaddingValues) {
                        items(items = newReleaseAlbumsFiltered.distinct(), key = { it.key }) {
                              //preferitesArtists.forEach { artist ->
                              //      if (artist.name == it.authors?.first()?.name)
                                        AlbumItem(
                                            album = it,
                                            thumbnailSizePx = thumbnailPx,
                                            thumbnailSizeDp = thumbnailDp,
                                            alternative = true,
                                            modifier = Modifier.clickable(onClick = {
                                                onNewReleaseAlbumClick(
                                                    it.key
                                                )
                                            })
                                        )
                               // }

                        }
                    }

                }

                if (page.newReleaseAlbums.isNotEmpty()) {
                    BasicText(
                        text = stringResource(R.string.new_albums),
                        style = typography.m.semiBold,
                        modifier = sectionTextModifier
                    )

                    LazyRow(contentPadding = endPaddingValues) {
                        items(items = page.newReleaseAlbums, key = { it.key }) {
                            AlbumItem(
                                album = it,
                                thumbnailSizePx = thumbnailPx,
                                thumbnailSizeDp = thumbnailDp,
                                alternative = true,
                                modifier = Modifier.clickable(onClick = { onNewReleaseAlbumClick(it.key) })
                            )
                        }
                    }

                }



                if (page.moods.isNotEmpty()) {
                    BasicText(
                        text = stringResource(R.string.moods_and_genres),
                        style = typography.m.semiBold,
                        modifier = sectionTextModifier
                    )

                    LazyHorizontalGrid(
                        state = lazyGridState,
                        rows = GridCells.Fixed(20),
                        flingBehavior = ScrollableDefaults.flingBehavior(),
                        //flingBehavior = rememberSnapFlingBehavior(snapLayoutInfoProvider),
                        contentPadding = endPaddingValues,
                        modifier = Modifier
                            .fillMaxWidth()
                            //.height((thumbnailSizeDp + Dimensions.itemsVerticalPadding * 8) * 8)
                            .height(Dimensions.itemsVerticalPadding * 16 * 8)
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

            } ?: discoverPage?.exceptionOrNull()?.let {
                BasicText(
                    text = stringResource(R.string.an_error_has_occurred),
                    style = typography.s.secondary.center,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(all = 16.dp)
                )
            } ?: ShimmerHost {
                TextPlaceholder(modifier = sectionTextModifier)
                Row {
                    repeat(2) {
                        AlbumItemPlaceholder(
                            thumbnailSizeDp = thumbnailDp,
                            alternative = true
                        )
                    }
                TextPlaceholder(modifier = sectionTextModifier)
                LazyHorizontalGrid(
                    state = lazyGridState,
                    rows = GridCells.Fixed(4),
                    flingBehavior = ScrollableDefaults.flingBehavior(),
                    //flingBehavior = rememberSnapFlingBehavior(snapLayoutInfoProvider),
                    contentPadding = endPaddingValues,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height((4 * (64 + 4)).dp)
                ) {
                    items(16) {
                        MoodItemPlaceholder(
                            //width = 92.dp, //itemWidth,
                            width = itemWidth,
                            modifier = Modifier.padding(4.dp)
                        )
                    }
                }

                }
            }
        }

        if(uiType == UiType.ViMusic)
        FloatingActionsContainerWithScrollToTop(
            scrollState = scrollState,
            iconId = R.drawable.search,
            onClick = onSearchClick
        )


    }
}

@Composable
fun MoodItemColored(
    mood: Innertube.Mood.Item,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (colorPalette, typography) = LocalAppearance.current
    var thumbnailRoundness by rememberPreference(
        thumbnailRoundnessKey,
        ThumbnailRoundness.Heavy
    )

    val moodColor by remember { derivedStateOf { Color(mood.stripeColor) } }

    Column (
        verticalArrangement = Arrangement.SpaceAround,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(5.dp)
            .clip(thumbnailRoundness.shape())
            .clickable { onClick() }

    ) {
        Row(
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .background(color = moodColor)
                .padding(start = 10.dp)
                .fillMaxHeight(0.9f)
        ) {
            Box(
                modifier = Modifier
                    .requiredWidth(150.dp)
                    .background(color = colorPalette.background4)
                    .fillMaxSize()
            ) {

                BasicText(
                    text = mood.title,
                    style = TextStyle(
                        color = colorPalette.text,
                        fontStyle = typography.xs.semiBold.fontStyle,
                        fontWeight = typography.xs.semiBold.fontWeight
                    ),
                    modifier = Modifier.padding(horizontal = 10.dp).align(Alignment.CenterStart),
                    maxLines = 2,

                    )
            }
        }
    }
}

@Composable
fun MoodGridItemColored(
    mood: Innertube.Mood.Item,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    thumbnailSizeDp: Dp
) {
    val (colorPalette, typography) = LocalAppearance.current
    var thumbnailRoundness by rememberPreference(
        thumbnailRoundnessKey,
        ThumbnailRoundness.Heavy
    )

    val moodColor by remember { derivedStateOf { Color(mood.stripeColor) } }

    Column (
        verticalArrangement = Arrangement.SpaceAround,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .size(thumbnailSizeDp,thumbnailSizeDp)
            .padding(5.dp)
            .clickable { onClick() }

    ) {
        Row(
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxSize(0.9f)
                .clip(thumbnailRoundness.shape())

        ) {
            Row(
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(color = moodColor)
                    .padding(start = 10.dp)
                    .fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .background(color = colorPalette.background4)
                        .fillMaxSize()
                ) {

                    BasicText(
                        text = mood.title,
                        style = TextStyle(
                            color = colorPalette.text,
                            fontStyle = typography.xs.semiBold.fontStyle,
                            fontWeight = typography.xs.semiBold.fontWeight
                        ),
                        modifier = Modifier.padding(horizontal = 10.dp)
                            .align(Alignment.CenterStart),
                        maxLines = 2,
                    )
                }
            }
        }
    }
}

@Composable
fun MoodItem(
    mood: Innertube.Mood.Item,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (colorPalette, typography) = LocalAppearance.current
    var thumbnailRoundness by rememberPreference(
        thumbnailRoundnessKey,
        ThumbnailRoundness.Heavy
    )


    Column (
        verticalArrangement = Arrangement.SpaceAround,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(5.dp)
            .clip(thumbnailRoundness.shape())
            .clickable { onClick() }

    ) {
        Box(
            modifier = Modifier
                .requiredWidth(150.dp)
                .background(color = colorPalette.background4, shape = thumbnailRoundness.shape())
                .fillMaxWidth(0.9f)
                .padding(all = 10.dp)
        ){

        BasicText(
            text = mood.title,
            style =  TextStyle(
                color = colorPalette.text,
                fontStyle = typography.xs.semiBold.fontStyle,
                fontWeight = typography.xs.semiBold.fontWeight
            ), //typography.xs.semiBold,
            modifier = Modifier.padding(start = 4.dp),
            maxLines = 1,

        )
        }
    }
}

@Composable
fun MoodGridItem(
    mood: Innertube.Mood.Item,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    thumbnailSizeDp: Dp
) {
    val (colorPalette, typography) = LocalAppearance.current
    var thumbnailRoundness by rememberPreference(
        thumbnailRoundnessKey,
        ThumbnailRoundness.Heavy
    )


    Column (
        verticalArrangement = Arrangement.SpaceAround,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .size(thumbnailSizeDp,thumbnailSizeDp)
            //.background(colorPalette.background1)
            .clip(thumbnailRoundness.shape())
            .clickable { onClick() }

    ) {
        Box(
            modifier = Modifier
                .background(color = colorPalette.background4, shape = thumbnailRoundness.shape())
                .fillMaxSize(0.9f)
                .padding(horizontal = 10.dp)
                .padding(vertical = 50.dp)
        ) {
            BasicText(
                text = mood.title,
                style = TextStyle(
                    color = colorPalette.text,
                    fontStyle = typography.xxl.semiBold.fontStyle,
                    fontWeight = typography.xxl.semiBold.fontWeight,
                    fontFamily = typography.xxl.fontFamily,
                    textAlign = TextAlign.Start
                ),
                modifier = modifier.padding(start = 4.dp),
                maxLines = 1
            )
        }
    }
}

@Composable
fun MoodItemPlaceholder(
    width: Dp,
    modifier: Modifier = Modifier
) {
    val (colorPalette, typography) = LocalAppearance.current
    Spacer(
        modifier
            .background(color = colorPalette.shimmer)
            .size(width, 64.dp)
    )
}
