package com.peecock.innertube.utils

import com.peecock.innertube.Innertube
import com.peecock.innertube.Innertube.getBestQuality
import com.peecock.innertube.models.MusicResponsiveListItemRenderer
import com.peecock.innertube.models.NavigationEndpoint

fun Innertube.SongItem.Companion.from(renderer: MusicResponsiveListItemRenderer): Innertube.SongItem? {
    val albumId = renderer
        .flexColumns
        .getOrNull(2)
        ?.musicResponsiveListItemFlexColumnRenderer
        ?.text
        ?.runs
        ?.firstOrNull()
        ?.navigationEndpoint?.browseEndpoint?.browseId
    /*
    // Album row is variable from 2 to 3
    val albumId1 = renderer
        .flexColumns
        .getOrNull(3)
        ?.musicResponsiveListItemFlexColumnRenderer
        ?.text
        ?.runs
        ?.firstOrNull()
        ?.navigationEndpoint?.browseEndpoint?.browseId
     */
    val albumRow = if (albumId == null) 3 else 2

    val explicitBadge = if (renderer
        .badges
        ?.find {
            it.musicInlineBadgeRenderer.icon.iconType == "MUSIC_EXPLICIT_BADGE"
        } != null) "e:" else ""

    /*
    println("mediaItem badges ${
        renderer
            .badges
            ?.find {
                it.musicInlineBadgeRenderer.icon.iconType == "MUSIC_EXPLICIT_BADGE"
            } != null
    }")

     */

    return Innertube.SongItem(
        info = renderer
            .flexColumns
            .getOrNull(0)
            ?.musicResponsiveListItemFlexColumnRenderer
            ?.text
            ?.runs
            ?.getOrNull(0)
            ?.let {
                if (it.navigationEndpoint?.endpoint is NavigationEndpoint.Endpoint.Watch) Innertube.Info(
                    name = "${explicitBadge}${it.text}",
                    endpoint = it.navigationEndpoint.endpoint as NavigationEndpoint.Endpoint.Watch
                ) else null
            },
            //?.let(Innertube::Info),

        authors = renderer
            .flexColumns
            .getOrNull(1)
            ?.musicResponsiveListItemFlexColumnRenderer
            ?.text
            ?.runs
            //?.map<Runs.Run, Innertube.Info<NavigationEndpoint.Endpoint.Browse>>(Innertube::Info)
            ?.map { Innertube.Info(name = it.text, endpoint = it.navigationEndpoint?.endpoint) }
            ?.filterIsInstance<Innertube.Info<NavigationEndpoint.Endpoint.Browse>>()
            ?.takeIf(List<Any>::isNotEmpty),
        durationText = renderer
            .fixedColumns
            ?.getOrNull(0)
            ?.musicResponsiveListItemFlexColumnRenderer
            ?.text
            ?.runs
            ?.getOrNull(0)
            ?.text,
        album = renderer
            .flexColumns
            .getOrNull(albumRow)
            ?.musicResponsiveListItemFlexColumnRenderer
            ?.text
            ?.runs
            ?.firstOrNull()
            ?.let(Innertube::Info),
        thumbnail = renderer
            .thumbnail
            ?.musicThumbnailRenderer
            ?.thumbnail
            ?.thumbnails
            ?.getBestQuality(),
            //?.lastOrNull(),
        explicit = renderer
            .badges
            ?.find {
                it.musicInlineBadgeRenderer.icon.iconType == "MUSIC_EXPLICIT_BADGE"
           } != null,
    ).takeIf { it.info?.endpoint?.videoId != null }
}
