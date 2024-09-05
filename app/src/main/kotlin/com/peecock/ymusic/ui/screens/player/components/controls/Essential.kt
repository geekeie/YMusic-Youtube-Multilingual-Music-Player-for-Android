package com.peecock.ymusic.ui.screens.player.components.controls

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.Icon
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import com.peecock.ymusic.Database
import com.peecock.ymusic.R
import com.peecock.ymusic.enums.ColorPaletteName
import com.peecock.ymusic.enums.NavRoutes
import com.peecock.ymusic.enums.PlayerPlayButtonType
import com.peecock.ymusic.models.Info
import com.peecock.ymusic.models.Song
import com.peecock.ymusic.models.ui.UiMedia
import com.peecock.ymusic.query
import com.peecock.ymusic.service.PlayerService
import com.peecock.ymusic.ui.components.themed.IconButton
import com.peecock.ymusic.ui.styling.LocalAppearance
import com.peecock.ymusic.ui.styling.favoritesIcon
import com.peecock.ymusic.utils.bold
import com.peecock.ymusic.utils.colorPaletteNameKey
import com.peecock.ymusic.utils.effectRotationKey
import com.peecock.ymusic.utils.getLikedIcon
import com.peecock.ymusic.utils.getUnlikedIcon
import com.peecock.ymusic.utils.rememberPreference
import com.peecock.ymusic.utils.semiBold
import com.peecock.ymusic.utils.trackLoopEnabledKey
import androidx.compose.ui.graphics.Color
import com.peecock.ymusic.enums.PlayerControlsType
import com.peecock.ymusic.utils.playerControlsTypeKey
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.drawscope.Stroke
import com.peecock.ymusic.enums.ButtonState
import com.peecock.ymusic.enums.ColorPaletteMode
import com.peecock.ymusic.enums.PlayerBackgroundColors
import com.peecock.ymusic.ui.components.themed.SelectorArtistsDialog
import com.peecock.ymusic.ui.items.EXPLICIT_PREFIX
import com.peecock.ymusic.ui.screens.player.bounceClick
import com.peecock.ymusic.utils.buttonStateKey
import com.peecock.ymusic.utils.cleanPrefix
import com.peecock.ymusic.utils.colorPaletteModeKey
import com.peecock.ymusic.utils.playerBackgroundColorsKey
import com.peecock.ymusic.utils.showthumbnailKey
import com.peecock.ymusic.utils.textoutlineKey


