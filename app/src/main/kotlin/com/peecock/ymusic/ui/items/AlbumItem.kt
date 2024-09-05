package com.peecock.ymusic.ui.items

import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.peecock.innertube.Innertube
import com.peecock.ymusic.models.Album
import com.peecock.ymusic.ui.components.themed.TextPlaceholder
import com.peecock.ymusic.ui.styling.LocalAppearance
import com.peecock.ymusic.ui.styling.shimmer
import com.peecock.ymusic.utils.cleanPrefix
import com.peecock.ymusic.utils.secondary
import com.peecock.ymusic.utils.semiBold
import com.peecock.ymusic.utils.thumbnail

@Composable
fun AlbumItem(
    album: Album,
    thumbnailSizePx: Int,
    thumbnailSizeDp: Dp,
    modifier: Modifier = Modifier,
    alternative: Boolean = false,
    yearCentered: Boolean? = true,
    showAuthors: Boolean? = false
) {
    AlbumItem(
        thumbnailUrl = album.thumbnailUrl,
        title = album.title,
        authors = album.authorsText,
        year = album.year,
        yearCentered = yearCentered,
        thumbnailSizePx = thumbnailSizePx,
        thumbnailSizeDp = thumbnailSizeDp,
        alternative = alternative,
        showAuthors = showAuthors,
        modifier = modifier
    )
}

@Composable
fun AlbumItem(
    album: Innertube.AlbumItem,
    thumbnailSizePx: Int,
    thumbnailSizeDp: Dp,
    modifier: Modifier = Modifier,
    alternative: Boolean = false,
    yearCentered: Boolean? = true,
    showAuthors: Boolean? = false
) {
    AlbumItem(
        thumbnailUrl = album.thumbnail?.url,
        title = album.info?.name,
        authors = album.authors?.joinToString("") { it.name ?: "" },
        year = album.year,
        yearCentered = yearCentered,
        thumbnailSizePx = thumbnailSizePx,
        thumbnailSizeDp = thumbnailSizeDp,
        alternative = alternative,
        modifier = modifier
    )
}

@Composable
fun AlbumItem(
    thumbnailUrl: String?,
    title: String?,
    authors: String?,
    year: String?,
    yearCentered: Boolean? = true,
    thumbnailSizePx: Int,
    thumbnailSizeDp: Dp,
    modifier: Modifier = Modifier,
    alternative: Boolean = false,
    showAuthors: Boolean? = false
) {
    val (_, typography, thumbnailShape) = LocalAppearance.current

    ItemContainer(
        alternative = alternative,
        thumbnailSizeDp = thumbnailSizeDp,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        AsyncImage(
            model = thumbnailUrl?.thumbnail(thumbnailSizePx)?.let { it1 -> cleanPrefix(it1) },
            contentDescription = null,
            //contentScale = ContentScale.Crop,
            modifier = Modifier
                .clip(thumbnailShape)
                .size(thumbnailSizeDp)
        )

        ItemInfoContainer {
            BasicText(
                text = cleanPrefix(title ?: ""),
                style = typography.xs.semiBold,
                maxLines = 1, //if (alternative) 1 else 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .basicMarquee(iterations = Int.MAX_VALUE)
            )

            if (!alternative || showAuthors == true) {
                authors?.let {
                    BasicText(
                        text = cleanPrefix(authors),
                        style = typography.xs.semiBold.secondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .basicMarquee(iterations = Int.MAX_VALUE)
                            .align(
                                if (yearCentered == true) Alignment.CenterHorizontally else Alignment.Start)
                    )
                }
            }

            BasicText(
                text = year ?: "",
                style = typography.xxs.semiBold.secondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .padding(top = 4.dp)
                    .align(
                        if (yearCentered == true) Alignment.CenterHorizontally else Alignment.Start)
            )
        }
    }
}

@Composable
fun AlbumItemPlaceholder(
    thumbnailSizeDp: Dp,
    modifier: Modifier = Modifier,
    alternative: Boolean = false
) {
    val (colorPalette, _, thumbnailShape) = LocalAppearance.current

    ItemContainer(
        alternative = alternative,
        thumbnailSizeDp = thumbnailSizeDp,
        modifier = modifier
    ) {
        Spacer(
            modifier = Modifier
                .background(color = colorPalette.shimmer, shape = thumbnailShape)
                .size(thumbnailSizeDp)
        )

        ItemInfoContainer {
            TextPlaceholder()

            if (!alternative) {
                TextPlaceholder()
            }

            TextPlaceholder(
                modifier = Modifier
                    .padding(top = 4.dp)
            )
        }
    }
}
