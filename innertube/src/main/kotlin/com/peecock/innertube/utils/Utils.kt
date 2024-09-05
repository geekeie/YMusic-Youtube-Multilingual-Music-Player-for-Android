package com.peecock.innertube.utils

import io.ktor.utils.io.CancellationException
import com.peecock.innertube.Innertube
import com.peecock.innertube.models.SectionListRenderer

internal fun SectionListRenderer.findSectionByTitle(text: String): SectionListRenderer.Content? {
    return contents?.find { content ->
        val title = content
            .musicCarouselShelfRenderer
            ?.header
            ?.musicCarouselShelfBasicHeaderRenderer
            ?.title
            ?: content
                .musicShelfRenderer
                ?.title

        title
            ?.runs
            ?.firstOrNull()
            ?.text == text
    }
}

internal fun SectionListRenderer.findSectionByStrapline(text: String): SectionListRenderer.Content? {
    return contents?.find { content ->
        content
            .musicCarouselShelfRenderer
            ?.header
            ?.musicCarouselShelfBasicHeaderRenderer
            ?.strapline
            ?.runs
            ?.firstOrNull()
            ?.text == text
    }
}

internal inline fun <R> runCatchingNonCancellable(block: () -> R): Result<R>? {
    val result = runCatching(block)
    return when (result.exceptionOrNull()) {
        is CancellationException -> null
        else -> result
    }
}

internal inline fun <T> runCatchingCancellable(block: () -> T) =
    runCatching(block).takeIf { it.exceptionOrNull() !is CancellationException }

infix operator fun <T : Innertube.Item> Innertube.ItemsPage<T>?.plus(other: Innertube.ItemsPage<T>) =
    other.copy(
        items = (this?.items?.plus(other.items ?: emptyList())
            ?: other.items)?.distinctBy(Innertube.Item::key)
    )
