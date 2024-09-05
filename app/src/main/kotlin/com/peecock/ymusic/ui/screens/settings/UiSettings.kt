package com.peecock.ymusic.ui.screens.settings

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import androidx.media3.common.util.UnstableApi
import com.peecock.ymusic.LocalPlayerServiceBinder
import com.peecock.ymusic.R
import com.peecock.ymusic.enums.AudioQualityFormat
import com.peecock.ymusic.enums.BackgroundProgress
import com.peecock.ymusic.enums.CarouselSize
import com.peecock.ymusic.enums.ClickLyricsText
import com.peecock.ymusic.enums.ColorPaletteMode
import com.peecock.ymusic.enums.ColorPaletteName
import com.peecock.ymusic.enums.DurationInMilliseconds
import com.peecock.ymusic.enums.DurationInMinutes
import com.peecock.ymusic.enums.ExoPlayerMinTimeForEvent
import com.peecock.ymusic.enums.FontType
import com.peecock.ymusic.enums.Languages
import com.peecock.ymusic.enums.MaxStatisticsItems
import com.peecock.ymusic.enums.HomeScreenTabs
import com.peecock.ymusic.enums.IconLikeType
import com.peecock.ymusic.enums.MaxSongs
import com.peecock.ymusic.enums.MaxTopPlaylistItems
import com.peecock.ymusic.enums.MenuStyle
import com.peecock.ymusic.enums.MessageType
import com.peecock.ymusic.enums.MiniPlayerType
import com.peecock.ymusic.enums.NavigationBarPosition
import com.peecock.ymusic.enums.NavigationBarType
import com.peecock.ymusic.enums.PauseBetweenSongs
import com.peecock.ymusic.enums.PlayerBackgroundColors
import com.peecock.ymusic.enums.PlayerControlsType
import com.peecock.ymusic.enums.PlayerInfoType
import com.peecock.ymusic.enums.PlayerPlayButtonType
import com.peecock.ymusic.enums.PlayerPosition
import com.peecock.ymusic.enums.PlayerThumbnailSize
import com.peecock.ymusic.enums.PlayerTimelineSize
import com.peecock.ymusic.enums.PlayerTimelineType
import com.peecock.ymusic.enums.PlayerType
import com.peecock.ymusic.enums.QueueType
import com.peecock.ymusic.enums.RecommendationsNumber
import com.peecock.ymusic.enums.ThumbnailRoundness
import com.peecock.ymusic.enums.ThumbnailType
import com.peecock.ymusic.enums.TransitionEffect
import com.peecock.ymusic.enums.UiType
import com.peecock.ymusic.ui.components.themed.ConfirmationDialog
import com.peecock.ymusic.ui.components.themed.HeaderIconButton
import com.peecock.ymusic.ui.components.themed.HeaderWithIcon
import com.peecock.ymusic.ui.components.themed.IconButton
import com.peecock.ymusic.ui.components.themed.SecondaryTextButton
import com.peecock.ymusic.ui.styling.DefaultDarkColorPalette
import com.peecock.ymusic.ui.styling.DefaultLightColorPalette
import com.peecock.ymusic.ui.styling.Dimensions
import com.peecock.ymusic.ui.styling.LocalAppearance
import com.peecock.ymusic.ui.styling.favoritesIcon
import com.peecock.ymusic.utils.MaxTopPlaylistItemsKey
import com.peecock.ymusic.utils.UiTypeKey
import com.peecock.ymusic.utils.actionspacedevenlyKey
import com.peecock.ymusic.utils.applyFontPaddingKey
import com.peecock.ymusic.utils.audioQualityFormatKey
import com.peecock.ymusic.utils.backgroundProgressKey
import com.peecock.ymusic.utils.blackgradientKey
import com.peecock.ymusic.utils.buttonzoomoutKey
import com.peecock.ymusic.utils.carouselKey
import com.peecock.ymusic.utils.carouselSizeKey
import com.peecock.ymusic.utils.clickLyricsTextKey
import com.peecock.ymusic.utils.closeWithBackButtonKey
import com.peecock.ymusic.utils.closebackgroundPlayerKey
import com.peecock.ymusic.utils.colorPaletteModeKey
import com.peecock.ymusic.utils.colorPaletteNameKey
import com.peecock.ymusic.utils.customThemeDark_Background0Key
import com.peecock.ymusic.utils.customThemeDark_Background1Key
import com.peecock.ymusic.utils.customThemeDark_Background2Key
import com.peecock.ymusic.utils.customThemeDark_Background3Key
import com.peecock.ymusic.utils.customThemeDark_Background4Key
import com.peecock.ymusic.utils.customThemeDark_TextKey
import com.peecock.ymusic.utils.customThemeDark_accentKey
import com.peecock.ymusic.utils.customThemeDark_iconButtonPlayerKey
import com.peecock.ymusic.utils.customThemeDark_textDisabledKey
import com.peecock.ymusic.utils.customThemeDark_textSecondaryKey
import com.peecock.ymusic.utils.customThemeLight_Background0Key
import com.peecock.ymusic.utils.customThemeLight_Background1Key
import com.peecock.ymusic.utils.customThemeLight_Background2Key
import com.peecock.ymusic.utils.customThemeLight_Background3Key
import com.peecock.ymusic.utils.customThemeLight_Background4Key
import com.peecock.ymusic.utils.customThemeLight_TextKey
import com.peecock.ymusic.utils.customThemeLight_accentKey
import com.peecock.ymusic.utils.customThemeLight_iconButtonPlayerKey
import com.peecock.ymusic.utils.customThemeLight_textDisabledKey
import com.peecock.ymusic.utils.customThemeLight_textSecondaryKey
import com.peecock.ymusic.utils.disableClosingPlayerSwipingDownKey
import com.peecock.ymusic.utils.disableIconButtonOnTopKey
import com.peecock.ymusic.utils.disablePlayerHorizontalSwipeKey
import com.peecock.ymusic.utils.disableScrollingTextKey
import com.peecock.ymusic.utils.discoverKey
import com.peecock.ymusic.utils.effectRotationKey
import com.peecock.ymusic.utils.enableCreateMonthlyPlaylistsKey
import com.peecock.ymusic.utils.excludeSongsWithDurationLimitKey
import com.peecock.ymusic.utils.exoPlayerMinTimeForEventKey
import com.peecock.ymusic.utils.expandedlyricsKey
import com.peecock.ymusic.utils.expandedplayertoggleKey
import com.peecock.ymusic.utils.fadingedgeKey
import com.peecock.ymusic.utils.fontTypeKey
import com.peecock.ymusic.utils.iconLikeTypeKey
import com.peecock.ymusic.utils.indexNavigationTabKey
import com.peecock.ymusic.utils.isAtLeastAndroid6
import com.peecock.ymusic.utils.isPauseOnVolumeZeroEnabledKey
import com.peecock.ymusic.utils.isSwipeToActionEnabledKey
import com.peecock.ymusic.utils.keepPlayerMinimizedKey
import com.peecock.ymusic.utils.languageAppKey
import com.peecock.ymusic.utils.languageDestinationName
import com.peecock.ymusic.utils.lastPlayerPlayButtonTypeKey
import com.peecock.ymusic.utils.lastPlayerThumbnailSizeKey
import com.peecock.ymusic.utils.lastPlayerTimelineTypeKey
import com.peecock.ymusic.utils.maxSongsInQueueKey
import com.peecock.ymusic.utils.maxStatisticsItemsKey
import com.peecock.ymusic.utils.menuStyleKey
import com.peecock.ymusic.utils.messageTypeKey
import com.peecock.ymusic.utils.miniPlayerTypeKey
import com.peecock.ymusic.utils.minimumSilenceDurationKey
import com.peecock.ymusic.utils.navigationBarPositionKey
import com.peecock.ymusic.utils.navigationBarTypeKey
import com.peecock.ymusic.utils.pauseBetweenSongsKey
import com.peecock.ymusic.utils.pauseListenHistoryKey
import com.peecock.ymusic.utils.persistentQueueKey
import com.peecock.ymusic.utils.playbackFadeAudioDurationKey
import com.peecock.ymusic.utils.playerBackgroundColorsKey
import com.peecock.ymusic.utils.playerControlsTypeKey
import com.peecock.ymusic.utils.playerEnableLyricsPopupMessageKey
import com.peecock.ymusic.utils.playerInfoShowIconsKey
import com.peecock.ymusic.utils.playerInfoTypeKey
import com.peecock.ymusic.utils.playerPlayButtonTypeKey
import com.peecock.ymusic.utils.playerPositionKey
import com.peecock.ymusic.utils.playerSwapControlsWithTimelineKey
import com.peecock.ymusic.utils.playerThumbnailSizeKey
import com.peecock.ymusic.utils.playerTimelineSizeKey
import com.peecock.ymusic.utils.playerTimelineTypeKey
import com.peecock.ymusic.utils.playerTypeKey
import com.peecock.ymusic.utils.playlistindicatorKey
import com.peecock.ymusic.utils.queueTypeKey
import com.peecock.ymusic.utils.recommendationsNumberKey
import com.peecock.ymusic.utils.rememberEqualizerLauncher
import com.peecock.ymusic.utils.rememberPreference
import com.peecock.ymusic.utils.resumePlaybackWhenDeviceConnectedKey
import com.peecock.ymusic.utils.secondary
import com.peecock.ymusic.utils.semiBold
import com.peecock.ymusic.utils.shakeEventEnabledKey
import com.peecock.ymusic.utils.showButtonPlayerAddToPlaylistKey
import com.peecock.ymusic.utils.showButtonPlayerArrowKey
import com.peecock.ymusic.utils.showButtonPlayerDiscoverKey
import com.peecock.ymusic.utils.showButtonPlayerDownloadKey
import com.peecock.ymusic.utils.showButtonPlayerLoopKey
import com.peecock.ymusic.utils.showButtonPlayerLyricsKey
import com.peecock.ymusic.utils.showButtonPlayerMenuKey
import com.peecock.ymusic.utils.showButtonPlayerShuffleKey
import com.peecock.ymusic.utils.showButtonPlayerSleepTimerKey
import com.peecock.ymusic.utils.showButtonPlayerSystemEqualizerKey
import com.peecock.ymusic.utils.showDownloadedPlaylistKey
import com.peecock.ymusic.utils.showFavoritesPlaylistKey
import com.peecock.ymusic.utils.showFloatingIconKey
import com.peecock.ymusic.utils.showMonthlyPlaylistsKey
import com.peecock.ymusic.utils.showMyTopPlaylistKey
import com.peecock.ymusic.utils.showNextSongsInPlayerKey
import com.peecock.ymusic.utils.showOnDevicePlaylistKey
import com.peecock.ymusic.utils.showPinnedPlaylistsKey
import com.peecock.ymusic.utils.showPipedPlaylistsKey
import com.peecock.ymusic.utils.showRemainingSongTimeKey
import com.peecock.ymusic.utils.showSearchTabKey
import com.peecock.ymusic.utils.showStatsInNavbarKey
import com.peecock.ymusic.utils.showStatsListeningTimeKey
import com.peecock.ymusic.utils.showTopActionsBarKey
import com.peecock.ymusic.utils.showTotalTimeQueueKey
import com.peecock.ymusic.utils.showthumbnailKey
import com.peecock.ymusic.utils.skipSilenceKey
import com.peecock.ymusic.utils.swipeUpQueueKey
import com.peecock.ymusic.utils.tapqueueKey
import com.peecock.ymusic.utils.thumbnailRoundnessKey
import com.peecock.ymusic.utils.thumbnailTapEnabledKey
import com.peecock.ymusic.utils.thumbnailTypeKey
import com.peecock.ymusic.utils.transitionEffectKey
import com.peecock.ymusic.utils.transparentBackgroundPlayerActionBarKey
import com.peecock.ymusic.utils.useSystemFontKey
import com.peecock.ymusic.utils.useVolumeKeysToChangeSongKey
import com.peecock.ymusic.utils.visualizerEnabledKey
import com.peecock.ymusic.utils.volumeNormalizationKey

