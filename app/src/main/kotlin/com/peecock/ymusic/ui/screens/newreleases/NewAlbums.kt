package com.peecock.ymusic.ui.screens.newreleases

import android.annotation.SuppressLint
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import com.peecock.compose.persist.persist
import com.peecock.innertube.Innertube
import com.peecock.innertube.requests.discoverPage
import com.peecock.ymusic.R
import com.peecock.ymusic.enums.NavRoutes
import com.peecock.ymusic.enums.NavigationBarPosition
import com.peecock.ymusic.ui.components.themed.HeaderWithIcon
import com.peecock.ymusic.ui.items.AlbumItem
import com.peecock.ymusic.ui.styling.Dimensions
import com.peecock.ymusic.ui.styling.LocalAppearance
import com.peecock.ymusic.ui.styling.px
import com.peecock.ymusic.utils.navigationBarPositionKey
import com.peecock.ymusic.utils.rememberPreference
import com.peecock.ymusic.utils.showSearchTabKey

@ExperimentalTextApi
@UnstableApi
@SuppressLint("SuspiciousIndentation")
@ExperimentalFoundationApi
@ExperimentalAnimationApi
@Composable
fun NewAlbums(
    navController: NavController
) {
    val (colorPalette, typography) = LocalAppearance.current

    var discoverPage by persist<Result<Innertube.DiscoverPage>>("home/discoveryAlbums")
    LaunchedEffect(Unit) {
        discoverPage = Innertube.discoverPage()
    }

    val thumbnailSizeDp = Dimensions.thumbnails.album + 24.dp
    val thumbnailSizePx = thumbnailSizeDp.px

    val navigationBarPosition by rememberPreference(
        navigationBarPositionKey,
        NavigationBarPosition.Bottom
    )

    val showSearchTab by rememberPreference(showSearchTabKey, false)

    val lazyGridState = rememberLazyGridState()


    Column(
        modifier = Modifier
            .background(colorPalette.background0)
            //.fillMaxSize()
            .fillMaxHeight()
            .fillMaxWidth(
                if (navigationBarPosition == NavigationBarPosition.Left ||
                    navigationBarPosition == NavigationBarPosition.Top ||
                    navigationBarPosition == NavigationBarPosition.Bottom
                ) 1f
                else Dimensions.contentWidthRightBar
            )
    ) {

        /***************/
        discoverPage?.getOrNull()?.let { page ->
            LazyVerticalGrid(
                state = lazyGridState,
                columns = GridCells.Adaptive(Dimensions.thumbnails.album + 24.dp),
                //contentPadding = LocalPlayerAwareWindowInsets.current.asPaddingValues(),
                modifier = Modifier
                    .background(colorPalette.background0)
                //.fillMaxSize()
            ) {
                item(
                    key = "header",
                    contentType = 0,
                    span = { GridItemSpan(maxLineSpan) }
                ) {
                    HeaderWithIcon(
                        title = stringResource(R.string.new_albums),
                        iconId = R.drawable.search,
                        enabled = true,
                        showIcon = !showSearchTab,
                        modifier = Modifier,
                        onClick = {}
                    )

                }

                items(
                    items = page.newReleaseAlbums.distinct(),
                    key = { it.key }) {
                    AlbumItem(
                        album = it,
                        thumbnailSizePx = thumbnailSizePx,
                        thumbnailSizeDp = thumbnailSizeDp,
                        alternative = true,
                        modifier = Modifier.clickable(onClick = {
                            navController.navigate(route = "${NavRoutes.album.name}/${it.key}")
                        })
                    )
                }
                item(
                    key = "footer",
                    contentType = 0,
                ) {
                    Spacer(modifier = Modifier.height(Dimensions.bottomSpacer))
                }
            }

        }
        /***************/


    }

}
