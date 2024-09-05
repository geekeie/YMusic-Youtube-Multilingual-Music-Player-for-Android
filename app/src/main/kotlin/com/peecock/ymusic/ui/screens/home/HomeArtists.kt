package com.peecock.ymusic.ui.screens.home


import android.annotation.SuppressLint
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.peecock.compose.persist.persistList
import com.peecock.ymusic.Database
import com.peecock.ymusic.R
import com.peecock.ymusic.enums.ArtistSortBy
import com.peecock.ymusic.enums.LibraryItemSize
import com.peecock.ymusic.enums.NavigationBarPosition
import com.peecock.ymusic.enums.SortOrder
import com.peecock.ymusic.enums.UiType
import com.peecock.ymusic.models.Artist
import com.peecock.ymusic.ui.components.LocalMenuState
import com.peecock.ymusic.ui.components.themed.FloatingActionsContainerWithScrollToTop
import com.peecock.ymusic.ui.components.themed.HeaderIconButton
import com.peecock.ymusic.ui.components.themed.HeaderInfo
import com.peecock.ymusic.ui.components.themed.HeaderWithIcon
import com.peecock.ymusic.ui.components.themed.Menu
import com.peecock.ymusic.ui.components.themed.MenuEntry
import com.peecock.ymusic.ui.components.themed.MultiFloatingActionsContainer
import com.peecock.ymusic.ui.components.themed.SortMenu
import com.peecock.ymusic.ui.items.ArtistItem
import com.peecock.ymusic.ui.styling.Dimensions
import com.peecock.ymusic.ui.styling.LocalAppearance
import com.peecock.ymusic.ui.styling.px
import com.peecock.ymusic.utils.UiTypeKey
import com.peecock.ymusic.utils.artistSortByKey
import com.peecock.ymusic.utils.artistSortOrderKey
import com.peecock.ymusic.utils.artistsItemSizeKey
import com.peecock.ymusic.utils.navigationBarPositionKey
import com.peecock.ymusic.utils.rememberPreference
import com.peecock.ymusic.utils.semiBold
import com.peecock.ymusic.utils.showFloatingIconKey
import com.peecock.ymusic.utils.showSearchTabKey
import kotlin.random.Random

