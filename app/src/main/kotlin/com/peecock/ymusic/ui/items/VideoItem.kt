package com.peecock.ymusic.ui.items

import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.peecock.innertube.Innertube
import com.peecock.ymusic.ui.components.themed.TextPlaceholder
import com.peecock.ymusic.ui.styling.LocalAppearance
import com.peecock.ymusic.ui.styling.onOverlay
import com.peecock.ymusic.ui.styling.overlay
import com.peecock.ymusic.ui.styling.shimmer
import com.peecock.ymusic.utils.color
import com.peecock.ymusic.utils.medium
import com.peecock.ymusic.utils.secondary
import com.peecock.ymusic.utils.semiBold

@Composable
fun VideoItem(
    video: Innertube.VideoItem,
    thumbnailHeightDp: Dp,
    thumbnailWidthDp: Dp,
    modifier: Modifier = Modifier
) {
    VideoItem(
        thumbnailUrl = video.thumbnail?.url,
        duration = video.durationText,
        title = video.info?.name,
        uploader = video.authors?.joinToString("") { it.name ?: "" },
        views = video.viewsText,
        thumbnailHeightDp = thumbnailHeightDp,
        thumbnailWidthDp = thumbnailWidthDp,
        modifier = modifier
    )
}

@Composable
fun VideoItem(
    thumbnailUrl: String?,
    duration: String?,
    title: String?,
    uploader: String?,
    views: String?,
    thumbnailHeightDp: Dp,
    thumbnailWidthDp: Dp,
    modifier: Modifier = Modifier
) {
    val (colorPalette, typography, thumbnailShape) = LocalAppearance.current

    ItemContainer(
        alternative = false,
        thumbnailSizeDp = 0.dp,
        modifier = modifier
    ) {
        Box {
            AsyncImage(
                model = thumbnailUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .clip(thumbnailShape)
                    .size(width = thumbnailWidthDp, height = thumbnailHeightDp)
            )

            duration?.let {
                BasicText(
                    text = duration,
                    style = typography.xxs.medium.color(colorPalette.onOverlay),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .padding(all = 4.dp)
                        .background(color = colorPalette.overlay, shape = RoundedCornerShape(2.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                        .align(Alignment.BottomEnd)
                )
            }
        }

        ItemInfoContainer {
            BasicText(
                text = title ?: "",
                style = typography.xs.semiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .basicMarquee(iterations = Int.MAX_VALUE)
            )

            BasicText(
                text = uploader ?: "",
                style = typography.xs.semiBold.secondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .basicMarquee(iterations = Int.MAX_VALUE)
            )

            views?.let {
                BasicText(
                    text = views,
                    style = typography.xxs.medium.secondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .basicMarquee(iterations = Int.MAX_VALUE)
                )
            }
        }
    }
}

@Composable
fun VideoItemPlaceholder(
    thumbnailHeightDp: Dp,
    thumbnailWidthDp: Dp,
    modifier: Modifier = Modifier
) {
    val (colorPalette, _, thumbnailShape) = LocalAppearance.current

    ItemContainer(
        alternative = false,
        thumbnailSizeDp = 0.dp,
        modifier = modifier
    ) {
        Spacer(
            modifier = Modifier
                .background(color = colorPalette.shimmer, shape = thumbnailShape)
                .size(width = thumbnailWidthDp, height = thumbnailHeightDp)
        )

        ItemInfoContainer {
            TextPlaceholder()
            TextPlaceholder()
            TextPlaceholder(
                modifier = Modifier
                    .padding(top = 8.dp)
            )
        }
    }
}