@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun InfoAlbumAndArtistEssential(
    binder: PlayerService.Binder,
    navController: NavController,
    albumId: String?,
    media: UiMedia,
    mediaId: String,
    title: String?,
    likedAt: Long?,
    artistIds: List<Info>?,
    artist: String?,
    onCollapse: () -> Unit,
    disableScrollingText: Boolean = false
) {
    val playerControlsType by rememberPreference(playerControlsTypeKey, PlayerControlsType.Modern)
    val (colorPalette, typography) = LocalAppearance.current
    val colorPaletteMode by rememberPreference(colorPaletteModeKey, ColorPaletteMode.System)
    var effectRotationEnabled by rememberPreference(effectRotationKey, true)
    var showthumbnail by rememberPreference(showthumbnailKey, false)
    var isRotated by rememberSaveable { mutableStateOf(false) }
    var showSelectDialog by remember { mutableStateOf(false) }
    var textoutline by rememberPreference(textoutlineKey, false)
    val buttonState by rememberPreference(buttonStateKey, ButtonState.Idle)
    val playerBackgroundColors by rememberPreference(playerBackgroundColorsKey,PlayerBackgroundColors.BlurredCoverColor)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .padding(horizontal = 10.dp)
            .fillMaxWidth()
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {


            var modifierTitle = Modifier
                .clickable {
                    if (albumId != null) {
                        navController.navigate(route = "${NavRoutes.album.name}/${albumId}")
                        onCollapse()
                    }
                }
            if (!disableScrollingText) modifierTitle = modifierTitle.basicMarquee()

            Box(
                modifier = Modifier.weight(0.1f)
            ){
             if (title?.startsWith(EXPLICIT_PREFIX) == true)
                 IconButton(
                     icon = R.drawable.explicit,
                     color = colorPalette.text,
                     enabled = true,
                     onClick = {},
                     modifier = Modifier
                         .size(18.dp)
                 )
            }

            Box(
                modifier = Modifier
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                BasicText(
                    text = cleanPrefix(title ?: ""),
                    style = TextStyle(
                        textAlign = TextAlign.Center,
                        color = if (albumId == null)
                                 if (showthumbnail) colorPalette.textDisabled else if (colorPaletteMode == ColorPaletteMode.Light) colorPalette.textDisabled.copy(0.5f).compositeOver(Color.Black) else colorPalette.textDisabled.copy(0.35f).compositeOver(Color.White)
                                else colorPalette.text,
                        fontStyle = typography.l.bold.fontStyle,
                        fontWeight = typography.l.bold.fontWeight,
                        fontSize = typography.l.bold.fontSize,
                        fontFamily = typography.l.bold.fontFamily
                    ),
                    maxLines = 1,
                    modifier = modifierTitle
                )
                BasicText(
                    text = cleanPrefix(title ?: ""),
                    style = TextStyle(
                        drawStyle = Stroke(width = 1.5f, join = StrokeJoin.Round),
                        textAlign = TextAlign.Center,
                        color = if (!textoutline) Color.Transparent else if (colorPaletteMode == ColorPaletteMode.Light || (colorPaletteMode == ColorPaletteMode.System && (!isSystemInDarkTheme()))) Color.White.copy(0.5f)
                        else Color.Black,
                        fontStyle = typography.l.bold.fontStyle,
                        fontWeight = typography.l.bold.fontWeight,
                        fontSize = typography.l.bold.fontSize,
                        fontFamily = typography.l.bold.fontFamily
                    ),
                    maxLines = 1,
                    modifier = modifierTitle
                )
            }

            //}
            if (playerControlsType == PlayerControlsType.Modern)
             Box(
                 modifier = Modifier.weight(0.1f)
             ) {
                 IconButton(
                     color = colorPalette.favoritesIcon,
                     icon = if (likedAt == null) getUnlikedIcon() else getLikedIcon(),
                     onClick = {
                         val currentMediaItem = binder.player.currentMediaItem
                         query {
                             if (Database.like(
                                     mediaId,
                                     if (likedAt == null) System.currentTimeMillis() else null
                                 ) == 0
                             ) {
                                 currentMediaItem
                                     ?.takeIf { it.mediaId == mediaId }
                                     ?.let {
                                         Database.insert(currentMediaItem, Song::toggleLike)
                                     }
                             }
                         }
                         if (effectRotationEnabled) isRotated = !isRotated
                     },
                     modifier = Modifier
                         .padding(start = 5.dp)
                         .size(24.dp)
                 )
                 if (playerBackgroundColors == PlayerBackgroundColors.BlurredCoverColor) {
                     Icon(
                         painter = painterResource(id = getUnlikedIcon()),
                         tint = colorPalette.text,
                         contentDescription = null,
                         modifier = Modifier
                             .padding(start = 5.dp)
                             .size(24.dp)
                     )
                 }
             }
            else
              Spacer(modifier = Modifier.weight(0.1f))
        }

    }


    Spacer(
        modifier = Modifier
            .height(10.dp)
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
            .padding(horizontal = 10.dp)
            .fillMaxWidth()
    ) {

        if (showSelectDialog)
            SelectorArtistsDialog(
                title = stringResource(R.string.artists),
                onDismiss = { showSelectDialog = false },
                values = artistIds,
                onValueSelected = {
                    navController.navigate(route = "${NavRoutes.artist.name}/${it}")
                    showSelectDialog = false
                    onCollapse()
                }
            )


        var modifierArtist = Modifier
            .clickable {
                if (artistIds?.isNotEmpty() == true && artistIds.size > 1)
                    showSelectDialog = true
                if (artistIds?.isNotEmpty() == true && artistIds.size == 1) {
                    navController.navigate(route = "${NavRoutes.artist.name}/${artistIds[0].id}")
                    onCollapse()
                }
            }
        if (!disableScrollingText) modifierArtist = modifierArtist.basicMarquee()

        Box(
            //modifier = Modifier
        ) {
            BasicText(
                text = artist ?: "",
                style = TextStyle(
                    textAlign = TextAlign.Center,
                    color = if (artistIds?.isEmpty() == true)
                        if (showthumbnail) colorPalette.textDisabled else if (colorPaletteMode == ColorPaletteMode.Light) colorPalette.textDisabled.copy(0.5f).compositeOver(Color.Black) else colorPalette.textDisabled.copy(0.35f).compositeOver(Color.White)
                            else colorPalette.text,
                    fontStyle = typography.m.bold.fontStyle,
                    fontSize = typography.m.bold.fontSize,
                    //fontWeight = typography.m.bold.fontWeight,
                    fontFamily = typography.m.bold.fontFamily
                ),
                maxLines = 1,
                modifier = modifierArtist

            )
            BasicText(
                text = artist ?: "",
                style = TextStyle(
                    drawStyle = Stroke(width = 1.5f, join = StrokeJoin.Round),
                    textAlign = TextAlign.Center,
                    color = if (!textoutline) Color.Transparent else if (colorPaletteMode == ColorPaletteMode.Light || (colorPaletteMode == ColorPaletteMode.System && (!isSystemInDarkTheme()))) Color.White.copy(0.5f)
                    else Color.Black,
                    fontStyle = typography.m.bold.fontStyle,
                    fontSize = typography.m.bold.fontSize,
                    //fontWeight = typography.m.bold.fontWeight,
                    fontFamily = typography.m.bold.fontFamily
                ),
                maxLines = 1,
                modifier = modifierArtist

            )
        }

    }

}

