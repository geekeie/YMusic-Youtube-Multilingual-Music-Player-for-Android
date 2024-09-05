package com.peecock.ymusic.ui.screens

import android.annotation.SuppressLint
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.media3.common.util.UnstableApi
import androidx.navigation.compose.rememberNavController
import com.peecock.compose.routing.Route0
import com.peecock.compose.routing.Route1
import com.peecock.compose.routing.Route3
import com.peecock.compose.routing.RouteHandlerScope
import com.peecock.ymusic.enums.BuiltInPlaylist
import com.peecock.ymusic.enums.DeviceLists
import com.peecock.ymusic.enums.SearchType
import com.peecock.ymusic.enums.StatisticsType
import com.peecock.ymusic.models.Mood
import com.peecock.ymusic.ui.screens.album.AlbumScreenWithoutScaffold
import com.peecock.ymusic.ui.screens.artist.ArtistScreen
import com.peecock.ymusic.ui.screens.playlist.PlaylistScreen
import com.peecock.ymusic.ui.screens.localplaylist.LocalPlaylistScreen
import com.peecock.ymusic.ui.screens.mood.MoodScreen
import com.peecock.ymusic.ui.screens.ondevice.DeviceListSongsScreen
import com.peecock.ymusic.ui.screens.search.SearchTypeScreen
import com.peecock.ymusic.ui.screens.statistics.StatisticsScreen

val quickpicksRoute = Route1<String?>("quickpicksRoute")
val albumRoute = Route1<String?>("albumRoute")
val artistRoute = Route1<String?>("artistRoute")
val builtInPlaylistRoute = Route1<BuiltInPlaylist>("builtInPlaylistRoute")
val deviceListSongRoute = Route1<String>("deviceListSongRoute")
val statisticsTypeRoute = Route1<StatisticsType>("statisticsTypeRoute")
val localPlaylistRoute = Route1<Long?>("localPlaylistRoute")
val searchResultRoute = Route1<String>("searchResultRoute")
val searchRoute = Route1<String>("searchRoute")
val searchTypeRoute = Route1<SearchType>("searchTypeRoute")
val settingsRoute = Route0("settingsRoute")
val homeRoute = Route0("homeRoute")
val moodRoute = Route1<Mood>("moodRoute")
//val playlistRoute = Route1<String?>("playlistRoute")
val playlistRoute = Route3<String?, String?, Int?>("playlistRoute")
//val playlistRoute = Route2<String?, String?>("playlistRoute")
val historyRoute = Route0("historyRoute")

@ExperimentalMaterialApi
@ExperimentalTextApi
@SuppressLint("ComposableNaming")
@Suppress("NOTHING_TO_INLINE")
@ExperimentalAnimationApi
@ExperimentalFoundationApi
@ExperimentalComposeUiApi
@UnstableApi
@Composable
inline fun RouteHandlerScope.globalRoutes() {

    val navController = rememberNavController()

    albumRoute { browseId ->
        AlbumScreenWithoutScaffold(
            navController = navController,
            browseId = browseId ?: error("browseId cannot be null")
        )
        /*
        AlbumScreen(
            browseId = browseId ?: error("browseId cannot be null")
        )
         */
    }

    artistRoute { browseId ->
        ArtistScreen(
            navController = navController,
            browseId = browseId ?: error("browseId cannot be null")
        )
    }

    localPlaylistRoute { playlistId ->
        LocalPlaylistScreen(
            navController = navController,
            playlistId = playlistId ?: error("playlistId cannot be null")
        )
    }


    playlistRoute { browseId, params, maxDepth ->
        PlaylistScreen(
            navController = navController,
            browseId = browseId ?: error("browseId cannot be null"),
            params = params,
            maxDepth = maxDepth
        )
    }
    /*
    playlistRoute { browseId, params ->
        PlaylistScreen(
            browseId = browseId ?: error("browseId cannot be null"),
            params = params
        )
    }
     */
    /*
    playlistRoute { browseId ->
        PlaylistScreen(
        )
    }
 */

    statisticsTypeRoute { browseId ->
        StatisticsScreen(
            navController = navController,
            statisticsType = browseId ?: error("browseId cannot be null")
        )
    }

    searchTypeRoute { browseId ->
        SearchTypeScreen(
            navController = navController,
            searchType = browseId ?: error("browseId cannot be null")
        )
    }

    /*
    homeRoute {
        HomeScreen(
            onPlaylistUrl = {pop},
            openTabFromShortcut = -1
        )
    }
     */

    moodRoute { mood ->
        MoodScreen(
            navController = navController,
            mood = mood
        )
    }


    deviceListSongRoute { browseId ->
        DeviceListSongsScreen(
            navController = navController,
            deviceLists = DeviceLists.LocalSongs
        )
    }

    quickpicksRoute { browseId ->

    }

}
