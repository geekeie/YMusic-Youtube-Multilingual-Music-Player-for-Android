package com.peecock.ymusic.ui.screens.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import com.peecock.ymusic.R
import com.peecock.ymusic.enums.BackgroundProgress
import com.peecock.ymusic.enums.CarouselSize
import com.peecock.ymusic.enums.ClickLyricsText
import com.peecock.ymusic.enums.IconLikeType
import com.peecock.ymusic.enums.MiniPlayerType
import com.peecock.ymusic.enums.NavigationBarPosition
import com.peecock.ymusic.enums.PlayerBackgroundColors
import com.peecock.ymusic.enums.PlayerControlsType
import com.peecock.ymusic.enums.PlayerInfoType
import com.peecock.ymusic.enums.PlayerPlayButtonType
import com.peecock.ymusic.enums.PlayerThumbnailSize
import com.peecock.ymusic.enums.PlayerTimelineSize
import com.peecock.ymusic.enums.PlayerTimelineType
import com.peecock.ymusic.enums.PlayerType
import com.peecock.ymusic.enums.PrevNextSongs
import com.peecock.ymusic.enums.QueueType
import com.peecock.ymusic.enums.SongsNumber
import com.peecock.ymusic.enums.ThumbnailRoundness
import com.peecock.ymusic.enums.ThumbnailType
import com.peecock.ymusic.ui.components.themed.HeaderIconButton
import com.peecock.ymusic.ui.components.themed.HeaderWithIcon
import com.peecock.ymusic.ui.components.themed.IconButton
import com.peecock.ymusic.ui.styling.Dimensions
import com.peecock.ymusic.ui.styling.LocalAppearance
import com.peecock.ymusic.ui.styling.favoritesIcon
import com.peecock.ymusic.utils.actionspacedevenlyKey
import com.peecock.ymusic.utils.backgroundProgressKey
import com.peecock.ymusic.utils.clickLyricsTextKey
import com.peecock.ymusic.utils.disablePlayerHorizontalSwipeKey
import com.peecock.ymusic.utils.disableScrollingTextKey
import com.peecock.ymusic.utils.effectRotationKey
import com.peecock.ymusic.utils.expandedplayertoggleKey
import com.peecock.ymusic.utils.iconLikeTypeKey
import com.peecock.ymusic.utils.isAtLeastAndroid13
import com.peecock.ymusic.utils.isLandscape
import com.peecock.ymusic.utils.isShowingThumbnailInLockscreenKey
import com.peecock.ymusic.utils.lastPlayerPlayButtonTypeKey
import com.peecock.ymusic.utils.miniPlayerTypeKey
import com.peecock.ymusic.utils.navigationBarPositionKey
import com.peecock.ymusic.utils.playerBackgroundColorsKey
import com.peecock.ymusic.utils.playerControlsTypeKey
import com.peecock.ymusic.utils.playerEnableLyricsPopupMessageKey
import com.peecock.ymusic.utils.playerInfoTypeKey
import com.peecock.ymusic.utils.playerPlayButtonTypeKey
import com.peecock.ymusic.utils.playerSwapControlsWithTimelineKey
import com.peecock.ymusic.utils.playerThumbnailSizeKey
import com.peecock.ymusic.utils.playerTimelineSizeKey
import com.peecock.ymusic.utils.playerTimelineTypeKey
import com.peecock.ymusic.utils.rememberPreference
import com.peecock.ymusic.utils.secondary
import com.peecock.ymusic.utils.semiBold
import com.peecock.ymusic.utils.showBackgroundLyricsKey
import com.peecock.ymusic.utils.showButtonPlayerAddToPlaylistKey
import com.peecock.ymusic.utils.showButtonPlayerArrowKey
import com.peecock.ymusic.utils.showButtonPlayerDownloadKey
import com.peecock.ymusic.utils.showButtonPlayerLoopKey
import com.peecock.ymusic.utils.showButtonPlayerLyricsKey
import com.peecock.ymusic.utils.showButtonPlayerMenuKey
import com.peecock.ymusic.utils.showButtonPlayerShuffleKey
import com.peecock.ymusic.utils.showButtonPlayerSleepTimerKey
import com.peecock.ymusic.utils.showButtonPlayerSystemEqualizerKey
import com.peecock.ymusic.utils.showDownloadButtonBackgroundPlayerKey
import com.peecock.ymusic.utils.showLikeButtonBackgroundPlayerKey
import com.peecock.ymusic.utils.showNextSongsInPlayerKey
import com.peecock.ymusic.utils.showRemainingSongTimeKey
import com.peecock.ymusic.utils.showTopActionsBarKey
import com.peecock.ymusic.utils.showTotalTimeQueueKey
import com.peecock.ymusic.utils.showlyricsthumbnailKey
import com.peecock.ymusic.utils.showthumbnailKey
import com.peecock.ymusic.utils.thumbnailRoundnessKey
import com.peecock.ymusic.utils.thumbnailTapEnabledKey
import com.peecock.ymusic.utils.transparentBackgroundPlayerActionBarKey
import com.peecock.ymusic.utils.transparentbarKey
import com.peecock.ymusic.utils.blackgradientKey
import com.peecock.ymusic.utils.visualizerEnabledKey
import com.peecock.ymusic.utils.bottomgradientKey
import com.peecock.ymusic.utils.buttonzoomoutKey
import com.peecock.ymusic.utils.carouselKey
import com.peecock.ymusic.utils.carouselSizeKey
import com.peecock.ymusic.utils.expandedlyricsKey
import com.peecock.ymusic.utils.fadingedgeKey
import com.peecock.ymusic.utils.showalbumcoverKey
import com.peecock.ymusic.utils.showsongsKey
import com.peecock.ymusic.utils.showvisthumbnailKey
import com.peecock.ymusic.utils.textoutlineKey
import com.peecock.ymusic.utils.thumbnailTypeKey
import com.peecock.ymusic.utils.thumbnailpauseKey
import com.peecock.ymusic.utils.playerTypeKey
import com.peecock.ymusic.utils.prevNextSongsKey
import com.peecock.ymusic.utils.showButtonPlayerDiscoverKey
import com.peecock.ymusic.utils.statsfornerdsKey
import com.peecock.ymusic.utils.tapqueueKey
import com.peecock.ymusic.utils.noblurKey
import com.peecock.ymusic.utils.keepPlayerMinimizedKey
import com.peecock.ymusic.utils.playerInfoShowIconsKey
import com.peecock.ymusic.utils.swipeUpQueueKey
import com.peecock.ymusic.utils.queueTypeKey

