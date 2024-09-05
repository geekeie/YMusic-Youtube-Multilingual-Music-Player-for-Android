package com.peecock.innertube

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.BrowserUserAgent
import io.ktor.client.plugins.compression.ContentEncoding
import io.ktor.client.plugins.compression.brotli
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import com.peecock.innertube.models.MusicNavigationButtonRenderer
import com.peecock.innertube.models.NavigationEndpoint
import com.peecock.innertube.models.Runs
import com.peecock.innertube.models.Thumbnail
import com.peecock.innertube.utils.ProxyPreferences
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import java.net.InetSocketAddress
import java.net.Proxy

object Innertube {
    val client = HttpClient(OkHttp) {
        BrowserUserAgent()

        expectSuccess = true

        install(ContentNegotiation) {
            @OptIn(ExperimentalSerializationApi::class)
            json(Json {
                ignoreUnknownKeys = true
                explicitNulls = false
                encodeDefaults = true
            })
        }

        install(ContentEncoding) {
            brotli()
        }

        ProxyPreferences.preference?.let {
            engine {
                proxy = Proxy(
                    it.proxyMode,
                    InetSocketAddress(
                        it.proxyHost,
                        it.proxyPort
                    )
                )
            }
        }

        defaultRequest {
            url(scheme = "https", host ="music.youtube.com") {
                headers.append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                headers.append("X-Goog-Api-Key", "AIzaSyAO_FJ2SlqU8Q4STEHLGCilw_Y9_11qcW8")
                parameters.append("prettyPrint", "false")
            }
        }
    }

    var proxy: Proxy? = null
        set(value) {
            field = value
            client.close()
            client
        }


    //var localeHl =  "en"
    //var localeHl =  Locale.getDefault().toLanguageTag() //"en"

    internal const val browse = "/youtubei/v1/browse"
    internal const val next = "/youtubei/v1/next"
    internal const val player = "/youtubei/v1/player"
    internal const val queue = "/youtubei/v1/music/get_queue"
    internal const val search = "/youtubei/v1/search"
    internal const val searchSuggestions = "/youtubei/v1/music/get_search_suggestions"

    internal const val musicResponsiveListItemRendererMask = "musicResponsiveListItemRenderer(flexColumns,fixedColumns,thumbnail,navigationEndpoint)"
    internal const val musicTwoRowItemRendererMask = "musicTwoRowItemRenderer(thumbnailRenderer,title,subtitle,navigationEndpoint)"
    const val playlistPanelVideoRendererMask = "playlistPanelVideoRenderer(title,navigationEndpoint,longBylineText,shortBylineText,thumbnail,lengthText)"

    internal fun HttpRequestBuilder.mask(value: String = "*") =
        header("X-Goog-FieldMask", value)



    data class Info<T : NavigationEndpoint.Endpoint>(
        val name: String?,
        val endpoint: T?
    ) {
        @Suppress("UNCHECKED_CAST")
        constructor(run: Runs.Run) : this(
            name = run.text,
            endpoint = run.navigationEndpoint?.endpoint as T?
        )
    }

    @JvmInline
    value class SearchFilter(val value: String) {
        companion object {
            val Song = SearchFilter("EgWKAQIIAWoKEAkQBRAKEAMQBA%3D%3D")
            val Video = SearchFilter("EgWKAQIQAWoKEAkQChAFEAMQBA%3D%3D")
            val Album = SearchFilter("EgWKAQIYAWoKEAkQChAFEAMQBA%3D%3D")
            val Artist = SearchFilter("EgWKAQIgAWoKEAkQChAFEAMQBA%3D%3D")
            val CommunityPlaylist = SearchFilter("EgeKAQQoAEABagoQAxAEEAoQCRAF")
            val FeaturedPlaylist = SearchFilter("EgeKAQQoADgBagwQDhAKEAMQBRAJEAQ%3D")
            val Podcast = SearchFilter("EgWKAQJQAWoIEBAQERADEBU%3D")
        }
    }

    sealed class Item {
        abstract val thumbnail: Thumbnail?
        abstract val key: String
    }

    data class SongItem(
        val info: Info<NavigationEndpoint.Endpoint.Watch>?,
        val authors: List<Info<NavigationEndpoint.Endpoint.Browse>>?,
        val album: Info<NavigationEndpoint.Endpoint.Browse>?,
        val durationText: String?,
        override val thumbnail: Thumbnail?,
        val explicit: Boolean = false
    ) : Item() {
        //override val key get() = info!!.endpoint!!.videoId!!
        override val key get() = info?.endpoint?.videoId ?: ""

        companion object
    }

    data class VideoItem(
        val info: Info<NavigationEndpoint.Endpoint.Watch>?,
        val authors: List<Info<NavigationEndpoint.Endpoint.Browse>>?,
        val viewsText: String?,
        val durationText: String?,
        override val thumbnail: Thumbnail?
    ) : Item() {
        override val key get() = info!!.endpoint!!.videoId!!

        val isOfficialMusicVideo: Boolean
            get() = info
                ?.endpoint
                ?.watchEndpointMusicSupportedConfigs
                ?.watchEndpointMusicConfig
                ?.musicVideoType == "MUSIC_VIDEO_TYPE_OMV"

        val isUserGeneratedContent: Boolean
            get() = info
                ?.endpoint
                ?.watchEndpointMusicSupportedConfigs
                ?.watchEndpointMusicConfig
                ?.musicVideoType == "MUSIC_VIDEO_TYPE_UGC"

        companion object
    }

