package com.peecock.ymusic.ui.screens.search

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download
import androidx.navigation.NavController
import com.peecock.compose.persist.persistList
import com.peecock.innertube.Innertube
import com.peecock.innertube.models.bodies.SearchSuggestionsBody
import com.peecock.innertube.requests.searchSuggestionsWithItems
import com.peecock.ymusic.Database
import com.peecock.ymusic.LocalPlayerAwareWindowInsets
import com.peecock.ymusic.LocalPlayerServiceBinder
import com.peecock.ymusic.R
import com.peecock.ymusic.enums.NavRoutes
import com.peecock.ymusic.enums.NavigationBarPosition
import com.peecock.ymusic.enums.ThumbnailRoundness
import com.peecock.ymusic.models.SearchQuery
import com.peecock.ymusic.query
import com.peecock.ymusic.ui.components.LocalMenuState
import com.peecock.ymusic.ui.components.themed.FloatingActionsContainerWithScrollToTop
import com.peecock.ymusic.ui.components.themed.Header
import com.peecock.ymusic.ui.components.themed.NonQueuedMediaItemMenu
import com.peecock.ymusic.ui.components.themed.TitleMiniSection
import com.peecock.ymusic.ui.items.AlbumItem
import com.peecock.ymusic.ui.items.ArtistItem
import com.peecock.ymusic.ui.items.SongItem
import com.peecock.ymusic.ui.styling.Dimensions
import com.peecock.ymusic.ui.styling.LocalAppearance
import com.peecock.ymusic.ui.styling.px
import com.peecock.ymusic.utils.align
import com.peecock.ymusic.utils.asMediaItem
import com.peecock.ymusic.utils.forcePlay
import com.peecock.ymusic.utils.medium
import com.peecock.ymusic.utils.navigationBarPositionKey
import com.peecock.ymusic.utils.pauseSearchHistoryKey
import com.peecock.ymusic.utils.preferences
import com.peecock.ymusic.utils.rememberPreference
import com.peecock.ymusic.utils.secondary
import com.peecock.ymusic.utils.thumbnailRoundnessKey
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged

