package com.peecock.innertube.requests

import io.ktor.client.call.body
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import com.peecock.innertube.Innertube
import com.peecock.innertube.models.BrowseResponse
import com.peecock.innertube.models.ContinuationResponse
import com.peecock.innertube.models.MusicCarouselShelfRenderer
import com.peecock.innertube.models.MusicShelfRenderer
import com.peecock.innertube.models.bodies.BrowseBody
import com.peecock.innertube.models.bodies.ContinuationBody
import com.peecock.innertube.utils.from
import com.peecock.innertube.utils.runCatchingNonCancellable

/**** api modified by youtube music 0624 ****/
suspend fun Innertube.playlistPage(body: BrowseBody) = runCatchingNonCancellable {
    val response = client.post(browse) {
        setBody(body)
        body.context.apply()
    }.body<BrowseResponse>()

    if (response.contents?.twoColumnBrowseResultsRenderer == null) {
        /* OLD */
        val header = response
            .header
            ?.musicDetailHeaderRenderer

        val contents = response
            .contents
            ?.singleColumnBrowseResultsRenderer
            ?.tabs
            ?.firstOrNull()
            ?.tabRenderer
            ?.content
            ?.sectionListRenderer
            ?.contents

        val musicShelfRenderer = contents
            ?.firstOrNull()
            ?.musicShelfRenderer

        val musicCarouselShelfRenderer = contents
            ?.getOrNull(1)
            ?.musicCarouselShelfRenderer

        Innertube.PlaylistOrAlbumPage(
            title = header
                ?.title
                ?.text,
            description = header
                ?.description
                ?.text,
            thumbnail = header
                ?.thumbnail
                ?.musicThumbnailRenderer
                ?.thumbnail
                ?.thumbnails
                ?.getBestQuality(),
                //?.maxByOrNull { (it.width ?: 0) * (it.height ?: 0) },
            authors = header
                ?.subtitle
                ?.splitBySeparator()
                ?.getOrNull(1)
                ?.map(Innertube::Info),
            year = header
                ?.subtitle
                ?.splitBySeparator()
                ?.getOrNull(2)
                ?.firstOrNull()
                ?.text,
            url = response
                .microformat
                ?.microformatDataRenderer
                ?.urlCanonical,
            songsPage = musicShelfRenderer
                ?.toSongsPage(),
            otherVersions = musicCarouselShelfRenderer
                ?.contents
                ?.mapNotNull(MusicCarouselShelfRenderer.Content::musicTwoRowItemRenderer)
                ?.mapNotNull(Innertube.AlbumItem::from),
            otherInfo = header
                ?.secondSubtitle
                ?.text
        )
    } else {
        /* NEW */
        val header = response
            .contents
            .twoColumnBrowseResultsRenderer
            .tabs
            ?.firstOrNull()
            ?.tabRenderer
            ?.content
            ?.sectionListRenderer
            ?.contents
            ?.firstOrNull()
            ?.musicResponsiveHeaderRenderer

        /*
        println("mediaItem header album year ${
            header
                ?.subtitle?.runs?.getOrNull(2)?.text
        }")

         */

        val contents = response
            .contents
            .twoColumnBrowseResultsRenderer
            .secondaryContents
            ?.sectionListRenderer
            ?.contents

        val musicShelfRenderer = contents
            ?.firstOrNull()
            ?.musicShelfRenderer

        val musicCarouselShelfRenderer = contents
            ?.getOrNull(1)
            ?.musicCarouselShelfRenderer

        Innertube.PlaylistOrAlbumPage(
            title = header
                ?.title
                ?.text,
            description = header
                ?.description
                ?.description
                ?.text,
            thumbnail = header
                ?.thumbnail
                ?.musicThumbnailRenderer
                ?.thumbnail
                ?.thumbnails
                ?.getBestQuality(),
                //?.maxByOrNull { (it.width ?: 0) * (it.height ?: 0) },
            authors = header
                ?.straplineTextOne
                ?.splitBySeparator()
                ?.getOrNull(0)
                ?.map(Innertube::Info),
            year = header
                ?.subtitle
                ?.runs
                ?.getOrNull(2)
                ?.text,
            url = response
                .microformat
                ?.microformatDataRenderer
                ?.urlCanonical,
            songsPage = musicShelfRenderer
                ?.toSongsPage(),
            otherVersions = musicCarouselShelfRenderer
                ?.contents
                ?.mapNotNull(MusicCarouselShelfRenderer.Content::musicTwoRowItemRenderer)
                ?.mapNotNull(Innertube.AlbumItem::from),
            otherInfo = header
                ?.secondSubtitle
                ?.text
        )
    }

}?.onFailure {
    println("mediaItem ERROR IN Innertube playlistpage " + it.message)
}