    data class AlbumItem(
        val info: Info<NavigationEndpoint.Endpoint.Browse>?,
        val authors: List<Info<NavigationEndpoint.Endpoint.Browse>>?,
        val year: String?,
        override val thumbnail: Thumbnail?
    ) : Item() {
        override val key get() = info!!.endpoint!!.browseId!!

        companion object
    }

    data class ArtistItem(
        val info: Info<NavigationEndpoint.Endpoint.Browse>?,
        val subscribersCountText: String?,
        override val thumbnail: Thumbnail?
    ) : Item() {
        override val key get() = info!!.endpoint!!.browseId!!

        companion object
    }

    data class PlaylistItem(
        val info: Info<NavigationEndpoint.Endpoint.Browse>?,
        val channel: Info<NavigationEndpoint.Endpoint.Browse>?,
        val songCount: Int?,
        override val thumbnail: Thumbnail?
    ) : Item() {
        override val key get() = info!!.endpoint!!.browseId!!

        companion object
    }

    data class ArtistPage(
        val name: String?,
        val description: String?,
        val subscriberCountText: String?,
        val thumbnail: Thumbnail?,
        val shuffleEndpoint: NavigationEndpoint.Endpoint.Watch?,
        val radioEndpoint: NavigationEndpoint.Endpoint.Watch?,
        val songs: List<SongItem>?,
        val songsEndpoint: NavigationEndpoint.Endpoint.Browse?,
        val albums: List<AlbumItem>?,
        val albumsEndpoint: NavigationEndpoint.Endpoint.Browse?,
        val singles: List<AlbumItem>?,
        val singlesEndpoint: NavigationEndpoint.Endpoint.Browse?,
        val playlists: List<PlaylistItem>?,
    )

    data class PlaylistOrAlbumPage(
        val title: String?,
        val authors: List<Info<NavigationEndpoint.Endpoint.Browse>>?,
        val year: String?,
        val thumbnail: Thumbnail?,
        val url: String?,
        val songsPage: ItemsPage<SongItem>?,
        val otherVersions: List<AlbumItem>?,
        val description: String?,
        val otherInfo: String?
    )

    data class NextPage(
        val itemsPage: ItemsPage<SongItem>?,
        val playlistId: String?,
        val params: String? = null,
        val playlistSetVideoId: String? = null
    )

    data class RelatedPage(
        val songs: List<SongItem>? = null,
        val playlists: List<PlaylistItem>? = null,
        val albums: List<AlbumItem>? = null,
        val artists: List<ArtistItem>? = null,
    )
    data class RelatedSongs(
        val songs: List<SongItem>? = null
    )

    data class DiscoverPage(
        val newReleaseAlbums: List<AlbumItem>,
        val moods: List<Mood.Item>
    )

    data class DiscoverPageAlbums(
        val newReleaseAlbums: List<AlbumItem>

    )

    data class Mood(
        val title: String,
        val items: List<Item>
    ) {
        data class Item(
            val title: String,
            val stripeColor: Long,
            val endpoint: NavigationEndpoint.Endpoint.Browse
        )
    }

    data class ItemsPage<T : Item>(
        var items: List<T>?,
        val continuation: String?
    )

    data class ChartsPage(
        val playlists: List<PlaylistItem>? = null,
        val artists: List<ArtistItem>? = null,
        val videos: List<VideoItem>? = null,
        val songs: List<SongItem>? = null,
        val trending: List<SongItem>? = null
    )

    data class Podcast(
        val title: String,
        //val author: ArtistItem,
        val author: String?,
        val authorThumbnail: String?,
        val thumbnail: List<Thumbnail>,
        val description: String?,
        val listEpisode: List<EpisodeItem>
    ) {
        data class EpisodeItem(
            val title: String,
            //val author: ArtistItem,
            val author: String?,
            val description: String?,
            val thumbnail: List<Thumbnail>,
            val createdDay: String?,
            val durationString: String?,
            val videoId: String
        )
    }

    data class SearchSuggestions(
        val queries: List<String>,
        val recommendedSong: SongItem?,
        val recommendedAlbum: AlbumItem?,
        val recommendedArtist: ArtistItem?,
        val recommendedPlaylist: PlaylistItem?,
        val recommendedVideo: VideoItem?,
    )

    fun MusicNavigationButtonRenderer.toMood(): Mood.Item? {
        return Mood.Item(
            title = buttonText.runs.firstOrNull()?.text ?: return null,
            stripeColor = solid?.leftStripeColor ?: return null,
            endpoint = clickCommand.browseEndpoint ?: return null
        )
    }

    fun List<Thumbnail>.getBestQuality() =
        maxByOrNull { (it.width ?: 0) * (it.height ?: 0) }

}