@UnstableApi
@ExperimentalFoundationApi
@ExperimentalAnimationApi
@ExperimentalTextApi
@Composable
fun OnlineSearch(
    navController: NavController,
    textFieldValue: TextFieldValue,
    onTextFieldValueChanged: (TextFieldValue) -> Unit,
    onSearch: (String) -> Unit,
    decorationBox: @Composable (@Composable () -> Unit) -> Unit,
) {
    val context = LocalContext.current

    val (colorPalette, typography) = LocalAppearance.current

    var history by persistList<SearchQuery>("search/online/history")

    var reloadHistory by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(textFieldValue.text, reloadHistory) {
        if (!context.preferences.getBoolean(pauseSearchHistoryKey, false)) {
            Database.queries("%${textFieldValue.text}%")
                .distinctUntilChanged { old, new -> old.size == new.size }
                .collect { history = it }
        }
    }

    //var suggestionsResult by persist<Result<List<String>?>?>("search/online/suggestionsResult")
    var suggestionsResult by remember {
        mutableStateOf<Result<Innertube.SearchSuggestions>?>(null)
    }

    LaunchedEffect(textFieldValue.text) {
        if (textFieldValue.text.isNotEmpty()) {
            delay(200)
            //suggestionsResult =
            //    Innertube.searchSuggestions(SearchSuggestionsBody(input = textFieldValue.text))
            suggestionsResult =
                Innertube.searchSuggestionsWithItems(SearchSuggestionsBody(input = textFieldValue.text))
        }
    }

    val playlistId = remember(textFieldValue.text) {
        val isPlaylistUrl = listOf(
            "https://www.youtube.com/playlist?",
            "https://youtube.com/playlist?",
            "https://music.youtube.com/playlist?",
            "https://m.youtube.com/playlist?"
        ).any(textFieldValue.text::startsWith)

        if (isPlaylistUrl) textFieldValue.text.toUri().getQueryParameter("list") else null
    }

    val rippleIndication = ripple(bounded = false)
    val timeIconPainter = painterResource(R.drawable.search_circle)
    val closeIconPainter = painterResource(R.drawable.trash)
    val arrowForwardIconPainter = painterResource(R.drawable.arrow_forward)

    val focusRequester = remember {
        FocusRequester()
    }

    var thumbnailRoundness by rememberPreference(
        thumbnailRoundnessKey,
        ThumbnailRoundness.Heavy
    )

    val lazyListState = rememberLazyListState()

    //val navigationBarPosition by rememberPreference(navigationBarPositionKey, NavigationBarPosition.Bottom)
    //val contentWidth = context.preferences.getFloat(contentWidthKey,0.8f)
    val navigationBarPosition by rememberPreference(navigationBarPositionKey, NavigationBarPosition.Bottom)

    var downloadState by remember {
        mutableStateOf(Download.STATE_STOPPED)
    }
    val songThumbnailSizeDp = Dimensions.thumbnails.song
    val songThumbnailSizePx = songThumbnailSizeDp.px
    val menuState = LocalMenuState.current
    val hapticFeedback = LocalHapticFeedback.current
    val binder = LocalPlayerServiceBinder.current

    Box(
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
        LazyColumn(
            state = lazyListState,
            contentPadding = LocalPlayerAwareWindowInsets.current
                .only(WindowInsetsSides.Vertical + WindowInsetsSides.End).asPaddingValues(),
            modifier = Modifier
                .fillMaxSize()
        ) {
            item(
                key = "header",
                contentType = 0
            ) {
                /*
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    HeaderWithIcon(
                        title = "${stringResource(R.string.search)} ${stringResource(R.string.online)}",
                        iconId = R.drawable.globe,
                        enabled = true,
                        showIcon = true,
                        modifier = Modifier
                            .padding(bottom = 8.dp),
                        onClick = {}
                    )

                }
                 */
                Header(
                    titleContent = {
                        BasicTextField(
                            value = textFieldValue,
                            onValueChange = onTextFieldValueChanged,
                            textStyle = typography.l.medium.align(TextAlign.Start),
                            singleLine = true,
                            maxLines = 1,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(
                                onSearch = {
                                    if (textFieldValue.text.isNotEmpty() && textFieldValue.text != "/") {
                                        onSearch(textFieldValue.text.replace("/","",true))
                                    }
                                }
                            ),
                            cursorBrush = SolidColor(colorPalette.text),
                            decorationBox = decorationBox,
                            modifier = Modifier
                                .background(
                                    //colorPalette.background4,
                                    colorPalette.background1,
                                    shape = thumbnailRoundness.shape()
                                )
                                .padding(all = 4.dp)
                                .focusRequester(focusRequester)
                                .fillMaxWidth()
                        )
                    },
                    actionsContent = {
                        /*
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .padding(horizontal = 40.dp)
                                .fillMaxWidth()
                        ) {
                            IconButton(
                                onClick = onAction1,
                                icon = R.drawable.globe,
                                color = colorPalette.favoritesIcon,
                                modifier = Modifier
                                    .size(24.dp)
                            )
                            IconButton(
                                onClick = onAction2,
                                icon = R.drawable.library,
                                color = colorPalette.favoritesIcon,
                                modifier = Modifier
                                    .size(24.dp)
                            )
                            IconButton(
                                onClick = onAction3,
                                icon = R.drawable.link,
                                color = colorPalette.favoritesIcon,
                                modifier = Modifier
                                    .size(24.dp)
                            )

                            /*
                            IconButton(
                                onClick = onAction4,
                                icon = R.drawable.chevron_back,
                                color = colorPalette.favoritesIcon,
                                modifier = Modifier
                                    .size(24.dp)
                            )
                             */
                        }
                        /*
                        if (playlistId != null) {
                            val isAlbum = playlistId.startsWith("OLAK5uy_")

                            SecondaryTextButton(
                                text = "View ${if (isAlbum) "album" else "playlist"}",
                                onClick = { onViewPlaylist(textFieldValue.text) }
                            )
                        }

                        Spacer(
                            modifier = Modifier
                                .weight(1f)
                        )

                         */
                        /*
                        if (textFieldValue.text.isNotEmpty()) {
                            SecondaryTextButton(
                                text = stringResource(R.string.clear),
                                onClick = { onTextFieldValueChanged(TextFieldValue()) }
                            )
                        }
                         */

                         */
                    },
                    /*
                    modifier = Modifier
                        .drawBehind {

                            val strokeWidth = 1 * density
                            val y = size.height - strokeWidth / 2

                            drawLine(
                                color = colorPalette.textDisabled,
                                start = Offset(x = 0f, y = y/2),
                                end = Offset(x = size.maxDimension, y = y/2),
                                strokeWidth = 2.dp.toPx()
                            )
                        }
                     */
                )
            }

            suggestionsResult?.getOrNull()?.let { suggestions ->

                item {
                    TitleMiniSection(title = stringResource(R.string.searches_suggestions),
                        modifier = Modifier.padding(start = 12.dp).padding(vertical = 10.dp)
                    )
                }

                suggestions.recommendedSong.let {
                    item{
                        it?.asMediaItem?.let { mediaItem ->
                            SongItem(
                                song = mediaItem,
                                thumbnailSizePx = songThumbnailSizePx,
                                thumbnailSizeDp = songThumbnailSizeDp,
                                isDownloaded = false,
                                onDownloadClick = {},
                                downloadState = downloadState,
                                modifier = Modifier
                                    .combinedClickable(
                                        onLongClick = {
                                            menuState.display {
                                                NonQueuedMediaItemMenu(
                                                    navController = navController,
                                                    onDismiss = menuState::hide,
                                                    mediaItem = mediaItem,
                                                )
                                            };
                                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                        },
                                        onClick = {
                                            binder?.player?.forcePlay(mediaItem)
                                        }
                                    )
                            )
                        }
                    }
                }
                suggestions.recommendedAlbum.let {
                    item{
                        it?.let { album ->
                            AlbumItem(
                                yearCentered = false,
                                album = album,
                                thumbnailSizePx = songThumbnailSizePx,
                                thumbnailSizeDp = songThumbnailSizeDp,
                                modifier = Modifier
                                    .clickable {
                                        navController.navigate(route = "${NavRoutes.album.name}/${album.key}")
                                    }

                            )
                        }
                    }
                }
                suggestions.recommendedArtist.let {
                    item{
                        it?.let { artist ->
                            ArtistItem(
                                artist = artist,
                                thumbnailSizePx = songThumbnailSizePx,
                                thumbnailSizeDp = songThumbnailSizeDp,
                                modifier = Modifier
                                    .clickable {
                                        navController.navigate(route = "${NavRoutes.artist.name}/${artist.key}")
                                    }

                            )
                        }
                    }
                }

                items(items = suggestions.queries) { query ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable(onClick = { onSearch(query.replace("/", "", true)) })
                            .fillMaxWidth()
                            .padding(all = 16.dp)
                    ) {
                        Spacer(
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                                .size(20.dp)
                        )

                        BasicText(
                            text = query,
                            style = typography.s.secondary,
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                                .weight(1f)
                        )

                        Image(
                            painter = arrowForwardIconPainter,
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(colorPalette.textDisabled),
                            modifier = Modifier
                                .clickable(
                                    indication = rippleIndication,
                                    interactionSource = remember { MutableInteractionSource() },
                                    onClick = {
                                        onTextFieldValueChanged(
                                            TextFieldValue(
                                                text = query,
                                                selection = TextRange(query.length)
                                            )
                                        )
                                    }
                                )
                                .rotate(225f)
                                .padding(horizontal = 8.dp)
                                .size(22.dp)
                        )
                    }
                }
            } ?: suggestionsResult?.exceptionOrNull()?.let {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        TitleMiniSection(title = stringResource(R.string.searches_no_suggestions),
                            modifier = Modifier.padding(start = 12.dp).padding(vertical = 10.dp)
                        )
                        /*
                        BasicText(
                            text = stringResource(R.string.error),
                            style = typography.s.secondary.center,
                            modifier = Modifier
                                .align(Alignment.Center)
                        )
                         */
                    }
                }
            }

            if(history.isNotEmpty())
                item {
                    TitleMiniSection(title = stringResource(R.string.searches_saved_searches), modifier = Modifier.padding(start = 12.dp))
                }

            items(
                items = history,
                key = SearchQuery::id
            ) { searchQuery ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable(onClick = { onSearch(searchQuery.query.replace("/", "", true)) })
                        .fillMaxWidth()
                        .padding(all = 16.dp)
                ) {
                    Spacer(
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .size(20.dp)
                            .paint(
                                painter = timeIconPainter,
                                colorFilter = ColorFilter.tint(colorPalette.textDisabled)
                            )
                    )

                    BasicText(
                        text = searchQuery.query,
                        style = typography.s.secondary,
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .weight(1f)
                    )

                    Image(
                        painter = closeIconPainter,
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(colorPalette.textDisabled),
                        modifier = Modifier
                            .combinedClickable(
                                indication = rippleIndication,
                                interactionSource = remember { MutableInteractionSource() },
                                onClick = {
                                    query {
                                        Database.delete(searchQuery)
                                    }
                                },
                                onLongClick = {
                                    query {
                                        history.forEach {
                                            Database.delete(it)
                                        }
                                    }
                                    reloadHistory = !reloadHistory
                                }
                            )
                            .padding(horizontal = 8.dp)
                            .size(20.dp)
                    )

                    Image(
                        painter = arrowForwardIconPainter,
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(colorPalette.textDisabled),
                        modifier = Modifier
                            .clickable(
                                indication = rippleIndication,
                                interactionSource = remember { MutableInteractionSource() },
                                onClick = {
                                    onTextFieldValueChanged(
                                        TextFieldValue(
                                            text = searchQuery.query,
                                            selection = TextRange(searchQuery.query.length)
                                        )
                                    )
                                }
                            )
                            .rotate(310f)
                            .padding(horizontal = 8.dp)
                            .size(22.dp)
                    )
                }
            }



        }

        FloatingActionsContainerWithScrollToTop(lazyListState = lazyListState)
    }

    LaunchedEffect(Unit) {
        delay(300)
        focusRequester.requestFocus()
    }

}