@ExperimentalAnimationApi
@UnstableApi
@Composable
fun AppearanceSettings() {

    var isShowingThumbnailInLockscreen by rememberPreference(
        isShowingThumbnailInLockscreenKey,
        true
    )

    var showthumbnail by rememberPreference(showthumbnailKey, false)
    var transparentbar by rememberPreference(transparentbarKey, true)
    var blackgradient by rememberPreference(blackgradientKey, false)
    var showlyricsthumbnail by rememberPreference(showlyricsthumbnailKey, false)
    var playerPlayButtonType by rememberPreference(
        playerPlayButtonTypeKey,
        PlayerPlayButtonType.Rectangular
    )
    var bottomgradient by rememberPreference(bottomgradientKey, false)
    var textoutline by rememberPreference(textoutlineKey, false)

    var lastPlayerPlayButtonType by rememberPreference(
        lastPlayerPlayButtonTypeKey,
        PlayerPlayButtonType.Rectangular
    )
    var disablePlayerHorizontalSwipe by rememberPreference(disablePlayerHorizontalSwipeKey, false)

    var disableScrollingText by rememberPreference(disableScrollingTextKey, false)
    var showLikeButtonBackgroundPlayer by rememberPreference(
        showLikeButtonBackgroundPlayerKey,
        true
    )
    var showDownloadButtonBackgroundPlayer by rememberPreference(
        showDownloadButtonBackgroundPlayerKey,
        true
    )
    var visualizerEnabled by rememberPreference(visualizerEnabledKey, false)
    /*
    var playerVisualizerType by rememberPreference(
        playerVisualizerTypeKey,
        PlayerVisualizerType.Disabled
    )
    */
    var playerTimelineType by rememberPreference(playerTimelineTypeKey, PlayerTimelineType.Default)
    var playerThumbnailSize by rememberPreference(
        playerThumbnailSizeKey,
        PlayerThumbnailSize.Biggest
    )
    var playerTimelineSize by rememberPreference(
        playerTimelineSizeKey,
        PlayerTimelineSize.Biggest
    )

    var effectRotationEnabled by rememberPreference(effectRotationKey, true)

    var thumbnailTapEnabled by rememberPreference(thumbnailTapEnabledKey, false)


    var showButtonPlayerAddToPlaylist by rememberPreference(showButtonPlayerAddToPlaylistKey, true)
    var showButtonPlayerArrow by rememberPreference(showButtonPlayerArrowKey, false)
    var showButtonPlayerDownload by rememberPreference(showButtonPlayerDownloadKey, true)
    var showButtonPlayerLoop by rememberPreference(showButtonPlayerLoopKey, true)
    var showButtonPlayerLyrics by rememberPreference(showButtonPlayerLyricsKey, true)
    var expandedplayertoggle by rememberPreference(expandedplayertoggleKey, true)
    var showButtonPlayerShuffle by rememberPreference(showButtonPlayerShuffleKey, true)
    var showButtonPlayerSleepTimer by rememberPreference(showButtonPlayerSleepTimerKey, false)
    var showButtonPlayerMenu by rememberPreference(showButtonPlayerMenuKey, false)
    var showButtonPlayerSystemEqualizer by rememberPreference(
        showButtonPlayerSystemEqualizerKey,
        false
    )
    var showButtonPlayerDiscover by rememberPreference(showButtonPlayerDiscoverKey, false)

    val navigationBarPosition by rememberPreference(
        navigationBarPositionKey,
        NavigationBarPosition.Bottom
    )

    //var isGradientBackgroundEnabled by rememberPreference(isGradientBackgroundEnabledKey, false)
    var showTotalTimeQueue by rememberPreference(showTotalTimeQueueKey, true)
    var backgroundProgress by rememberPreference(
        backgroundProgressKey,
        BackgroundProgress.Both
    )
    var showNextSongsInPlayer by rememberPreference(showNextSongsInPlayerKey, false)
    var showRemainingSongTime by rememberPreference(showRemainingSongTimeKey, true)
    var clickLyricsText by rememberPreference(clickLyricsTextKey, ClickLyricsText.FullScreen)
    var showBackgroundLyrics by rememberPreference(showBackgroundLyricsKey, false)
    val (colorPalette, typography, thumbnailShape) = LocalAppearance.current
    var searching by rememberSaveable { mutableStateOf(false) }
    var filter: String? by rememberSaveable { mutableStateOf(null) }
    // var filterCharSequence: CharSequence
    var filterCharSequence: CharSequence = filter.toString()
    var thumbnailRoundness by rememberPreference(
        thumbnailRoundnessKey,
        ThumbnailRoundness.Heavy

    )

    var miniPlayerType by rememberPreference(
        miniPlayerTypeKey,
        MiniPlayerType.Modern
    )
    var playerBackgroundColors by rememberPreference(
        playerBackgroundColorsKey,
        PlayerBackgroundColors.BlurredCoverColor
    )

    var showTopActionsBar by rememberPreference(showTopActionsBarKey, true)
    var playerControlsType by rememberPreference(playerControlsTypeKey, PlayerControlsType.Modern)
    var playerInfoType by rememberPreference(playerInfoTypeKey, PlayerInfoType.Modern)
    var transparentBackgroundActionBarPlayer by rememberPreference(
        transparentBackgroundPlayerActionBarKey,
        false
    )
    var iconLikeType by rememberPreference(iconLikeTypeKey, IconLikeType.Essential)
    var playerSwapControlsWithTimeline by rememberPreference(
        playerSwapControlsWithTimelineKey,
        false
    )
    var playerEnableLyricsPopupMessage by rememberPreference(
        playerEnableLyricsPopupMessageKey,
        true
    )
    var actionspacedevenly by rememberPreference(actionspacedevenlyKey, false)
    var thumbnailType by rememberPreference(thumbnailTypeKey, ThumbnailType.Modern)
    var showvisthumbnail by rememberPreference(showvisthumbnailKey, false)
    var expandedlyrics by rememberPreference(expandedlyricsKey, true)
    var buttonzoomout by rememberPreference(buttonzoomoutKey, false)
    var thumbnailpause by rememberPreference(thumbnailpauseKey, false)
    var showsongs by rememberPreference(showsongsKey, SongsNumber.`2`)
    var showalbumcover by rememberPreference(showalbumcoverKey, true)
    var prevNextSongs by rememberPreference(prevNextSongsKey, PrevNextSongs.twosongs)
    var tapqueue by rememberPreference(tapqueueKey, true)
    var swipeUpQueue by rememberPreference(swipeUpQueueKey, true)
    var statsfornerds by rememberPreference(statsfornerdsKey, false)

    var playerType by rememberPreference(playerTypeKey, PlayerType.Essential)
    var queueType by rememberPreference(queueTypeKey, QueueType.Essential)
    var noblur by rememberPreference(noblurKey, true)
    var fadingedge by rememberPreference(fadingedgeKey, false)
    var carousel by rememberPreference(carouselKey, true)
    var carouselSize by rememberPreference(carouselSizeKey, CarouselSize.Biggest)
    var keepPlayerMinimized by rememberPreference(keepPlayerMinimizedKey,false)
    var playerInfoShowIcons by rememberPreference(playerInfoShowIconsKey, true)

    Column(
        modifier = Modifier
            .background(colorPalette.background0)
            //.fillMaxSize()
            .fillMaxHeight()
            .fillMaxWidth(
                if (navigationBarPosition == NavigationBarPosition.Left ||
                    navigationBarPosition == NavigationBarPosition.Top ||
                    navigationBarPosition == NavigationBarPosition.Bottom
                ) 1f
                else Dimensions.contentWidthRightBar
            )
            .verticalScroll(rememberScrollState())
        /*
        .padding(
            LocalPlayerAwareWindowInsets.current
                .only(WindowInsetsSides.Vertical + WindowInsetsSides.End)
                .asPaddingValues()
        )

         */
    ) {
        HeaderWithIcon(
            title = stringResource(R.string.player_appearance),
            iconId = R.drawable.color_palette,
            enabled = false,
            showIcon = true,
            modifier = Modifier,
            onClick = {}
        )

        HeaderIconButton(
            modifier = Modifier.padding(start = 25.dp),
            onClick = { searching = !searching },
            icon = R.drawable.search_circle,
            color = colorPalette.text,
            iconSize = 24.dp
        )
        /*   Search   */
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Bottom,
            modifier = Modifier
                .padding(all = 10.dp)
                .fillMaxWidth()
        ) {
            AnimatedVisibility(visible = searching) {
                val focusRequester = remember { FocusRequester() }
                val focusManager = LocalFocusManager.current
                val keyboardController = LocalSoftwareKeyboardController.current

                LaunchedEffect(searching) {
                    focusRequester.requestFocus()
                }

                BasicTextField(
                    value = filter ?: "",
                    onValueChange = { filter = it },
                    textStyle = typography.xs.semiBold,
                    singleLine = true,
                    maxLines = 1,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        if (filter.isNullOrBlank()) filter = ""
                        focusManager.clearFocus()
                    }),
                    cursorBrush = SolidColor(colorPalette.text),
                    decorationBox = { innerTextField ->
                        Box(
                            contentAlignment = Alignment.CenterStart,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 10.dp)
                        ) {
                            IconButton(
                                onClick = {},
                                icon = R.drawable.search,
                                color = colorPalette.favoritesIcon,
                                modifier = Modifier
                                    .align(Alignment.CenterStart)
                                    .size(16.dp)
                            )
                        }
                        Box(
                            contentAlignment = Alignment.CenterStart,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 30.dp)
                        ) {
                            androidx.compose.animation.AnimatedVisibility(
                                visible = filter?.isEmpty() ?: true,
                                enter = fadeIn(tween(100)),
                                exit = fadeOut(tween(100)),
                            ) {
                                BasicText(
                                    text = stringResource(R.string.search),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = typography.xs.semiBold.secondary.copy(color = colorPalette.textDisabled),
                                )
                            }

                            innerTextField()
                        }
                    },
                    modifier = Modifier
                        .height(30.dp)
                        .fillMaxWidth()
                        .background(
                            colorPalette.background4,
                            shape = thumbnailRoundness.shape()
                        )
                        .focusRequester(focusRequester)
                        .onFocusChanged {
                            if (!it.hasFocus) {
                                keyboardController?.hide()
                                if (filter?.isBlank() == true) {
                                    filter = null
                                    searching = false
                                }
                            }
                        }
                )
            }
        }
        /*  Search  */

        //SettingsEntryGroupText(stringResource(R.string.user_interface))

        //SettingsGroupSpacer()
        SettingsEntryGroupText(title = stringResource(R.string.player))

        if (playerBackgroundColors != PlayerBackgroundColors.BlurredCoverColor)
            showthumbnail = true
        if (!visualizerEnabled) showvisthumbnail = false
        if (!showthumbnail) {showlyricsthumbnail = false; showvisthumbnail = false}
        if (playerType == PlayerType.Modern) {
            showlyricsthumbnail = false
            showvisthumbnail = false
            thumbnailpause = false
            //keepPlayerMinimized = false
        }

        if (showlyricsthumbnail) expandedlyrics = false

        if (filter.isNullOrBlank() || stringResource(R.string.show_player_top_actions_bar).contains(
                filterCharSequence,
                true
            )
        )
            SwitchSettingEntry(
                title = stringResource(R.string.show_player_top_actions_bar),
                text = "",
                isChecked = showTopActionsBar,
                onCheckedChange = { showTopActionsBar = it }
            )
        if (filter.isNullOrBlank() || stringResource(R.string.playertype).contains(
                filterCharSequence,
                true
            )
        )
            EnumValueSelectorSettingsEntry(
                title = stringResource(R.string.playertype),
                selectedValue = playerType,
                onValueSelected = {
                    playerType = it
                },
                valueText = {
                    when (it) {
                        PlayerType.Modern -> stringResource(R.string.pcontrols_modern)
                        PlayerType.Essential -> stringResource(R.string.pcontrols_essential)
                    }
                },
            )

        if (filter.isNullOrBlank() || stringResource(R.string.queuetype).contains(
                filterCharSequence,
                true
            )
        )
            EnumValueSelectorSettingsEntry(
                title = stringResource(R.string.queuetype),
                selectedValue = queueType,
                onValueSelected = {
                    queueType = it
                },
                valueText = {
                    when (it) {
                        QueueType.Modern -> stringResource(R.string.pcontrols_modern)
                        QueueType.Essential -> stringResource(R.string.pcontrols_essential)
                    }
                },
            )

        if (playerBackgroundColors == PlayerBackgroundColors.BlurredCoverColor) {
            if (filter.isNullOrBlank() || stringResource(R.string.show_thumbnail).contains(
                    filterCharSequence,
                    true
                )
            )
                SwitchSettingEntry(
                    title = stringResource(R.string.show_thumbnail),
                    text = "",
                    isChecked = showthumbnail,
                    onCheckedChange = {showthumbnail = it},
                )
        }
        AnimatedVisibility(visible = showthumbnail) {
            Column {
                if (playerType == PlayerType.Modern) {
                    if (filter.isNullOrBlank() || stringResource(R.string.fadingedge).contains(
                            filterCharSequence,
                            true
                        )
                    )
                        SwitchSettingEntry(
                            title = stringResource(R.string.fadingedge),
                            text = "",
                            isChecked = fadingedge,
                            onCheckedChange = { fadingedge = it },
                            modifier = Modifier.padding(start = if (playerBackgroundColors == PlayerBackgroundColors.BlurredCoverColor) 25.dp else 0.dp)
                        )
                }

                if (playerType == PlayerType.Modern && !isLandscape) {
                    if (filter.isNullOrBlank() || stringResource(R.string.carousel).contains(
                            filterCharSequence,
                            true
                        )
                    )
                        SwitchSettingEntry(
                            title = stringResource(R.string.carousel),
                            text = "",
                            isChecked = carousel,
                            onCheckedChange = { carousel = it },
                            modifier = Modifier.padding(start = if (playerBackgroundColors == PlayerBackgroundColors.BlurredCoverColor) 25.dp else 0.dp)
                        )
                    if (carousel) {
                        if (filter.isNullOrBlank() || stringResource(R.string.carouselsize).contains(
                                filterCharSequence,
                                true
                            )
                        )
                            EnumValueSelectorSettingsEntry(
                                title = stringResource(R.string.carouselsize),
                                selectedValue = carouselSize,
                                onValueSelected = { carouselSize = it },
                                valueText = {
                                    when (it) {
                                        CarouselSize.Small -> stringResource(R.string.small)
                                        CarouselSize.Medium -> stringResource(R.string.medium)
                                        CarouselSize.Big -> stringResource(R.string.big)
                                        CarouselSize.Biggest -> stringResource(R.string.biggest)
                                        CarouselSize.Expanded -> stringResource(R.string.expanded)
                                    }
                                },
                                modifier = Modifier.padding(start = if (playerBackgroundColors == PlayerBackgroundColors.BlurredCoverColor) 25.dp else 0.dp)
                            )
                    }
                }
                if (playerType == PlayerType.Essential) {
                    if (filter.isNullOrBlank() || stringResource(R.string.thumbnailpause).contains(
                            filterCharSequence,
                            true
                        )
                    )
                        SwitchSettingEntry(
                            title = stringResource(R.string.thumbnailpause),
                            text = "",
                            isChecked = thumbnailpause,
                            onCheckedChange = { thumbnailpause = it },
                            modifier = Modifier.padding(start = if (playerBackgroundColors == PlayerBackgroundColors.BlurredCoverColor) 25.dp else 0.dp)
                        )

                    if (filter.isNullOrBlank() || stringResource(R.string.show_lyrics_thumbnail).contains(
                            filterCharSequence,
                            true
                        )
                    )
                        SwitchSettingEntry(
                            title = stringResource(R.string.show_lyrics_thumbnail),
                            text = "",
                            isChecked = showlyricsthumbnail,
                            onCheckedChange = { showlyricsthumbnail = it },
                            modifier = Modifier.padding(start = if (playerBackgroundColors == PlayerBackgroundColors.BlurredCoverColor) 25.dp else 0.dp)
                        )
                    if (visualizerEnabled) {
                        if (filter.isNullOrBlank() || stringResource(R.string.showvisthumbnail).contains(
                                filterCharSequence,
                                true
                            )
                        )
                            SwitchSettingEntry(
                                title = stringResource(R.string.showvisthumbnail),
                                text = "",
                                isChecked = showvisthumbnail,
                                onCheckedChange = { showvisthumbnail = it },
                                modifier = Modifier.padding(start = if (playerBackgroundColors == PlayerBackgroundColors.BlurredCoverColor) 25.dp else 0.dp)
                            )
                    }
                }

                if (filter.isNullOrBlank() || stringResource(R.string.player_thumbnail_size).contains(
                        filterCharSequence,
                        true
                    )
                )
                    EnumValueSelectorSettingsEntry(
                        title = stringResource(R.string.player_thumbnail_size),
                        selectedValue = playerThumbnailSize,
                        onValueSelected = { playerThumbnailSize = it },
                        valueText = {
                            when (it) {
                                PlayerThumbnailSize.Small -> stringResource(R.string.small)
                                PlayerThumbnailSize.Medium -> stringResource(R.string.medium)
                                PlayerThumbnailSize.Big -> stringResource(R.string.big)
                                PlayerThumbnailSize.Biggest -> stringResource(R.string.biggest)
                                PlayerThumbnailSize.Expanded -> stringResource(R.string.expanded)
                            }
                        },
                        modifier = Modifier.padding(start = if (playerBackgroundColors == PlayerBackgroundColors.BlurredCoverColor) 25.dp else 0.dp)
                    )
                if (filter.isNullOrBlank() || stringResource(R.string.thumbnailtype).contains(
                        filterCharSequence,
                        true
                    )
                )
                    EnumValueSelectorSettingsEntry(
                        title = stringResource(R.string.thumbnailtype),
                        selectedValue = thumbnailType,
                        onValueSelected = {
                            thumbnailType = it
                        },
                        valueText = {
                            when (it) {
                                ThumbnailType.Modern -> stringResource(R.string.pcontrols_modern)
                                ThumbnailType.Essential -> stringResource(R.string.pcontrols_essential)
                            }
                        },
                        modifier = Modifier.padding(start = if (playerBackgroundColors == PlayerBackgroundColors.BlurredCoverColor) 25.dp else 0.dp)
                    )

                if (filter.isNullOrBlank() || stringResource(R.string.thumbnail_roundness).contains(
                        filterCharSequence,
                        true
                    )
                )
                    EnumValueSelectorSettingsEntry(
                        title = stringResource(R.string.thumbnail_roundness),
                        selectedValue = thumbnailRoundness,
                        onValueSelected = { thumbnailRoundness = it },
                        trailingContent = {
                            Spacer(
                                modifier = Modifier
                                    .border(
                                        width = 1.dp,
                                        color = colorPalette.accent,
                                        shape = thumbnailRoundness.shape()
                                    )
                                    .background(
                                        color = colorPalette.background1,
                                        shape = thumbnailRoundness.shape()
                                    )
                                    .size(36.dp)
                            )
                        },
                        valueText = {
                            when (it) {
                                ThumbnailRoundness.None -> stringResource(R.string.none)
                                ThumbnailRoundness.Light -> stringResource(R.string.light)
                                ThumbnailRoundness.Heavy -> stringResource(R.string.heavy)
                                ThumbnailRoundness.Medium -> stringResource(R.string.medium)
                            }
                        },
                        modifier = Modifier.padding(start = if (playerBackgroundColors == PlayerBackgroundColors.BlurredCoverColor) 25.dp else 0.dp)
                    )
            }
        }
        if (!showthumbnail) {
            if (filter.isNullOrBlank() || stringResource(R.string.noblur).contains(
                    filterCharSequence,
                    true
                )
            )
                SwitchSettingEntry(
                    title = stringResource(R.string.noblur),
                    text = "",
                    isChecked = noblur,
                    onCheckedChange = { noblur = it }
                )


        }
        if (filter.isNullOrBlank() || stringResource(R.string.statsfornerdsplayer).contains(
                filterCharSequence,
                true
            )
        )
            SwitchSettingEntry(
                title = stringResource(R.string.statsfornerdsplayer),
                text = "",
                isChecked = statsfornerds,
                onCheckedChange = { statsfornerds = it }
            )

        if (!showlyricsthumbnail && !isLandscape)
            if (filter.isNullOrBlank() || stringResource(R.string.expandedlyrics).contains(
                    filterCharSequence,
                    true
                )
            )
                SwitchSettingEntry(
                    title = stringResource(R.string.expandedlyrics),
                    text = stringResource(R.string.expandedlyricsinfo),
                    isChecked = expandedlyrics,
                    onCheckedChange = { expandedlyrics = it }
                )

        if (filter.isNullOrBlank() || stringResource(R.string.timelinesize).contains(
                filterCharSequence,
                true
            )
        )
            EnumValueSelectorSettingsEntry(
                title = stringResource(R.string.timelinesize),
                selectedValue = playerTimelineSize,
                onValueSelected = { playerTimelineSize = it },
                valueText = {
                    when (it) {
                        PlayerTimelineSize.Small -> stringResource(R.string.small)
                        PlayerTimelineSize.Medium -> stringResource(R.string.medium)
                        PlayerTimelineSize.Big -> stringResource(R.string.big)
                        PlayerTimelineSize.Biggest -> stringResource(R.string.biggest)
                        PlayerTimelineSize.Expanded -> stringResource(R.string.expanded)
                    }
                }
            )

        if (filter.isNullOrBlank() || stringResource(R.string.pinfo_type).contains(
                filterCharSequence,
                true
            )
        ) {
            EnumValueSelectorSettingsEntry(
                title = stringResource(R.string.pinfo_type),
                selectedValue = playerInfoType,
                onValueSelected = {
                    playerInfoType = it
                },
                valueText = {
                    when (it) {
                        PlayerInfoType.Modern -> stringResource(R.string.pcontrols_modern)
                        PlayerInfoType.Essential -> stringResource(R.string.pcontrols_essential)
                    }
                },
            )
            SettingsDescription(text = stringResource(R.string.pinfo_album_and_artist_name))

            AnimatedVisibility( visible = playerInfoType == PlayerInfoType.Modern) {
                Column {
                    if (filter.isNullOrBlank() || stringResource(R.string.pinfo_show_icons).contains(
                            filterCharSequence,
                            true
                        )
                    )
                        SwitchSettingEntry(
                            title = stringResource(R.string.pinfo_show_icons),
                            text = "",
                            isChecked = playerInfoShowIcons,
                            onCheckedChange = { playerInfoShowIcons = it },
                            modifier = Modifier
                                .padding(start = 25.dp)
                        )
                }
            }

        }



        if (filter.isNullOrBlank() || stringResource(R.string.miniplayertype).contains(
                filterCharSequence,
                true
            )
        )
            EnumValueSelectorSettingsEntry(
                title = stringResource(R.string.miniplayertype),
                selectedValue = miniPlayerType,
                onValueSelected = {
                    miniPlayerType = it
                },
                valueText = {
                    when (it) {
                        MiniPlayerType.Modern -> stringResource(R.string.pcontrols_modern)
                        MiniPlayerType.Essential -> stringResource(R.string.pcontrols_essential)
                    }
                },
            )

        if (filter.isNullOrBlank() || stringResource(R.string.player_swap_controls_with_timeline).contains(
                filterCharSequence,
                true
            )
        )
            SwitchSettingEntry(
                title = stringResource(R.string.player_swap_controls_with_timeline),
                text = "",
                isChecked = playerSwapControlsWithTimeline,
                onCheckedChange = { playerSwapControlsWithTimeline = it }
            )

        if (filter.isNullOrBlank() || stringResource(R.string.timeline).contains(
                filterCharSequence,
                true
            )
        )
            EnumValueSelectorSettingsEntry(
                title = stringResource(R.string.timeline),
                selectedValue = playerTimelineType,
                onValueSelected = { playerTimelineType = it },
                valueText = {
                    when (it) {
                        PlayerTimelineType.Default -> stringResource(R.string._default)
                        PlayerTimelineType.Wavy -> stringResource(R.string.wavy_timeline)
                        PlayerTimelineType.BodiedBar -> stringResource(R.string.bodied_bar)
                        PlayerTimelineType.PinBar -> stringResource(R.string.pin_bar)
                        PlayerTimelineType.FakeAudioBar -> stringResource(R.string.fake_audio_bar)
                        PlayerTimelineType.ThinBar -> stringResource(R.string.thin_bar)
                        //PlayerTimelineType.ColoredBar -> "Colored bar"
                    }
                }
            )

        if (filter.isNullOrBlank() || stringResource(R.string.transparentbar).contains(
                filterCharSequence,
                true
            )
        )
            SwitchSettingEntry(
                title = stringResource(R.string.transparentbar),
                text = "",
                isChecked = transparentbar,
                onCheckedChange = { transparentbar = it }
            )

        if (filter.isNullOrBlank() || stringResource(R.string.pcontrols_type).contains(
                filterCharSequence,
                true
            )
        )
            EnumValueSelectorSettingsEntry(
                title = stringResource(R.string.pcontrols_type),
                selectedValue = playerControlsType,
                onValueSelected = {
                    playerControlsType = it
                },
                valueText = {
                    when (it) {
                        PlayerControlsType.Modern -> stringResource(R.string.pcontrols_modern)
                        PlayerControlsType.Essential -> stringResource(R.string.pcontrols_essential)
                    }
                },
            )


        if (filter.isNullOrBlank() || stringResource(R.string.play_button).contains(
                filterCharSequence,
                true
            )
        )
            EnumValueSelectorSettingsEntry(
                title = stringResource(R.string.play_button),
                selectedValue = playerPlayButtonType,
                onValueSelected = {
                    playerPlayButtonType = it
                    lastPlayerPlayButtonType = it
                },
                valueText = {
                    when (it) {
                        PlayerPlayButtonType.Disabled -> stringResource(R.string.vt_disabled)
                        PlayerPlayButtonType.Default -> stringResource(R.string._default)
                        PlayerPlayButtonType.Rectangular -> stringResource(R.string.rectangular)
                        PlayerPlayButtonType.Square -> stringResource(R.string.square)
                        PlayerPlayButtonType.CircularRibbed -> stringResource(R.string.circular_ribbed)
                    }
                },
            )

        if (filter.isNullOrBlank() || stringResource(R.string.buttonzoomout).contains(
                filterCharSequence,
                true
            )
        )
            SwitchSettingEntry(
                title = stringResource(R.string.buttonzoomout),
                text = "",
                isChecked = buttonzoomout,
                onCheckedChange = { buttonzoomout = it }
            )


        if (filter.isNullOrBlank() || stringResource(R.string.play_button).contains(
                filterCharSequence,
                true
            )
        )
            EnumValueSelectorSettingsEntry(
                title = stringResource(R.string.icon_like_button),
                selectedValue = iconLikeType,
                onValueSelected = {
                    iconLikeType = it
                },
                valueText = {
                    when (it) {
                        IconLikeType.Essential -> stringResource(R.string.pcontrols_essential)
                        IconLikeType.Apple -> stringResource(R.string.icon_like_apple)
                        IconLikeType.Breaked -> stringResource(R.string.icon_like_breaked)
                        IconLikeType.Gift -> stringResource(R.string.icon_like_gift)
                        IconLikeType.Shape -> stringResource(R.string.icon_like_shape)
                        IconLikeType.Striped -> stringResource(R.string.icon_like_striped)
                        IconLikeType.Brilliant -> stringResource(R.string.icon_like_brilliant)
                    }
                },
            )

        /*

        if (filter.isNullOrBlank() || stringResource(R.string.use_gradient_background).contains(filterCharSequence,true))
            SwitchSettingEntry(
                title = stringResource(R.string.use_gradient_background),
                text = "",
                isChecked = isGradientBackgroundEnabled,
                onCheckedChange = { isGradientBackgroundEnabled = it }
            )
         */

        if (filter.isNullOrBlank() || stringResource(R.string.background_colors).contains(
                filterCharSequence,
                true
            )
        )
            EnumValueSelectorSettingsEntry(
                title = stringResource(R.string.background_colors),
                selectedValue = playerBackgroundColors,
                onValueSelected = {
                    playerBackgroundColors = it
                },
                valueText = {
                    when (it) {
                        PlayerBackgroundColors.CoverColor -> stringResource(R.string.bg_colors_background_from_cover)
                        PlayerBackgroundColors.ThemeColor -> stringResource(R.string.bg_colors_background_from_theme)
                        PlayerBackgroundColors.CoverColorGradient -> stringResource(R.string.bg_colors_gradient_background_from_cover)
                        PlayerBackgroundColors.ThemeColorGradient -> stringResource(R.string.bg_colors_gradient_background_from_theme)
                        PlayerBackgroundColors.FluidThemeColorGradient -> stringResource(R.string.bg_colors_fluid_gradient_background_from_theme)
                        PlayerBackgroundColors.FluidCoverColorGradient -> stringResource(R.string.bg_colors_fluid_gradient_background_from_cover)
                        PlayerBackgroundColors.BlurredCoverColor -> stringResource(R.string.bg_colors_blurred_cover_background)
                    }
                },
            )

        if ((playerBackgroundColors == PlayerBackgroundColors.CoverColorGradient) || (playerBackgroundColors == PlayerBackgroundColors.ThemeColorGradient))
            if (filter.isNullOrBlank() || stringResource(R.string.blackgradient).contains(
                    filterCharSequence,
                    true
                )
            )
                SwitchSettingEntry(
                    title = stringResource(R.string.blackgradient),
                    text = "",
                    isChecked = blackgradient,
                    onCheckedChange = { blackgradient = it }
                )
        if (playerBackgroundColors == PlayerBackgroundColors.BlurredCoverColor)
            if (filter.isNullOrBlank() || stringResource(R.string.bottomgradient).contains(
                    filterCharSequence,
                    true
                )
            )
                SwitchSettingEntry(
                    title = stringResource(R.string.bottomgradient),
                    text = "",
                    isChecked = bottomgradient,
                    onCheckedChange = { bottomgradient = it }
                )
        if (playerBackgroundColors == PlayerBackgroundColors.BlurredCoverColor)
           if (filter.isNullOrBlank() || stringResource(R.string.textoutline).contains(
                filterCharSequence,
                true
                )
           )
               SwitchSettingEntry(
                   title = stringResource(R.string.textoutline),
                   text = "",
                   isChecked = textoutline,
                   onCheckedChange = { textoutline = it }
               )
       if (filter.isNullOrBlank() || stringResource(R.string.show_total_time_of_queue).contains(
                filterCharSequence,
                true
            )
        )
            SwitchSettingEntry(
                title = stringResource(R.string.show_total_time_of_queue),
                text = "",
                isChecked = showTotalTimeQueue,
                onCheckedChange = { showTotalTimeQueue = it }
            )

        if (filter.isNullOrBlank() || stringResource(R.string.show_remaining_song_time).contains(
                filterCharSequence,
                true
            )
        )
            SwitchSettingEntry(
                title = stringResource(R.string.show_remaining_song_time),
                text = "",
                isChecked = showRemainingSongTime,
                onCheckedChange = { showRemainingSongTime = it }
            )

        if (filter.isNullOrBlank() || stringResource(R.string.show_next_songs_in_player).contains(
                filterCharSequence,
                true
            )
        )
            SwitchSettingEntry(
                title = stringResource(R.string.show_next_songs_in_player),
                text = "",
                isChecked = showNextSongsInPlayer,
                onCheckedChange = { showNextSongsInPlayer = it }
            )
        AnimatedVisibility( visible = showNextSongsInPlayer) {
          Column {
              if (filter.isNullOrBlank() || stringResource(R.string.showtwosongs).contains(filterCharSequence,true))
                  EnumValueSelectorSettingsEntry(
                      title = stringResource(R.string.songs_number_to_show),
                      selectedValue = showsongs,
                      onValueSelected = {
                          showsongs = it
                      },
                      valueText = {
                          it.name
                      },
                      modifier = Modifier
                          .padding(start = 25.dp)
                  )


            if (filter.isNullOrBlank() || stringResource(R.string.showalbumcover).contains(
                    filterCharSequence,
                    true
                )
            )
                SwitchSettingEntry(
                    title = stringResource(R.string.showalbumcover),
                    text = "",
                    isChecked = showalbumcover,
                    onCheckedChange = { showalbumcover = it },
                      modifier = Modifier.padding(start = 25.dp)
                  )
          }
        }

        if (filter.isNullOrBlank() || stringResource(R.string.disable_scrolling_text).contains(
                filterCharSequence,
                true
            )
        )
            SwitchSettingEntry(
                title = stringResource(R.string.disable_scrolling_text),
                text = stringResource(R.string.scrolling_text_is_used_for_long_texts),
                isChecked = disableScrollingText,
                onCheckedChange = { disableScrollingText = it }
            )
        if (playerType == PlayerType.Essential) {
            if (filter.isNullOrBlank() || stringResource(R.string.disable_horizontal_swipe).contains(
                    filterCharSequence,
                    true
                )
            )
                SwitchSettingEntry(
                    title = stringResource(R.string.disable_horizontal_swipe),
                    text = stringResource(R.string.disable_song_switching_via_swipe),
                    isChecked = disablePlayerHorizontalSwipe,
                    onCheckedChange = { disablePlayerHorizontalSwipe = it }
                )
        }

        if (filter.isNullOrBlank() || stringResource(R.string.player_rotating_buttons).contains(
                filterCharSequence,
                true
            )
        )
            SwitchSettingEntry(
                title = stringResource(R.string.player_rotating_buttons),
                text = stringResource(R.string.player_enable_rotation_buttons),
                isChecked = effectRotationEnabled,
                onCheckedChange = { effectRotationEnabled = it }
            )

        if (filter.isNullOrBlank() || stringResource(R.string.toggle_lyrics).contains(
                filterCharSequence,
                true
            )
        )
            SwitchSettingEntry(
                title = stringResource(R.string.toggle_lyrics),
                text = stringResource(R.string.by_tapping_on_the_thumbnail),
                isChecked = thumbnailTapEnabled,
                onCheckedChange = { thumbnailTapEnabled = it }
            )

        if (filter.isNullOrBlank() || stringResource(R.string.click_lyrics_text).contains(
                filterCharSequence,
                true
            )
        )
            EnumValueSelectorSettingsEntry(
                title = stringResource(R.string.click_lyrics_text),
                selectedValue = clickLyricsText,
                onValueSelected = {
                    clickLyricsText = it
                },
                valueText = {
                    when (it) {
                        ClickLyricsText.Player -> stringResource(R.string.player)
                        ClickLyricsText.FullScreen -> stringResource(R.string.full_screen)
                        ClickLyricsText.Both -> stringResource(R.string.both)
                    }
                },
            )
        if (showlyricsthumbnail)
            if (filter.isNullOrBlank() || stringResource(R.string.show_background_in_lyrics).contains(
                    filterCharSequence,
                    true
                )
            )
                SwitchSettingEntry(
                    title = stringResource(R.string.show_background_in_lyrics),
                    text = "",
                    isChecked = showBackgroundLyrics,
                    onCheckedChange = { showBackgroundLyrics = it }
                )

        if (filter.isNullOrBlank() || stringResource(R.string.player_enable_lyrics_popup_message).contains(
                filterCharSequence,
                true
            )
        )
            SwitchSettingEntry(
                title = stringResource(R.string.player_enable_lyrics_popup_message),
                text = "",
                isChecked = playerEnableLyricsPopupMessage,
                onCheckedChange = { playerEnableLyricsPopupMessage = it }
            )

        if (filter.isNullOrBlank() || stringResource(R.string.background_progress_bar).contains(
                filterCharSequence,
                true
            )
        )
            EnumValueSelectorSettingsEntry(
                title = stringResource(R.string.background_progress_bar),
                selectedValue = backgroundProgress,
                onValueSelected = {
                    backgroundProgress = it
                },
                valueText = {
                    when (it) {
                        BackgroundProgress.Player -> stringResource(R.string.player)
                        BackgroundProgress.MiniPlayer -> stringResource(R.string.minimized_player)
                        BackgroundProgress.Both -> stringResource(R.string.both)
                        BackgroundProgress.Disabled -> stringResource(R.string.vt_disabled)
                    }
                },
            )


        if (filter.isNullOrBlank() || stringResource(R.string.visualizer).contains(
                filterCharSequence,
                true
            )
        ) {
            SwitchSettingEntry(
                title = stringResource(R.string.visualizer),
                text = "",
                isChecked = visualizerEnabled,
                onCheckedChange = { visualizerEnabled = it }
            )
            /*
            EnumValueSelectorSettingsEntry(
                title = stringResource(R.string.visualizer),
                selectedValue = playerVisualizerType,
                onValueSelected = { playerVisualizerType = it },
                valueText = {
                    when (it) {
                        PlayerVisualizerType.Fancy -> stringResource(R.string.vt_fancy)
                        PlayerVisualizerType.Circular -> stringResource(R.string.vt_circular)
                        PlayerVisualizerType.Disabled -> stringResource(R.string.vt_disabled)
                        PlayerVisualizerType.Stacked -> stringResource(R.string.vt_stacked)
                        PlayerVisualizerType.Oneside -> stringResource(R.string.vt_one_side)
                        PlayerVisualizerType.Doubleside -> stringResource(R.string.vt_double_side)
                        PlayerVisualizerType.DoublesideCircular -> stringResource(R.string.vt_double_side_circular)
                        PlayerVisualizerType.Full -> stringResource(R.string.vt_full)
                    }
                }
            )
            */
            ImportantSettingsDescription(text = stringResource(R.string.visualizer_require_mic_permission))
        }

        SettingsGroupSpacer()
        SettingsEntryGroupText(title = stringResource(R.string.player_action_bar))

        if (filter.isNullOrBlank() || stringResource(R.string.action_bar_transparent_background).contains(
                filterCharSequence,
                true
            )
        )
            SwitchSettingEntry(
                title = stringResource(R.string.action_bar_transparent_background),
                text = "",
                isChecked = transparentBackgroundActionBarPlayer,
                onCheckedChange = { transparentBackgroundActionBarPlayer = it }
            )

        if (filter.isNullOrBlank() || stringResource(R.string.actionspacedevenly).contains(
                filterCharSequence,
                true
            )
        )
            SwitchSettingEntry(
                title = stringResource(R.string.actionspacedevenly),
                text = "",
                isChecked = actionspacedevenly,
                onCheckedChange = { actionspacedevenly = it }
            )

        if (filter.isNullOrBlank() || stringResource(R.string.tapqueue).contains(
                filterCharSequence,
                true
            )
        )
            SwitchSettingEntry(
                title = stringResource(R.string.tapqueue),
                text = "",
                isChecked = tapqueue,
                onCheckedChange = { tapqueue = it }
            )

        if (filter.isNullOrBlank() || stringResource(R.string.swipe_up_to_open_the_queue).contains(
                filterCharSequence,
                true
            )
        )
            SwitchSettingEntry(
                title = stringResource(R.string.swipe_up_to_open_the_queue),
                text = "",
                isChecked = swipeUpQueue,
                onCheckedChange = { swipeUpQueue = it }
            )

        if (filter.isNullOrBlank() || stringResource(R.string.action_bar_show_discover_button).contains(
                filterCharSequence,
                true
            )
        )
            SwitchSettingEntry(
                title = stringResource(R.string.action_bar_show_discover_button),
                text = "",
                isChecked = showButtonPlayerDiscover,
                onCheckedChange = { showButtonPlayerDiscover = it }
            )

        if (filter.isNullOrBlank() || stringResource(R.string.action_bar_show_download_button).contains(
                filterCharSequence,
                true
            )
        )
            SwitchSettingEntry(
                title = stringResource(R.string.action_bar_show_download_button),
                text = "",
                isChecked = showButtonPlayerDownload,
                onCheckedChange = { showButtonPlayerDownload = it }
            )

        if (filter.isNullOrBlank() || stringResource(R.string.action_bar_show_add_to_playlist_button).contains(
                filterCharSequence,
                true
            )
        )
            SwitchSettingEntry(
                title = stringResource(R.string.action_bar_show_add_to_playlist_button),
                text = "",
                isChecked = showButtonPlayerAddToPlaylist,
                onCheckedChange = { showButtonPlayerAddToPlaylist = it }
            )

        if (filter.isNullOrBlank() || stringResource(R.string.action_bar_show_loop_button).contains(
                filterCharSequence,
                true
            )
        )
            SwitchSettingEntry(
                title = stringResource(R.string.action_bar_show_loop_button),
                text = "",
                isChecked = showButtonPlayerLoop,
                onCheckedChange = { showButtonPlayerLoop = it }
            )

        if (filter.isNullOrBlank() || stringResource(R.string.action_bar_show_shuffle_button).contains(
                filterCharSequence,
                true
            )
        )
            SwitchSettingEntry(
                title = stringResource(R.string.action_bar_show_shuffle_button),
                text = "",
                isChecked = showButtonPlayerShuffle,
                onCheckedChange = { showButtonPlayerShuffle = it }
            )

        if (filter.isNullOrBlank() || stringResource(R.string.action_bar_show_lyrics_button).contains(
                filterCharSequence,
                true
            )
        )
            SwitchSettingEntry(
                title = stringResource(R.string.action_bar_show_lyrics_button),
                text = "",
                isChecked = showButtonPlayerLyrics,
                onCheckedChange = { showButtonPlayerLyrics = it }
            )
        if (!isLandscape || !showthumbnail) {
            if (!showlyricsthumbnail and !expandedlyrics) {
                if (filter.isNullOrBlank() || stringResource(R.string.expandedplayer).contains(
                        filterCharSequence,
                        true
                    )
                )
                    SwitchSettingEntry(
                        title = stringResource(R.string.expandedplayer),
                        text = "",
                        isChecked = expandedplayertoggle,
                        onCheckedChange = { expandedplayertoggle = it }
                    )
            }
        }

        if (filter.isNullOrBlank() || stringResource(R.string.action_bar_show_sleep_timer_button).contains(
                filterCharSequence,
                true
            )
        )
            SwitchSettingEntry(
                title = stringResource(R.string.action_bar_show_sleep_timer_button),
                text = "",
                isChecked = showButtonPlayerSleepTimer,
                onCheckedChange = { showButtonPlayerSleepTimer = it }
            )

        if (filter.isNullOrBlank() || stringResource(R.string.show_equalizer).contains(
                filterCharSequence,
                true
            )
        )
            SwitchSettingEntry(
                title = stringResource(R.string.show_equalizer),
                text = "",
                isChecked = showButtonPlayerSystemEqualizer,
                onCheckedChange = { showButtonPlayerSystemEqualizer = it }
            )

        if (filter.isNullOrBlank() || stringResource(R.string.action_bar_show_arrow_button_to_open_queue).contains(
                filterCharSequence,
                true
            )
        )
            SwitchSettingEntry(
                title = stringResource(R.string.action_bar_show_arrow_button_to_open_queue),
                text = "",
                isChecked = showButtonPlayerArrow,
                onCheckedChange = { showButtonPlayerArrow = it }
            )

        if (filter.isNullOrBlank() || stringResource(R.string.action_bar_show_menu_button).contains(
                filterCharSequence,
                true
            )
        )
            SwitchSettingEntry(
                title = stringResource(R.string.action_bar_show_menu_button),
                text = "",
                isChecked = showButtonPlayerMenu,
                onCheckedChange = { showButtonPlayerMenu = it }
            )

        SettingsGroupSpacer()
        SettingsEntryGroupText(title = stringResource(R.string.background_player))

        if (filter.isNullOrBlank() || stringResource(R.string.show_favorite_button).contains(
                filterCharSequence,
                true
            )
        ) {
            SwitchSettingEntry(
                title = stringResource(R.string.show_favorite_button),
                text = stringResource(R.string.show_favorite_button_in_lock_screen_and_notification_area),
                isChecked = showLikeButtonBackgroundPlayer,
                onCheckedChange = { showLikeButtonBackgroundPlayer = it }
            )
            ImportantSettingsDescription(text = stringResource(R.string.restarting_rimusic_is_required))
        }
        if (filter.isNullOrBlank() || stringResource(R.string.show_download_button).contains(
                filterCharSequence,
                true
            )
        ) {
            SwitchSettingEntry(
                title = stringResource(R.string.show_download_button),
                text = stringResource(R.string.show_download_button_in_lock_screen_and_notification_area),
                isChecked = showDownloadButtonBackgroundPlayer,
                onCheckedChange = { showDownloadButtonBackgroundPlayer = it }
            )

            ImportantSettingsDescription(text = stringResource(R.string.restarting_rimusic_is_required))
        }

        //SettingsGroupSpacer()
        //SettingsEntryGroupText(title = stringResource(R.string.text))


        if (filter.isNullOrBlank() || stringResource(R.string.show_song_cover).contains(
                filterCharSequence,
                true
            )
        )
            if (!isAtLeastAndroid13) {
                SettingsGroupSpacer()

                SettingsEntryGroupText(title = stringResource(R.string.lockscreen))

                SwitchSettingEntry(
                    title = stringResource(R.string.show_song_cover),
                    text = stringResource(R.string.use_song_cover_on_lockscreen),
                    isChecked = isShowingThumbnailInLockscreen,
                    onCheckedChange = { isShowingThumbnailInLockscreen = it }
                )
            }
        SettingsGroupSpacer(
            modifier = Modifier.height(Dimensions.bottomSpacer)
        )
    }
}