/*
suspend fun Innertube.playlistPage(body: BrowseBody) = runCatching {
    val response = client.post(browse) {
        setBody(body)
        mask("contents.singleColumnBrowseResultsRenderer.tabs.tabRenderer.content.sectionListRenderer.contents(musicPlaylistShelfRenderer(continuations,contents.$musicResponsiveListItemRendererMask),musicCarouselShelfRenderer.contents.$musicTwoRowItemRendererMask),header.musicDetailHeaderRenderer(title,subtitle,thumbnail),microformat")
        body.context.apply()
    }.body<BrowseResponse>()

    val musicDetailHeaderRenderer = response
        .header
        ?.musicDetailHeaderRenderer

    val sectionListRendererContents = response
        .contents
        ?.singleColumnBrowseResultsRenderer
        ?.tabs
        ?.firstOrNull()
        ?.tabRenderer
        ?.content
        ?.sectionListRenderer
        ?.contents

    val musicShelfRenderer = sectionListRendererContents
        ?.firstOrNull()
        ?.musicShelfRenderer

    val musicCarouselShelfRenderer = sectionListRendererContents
        ?.getOrNull(1)
        ?.musicCarouselShelfRenderer

    Innertube.PlaylistOrAlbumPage(
        title = musicDetailHeaderRenderer
            ?.title
            ?.text,
        description = musicDetailHeaderRenderer
            ?.description
            ?.text,
        thumbnail = musicDetailHeaderRenderer
            ?.thumbnail
            ?.musicThumbnailRenderer
            ?.thumbnail
            ?.thumbnails
            ?.firstOrNull(),
        authors = musicDetailHeaderRenderer
            ?.subtitle
            ?.splitBySeparator()
            ?.getOrNull(1)
            ?.map(Innertube::Info),
        year = musicDetailHeaderRenderer
            ?.subtitle
            ?.splitBySeparator()
            ?.getOrNull(2)
            ?.firstOrNull()
            ?.text,
        url = response
            .microformat
            ?.microformatDataRenderer
            ?.urlCanonical,
        songsPage = musicShelfRenderer
            ?.toSongsPage(),
        otherVersions = musicCarouselShelfRenderer
            ?.contents
            ?.mapNotNull(MusicCarouselShelfRenderer.Content::musicTwoRowItemRenderer)
            ?.mapNotNull(Innertube.AlbumItem::from),
        otherInfo = musicDetailHeaderRenderer
            ?.secondSubtitle
            ?.text
    )
}
 */

suspend fun Innertube.playlistPage(body: ContinuationBody) = runCatchingNonCancellable {
    val response = client.post(browse) {
        setBody(body)
        parameter("continuation", body.continuation)
        parameter("ctoken", body.continuation)
        parameter("type", "next")
        body.context.apply()
    }.body<ContinuationResponse>()

    response
        .continuationContents
        ?.musicShelfContinuation
        ?.toSongsPage()
}

suspend fun Innertube.playlistPageLong(body: ContinuationBody) = runCatching {
    val response = client.post(browse) {
        //---
        //mask("continuationContents.musicPlaylistShelfContinuation(continuations,contents.$musicResponsiveListItemRendererMask)")
        //---
        parameter("continuation", body.continuation)
        parameter("ctoken", body.continuation)
        //parameter("type", "next")
        setBody(body) // required for long playlist
        body.context.apply()
    }.body<ContinuationResponse>()

    println("mediaItem ContinuationBody playlistPageLong continuation ${response.continuationContents?.musicShelfContinuation?.continuations?.firstOrNull()?.nextContinuationData?.continuation}")

    response
        .continuationContents
        ?.musicShelfContinuation
        ?.toSongsPage()
}

private fun MusicShelfRenderer?.toSongsPage() = Innertube.ItemsPage(
    items = this
        ?.contents
        ?.mapNotNull(MusicShelfRenderer.Content::musicResponsiveListItemRenderer)
        ?.mapNotNull(Innertube.SongItem::from),
    continuation = this
        ?.continuations
        ?.firstOrNull()
        ?.nextContinuationData
        ?.continuation
)