@ExperimentalAnimationApi
@UnstableApi
@Composable
fun  UiSettings() {
    val binder = LocalPlayerServiceBinder.current
    val context = LocalContext.current

    var languageApp  by rememberPreference(languageAppKey, Languages.English)
    val systemLocale = LocaleListCompat.getDefault().get(0).toString()

    var exoPlayerMinTimeForEvent by rememberPreference(
        exoPlayerMinTimeForEventKey,
        ExoPlayerMinTimeForEvent.`20s`
    )
    var persistentQueue by rememberPreference(persistentQueueKey, false)
    var closebackgroundPlayer by rememberPreference(closebackgroundPlayerKey, false)
    var closeWithBackButton by rememberPreference(closeWithBackButtonKey, true)
    var resumePlaybackWhenDeviceConnected by rememberPreference(
        resumePlaybackWhenDeviceConnectedKey,
        false
    )

    var skipSilence by rememberPreference(skipSilenceKey, false)
    var volumeNormalization by rememberPreference(volumeNormalizationKey, false)
    var audioQualityFormat by rememberPreference(audioQualityFormatKey, AudioQualityFormat.Auto)

    val activityResultLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { }


    var recommendationsNumber by rememberPreference(recommendationsNumberKey,   RecommendationsNumber.`5`)

    var keepPlayerMinimized by rememberPreference(keepPlayerMinimizedKey,   false)

    var disableIconButtonOnTop by rememberPreference(disableIconButtonOnTopKey, false)
    //var lastPlayerVisualizerType by rememberPreference(lastPlayerVisualizerTypeKey, PlayerVisualizerType.Disabled)
    var lastPlayerTimelineType by rememberPreference(lastPlayerTimelineTypeKey, PlayerTimelineType.Default)
    var lastPlayerThumbnailSize by rememberPreference(lastPlayerThumbnailSizeKey, PlayerThumbnailSize.Medium)
    var uiType  by rememberPreference(UiTypeKey, UiType.RiMusic)
    var disablePlayerHorizontalSwipe by rememberPreference(disablePlayerHorizontalSwipeKey, false)

    var lastPlayerPlayButtonType by rememberPreference(lastPlayerPlayButtonTypeKey, PlayerPlayButtonType.Rectangular)

    var colorPaletteName by rememberPreference(colorPaletteNameKey, ColorPaletteName.Dynamic)
    var colorPaletteMode by rememberPreference(colorPaletteModeKey, ColorPaletteMode.Dark)
    var indexNavigationTab by rememberPreference(
        indexNavigationTabKey,
        HomeScreenTabs.Default
    )
    var fontType by rememberPreference(fontTypeKey, FontType.Rubik)
    var useSystemFont by rememberPreference(useSystemFontKey, false)
    var applyFontPadding by rememberPreference(applyFontPaddingKey, false)
    var isSwipeToActionEnabled by rememberPreference(isSwipeToActionEnabledKey, true)
    var disableClosingPlayerSwipingDown by rememberPreference(disableClosingPlayerSwipingDownKey, true)
    var showSearchTab by rememberPreference(showSearchTabKey, false)
    var showStatsInNavbar by rememberPreference(showStatsInNavbarKey, false)

    var maxStatisticsItems by rememberPreference(
        maxStatisticsItemsKey,
        MaxStatisticsItems.`10`
    )

    var showStatsListeningTime by rememberPreference(showStatsListeningTimeKey,   true)

    var maxTopPlaylistItems by rememberPreference(
        MaxTopPlaylistItemsKey,
        MaxTopPlaylistItems.`10`
    )

    var navigationBarPosition by rememberPreference(navigationBarPositionKey, NavigationBarPosition.Bottom)
    var navigationBarType by rememberPreference(navigationBarTypeKey, NavigationBarType.IconAndText)
    var pauseBetweenSongs  by rememberPreference(pauseBetweenSongsKey, PauseBetweenSongs.`0`)
    var maxSongsInQueue  by rememberPreference(maxSongsInQueueKey, MaxSongs.`500`)

    val (colorPalette, typography) = LocalAppearance.current
    var searching by rememberSaveable { mutableStateOf(false) }
    var filter: String? by rememberSaveable { mutableStateOf(null) }
   // var filterCharSequence: CharSequence
    var filterCharSequence: CharSequence = filter.toString()
    var thumbnailRoundness by rememberPreference(
        thumbnailRoundnessKey,
        ThumbnailRoundness.Heavy
    )

    var showFavoritesPlaylist by rememberPreference(showFavoritesPlaylistKey, true)
    //var showCachedPlaylist by rememberPreference(showCachedPlaylistKey, true)
    var showMyTopPlaylist by rememberPreference(showMyTopPlaylistKey, true)
    var showDownloadedPlaylist by rememberPreference(showDownloadedPlaylistKey, true)
    var showOnDevicePlaylist by rememberPreference(showOnDevicePlaylistKey, true)
    //var showPlaylists by rememberPreference(showPlaylistsKey, true)
    var shakeEventEnabled by rememberPreference(shakeEventEnabledKey, false)
    var useVolumeKeysToChangeSong by rememberPreference(useVolumeKeysToChangeSongKey, false)
    var showFloatingIcon by rememberPreference(showFloatingIconKey, false)
    var menuStyle by rememberPreference(menuStyleKey, MenuStyle.List)
    var transitionEffect by rememberPreference(transitionEffectKey, TransitionEffect.Scale)
    var enableCreateMonthlyPlaylists by rememberPreference(enableCreateMonthlyPlaylistsKey, true)
    //var showMonthlyPlaylistInLibrary by rememberPreference(showMonthlyPlaylistInLibraryKey, true)
    var showPipedPlaylists by rememberPreference(showPipedPlaylistsKey, true)
    var showPinnedPlaylists by rememberPreference(showPinnedPlaylistsKey, true)
    var showMonthlyPlaylists by rememberPreference(showMonthlyPlaylistsKey, true)

    var customThemeLight_Background0 by rememberPreference(customThemeLight_Background0Key, DefaultLightColorPalette.background0.hashCode())
    var customThemeLight_Background1 by rememberPreference(customThemeLight_Background1Key, DefaultLightColorPalette.background1.hashCode())
    var customThemeLight_Background2 by rememberPreference(customThemeLight_Background2Key, DefaultLightColorPalette.background2.hashCode())
    var customThemeLight_Background3 by rememberPreference(customThemeLight_Background3Key, DefaultLightColorPalette.background3.hashCode())
    var customThemeLight_Background4 by rememberPreference(customThemeLight_Background4Key, DefaultLightColorPalette.background4.hashCode())
    var customThemeLight_Text by rememberPreference(customThemeLight_TextKey, DefaultLightColorPalette.text.hashCode())
    var customThemeLight_TextSecondary by rememberPreference(customThemeLight_textSecondaryKey, DefaultLightColorPalette.textSecondary.hashCode())
    var customThemeLight_TextDisabled by rememberPreference(customThemeLight_textDisabledKey, DefaultLightColorPalette.textDisabled.hashCode())
    var customThemeLight_IconButtonPlayer by rememberPreference(customThemeLight_iconButtonPlayerKey, DefaultLightColorPalette.iconButtonPlayer.hashCode())
    var customThemeLight_Accent by rememberPreference(customThemeLight_accentKey, DefaultLightColorPalette.accent.hashCode())

    var customThemeDark_Background0 by rememberPreference(customThemeDark_Background0Key, DefaultDarkColorPalette.background0.hashCode())
    var customThemeDark_Background1 by rememberPreference(customThemeDark_Background1Key, DefaultDarkColorPalette.background1.hashCode())
    var customThemeDark_Background2 by rememberPreference(customThemeDark_Background2Key, DefaultDarkColorPalette.background2.hashCode())
    var customThemeDark_Background3 by rememberPreference(customThemeDark_Background3Key, DefaultDarkColorPalette.background3.hashCode())
    var customThemeDark_Background4 by rememberPreference(customThemeDark_Background4Key, DefaultDarkColorPalette.background4.hashCode())
    var customThemeDark_Text by rememberPreference(customThemeDark_TextKey, DefaultDarkColorPalette.text.hashCode())
    var customThemeDark_TextSecondary by rememberPreference(customThemeDark_textSecondaryKey, DefaultDarkColorPalette.textSecondary.hashCode())
    var customThemeDark_TextDisabled by rememberPreference(customThemeDark_textDisabledKey, DefaultDarkColorPalette.textDisabled.hashCode())
    var customThemeDark_IconButtonPlayer by rememberPreference(customThemeDark_iconButtonPlayerKey, DefaultDarkColorPalette.iconButtonPlayer.hashCode())
    var customThemeDark_Accent by rememberPreference(customThemeDark_accentKey, DefaultDarkColorPalette.accent.hashCode())

    var resetCustomLightThemeDialog by rememberSaveable { mutableStateOf(false) }
    var resetCustomDarkThemeDialog by rememberSaveable { mutableStateOf(false) }
    //var playbackFadeDuration by rememberPreference(playbackFadeDurationKey, DurationInSeconds.Disabled)
    var playbackFadeAudioDuration by rememberPreference(playbackFadeAudioDurationKey, DurationInMilliseconds.Disabled)
    var playerPosition by rememberPreference(playerPositionKey, PlayerPosition.Bottom)
    var excludeSongWithDurationLimit by rememberPreference(excludeSongsWithDurationLimitKey, DurationInMinutes.Disabled)
    var playlistindicator by rememberPreference(playlistindicatorKey, false)
    var discoverIsEnabled by rememberPreference(discoverKey, false)
    var isPauseOnVolumeZeroEnabled by rememberPreference(isPauseOnVolumeZeroEnabledKey, false)

    var messageType by rememberPreference(messageTypeKey, MessageType.Modern)


    val launchEqualizer by rememberEqualizerLauncher(audioSessionId = { binder?.player?.audioSessionId })

    var minimumSilenceDuration by rememberPreference(minimumSilenceDurationKey, 2_000_000L)

    var pauseListenHistory by rememberPreference(pauseListenHistoryKey, false)
    var restartService by rememberSaveable { mutableStateOf(false) }

    /*  ViMusic Mode Settings  */
    var showTopActionsBar by rememberPreference(showTopActionsBarKey, true)
    var playerControlsType by rememberPreference(playerControlsTypeKey, PlayerControlsType.Modern)
    var playerInfoType by rememberPreference(playerInfoTypeKey, PlayerInfoType.Modern)
    var playerType by rememberPreference(playerTypeKey, PlayerType.Essential)
    var queueType by rememberPreference(queueTypeKey, QueueType.Essential)
    var fadingedge by rememberPreference(fadingedgeKey, false)
    var carousel by rememberPreference(carouselKey, true)
    var carouselSize by rememberPreference(carouselSizeKey, CarouselSize.Biggest)
    var thumbnailType by rememberPreference(thumbnailTypeKey, ThumbnailType.Modern)
    var expandedlyrics by rememberPreference(expandedlyricsKey, true)
    var playerTimelineType by rememberPreference(playerTimelineTypeKey, PlayerTimelineType.Default)
    var playerThumbnailSize by rememberPreference(
        playerThumbnailSizeKey,
        PlayerThumbnailSize.Biggest
    )
    var playerTimelineSize by rememberPreference(
        playerTimelineSizeKey,
        PlayerTimelineSize.Biggest
    )
    var playerInfoShowIcons by rememberPreference(playerInfoShowIconsKey, true)
    var miniPlayerType by rememberPreference(
        miniPlayerTypeKey,
        MiniPlayerType.Modern
    )
    var playerSwapControlsWithTimeline by rememberPreference(
        playerSwapControlsWithTimelineKey,
        false
    )
    var playerPlayButtonType by rememberPreference(
        playerPlayButtonTypeKey,
        PlayerPlayButtonType.Rectangular
    )
    var buttonzoomout by rememberPreference(buttonzoomoutKey, false)
    var iconLikeType by rememberPreference(iconLikeTypeKey, IconLikeType.Essential)
    var playerBackgroundColors by rememberPreference(
        playerBackgroundColorsKey,
        PlayerBackgroundColors.BlurredCoverColor
    )
    var blackgradient by rememberPreference(blackgradientKey, false)
    var showTotalTimeQueue by rememberPreference(showTotalTimeQueueKey, true)
    var showNextSongsInPlayer by rememberPreference(showNextSongsInPlayerKey, false)
    var showRemainingSongTime by rememberPreference(showRemainingSongTimeKey, true)
    var disableScrollingText by rememberPreference(disableScrollingTextKey, false)
    var effectRotationEnabled by rememberPreference(effectRotationKey, true)
    var thumbnailTapEnabled by rememberPreference(thumbnailTapEnabledKey, false)
    var clickLyricsText by rememberPreference(clickLyricsTextKey, ClickLyricsText.FullScreen)
    var backgroundProgress by rememberPreference(
        backgroundProgressKey,
        BackgroundProgress.Both
    )
    var transparentBackgroundActionBarPlayer by rememberPreference(
        transparentBackgroundPlayerActionBarKey,
        false
    )
    var actionspacedevenly by rememberPreference(actionspacedevenlyKey, false)
    var tapqueue by rememberPreference(tapqueueKey, true)
    var swipeUpQueue by rememberPreference(swipeUpQueueKey, true)
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
    var playerEnableLyricsPopupMessage by rememberPreference(
        playerEnableLyricsPopupMessageKey,
        true
    )
    var visualizerEnabled by rememberPreference(visualizerEnabledKey, false)
    var showthumbnail by rememberPreference(showthumbnailKey, false)
    /*  ViMusic Mode Settings  */

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
            title = stringResource(R.string.user_interface),
            iconId = R.drawable.ui,
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
        Row (
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


        if (resetCustomLightThemeDialog) {
            ConfirmationDialog(
                text = stringResource(R.string.do_you_really_want_to_reset_the_custom_light_theme_colors),
                onDismiss = { resetCustomLightThemeDialog = false },
                onConfirm = {
                    resetCustomLightThemeDialog = false
                    customThemeLight_Background0 = DefaultLightColorPalette.background0.hashCode()
                    customThemeLight_Background1 = DefaultLightColorPalette.background1.hashCode()
                    customThemeLight_Background2 = DefaultLightColorPalette.background2.hashCode()
                    customThemeLight_Background3 = DefaultLightColorPalette.background3.hashCode()
                    customThemeLight_Background4 = DefaultLightColorPalette.background4.hashCode()
                    customThemeLight_Text = DefaultLightColorPalette.text.hashCode()
                    customThemeLight_TextSecondary = DefaultLightColorPalette.textSecondary.hashCode()
                    customThemeLight_TextDisabled = DefaultLightColorPalette.textDisabled.hashCode()
                    customThemeLight_IconButtonPlayer = DefaultLightColorPalette.iconButtonPlayer.hashCode()
                    customThemeLight_Accent = DefaultLightColorPalette.accent.hashCode()
                }
            )
        }

        if (resetCustomDarkThemeDialog) {
            ConfirmationDialog(
                text = stringResource(R.string.do_you_really_want_to_reset_the_custom_dark_theme_colors),
                onDismiss = { resetCustomDarkThemeDialog = false },
                onConfirm = {
                    resetCustomDarkThemeDialog = false
                    customThemeDark_Background0 = DefaultDarkColorPalette.background0.hashCode()
                    customThemeDark_Background1 = DefaultDarkColorPalette.background1.hashCode()
                    customThemeDark_Background2 = DefaultDarkColorPalette.background2.hashCode()
                    customThemeDark_Background3 = DefaultDarkColorPalette.background3.hashCode()
                    customThemeDark_Background4 = DefaultDarkColorPalette.background4.hashCode()
                    customThemeDark_Text = DefaultDarkColorPalette.text.hashCode()
                    customThemeDark_TextSecondary = DefaultDarkColorPalette.textSecondary.hashCode()
                    customThemeDark_TextDisabled = DefaultDarkColorPalette.textDisabled.hashCode()
                    customThemeDark_IconButtonPlayer = DefaultDarkColorPalette.iconButtonPlayer.hashCode()
                    customThemeDark_Accent = DefaultDarkColorPalette.accent.hashCode()
                }
            )
        }



        //SettingsGroupSpacer()
        SettingsEntryGroupText(title = stringResource(R.string.languages))

        SettingsDescription(text = stringResource(R.string.system_language)+": $systemLocale")

        if (filter.isNullOrBlank() || stringResource(R.string.app_language).contains(filterCharSequence,true))
            EnumValueSelectorSettingsEntry(
                title = stringResource(R.string.app_language),
                selectedValue = languageApp,
                onValueSelected = {languageApp = it },
                valueText = {
                    languageDestinationName(it)
                }
            )



        SettingsGroupSpacer()
        SettingsEntryGroupText(stringResource(R.string.player))

        if (filter.isNullOrBlank() || stringResource(R.string.audio_quality_format).contains(filterCharSequence,true)) {
            EnumValueSelectorSettingsEntry(
                title = stringResource(R.string.audio_quality_format),
                selectedValue = audioQualityFormat,
                onValueSelected = {
                    audioQualityFormat = it
                    restartService = true
                },
                valueText = {
                    when (it) {
                        AudioQualityFormat.Auto -> stringResource(R.string.audio_quality_automatic)
                        AudioQualityFormat.High -> stringResource(R.string.audio_quality_format_high)
                        AudioQualityFormat.Medium -> stringResource(R.string.audio_quality_format_medium)
                        AudioQualityFormat.Low -> stringResource(R.string.audio_quality_format_low)
                    }
                }
            )
            AnimatedVisibility(visible = restartService) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    SettingsDescription(
                        text = stringResource(R.string.minimum_silence_length_warning),
                        important = true,
                        modifier = Modifier.weight(2f)
                    )
                    SecondaryTextButton(
                        text = stringResource(R.string.restart_service),
                        onClick = {
                            binder?.restartForegroundOrStop()?.let { restartService = false }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 24.dp)
                    )
                }
            }
        }

        if (filter.isNullOrBlank() || stringResource(R.string.player_pause_listen_history).contains(filterCharSequence,true)) {
            SwitchSettingEntry(
                title = stringResource(R.string.player_pause_listen_history),
                text = "Does not save playback events used for statistics, history and suggestions in quick pics",
                isChecked = pauseListenHistory,
                onCheckedChange = {
                    pauseListenHistory = it
                    restartService = true
                }
            )
            AnimatedVisibility(visible = restartService) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    SettingsDescription(
                        text = stringResource(R.string.minimum_silence_length_warning),
                        important = true,
                        modifier = Modifier.weight(2f)
                    )
                    SecondaryTextButton(
                        text = stringResource(R.string.restart_service),
                        onClick = {
                            binder?.restartForegroundOrStop()?.let { restartService = false }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 24.dp)
                    )
                }
            }
        }

        if (filter.isNullOrBlank() || stringResource(R.string.min_listening_time).contains(filterCharSequence,true)) {
            EnumValueSelectorSettingsEntry(
                title = stringResource(R.string.min_listening_time),
                selectedValue = exoPlayerMinTimeForEvent,
                onValueSelected = { exoPlayerMinTimeForEvent = it },
                valueText = {
                    when (it) {
                        ExoPlayerMinTimeForEvent.`10s` -> "10s"
                        ExoPlayerMinTimeForEvent.`15s` -> "15s"
                        ExoPlayerMinTimeForEvent.`20s` -> "20s"
                        ExoPlayerMinTimeForEvent.`30s` -> "30s"
                        ExoPlayerMinTimeForEvent.`40s` -> "40s"
                        ExoPlayerMinTimeForEvent.`60s` -> "60s"
                    }
                }
            )
            SettingsDescription(text = stringResource(R.string.is_min_list_time_for_tips_or_quick_pics))
        }

        if (filter.isNullOrBlank() || stringResource(R.string.min_listening_time).contains(filterCharSequence,true)) {
            EnumValueSelectorSettingsEntry(
                title = stringResource(R.string.exclude_songs_with_duration_limit),
                selectedValue = excludeSongWithDurationLimit,
                onValueSelected = { excludeSongWithDurationLimit = it },
                valueText = {
                    when (it) {
                        DurationInMinutes.Disabled -> stringResource(R.string.vt_disabled)
                        DurationInMinutes.`3` -> "3m"
                        DurationInMinutes.`5` -> "5m"
                        DurationInMinutes.`10` -> "10m"
                        DurationInMinutes.`15` -> "15m"
                        DurationInMinutes.`20` -> "20m"
                        DurationInMinutes.`25` -> "25m"
                        DurationInMinutes.`30` -> "30m"
                        DurationInMinutes.`60` -> "60m"
                    }
                }
            )
            SettingsDescription(text = stringResource(R.string.exclude_songs_with_duration_limit_description))
        }

        if (filter.isNullOrBlank() || stringResource(R.string.pause_between_songs).contains(filterCharSequence,true))
            EnumValueSelectorSettingsEntry(
                title = stringResource(R.string.pause_between_songs),
                selectedValue = pauseBetweenSongs,
                onValueSelected = { pauseBetweenSongs = it },
                valueText = {
                    when (it) {
                        PauseBetweenSongs.`0` -> "0s"
                        PauseBetweenSongs.`5` -> "5s"
                        PauseBetweenSongs.`10` -> "10s"
                        PauseBetweenSongs.`15` -> "15s"
                        PauseBetweenSongs.`20` -> "20s"
                        PauseBetweenSongs.`30` -> "30s"
                        PauseBetweenSongs.`40` -> "40s"
                        PauseBetweenSongs.`50` -> "50s"
                        PauseBetweenSongs.`60` -> "60s"
                    }
                }
            )

        if (filter.isNullOrBlank() || stringResource(R.string.player_pause_on_volume_zero).contains(filterCharSequence,true))
            SwitchSettingEntry(
                title = stringResource(R.string.player_pause_on_volume_zero),
                text = stringResource(R.string.info_pauses_player_when_volume_zero),
                isChecked = isPauseOnVolumeZeroEnabled,
                onCheckedChange = {
                    isPauseOnVolumeZeroEnabled = it
                }
            )

        if (filter.isNullOrBlank() || stringResource(R.string.effect_fade_audio).contains(filterCharSequence,true)) {
            EnumValueSelectorSettingsEntry(
                title = stringResource(R.string.effect_fade_audio),
                selectedValue = playbackFadeAudioDuration,
                onValueSelected = { playbackFadeAudioDuration = it },
                valueText = {
                    when (it) {
                        DurationInMilliseconds.Disabled -> stringResource(R.string.vt_disabled)
                        else -> {
                            it.toString()
                        }
                    }
                }
            )
            SettingsDescription(text = stringResource(R.string.effect_fade_audio_description))
        }

        /*
        if (filter.isNullOrBlank() || stringResource(R.string.effect_fade_songs).contains(filterCharSequence,true))
            EnumValueSelectorSettingsEntry(
                title = stringResource(R.string.effect_fade_songs),
                selectedValue = playbackFadeDuration,
                onValueSelected = { playbackFadeDuration = it },
                valueText = {
                    when (it) {
                        DurationInSeconds.Disabled -> stringResource(R.string.vt_disabled)
                        DurationInSeconds.`3` -> "3 %s".format(stringResource(R.string.time_seconds))
                        DurationInSeconds.`4` -> "4 %s".format(stringResource(R.string.time_seconds))
                        DurationInSeconds.`5` -> "5 %s".format(stringResource(R.string.time_seconds))
                        DurationInSeconds.`6` -> "6 %s".format(stringResource(R.string.time_seconds))
                        DurationInSeconds.`7` -> "7 %s".format(stringResource(R.string.time_seconds))
                        DurationInSeconds.`8` -> "8 %s".format(stringResource(R.string.time_seconds))
                        DurationInSeconds.`9` -> "9 %s".format(stringResource(R.string.time_seconds))
                        DurationInSeconds.`10` -> "10 %s".format(stringResource(R.string.time_seconds))
                        DurationInSeconds.`11` -> "11 %s".format(stringResource(R.string.time_seconds))
                        DurationInSeconds.`12` -> "12 %s".format(stringResource(R.string.time_seconds))
                    }
                }
            )
         */



            if (filter.isNullOrBlank() || stringResource(R.string.player_keep_minimized).contains(filterCharSequence,true))
                SwitchSettingEntry(
                    title = stringResource(R.string.player_keep_minimized),
                    text = stringResource(R.string.when_click_on_a_song_player_start_minimized),
                    isChecked = keepPlayerMinimized,
                    onCheckedChange = {
                        keepPlayerMinimized = it
                    }
                )


        if (filter.isNullOrBlank() || stringResource(R.string.player_collapsed_disable_swiping_down).contains(filterCharSequence,true))
            SwitchSettingEntry(
                title = stringResource(R.string.player_collapsed_disable_swiping_down),
                text = stringResource(R.string.avoid_closing_the_player_cleaning_queue_by_swiping_down),
                isChecked = disableClosingPlayerSwipingDown,
                onCheckedChange = {
                    disableClosingPlayerSwipingDown = it
                }
            )

        if (filter.isNullOrBlank() || stringResource(R.string.max_songs_in_queue).contains(filterCharSequence,true))
            EnumValueSelectorSettingsEntry(
                title = stringResource(R.string.max_songs_in_queue),
                selectedValue = maxSongsInQueue,
                onValueSelected = { maxSongsInQueue = it },
                valueText = {
                    when (it) {
                        MaxSongs.Unlimited -> stringResource(R.string.unlimited)
                        MaxSongs.`50` -> MaxSongs.`50`.name
                        MaxSongs.`100` -> MaxSongs.`100`.name
                        MaxSongs.`200` -> MaxSongs.`200`.name
                        MaxSongs.`300` -> MaxSongs.`300`.name
                        MaxSongs.`500` -> MaxSongs.`500`.name
                        MaxSongs.`1000` -> MaxSongs.`1000`.name
                        MaxSongs.`2000` -> MaxSongs.`2000`.name
                        MaxSongs.`3000` -> MaxSongs.`3000`.name
                    }
                }
            )

        if (filter.isNullOrBlank() || stringResource(R.string.discover).contains(
                filterCharSequence,
                true
            )
        )
            SwitchSettingEntry(
                title = stringResource(R.string.discover),
                text = stringResource(R.string.discoverinfo),
                isChecked = discoverIsEnabled,
                onCheckedChange = { discoverIsEnabled = it }
            )

        if (filter.isNullOrBlank() || stringResource(R.string.playlistindicator).contains(filterCharSequence,true))
            SwitchSettingEntry(
                title = stringResource(R.string.playlistindicator),
                text = stringResource(R.string.playlistindicatorinfo),
                isChecked = playlistindicator,
                onCheckedChange = {
                    playlistindicator = it
                }
            )

        if (filter.isNullOrBlank() || stringResource(R.string.persistent_queue).contains(filterCharSequence,true))
            SwitchSettingEntry(
                title = stringResource(R.string.persistent_queue),
                text = stringResource(R.string.save_and_restore_playing_songs),
                isChecked = persistentQueue,
                onCheckedChange = {
                    persistentQueue = it
                }
            )


        if (filter.isNullOrBlank() || stringResource(R.string.resume_playback).contains(filterCharSequence,true))
            if (isAtLeastAndroid6) {
                SwitchSettingEntry(
                    title = stringResource(R.string.resume_playback),
                    text = stringResource(R.string.when_device_is_connected),
                    isChecked = resumePlaybackWhenDeviceConnected,
                    onCheckedChange = {
                        resumePlaybackWhenDeviceConnected = it
                    }
                )
            }

        if (filter.isNullOrBlank() || stringResource(R.string.close_app_with_back_button).contains(filterCharSequence,true)) {
            SwitchSettingEntry(
                isEnabled = Build.VERSION.SDK_INT >= 33,
                title = stringResource(R.string.close_app_with_back_button),
                text = stringResource(R.string.when_you_use_the_back_button_from_the_home_page),
                isChecked = closeWithBackButton,
                onCheckedChange = {
                    closeWithBackButton = it
                }
            )
            ImportantSettingsDescription(text = stringResource(R.string.restarting_rimusic_is_required))
        }

        if (filter.isNullOrBlank() || stringResource(R.string.close_background_player).contains(filterCharSequence,true))
            SwitchSettingEntry(
                title = stringResource(R.string.close_background_player),
                text = stringResource(R.string.when_app_swipe_out_from_task_manager),
                isChecked = closebackgroundPlayer,
                onCheckedChange = {
                    closebackgroundPlayer = it
                }
            )

        if (filter.isNullOrBlank() || stringResource(R.string.skip_silence).contains(filterCharSequence,true)) {
            SwitchSettingEntry(
                title = stringResource(R.string.skip_silence),
                text = stringResource(R.string.skip_silent_parts_during_playback),
                isChecked = skipSilence,
                onCheckedChange = {
                    skipSilence = it
                }
            )

            AnimatedVisibility(visible = skipSilence) {
                val initialValue by remember { derivedStateOf { minimumSilenceDuration.toFloat() / 1000L } }
                var newValue by remember(initialValue) { mutableFloatStateOf(initialValue) }


                Column(
                    modifier = Modifier.padding(start = 25.dp)
                ) {
                    SliderSettingsEntry(
                        title = stringResource(R.string.minimum_silence_length),
                        text = stringResource(R.string.minimum_silence_length_description),
                        state = newValue,
                        onSlide = { newValue = it },
                        onSlideComplete = {
                            minimumSilenceDuration = newValue.toLong() * 1000L
                            restartService = true
                        },
                        toDisplay = { stringResource(R.string.format_ms, it.toLong()) },
                        range = 1.00f..2000.000f
                    )

                    AnimatedVisibility(visible = restartService) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            SettingsDescription(
                                text = stringResource(R.string.minimum_silence_length_warning),
                                important = true,
                                modifier = Modifier.weight(2f)
                            )
                            SecondaryTextButton(
                                text = stringResource(R.string.restart_service),
                                onClick = {
                                    binder?.restartForegroundOrStop()?.let { restartService = false }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 24.dp)
                            )
                        }
                    }
                }
            }

        }

        if (filter.isNullOrBlank() || stringResource(R.string.loudness_normalization).contains(filterCharSequence,true))
            SwitchSettingEntry(
                title = stringResource(R.string.loudness_normalization),
                text = stringResource(R.string.autoadjust_the_volume),
                isChecked = volumeNormalization,
                onCheckedChange = {
                    volumeNormalization = it
                }
            )


        if (filter.isNullOrBlank() || stringResource(R.string.event_volumekeys).contains(filterCharSequence,true)) {
            SwitchSettingEntry(
                title = stringResource(R.string.event_volumekeys),
                text = stringResource(R.string.event_volumekeysinfo),
                isChecked = useVolumeKeysToChangeSong,
                onCheckedChange = {
                    useVolumeKeysToChangeSong = it
                }
            )
            ImportantSettingsDescription(text = stringResource(R.string.restarting_rimusic_is_required))
        }


        if (filter.isNullOrBlank() || stringResource(R.string.event_shake).contains(filterCharSequence,true))
            SwitchSettingEntry(
                title = stringResource(R.string.event_shake),
                text = stringResource(R.string.shake_to_change_song),
                isChecked = shakeEventEnabled,
                onCheckedChange = {
                    shakeEventEnabled = it
                }
            )

        if (filter.isNullOrBlank() || stringResource(R.string.equalizer).contains(filterCharSequence,true))
            SettingsEntry(
                title = stringResource(R.string.equalizer),
                text = stringResource(R.string.interact_with_the_system_equalizer),
                onClick = launchEqualizer
                /*
                onClick = {
                    val intent = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL).apply {
                        putExtra(AudioEffect.EXTRA_AUDIO_SESSION, binder?.player?.audioSessionId)
                        putExtra(AudioEffect.EXTRA_PACKAGE_NAME, context.packageName)
                        putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC)
                    }

                    try {
                        activityResultLauncher.launch(intent)
                    } catch (e: ActivityNotFoundException) {
                        SmartMessage(context.resources.getString(R.string.info_not_find_application_audio), type = PopupType.Warning, context = context)
                    }
                }
                 */
            )

        SettingsGroupSpacer()
        SettingsEntryGroupText(stringResource(R.string.user_interface))

        if (filter.isNullOrBlank() || stringResource(R.string.interface_in_use).contains(filterCharSequence,true))
            EnumValueSelectorSettingsEntry(
                title = stringResource(R.string.interface_in_use),
                selectedValue = uiType,
                onValueSelected = {
                    uiType = it
                    if (uiType == UiType.ViMusic) {
                        disablePlayerHorizontalSwipe = true
                        disableIconButtonOnTop = true
                        playerTimelineType = PlayerTimelineType.FakeAudioBar
                        visualizerEnabled = false
                        playerThumbnailSize = PlayerThumbnailSize.Medium
                        thumbnailTapEnabled = true
                        showSearchTab = true
                        showStatsInNavbar = true
                        navigationBarPosition = NavigationBarPosition.Left
                        showTopActionsBar = false
                        playerType = PlayerType.Modern
                        queueType = QueueType.Modern
                        fadingedge = false
                        carousel = true
                        carouselSize = CarouselSize.Medium
                        thumbnailType = ThumbnailType.Essential
                        expandedlyrics = false
                        playerTimelineSize = PlayerTimelineSize.Medium
                        playerInfoShowIcons = true
                        miniPlayerType = MiniPlayerType.Modern
                        playerSwapControlsWithTimeline = false
                        transparentBackgroundActionBarPlayer = false
                        playerControlsType = PlayerControlsType.Essential
                        playerPlayButtonType = PlayerPlayButtonType.Disabled
                        buttonzoomout = true
                        iconLikeType = IconLikeType.Essential
                        playerBackgroundColors = PlayerBackgroundColors.CoverColorGradient
                        blackgradient = true
                        showTotalTimeQueue = false
                        showRemainingSongTime = false
                        showNextSongsInPlayer = false
                        disableScrollingText = false
                        effectRotationEnabled = true
                        clickLyricsText = ClickLyricsText.FullScreen
                        playerEnableLyricsPopupMessage = true
                        backgroundProgress = BackgroundProgress.MiniPlayer
                        transparentBackgroundActionBarPlayer = true
                        actionspacedevenly = false
                        tapqueue = false
                        swipeUpQueue = true
                        showButtonPlayerDiscover = false
                        showButtonPlayerDownload = false
                        showButtonPlayerAddToPlaylist = false
                        showButtonPlayerLoop = false
                        showButtonPlayerShuffle = false
                        showButtonPlayerLyrics = false
                        expandedplayertoggle = false
                        showButtonPlayerSleepTimer = false
                        showButtonPlayerSystemEqualizer = false
                        showButtonPlayerArrow = false
                        showButtonPlayerShuffle = false
                        showButtonPlayerMenu = true
                        showthumbnail = true
                        keepPlayerMinimized = false
                    } else {
                        disablePlayerHorizontalSwipe = false
                        disableIconButtonOnTop = false
                        playerTimelineType = lastPlayerTimelineType
                        playerThumbnailSize = lastPlayerThumbnailSize
                        playerPlayButtonType = lastPlayerPlayButtonType

                    }

                },
                valueText = {
                    it.name
                }
            )

        if (filter.isNullOrBlank() || stringResource(R.string.theme).contains(filterCharSequence,true))
            EnumValueSelectorSettingsEntry(
                title = stringResource(R.string.theme),
                selectedValue = colorPaletteName,
                onValueSelected = {
                    colorPaletteName = it
                   when (it) {
                       ColorPaletteName.PureBlack,
                       ColorPaletteName.ModernBlack -> colorPaletteMode = ColorPaletteMode.System
                       else -> {}
                   }
                },
                valueText = {
                    when (it) {
                        ColorPaletteName.Default -> stringResource(R.string._default)
                        ColorPaletteName.Dynamic -> stringResource(R.string.dynamic)
                        ColorPaletteName.PureBlack -> stringResource(R.string.theme_pure_black)
                        ColorPaletteName.ModernBlack -> stringResource(R.string.theme_modern_black)
                        ColorPaletteName.MaterialYou -> stringResource(R.string.theme_material_you)
                        ColorPaletteName.Customized -> stringResource(R.string.theme_customized)
                    }
                }
            )

        AnimatedVisibility(visible = colorPaletteName == ColorPaletteName.Customized) {
            Column {
                SettingsEntryGroupText(stringResource(R.string.title_customized_light_theme_colors))
                ButtonBarSettingEntry(
                    title = stringResource(R.string.title_reset_customized_light_colors),
                    text = stringResource(R.string.info_click_to_reset_default_light_colors),
                    icon = R.drawable.trash,
                    onClick = { resetCustomLightThemeDialog = true }
                )
                ColorSettingEntry(
                    title = stringResource(R.string.color_background_1),
                    text = "",
                    color = Color(customThemeLight_Background0),
                    onColorSelected = {
                        customThemeLight_Background0 = it.hashCode()
                    }
                )
                ColorSettingEntry(
                    title = stringResource(R.string.color_background_2),
                    text = "",
                    color = Color(customThemeLight_Background1),
                    onColorSelected = {
                        customThemeLight_Background1 = it.hashCode()
                    }
                )
                ColorSettingEntry(
                    title = stringResource(R.string.color_background_3),
                    text = "",
                    color = Color(customThemeLight_Background2),
                    onColorSelected = {
                        customThemeLight_Background2 = it.hashCode()
                    }
                )
                ColorSettingEntry(
                    title = stringResource(R.string.color_background_4),
                    text = "",
                    color = Color(customThemeLight_Background3),
                    onColorSelected = {
                        customThemeLight_Background3 = it.hashCode()
                    }
                )
                ColorSettingEntry(
                    title = stringResource(R.string.color_background_5),
                    text = "",
                    color = Color(customThemeLight_Background4),
                    onColorSelected = {
                        customThemeLight_Background4 = it.hashCode()
                    }
                )
                ColorSettingEntry(
                    title = stringResource(R.string.color_text),
                    text = "",
                    color = Color(customThemeLight_Text),
                    onColorSelected = {
                        customThemeLight_Text= it.hashCode()
                    }
                )
                ColorSettingEntry(
                    title = stringResource(R.string.color_text_secondary),
                    text = "",
                    color = Color(customThemeLight_TextSecondary),
                    onColorSelected = {
                        customThemeLight_TextSecondary = it.hashCode()
                    }
                )
                ColorSettingEntry(
                    title = stringResource(R.string.color_text_disabled),
                    text = "",
                    color = Color(customThemeLight_TextDisabled),
                    onColorSelected = {
                        customThemeLight_TextDisabled = it.hashCode()
                    }
                )
                ColorSettingEntry(
                    title = stringResource(R.string.color_icon_button_player),
                    text = "",
                    color = Color(customThemeLight_IconButtonPlayer),
                    onColorSelected = {
                        customThemeLight_IconButtonPlayer = it.hashCode()
                    }
                )
                ColorSettingEntry(
                    title = stringResource(R.string.color_accent),
                    text = "",
                    color = Color(customThemeLight_Accent),
                    onColorSelected = {
                        customThemeLight_Accent = it.hashCode()
                    }
                )

                SettingsEntryGroupText(stringResource(R.string.title_customized_dark_theme_colors))
                ButtonBarSettingEntry(
                    title = stringResource(R.string.title_reset_customized_dark_colors),
                    text = stringResource(R.string.click_to_reset_default_dark_colors),
                    icon = R.drawable.trash,
                    onClick = { resetCustomDarkThemeDialog = true }
                )
                ColorSettingEntry(
                    title = stringResource(R.string.color_background_1),
                    text = "",
                    color = Color(customThemeDark_Background0),
                    onColorSelected = {
                        customThemeDark_Background0 = it.hashCode()
                    }
                )
                ColorSettingEntry(
                    title = stringResource(R.string.color_background_2),
                    text = "",
                    color = Color(customThemeDark_Background1),
                    onColorSelected = {
                        customThemeDark_Background1 = it.hashCode()
                    }
                )
                ColorSettingEntry(
                    title = stringResource(R.string.color_background_3),
                    text = "",
                    color = Color(customThemeDark_Background2),
                    onColorSelected = {
                        customThemeDark_Background2 = it.hashCode()
                    }
                )
                ColorSettingEntry(
                    title = stringResource(R.string.color_background_4),
                    text = "",
                    color = Color(customThemeDark_Background3),
                    onColorSelected = {
                        customThemeDark_Background3 = it.hashCode()
                    }
                )
                ColorSettingEntry(
                    title = stringResource(R.string.color_background_5),
                    text = "",
                    color = Color(customThemeDark_Background4),
                    onColorSelected = {
                        customThemeDark_Background4 = it.hashCode()
                    }
                )
                ColorSettingEntry(
                    title = stringResource(R.string.color_text),
                    text = "",
                    color = Color(customThemeDark_Text),
                    onColorSelected = {
                        customThemeDark_Text= it.hashCode()
                    }
                )
                ColorSettingEntry(
                    title = stringResource(R.string.color_text_secondary),
                    text = "",
                    color = Color(customThemeDark_TextSecondary),
                    onColorSelected = {
                        customThemeDark_TextSecondary = it.hashCode()
                    }
                )
                ColorSettingEntry(
                    title = stringResource(R.string.color_text_disabled),
                    text = "",
                    color = Color(customThemeDark_TextDisabled),
                    onColorSelected = {
                        customThemeDark_TextDisabled = it.hashCode()
                    }
                )
                ColorSettingEntry(
                    title = stringResource(R.string.color_icon_button_player),
                    text = "",
                    color = Color(customThemeDark_IconButtonPlayer),
                    onColorSelected = {
                        customThemeDark_IconButtonPlayer = it.hashCode()
                    }
                )
                ColorSettingEntry(
                    title = stringResource(R.string.color_accent),
                    text = "",
                    color = Color(customThemeDark_Accent),
                    onColorSelected = {
                        customThemeDark_Accent = it.hashCode()
                    }
                )
            }
        }

        if (filter.isNullOrBlank() || stringResource(R.string.theme_mode).contains(filterCharSequence,true))
            EnumValueSelectorSettingsEntry(
                title = stringResource(R.string.theme_mode),
                selectedValue = colorPaletteMode,
                isEnabled = when (colorPaletteName) {
                    ColorPaletteName.PureBlack -> false
                    ColorPaletteName.ModernBlack -> false
                    else -> { true }
                },
                onValueSelected = {
                    colorPaletteMode = it
                    //if (it == ColorPaletteMode.PitchBlack) colorPaletteName = ColorPaletteName.ModernBlack
                },
                valueText = {
                    when (it) {
                        ColorPaletteMode.Dark -> stringResource(R.string.dark)
                        ColorPaletteMode.Light -> stringResource(R.string._light)
                        ColorPaletteMode.System -> stringResource(R.string.system)
                        ColorPaletteMode.PitchBlack -> stringResource(R.string.theme_mode_pitch_black)
                    }
                }
            )

        if (filter.isNullOrBlank() || stringResource(R.string.navigation_bar_position).contains(filterCharSequence,true))
            EnumValueSelectorSettingsEntry(
                title = stringResource(R.string.navigation_bar_position),
                selectedValue = navigationBarPosition,
                onValueSelected = { navigationBarPosition = it },
                valueText = {
                    when (it) {
                        NavigationBarPosition.Left -> stringResource(R.string.direction_left)
                        NavigationBarPosition.Right -> stringResource(R.string.direction_right)
                        NavigationBarPosition.Top -> stringResource(R.string.direction_top)
                        NavigationBarPosition.Bottom -> stringResource(R.string.direction_bottom)
                    }
                }
            )

        if (filter.isNullOrBlank() || stringResource(R.string.navigation_bar_type).contains(filterCharSequence,true))
            EnumValueSelectorSettingsEntry(
                title = stringResource(R.string.navigation_bar_type),
                selectedValue = navigationBarType,
                onValueSelected = { navigationBarType = it },
                valueText = {
                    when (it) {
                        NavigationBarType.IconAndText -> stringResource(R.string.icon_and_text)
                        NavigationBarType.IconOnly -> stringResource(R.string.only_icon)
                    }
                }
            )

        if (filter.isNullOrBlank() || stringResource(R.string.player_position).contains(filterCharSequence,true))
            EnumValueSelectorSettingsEntry(
                title = stringResource(R.string.player_position),
                selectedValue = playerPosition,
                onValueSelected = { playerPosition = it },
                valueText = {
                    when (it) {
                        PlayerPosition.Top -> stringResource(R.string.position_top)
                        PlayerPosition.Bottom -> stringResource(R.string.position_bottom)
                    }
                }
            )

        if (filter.isNullOrBlank() || stringResource(R.string.menu_style).contains(filterCharSequence,true))
            EnumValueSelectorSettingsEntry(
                title = stringResource(R.string.menu_style),
                selectedValue = menuStyle,
                onValueSelected = { menuStyle = it },
                valueText = {
                    when (it) {
                        MenuStyle.Grid -> stringResource(R.string.style_grid)
                        MenuStyle.List -> stringResource(R.string.style_list)
                    }
                }
            )

        if (filter.isNullOrBlank() || stringResource(R.string.message_type).contains(filterCharSequence,true))
            EnumValueSelectorSettingsEntry(
                title = stringResource(R.string.message_type),
                selectedValue = messageType,
                onValueSelected = { messageType = it },
                valueText = {
                    when (it) {
                        MessageType.Modern -> stringResource(R.string.message_type_modern)
                        MessageType.Essential -> stringResource(R.string.message_type_essential)
                    }
                }
            )

        if (filter.isNullOrBlank() || stringResource(R.string.default_page).contains(filterCharSequence,true))
            EnumValueSelectorSettingsEntry(
                title = stringResource(R.string.default_page),
                selectedValue = indexNavigationTab,
                onValueSelected = {indexNavigationTab = it},
                valueText = {
                    when (it) {
                        HomeScreenTabs.Default -> stringResource(R.string._default)
                        HomeScreenTabs.QuickPics -> stringResource(R.string.quick_picks)
                        HomeScreenTabs.Songs -> stringResource(R.string.songs)
                        HomeScreenTabs.Albums -> stringResource(R.string.albums)
                        HomeScreenTabs.Artists -> stringResource(R.string.artists)
                        HomeScreenTabs.Playlists -> stringResource(R.string.playlists)
                        HomeScreenTabs.Search -> stringResource(R.string.search)
                    }
                }
            )

        if (filter.isNullOrBlank() || stringResource(R.string.transition_effect).contains(filterCharSequence,true))
            EnumValueSelectorSettingsEntry(
                title = stringResource(R.string.transition_effect),
                selectedValue = transitionEffect,
                onValueSelected = { transitionEffect = it },
                valueText = {
                    when (it) {
                        TransitionEffect.None -> stringResource(R.string.none)
                        TransitionEffect.Expand -> stringResource(R.string.te_expand)
                        TransitionEffect.Fade -> stringResource(R.string.te_fade)
                        TransitionEffect.Scale -> stringResource(R.string.te_scale)
                        TransitionEffect.SlideVertical -> stringResource(R.string.te_slide_vertical)
                        TransitionEffect.SlideHorizontal -> stringResource(R.string.te_slide_horizontal)
                    }
                }
            )

        if (uiType == UiType.ViMusic) {
            if (filter.isNullOrBlank() || stringResource(R.string.vimusic_show_search_button_in_navigation_bar).contains(
                    filterCharSequence,
                    true
                )
            )
                SwitchSettingEntry(
                    title = stringResource(R.string.vimusic_show_search_button_in_navigation_bar),
                    text = stringResource(R.string.vismusic_only_in_left_right_navigation_bar),
                    isChecked = showSearchTab,
                    onCheckedChange = { showSearchTab = it }
                )



            if (filter.isNullOrBlank() || stringResource(R.string.show_statistics_in_navigation_bar).contains(
                    filterCharSequence,
                    true
                )
            )
                SwitchSettingEntry(
                    title = stringResource(R.string.show_statistics_in_navigation_bar),
                    text = "",
                    isChecked = showStatsInNavbar,
                    onCheckedChange = { showStatsInNavbar = it }
                )
        }

        if (filter.isNullOrBlank() || stringResource(R.string.show_floating_icon).contains(filterCharSequence,true))
            SwitchSettingEntry(
                title = stringResource(R.string.show_floating_icon),
                text = "",
                isChecked = showFloatingIcon,
                onCheckedChange = { showFloatingIcon = it }
            )



        if (filter.isNullOrBlank() || stringResource(R.string.settings_use_font_type).contains(filterCharSequence,true))
            EnumValueSelectorSettingsEntry(
                title = stringResource(R.string.settings_use_font_type),
                selectedValue = fontType,
                onValueSelected = { fontType = it },
                valueText = {
                    when (it) {
                        FontType.Rubik -> FontType.Rubik.name
                        FontType.Poppins -> FontType.Poppins.name
                    }
                }
            )

        if (filter.isNullOrBlank() || stringResource(R.string.use_system_font).contains(filterCharSequence,true))
            SwitchSettingEntry(
                title = stringResource(R.string.use_system_font),
                text = stringResource(R.string.use_font_by_the_system),
                isChecked = useSystemFont,
                onCheckedChange = { useSystemFont = it }
            )

        if (filter.isNullOrBlank() || stringResource(R.string.apply_font_padding).contains(filterCharSequence,true))
            SwitchSettingEntry(
                title = stringResource(R.string.apply_font_padding),
                text = stringResource(R.string.add_spacing_around_texts),
                isChecked = applyFontPadding,
                onCheckedChange = { applyFontPadding = it }
            )


        if (filter.isNullOrBlank() || stringResource(R.string.swipe_to_action).contains(filterCharSequence,true))
            SwitchSettingEntry(
                title = stringResource(R.string.swipe_to_action),
                text = stringResource(R.string.activate_the_action_menu_by_swiping_the_song_left_or_right),
                isChecked = isSwipeToActionEnabled,
                onCheckedChange = { isSwipeToActionEnabled = it }
            )


        SettingsGroupSpacer()
        SettingsEntryGroupText(title = stringResource(R.string.songs).uppercase())

        if (filter.isNullOrBlank() || "${stringResource(R.string.show)} ${stringResource(R.string.favorites)}".contains(filterCharSequence,true))
            SwitchSettingEntry(
                title = "${stringResource(R.string.show)} ${stringResource(R.string.favorites)}",
                text = "",
                isChecked = showFavoritesPlaylist,
                onCheckedChange = { showFavoritesPlaylist = it }
            )
        /*
        if (filter.isNullOrBlank() || "${stringResource(R.string.show)} ${stringResource(R.string.cached)}".contains(filterCharSequence,true))
            SwitchSettingEntry(
                title = "${stringResource(R.string.show)} ${stringResource(R.string.cached)}",
                text = "",
                isChecked = showCachedPlaylist,
                onCheckedChange = { showCachedPlaylist = it }
            )
         */
        if (filter.isNullOrBlank() || "${stringResource(R.string.show)} ${stringResource(R.string.downloaded)}".contains(filterCharSequence,true))
            SwitchSettingEntry(
                title = "${stringResource(R.string.show)} ${stringResource(R.string.downloaded)}",
                text = "",
                isChecked = showDownloadedPlaylist,
                onCheckedChange = { showDownloadedPlaylist = it }
            )
        if (filter.isNullOrBlank() || "${stringResource(R.string.show)} ${stringResource(R.string.my_playlist_top)}".contains(filterCharSequence,true))
            SwitchSettingEntry(
                title = "${stringResource(R.string.show)} ${stringResource(R.string.my_playlist_top).format(maxTopPlaylistItems)}",
                text = "",
                isChecked = showMyTopPlaylist,
                onCheckedChange = { showMyTopPlaylist = it }
            )
        if (filter.isNullOrBlank() || "${stringResource(R.string.show)} ${stringResource(R.string.on_device)}".contains(filterCharSequence,true))
            SwitchSettingEntry(
                title = "${stringResource(R.string.show)} ${stringResource(R.string.on_device)}",
                text = "",
                isChecked = showOnDevicePlaylist,
                onCheckedChange = { showOnDevicePlaylist = it }
            )

        /*
        SettingsGroupSpacer()
        SettingsEntryGroupText(title = stringResource(R.string.playlists).uppercase())

        if (filter.isNullOrBlank() || "${stringResource(R.string.show)} ${stringResource(R.string.playlists)}".contains(filterCharSequence,true))
            SwitchSettingEntry(
                title = "${stringResource(R.string.show)} ${stringResource(R.string.playlists)}",
                text = "",
                isChecked = showPlaylists,
                onCheckedChange = { showPlaylists = it }
            )
        if (filter.isNullOrBlank() || "${stringResource(R.string.show)} ${stringResource(R.string.monthly_playlists)}".contains(filterCharSequence,true))
            SwitchSettingEntry(
                title = "${stringResource(R.string.show)} ${stringResource(R.string.monthly_playlists)}",
                text = "",
                isChecked = showMonthlyPlaylistInLibrary,
                onCheckedChange = { showMonthlyPlaylistInLibrary = it }
            )
         */

        SettingsGroupSpacer()
        SettingsEntryGroupText(title = stringResource(R.string.playlists).uppercase())

        if (filter.isNullOrBlank() || "${stringResource(R.string.show)} ${stringResource(R.string.piped_playlists)}".contains(filterCharSequence,true))
            SwitchSettingEntry(
                title = "${stringResource(R.string.show)} ${stringResource(R.string.piped_playlists)}",
                text = "",
                isChecked = showPipedPlaylists,
                onCheckedChange = { showPipedPlaylists = it }
            )

        if (filter.isNullOrBlank() || "${stringResource(R.string.show)} ${stringResource(R.string.pinned_playlists)}".contains(filterCharSequence,true))
            SwitchSettingEntry(
                title = "${stringResource(R.string.show)} ${stringResource(R.string.pinned_playlists)}",
                text = "",
                isChecked = showPinnedPlaylists,
                onCheckedChange = { showPinnedPlaylists = it }
            )

        if (filter.isNullOrBlank() || "${stringResource(R.string.show)} ${stringResource(R.string.monthly_playlists)}".contains(filterCharSequence,true))
            SwitchSettingEntry(
                title = "${stringResource(R.string.show)} ${stringResource(R.string.monthly_playlists)}",
                text = "",
                isChecked = showMonthlyPlaylists,
                onCheckedChange = { showMonthlyPlaylists = it }
            )

        SettingsGroupSpacer()
        SettingsEntryGroupText(stringResource(R.string.monthly_playlists).uppercase())

        if (filter.isNullOrBlank() || stringResource(R.string.monthly_playlists).contains(filterCharSequence,true))
            SwitchSettingEntry(
                title = stringResource(R.string.enable_monthly_playlists_creation),
                text = "",
                isChecked = enableCreateMonthlyPlaylists,
                onCheckedChange = {
                    enableCreateMonthlyPlaylists = it
                }
            )

        SettingsGroupSpacer()
        SettingsEntryGroupText(stringResource(R.string.smart_recommendations))

        if (filter.isNullOrBlank() || stringResource(R.string.statistics_max_number_of_items).contains(filterCharSequence,true))
            EnumValueSelectorSettingsEntry(
                title = stringResource(R.string.statistics_max_number_of_items),
                selectedValue = recommendationsNumber,
                onValueSelected = { recommendationsNumber = it },
                valueText = {
                    it.number.toString()
                }
            )

        SettingsGroupSpacer()
        SettingsEntryGroupText(stringResource(R.string.statistics))

        if (filter.isNullOrBlank() || stringResource(R.string.statistics_max_number_of_items).contains(filterCharSequence,true))
            EnumValueSelectorSettingsEntry(
                title = stringResource(R.string.statistics_max_number_of_items),
                selectedValue = maxStatisticsItems,
                onValueSelected = { maxStatisticsItems = it },
                valueText = {
                    it.number.toString()
                }
            )

        if (filter.isNullOrBlank() || stringResource(R.string.listening_time).contains(filterCharSequence,true))
            SwitchSettingEntry(
                title = stringResource(R.string.listening_time),
                text = stringResource(R.string.shows_the_number_of_songs_heard_and_their_listening_time),
                isChecked = showStatsListeningTime,
                onCheckedChange = {
                    showStatsListeningTime = it
                }
            )

        SettingsGroupSpacer()
        SettingsEntryGroupText(stringResource(R.string.playlist_top))

        if (filter.isNullOrBlank() || stringResource(R.string.statistics_max_number_of_items).contains(filterCharSequence,true))
            EnumValueSelectorSettingsEntry(
                title = stringResource(R.string.statistics_max_number_of_items),
                selectedValue = maxTopPlaylistItems,
                onValueSelected = { maxTopPlaylistItems = it },
                valueText = {
                    it.number.toString()
                }
            )

        SettingsGroupSpacer(
            modifier = Modifier.height(Dimensions.bottomSpacer)
        )

    }
}