@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ControlsEssential(
    binder: PlayerService.Binder,
    position: Long,
    playbackSpeed: Float,
    shouldBePlaying: Boolean,
    likedAt: Long?,
    mediaId: String,
    playerPlayButtonType: PlayerPlayButtonType,
    rotationAngle: Float,
    isGradientBackgroundEnabled: Boolean,
    onShowSpeedPlayerDialog: () -> Unit,
) {


    val (colorPalette, typography) = LocalAppearance.current
    val colorPaletteName by rememberPreference(colorPaletteNameKey, ColorPaletteName.Dynamic)
    var effectRotationEnabled by rememberPreference(effectRotationKey, true)
    var isRotated by rememberSaveable { mutableStateOf(false) }
    val shouldBePlayingTransition = updateTransition(shouldBePlaying, label = "shouldBePlaying")
    val playPauseRoundness by shouldBePlayingTransition.animateDp(
        transitionSpec = { tween(durationMillis = 100, easing = LinearEasing) },
        label = "playPauseRoundness",
        targetValueByState = { if (it) 32.dp else 16.dp }
    )

    var trackLoopEnabled by rememberPreference(trackLoopEnabledKey, defaultValue = false)
    val playerBackgroundColors by rememberPreference(playerBackgroundColorsKey,PlayerBackgroundColors.BlurredCoverColor)
    Box {
        IconButton(
            color = colorPalette.favoritesIcon,
            icon = if (likedAt == null) getUnlikedIcon() else getLikedIcon(),
            onClick = {
                val currentMediaItem = binder.player.currentMediaItem
                query {
                    if (Database.like(
                            mediaId,
                            if (likedAt == null) System.currentTimeMillis() else null
                        ) == 0
                    ) {
                        currentMediaItem
                            ?.takeIf { it.mediaId == mediaId }
                            ?.let {
                                Database.insert(currentMediaItem, Song::toggleLike)
                            }
                    }
                }
                if (effectRotationEnabled) isRotated = !isRotated
            },
            modifier = Modifier
                //.padding(10.dp)
                .size(26.dp)
        )
        if (playerBackgroundColors == PlayerBackgroundColors.BlurredCoverColor) {
            Icon(
                painter = painterResource(id = getUnlikedIcon()),
                tint = colorPalette.text,
                contentDescription = null,
                modifier = Modifier
                    //.padding(10.dp)
                    .size(26.dp)
            )
        }

    }

    Image(
        painter = painterResource(R.drawable.play_skip_back),
        contentDescription = null,
        colorFilter = ColorFilter.tint(colorPalette.text),
        modifier = Modifier
            .combinedClickable(
                indication = ripple(bounded = false),
                interactionSource = remember { MutableInteractionSource() },
                onClick = {
                    binder.player.seekToPrevious()
                    if (effectRotationEnabled) isRotated = !isRotated
                },
                onLongClick = {
                    binder.player.seekTo(position - 5000)
                }
            )
            .rotate(rotationAngle)
            .padding(10.dp)
            .size(26.dp)

    )



    Box(
        modifier = Modifier
            .combinedClickable(
                indication = ripple(bounded = false),
                interactionSource = remember { MutableInteractionSource() },
                onClick = {
                    if (shouldBePlaying) {
                        //binder.player.pause()
                        binder.callPause({} )
                    } else {
                        /*
                        if (binder.player.playbackState == Player.STATE_IDLE) {
                            binder.player.prepare()
                        }
                         */
                        binder.player.play()
                    }
                    if (effectRotationEnabled) isRotated = !isRotated
                },
                onLongClick = onShowSpeedPlayerDialog
            )
            .bounceClick()
            .clip(RoundedCornerShape(playPauseRoundness))
            .background(
                when (colorPaletteName) {
                    ColorPaletteName.Dynamic, ColorPaletteName.Default,
                    ColorPaletteName.MaterialYou, ColorPaletteName.Customized -> {
                        when (playerPlayButtonType) {
                            PlayerPlayButtonType.CircularRibbed, PlayerPlayButtonType.Disabled -> Color.Transparent
                            else -> {
                                if (isGradientBackgroundEnabled) colorPalette.background1
                                else colorPalette.background2
                            }
                        }
                    }

                    ColorPaletteName.PureBlack, ColorPaletteName.ModernBlack ->
                        if (playerPlayButtonType == PlayerPlayButtonType.CircularRibbed)
                            colorPalette.background1 else
                            if (playerPlayButtonType != PlayerPlayButtonType.Disabled)
                                colorPalette.background4 else Color.Transparent
                }
            )
            .width(playerPlayButtonType.width.dp)
            .height(playerPlayButtonType.height.dp)

    ) {

        if (playerPlayButtonType == PlayerPlayButtonType.CircularRibbed)
            Image(
                painter = painterResource(R.drawable.a13shape),
                colorFilter = ColorFilter.tint(
                    when (colorPaletteName) {
                        ColorPaletteName.PureBlack, ColorPaletteName.ModernBlack -> colorPalette.background4
                        else -> if (isGradientBackgroundEnabled) colorPalette.background1
                        else colorPalette.background2
                    }
                ),
                modifier = Modifier
                    .fillMaxSize()
                    .rotate(rotationAngle)
                    .bounceClick(),
                contentDescription = "Background Image",
                contentScale = ContentScale.Fit
            )

        Image(
            painter = painterResource(if (shouldBePlaying) R.drawable.pause else R.drawable.play),
            contentDescription = null,
            colorFilter = ColorFilter.tint(if (playerPlayButtonType == PlayerPlayButtonType.Disabled) colorPalette.accent else colorPalette.text),
            modifier = Modifier
                .rotate(rotationAngle)
                .align(Alignment.Center)
                .size(if (playerPlayButtonType == PlayerPlayButtonType.Disabled) 40.dp else 30.dp)
                .bounceClick()
        )

        val fmtSpeed = "%.1fx".format(playbackSpeed).replace(",", ".")
        if (fmtSpeed != "1.0x")
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)

            ) {
                BasicText(
                    text = fmtSpeed,
                    style = TextStyle(
                        color = colorPalette.text,
                        fontStyle = typography.xxxs.semiBold.fontStyle,
                        fontSize = typography.xxxs.semiBold.fontSize
                    ),
                    maxLines = 1,
                    modifier = Modifier
                        .padding(bottom = if (playerPlayButtonType != PlayerPlayButtonType.CircularRibbed) 5.dp else 15.dp)
                )
            }
    }




    Image(
        painter = painterResource(R.drawable.play_skip_forward),
        contentDescription = null,
        colorFilter = ColorFilter.tint(colorPalette.text),
        modifier = Modifier
            .combinedClickable(
                indication = ripple(bounded = false),
                interactionSource = remember { MutableInteractionSource() },
                onClick = {
                    //binder.player.forceSeekToNext()
                    binder.player.seekToNext()
                    if (effectRotationEnabled) isRotated = !isRotated
                },
                onLongClick = {
                    binder.player.seekTo(position + 5000)
                }
            )
            .rotate(rotationAngle)
            .padding(10.dp)
            .size(26.dp)

    )



    IconButton(
        icon = R.drawable.repeat,
        color = if (trackLoopEnabled) colorPalette.iconButtonPlayer else colorPalette.textDisabled,
        onClick = {
            trackLoopEnabled = !trackLoopEnabled
        },
        modifier = Modifier
            //.padding(10.dp)
            .size(26.dp)
    )

}