package com.peecock.ymusic.ui.screens.mood

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
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
import com.peecock.innertube.requests.discoverPage
import com.peecock.ymusic.LocalPlayerAwareWindowInsets
import com.peecock.ymusic.R
import com.peecock.ymusic.enums.NavRoutes
import com.peecock.ymusic.enums.NavigationBarPosition
import com.peecock.ymusic.models.toUiMood
import com.peecock.ymusic.ui.components.ShimmerHost
import com.peecock.ymusic.ui.components.themed.HeaderPlaceholder
import com.peecock.ymusic.ui.components.themed.HeaderWithIcon
import com.peecock.ymusic.ui.components.themed.TextPlaceholder
import com.peecock.ymusic.ui.items.AlbumItemPlaceholder
import com.peecock.ymusic.ui.screens.home.MoodGridItemColored
import com.peecock.ymusic.ui.styling.Dimensions
import com.peecock.ymusic.ui.styling.LocalAppearance
import com.peecock.ymusic.ui.styling.px
import com.peecock.ymusic.utils.center
import com.peecock.ymusic.utils.navigationBarPositionKey
import com.peecock.ymusic.utils.rememberPreference
import com.peecock.ymusic.utils.secondary

@ExperimentalFoundationApi
@ExperimentalAnimationApi
@Composable
fun MoodsPage(
    navController: NavController
) {
    val (colorPalette, typography) = LocalAppearance.current
    val windowInsets = LocalPlayerAwareWindowInsets.current

    var discoverPage by persist<Result<Innertube.DiscoverPage>>("home/discoveryMoods")
    LaunchedEffect(Unit) {
        discoverPage = Innertube.discoverPage()
    }
    val thumbnailSizeDp = Dimensions.thumbnails.album + 24.dp
    val thumbnailSizePx = thumbnailSizeDp.px


    val moodAngGenresLazyGridState = rememberLazyGridState()

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
        discoverPage?.getOrNull()?.let { moodResult ->
            LazyVerticalGrid(
                state = moodAngGenresLazyGridState,
                columns = GridCells.Adaptive(Dimensions.thumbnails.album + 24.dp),
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
                        title = stringResource(R.string.moods_and_genres),
                        iconId = R.drawable.search,
                        enabled = true,
                        showIcon = false,
                        modifier = Modifier,
                        onClick = {}
                    )
                }

                discoverPage?.getOrNull()?.let { page ->
                    if (page.moods.isNotEmpty()) {

                            items(
                                items = page.moods.sortedBy { it.title },
                                key = { it.endpoint.params ?: it.title }
                            ) {
                                MoodGridItemColored(
                                    mood = it,
                                    onClick = { it.endpoint.browseId?.let { _ ->
                                        navController.currentBackStackEntry?.savedStateHandle?.set("mood", it.toUiMood())
                                        navController.navigate(NavRoutes.mood.name)
                                    } },
                                    thumbnailSizeDp = thumbnailSizeDp,
                                    modifier = Modifier
                                        .animateItemPlacement()

                                )
                            }
                        }

                    }

                item(key = "bottom") {
                    Spacer(modifier = Modifier.height(Dimensions.bottomSpacer))
                }

                }


        } ?: discoverPage?.exceptionOrNull()?.let {
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
