package com.peecock.innertube.requests

import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.Url
import com.peecock.innertube.Innertube
import com.peecock.innertube.models.NavigationEndpoint
import com.peecock.innertube.models.bodies.BrowseBody
import com.peecock.innertube.models.v0624.BrowseResponse0624

suspend fun Innertube.albumPage(body: BrowseBody) = playlistPage(body)?.map { album ->
    album.url?.let { Url(it).parameters["list"] }?.let { playlistId ->
        playlistPage(BrowseBody(browseId = "VL$playlistId"))?.getOrNull()?.let { playlist ->
            album.copy(songsPage = playlist.songsPage)
        }
    } ?: album
}
    ?.map { album ->
        albumPageDetails(BrowseBody(browseId = body.browseId)).getOrNull()?.let {
            //println("mediaItem albumPage pre songsPage ${album.songsPage?.items?.size} ")
            album.copy(description = it.description, otherInfo = it.otherInfo)
        }
    }
    ?.map { album ->
        //println("mediaItem albumPage post songsPage ${album?.songsPage?.items?.size} des ${album?.description} browseId ${body.browseId}")

        val albumInfo = Innertube.Info(
            name = album?.title,
            endpoint = NavigationEndpoint.Endpoint.Browse(
                browseId = body.browseId,
                params = body.params
            )
        )

        album?.copy(
            songsPage = album.songsPage?.copy(
                items = album.songsPage.items?.map { song ->
                    song.copy(
                        authors = song.authors ?: album.authors,
                        album = albumInfo,
                        thumbnail = album.thumbnail
                    )
                }
            )
        )

    }?.onFailure {
        println("ERROR IN Innertube albumPage "+it.message)
    }

/*
suspend fun Innertube.albumPage(body: BrowseBody) = playlistPage(body)?.map { album ->
    album.url?.let { Url(it).parameters["list"] }?.let { playlistId ->
        playlistPage(BrowseBody(browseId = "VL$playlistId"))?.getOrNull()?.let { playlist ->
            album.copy(songsPage = playlist.songsPage)
        } /*
        albumPageDetails(BrowseBody(browseId = body.browseId)).getOrNull()?.let {
            println("mediaItem albumPage pre songsPage ${it.songsPage?.items?.size} ")
            album.copy(description = it.description, otherInfo = it.otherInfo)
        }
        */
    } ?: album
}?.map { album ->

    //println("mediaItem albumPage post songsPage ${album.songsPage?.items?.size} des ${album.description} browseId ${body.browseId}")

    val albumInfo = Innertube.Info(
        name = album.title,
        endpoint = NavigationEndpoint.Endpoint.Browse(
            browseId = body.browseId,
            params = body.params
        )
    )



    album.copy(
        songsPage = album.songsPage?.copy(
            items = album.songsPage.items?.map { song ->
                song.copy(
                    authors = song.authors ?: album.authors,
                    album = albumInfo,
                    thumbnail = album.thumbnail
                )
            }
        )
    )

}
*/

suspend fun Innertube.albumPageDetails(body: BrowseBody) = runCatching {
    val response = client.post(browse) {
        setBody(body)
        body.context.apply()
    }.body<BrowseResponse0624>()

    val musicDetailHeaderRenderer =
        response.contents.twoColumnBrowseResultsRenderer.tabs.firstOrNull()
            ?.tabRenderer?.content?.sectionListRenderer?.contents?.firstOrNull()
            ?.musicResponsiveHeaderRenderer

    /*

    println("mediaItem albumPageDetails ${
        musicDetailHeaderRenderer
            ?.description
            ?.musicDescriptionShelfRenderer?.description?.runs
        ?.joinToString("") { it.text ?: "" }} browseId ${body.browseId}")

    println("mediaItem albumPageDetails response ${response} ")

     */


    Innertube.PlaylistOrAlbumPage(
        title = null,
        description = musicDetailHeaderRenderer
            ?.description
            ?.musicDescriptionShelfRenderer?.description?.runs?.joinToString("") { it.text },
        thumbnail = null,
        authors = null,
        year = null,
        url = null,
        songsPage = null,
        otherVersions = null,
        otherInfo = musicDetailHeaderRenderer
            ?.secondSubtitle?.runs?.joinToString("") { it.text }

    )

}.onFailure {
    println("mediaItem ERROR IN Innertube albumPageDetails " + it.message)
}
