package com.peecock.innertube.requests


import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import com.peecock.innertube.Innertube
import com.peecock.innertube.models.BrowseResponse
import com.peecock.innertube.models.MusicCarouselShelfRenderer
import com.peecock.innertube.models.NextResponse
import com.peecock.innertube.models.bodies.BrowseBody
import com.peecock.innertube.models.bodies.NextBody
import com.peecock.innertube.utils.findSectionByTitle
import com.peecock.innertube.utils.from
import com.peecock.innertube.utils.runCatchingNonCancellable



suspend fun Innertube.relatedSongs(body: NextBody) = runCatchingNonCancellable {
    val nextResponse = client.post(next) {
        setBody(body)
        mask("contents.singleColumnMusicWatchNextResultsRenderer.tabbedRenderer.watchNextTabbedResultsRenderer.tabs.tabRenderer(endpoint,title)")
    }.body<NextResponse>()

    val browseId = nextResponse
        .contents
        ?.singleColumnMusicWatchNextResultsRenderer
        ?.tabbedRenderer
        ?.watchNextTabbedResultsRenderer
        ?.tabs
        ?.getOrNull(2)
        ?.tabRenderer
        ?.endpoint
        ?.browseEndpoint
        ?.browseId
        ?: return@runCatchingNonCancellable null

    val response = client.post(browse) {
        setBody(BrowseBody(browseId = browseId))
        mask("contents.sectionListRenderer.contents.musicCarouselShelfRenderer(header.musicCarouselShelfBasicHeaderRenderer(title,strapline),contents($musicResponsiveListItemRendererMask,$musicTwoRowItemRendererMask))")
    }.body<BrowseResponse>()

    val sectionListRenderer = response
        .contents
        ?.sectionListRenderer



    Innertube.RelatedSongs(
        songs = sectionListRenderer
            ?.findSectionByTitle("You might also like")
            ?.musicCarouselShelfRenderer
            ?.contents
            ?.mapNotNull(MusicCarouselShelfRenderer.Content::musicResponsiveListItemRenderer)
            ?.mapNotNull(Innertube.SongItem::from)
    )
}