@ExperimentalMaterialApi
@SuppressLint("SuspiciousIndentation")
@ExperimentalFoundationApi
@ExperimentalAnimationApi
@ExperimentalComposeUiApi
@Composable
fun HomeArtists(
    onArtistClick: (Artist) -> Unit,
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val (colorPalette, typography) = LocalAppearance.current
    val menuState = LocalMenuState.current
    var sortBy by rememberPreference(artistSortByKey, ArtistSortBy.DateAdded)
    var sortOrder by rememberPreference(artistSortOrderKey, SortOrder.Descending)
    val uiType  by rememberPreference(UiTypeKey, UiType.RiMusic)

    var items by persistList<Artist>("home/artists")

    LaunchedEffect(sortBy, sortOrder) {
        Database.artists(sortBy, sortOrder).collect { items = it }
    }


    var itemSize by rememberPreference(artistsItemSizeKey, LibraryItemSize.Small.size)
    val thumbnailSizeDp = itemSize.dp + 24.dp
    val thumbnailSizePx = thumbnailSizeDp.px

    val sortOrderIconRotation by animateFloatAsState(
        targetValue = if (sortOrder == SortOrder.Ascending) 0f else 180f,
        animationSpec = tween(durationMillis = 400, easing = LinearEasing), label = ""
    )

    val lazyGridState = rememberLazyGridState()

    val navigationBarPosition by rememberPreference(navigationBarPositionKey, NavigationBarPosition.Bottom)

    val showSearchTab by rememberPreference(showSearchTabKey, false)
    //val effectRotationEnabled by rememberPreference(effectRotationKey, true)
    var isRotated by rememberSaveable { mutableStateOf(false) }
    val rotationAngle by animateFloatAsState(
        targetValue = if (isRotated) 360F else 0f,
        animationSpec = tween(durationMillis = 300), label = ""
    )

    Box (
        modifier = Modifier
            .background(colorPalette.background0)
            .fillMaxHeight()
            .fillMaxWidth(if (navigationBarPosition == NavigationBarPosition.Left ||
                navigationBarPosition == NavigationBarPosition.Top ||
                navigationBarPosition == NavigationBarPosition.Bottom) 1f
            else Dimensions.contentWidthRightBar)
    ) {
        LazyVerticalGrid(
            state = lazyGridState,
            columns = GridCells.Adaptive(itemSize.dp + 24.dp),
            //contentPadding = LocalPlayerAwareWindowInsets.current.asPaddingValues(),
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
                    title = stringResource(R.string.artists),
                    iconId = R.drawable.search,
                    enabled = true,
                    showIcon = !showSearchTab,
                    modifier = Modifier,
                    onClick = onSearchClick
                )
            }

            item(
                key = "filter",
                contentType = 0,
                span = { GridItemSpan(maxLineSpan) }
            ) {

                Row (
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .padding(bottom = 20.dp)
                        .fillMaxWidth()
                ){

                    HeaderInfo(
                        title = "${items.size}",
                        icon = painterResource(R.drawable.artists),
                        spacer = 0
                    )

                    var randomGenerator = Random(System.currentTimeMillis())
                    var result = randomGenerator.nextInt(30, 50)

                    HeaderIconButton(
                        modifier = Modifier.rotate(rotationAngle),
                        icon = R.drawable.dice,
                        enabled = items.isNotEmpty() ,
                        color = colorPalette.text,
                        onClick = {
                            isRotated = !isRotated
                            //onArtistClick(items.get((0..<items.size).random()))
                            onArtistClick(items.get(
                                Random(System.currentTimeMillis()).nextInt(0, items.size-1)
                            ))
                        },
                        iconSize = 16.dp
                    )

                    HeaderIconButton(
                        onClick = {
                            menuState.display {
                                Menu {
                                    MenuEntry(
                                        icon = R.drawable.arrow_forward,
                                        text = stringResource(R.string.small),
                                        onClick = {
                                            itemSize = LibraryItemSize.Small.size
                                            menuState.hide()
                                        }
                                    )
                                    MenuEntry(
                                        icon = R.drawable.arrow_forward,
                                        text = stringResource(R.string.medium),
                                        onClick = {
                                            itemSize = LibraryItemSize.Medium.size
                                            menuState.hide()
                                        }
                                    )
                                    MenuEntry(
                                        icon = R.drawable.arrow_forward,
                                        text = stringResource(R.string.big),
                                        onClick = {
                                            itemSize = LibraryItemSize.Big.size
                                            menuState.hide()
                                        }
                                    )
                                }
                            }
                        },
                        icon = R.drawable.resize,
                        color = colorPalette.text
                    )

                    Spacer(
                        modifier = Modifier
                            .weight(1f)
                    )

                    BasicText(
                        text = when (sortBy) {
                            ArtistSortBy.Name -> stringResource(R.string.sort_name)
                            ArtistSortBy.DateAdded -> stringResource(R.string.sort_date_added)
                        },
                        style = typography.xs.semiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .clickable {
                                menuState.display{
                                    SortMenu(
                                        title = stringResource(R.string.sorting_order),
                                        onDismiss = menuState::hide,
                                        onName= { sortBy = ArtistSortBy.Name },
                                        onDateAdded = { sortBy = ArtistSortBy.DateAdded },
                                    )
                                }
                                //showSortTypeSelectDialog = true
                            }
                    )


                    HeaderIconButton(
                        icon = R.drawable.arrow_up,
                        color = colorPalette.text,
                        onClick = { sortOrder = !sortOrder },
                        modifier = Modifier
                            .graphicsLayer { rotationZ = sortOrderIconRotation }
                    )

            }

            }

            items(items = items, key = Artist::id) { artist ->
                ArtistItem(
                    artist = artist,
                    thumbnailSizePx = thumbnailSizePx,
                    thumbnailSizeDp = thumbnailSizeDp,
                    alternative = true,
                    modifier = Modifier
                        .clickable(onClick = { onArtistClick(artist) })
                        .animateItemPlacement()
                )
            }
            item(
                key = "footer",
                contentType = 0,
                span = { GridItemSpan(maxLineSpan) }
            ) {
                Spacer(modifier = Modifier.height(Dimensions.bottomSpacer))
            }
        }

        FloatingActionsContainerWithScrollToTop(lazyGridState = lazyGridState)

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
                lazyGridState = lazyGridState,
                iconId = R.drawable.search,
                onClick = onSearchClick
            )
             */






    }
}
