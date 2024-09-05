package com.peecock.ymusic.ui.screens.search

import android.annotation.SuppressLint
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import com.peecock.innertube.Innertube
import com.peecock.innertube.models.bodies.BrowseBody
import com.peecock.innertube.requests.playlistPage
import com.peecock.innertube.requests.song
import com.peecock.ymusic.LocalPlayerAwareWindowInsets
import com.peecock.ymusic.LocalPlayerServiceBinder
import com.peecock.ymusic.R
import com.peecock.ymusic.enums.NavRoutes
import com.peecock.ymusic.enums.NavigationBarPosition
import com.peecock.ymusic.ui.components.themed.HeaderWithIcon
import com.peecock.ymusic.ui.components.themed.InputTextField
import com.peecock.ymusic.ui.styling.Dimensions
import com.peecock.ymusic.ui.styling.LocalAppearance
import com.peecock.ymusic.utils.asMediaItem
import com.peecock.ymusic.utils.forcePlay
import com.peecock.ymusic.utils.navigationBarPositionKey
import com.peecock.ymusic.utils.rememberPreference
import com.peecock.ymusic.utils.semiBold
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.withContext

@ExperimentalTextApi
@SuppressLint("SuspiciousIndentation")
@ExperimentalFoundationApi
@ExperimentalAnimationApi
@UnstableApi
@Composable
fun GoToLink(
    navController: NavController,
    textFieldValue: TextFieldValue,
    onTextFieldValueChanged: (TextFieldValue) -> Unit,
    decorationBox: @Composable (@Composable () -> Unit) -> Unit,
    onAction1: () -> Unit,
    onAction2: () -> Unit,
    onAction3: () -> Unit,
    onAction4: () -> Unit,
) {

    val (colorPalette, typography) = LocalAppearance.current
    val binder = LocalPlayerServiceBinder.current
    val coroutineScope = CoroutineScope(Dispatchers.IO) + Job()

    val lazyListState = rememberLazyListState()

    var textLink by remember {
        mutableStateOf("")
    }

    //val context = LocalContext.current
    //val navigationBarPosition by rememberPreference(navigationBarPositionKey, NavigationBarPosition.Bottom)
    //val contentWidth = context.preferences.getFloat(contentWidthKey,0.8f)
    val navigationBarPosition by rememberPreference(navigationBarPositionKey, NavigationBarPosition.Bottom)


    Box(
        modifier = Modifier
            .background(colorPalette.background0)
            //.fillMaxSize()
            .fillMaxHeight()
            .fillMaxWidth(if (navigationBarPosition == NavigationBarPosition.Left ||
                navigationBarPosition == NavigationBarPosition.Top ||
                navigationBarPosition == NavigationBarPosition.Bottom) 1f
            else Dimensions.contentWidthRightBar)
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

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    HeaderWithIcon(
                        title = stringResource(R.string.go_to_link),
                        iconId = R.drawable.query_stats,
                        enabled = true,
                        showIcon = true,
                        modifier = Modifier
                            .padding(bottom = 8.dp),
                        onClick = {}
                    )

                }

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
                */
                InputTextField(
                    onDismiss = { },
                    title = stringResource(R.string.paste_or_type_a_valid_url),
                    value = textFieldValue.text,
                    placeholder = "https://........",
                    setValue = { textLink = it }
                )

                BasicText(
                    text = stringResource(R.string.you_can_put_a_complete_link),
                    style = typography.s.semiBold,
                    modifier = Modifier
                        .padding(vertical = 8.dp, horizontal = 24.dp)
                )

                if (textLink.isNotEmpty()) {

                    val uri = textLink.toUri()

                    LaunchedEffect(Unit) {
                        coroutineScope.launch(Dispatchers.Main) {
                            println("mediaItem channelId: ${uri.pathSegments}")
                            when (val path = uri.pathSegments.firstOrNull()) {
                                "playlist" -> uri.getQueryParameter("list")?.let { playlistId ->
                                    val browseId = "VL$playlistId"

                                    if (playlistId.startsWith("OLAK5uy_")) {
                                        Innertube.playlistPage(BrowseBody(browseId = browseId))
                                            ?.getOrNull()?.let {
                                                it.songsPage?.items?.firstOrNull()?.album?.endpoint?.browseId?.let { browseId ->
                                                    //albumRoute.ensureGlobal(browseId)
                                                    navController.navigate(route = "${NavRoutes.album.name}/$browseId")
                                                }
                                            }
                                    } else {
                                        navController.navigate(route = "${NavRoutes.playlist.name}/$browseId")
                                    }
                                }

                                "channel", "c" -> uri.lastPathSegment?.let { channelId ->
                                    navController.navigate(route = "${NavRoutes.artist.name}/$channelId")
                                }

                                "search" -> uri.getQueryParameter("q")?.let { query ->
                                        navController.navigate(route = "${NavRoutes.searchResults.name}/$query")
                                }

                                else -> when {
                                    path == "watch" -> uri.getQueryParameter("v")
                                    uri.host == "youtu.be" -> path
                                    else -> null
                                }?.let { videoId ->
                                    Innertube.song(videoId)?.getOrNull()?.let { song ->
                                        val binder = snapshotFlow { binder }.filterNotNull().first()
                                        withContext(Dispatchers.Main) {
                                            binder.player.forcePlay(song.asMediaItem)
                                        }
                                    }
                                }
                            }
/*
                            if (uri.pathSegments.firstOrNull()?.startsWith("@") == true)
                                uri.pathSegments.firstOrNull()?.let { channelId ->
                                    navController.navigate(route = "${NavRoutes.artist.name}/${channelId.removePrefix("@")}")
                                }

 */

                        }

                    }
                }

            }

        }

    }
}
