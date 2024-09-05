package com.peecock.ymusic.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.pm.ServiceInfo
import android.content.res.Configuration
import android.database.SQLException
import android.graphics.Bitmap
import android.graphics.Color
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.media.audiofx.AudioEffect
import android.media.audiofx.LoudnessEnhancer
import android.media.session.PlaybackState
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.RatingCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.text.format.DateUtils
import android.view.KeyEvent
import androidx.annotation.OptIn
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startForegroundService
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import androidx.core.text.isDigitsOnly
import androidx.media.VolumeProviderCompat
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.common.audio.SonicAudioProcessor
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.ResolvingDataSource
import androidx.media3.datasource.cache.Cache
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.NoOpCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.RenderersFactory
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.exoplayer.analytics.PlaybackStats
import androidx.media3.exoplayer.analytics.PlaybackStatsListener
import androidx.media3.exoplayer.audio.AudioRendererEventListener
import androidx.media3.exoplayer.audio.AudioSink
import androidx.media3.exoplayer.audio.DefaultAudioOffloadSupportProvider
import androidx.media3.exoplayer.audio.DefaultAudioSink
import androidx.media3.exoplayer.audio.DefaultAudioSink.DefaultAudioProcessorChain
import androidx.media3.exoplayer.audio.MediaCodecAudioRenderer
import androidx.media3.exoplayer.audio.SilenceSkippingAudioProcessor
import androidx.media3.exoplayer.mediacodec.MediaCodecSelector
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.extractor.DefaultExtractorsFactory
import androidx.media3.extractor.ExtractorsFactory
import androidx.media3.extractor.mkv.MatroskaExtractor
import androidx.media3.extractor.mp4.FragmentedMp4Extractor
import androidx.media3.extractor.text.DefaultSubtitleParserFactory
import com.peecock.innertube.Innertube
import com.peecock.innertube.models.NavigationEndpoint
import com.peecock.innertube.models.bodies.PlayerBody
import com.peecock.innertube.models.bodies.SearchBody
import com.peecock.innertube.requests.player
import com.peecock.innertube.requests.searchPage
import com.peecock.innertube.utils.ProxyPreferences
import com.peecock.innertube.utils.from
import com.peecock.ymusic.Database
import com.peecock.ymusic.MainActivity
import com.peecock.ymusic.R
import com.peecock.ymusic.enums.AudioQualityFormat
import com.peecock.ymusic.enums.DurationInMilliseconds
import com.peecock.ymusic.enums.ExoPlayerCacheLocation
import com.peecock.ymusic.enums.ExoPlayerDiskCacheMaxSize
import com.peecock.ymusic.enums.ExoPlayerMinTimeForEvent
import com.peecock.ymusic.extensions.audiovolume.AudioVolumeObserver
import com.peecock.ymusic.extensions.audiovolume.OnAudioVolumeChangedListener
import com.peecock.ymusic.models.Event
import com.peecock.ymusic.models.Format
import com.peecock.ymusic.models.PersistentQueue
import com.peecock.ymusic.models.PersistentSong
import com.peecock.ymusic.models.QueuedMediaItem
import com.peecock.ymusic.models.Song
import com.peecock.ymusic.models.SongEntity
import com.peecock.ymusic.models.asMediaItem
import com.peecock.ymusic.query
import com.peecock.ymusic.transaction
import com.peecock.ymusic.utils.InvincibleService
import com.peecock.ymusic.utils.RingBuffer
import com.peecock.ymusic.utils.TimerJob
import com.peecock.ymusic.utils.YouTubeRadio
import com.peecock.ymusic.utils.activityPendingIntent
import com.peecock.ymusic.utils.asSong
import com.peecock.ymusic.utils.audioQualityFormatKey
import com.peecock.ymusic.utils.broadCastPendingIntent
import com.peecock.ymusic.utils.cleanPrefix
import com.peecock.ymusic.utils.closebackgroundPlayerKey
import com.peecock.ymusic.utils.discoverKey
import com.peecock.ymusic.utils.exoPlayerCacheLocationKey
import com.peecock.ymusic.utils.exoPlayerCustomCacheKey
import com.peecock.ymusic.utils.exoPlayerDiskCacheMaxSizeKey
import com.peecock.ymusic.utils.exoPlayerMinTimeForEventKey
import com.peecock.ymusic.utils.findNextMediaItemById
import com.peecock.ymusic.utils.forcePlayFromBeginning
import com.peecock.ymusic.utils.getEnum
import com.peecock.ymusic.utils.intent
import com.peecock.ymusic.utils.isAtLeastAndroid10
import com.peecock.ymusic.utils.isAtLeastAndroid12
import com.peecock.ymusic.utils.isAtLeastAndroid13
import com.peecock.ymusic.utils.isAtLeastAndroid6
import com.peecock.ymusic.utils.isAtLeastAndroid8
import com.peecock.ymusic.utils.isInvincibilityEnabledKey
import com.peecock.ymusic.utils.isPauseOnVolumeZeroEnabledKey
import com.peecock.ymusic.utils.isShowingThumbnailInLockscreenKey
import com.peecock.ymusic.utils.manageDownload
import com.peecock.ymusic.utils.mediaItems
import com.peecock.ymusic.utils.minimumSilenceDurationKey
import com.peecock.ymusic.utils.pauseListenHistoryKey
import com.peecock.ymusic.utils.persistentQueueKey
import com.peecock.ymusic.utils.playbackFadeAudioDurationKey
import com.peecock.ymusic.utils.preferences
import com.peecock.ymusic.utils.queueLoopEnabledKey
import com.peecock.ymusic.utils.resumePlaybackWhenDeviceConnectedKey
import com.peecock.ymusic.utils.shouldBePlaying
import com.peecock.ymusic.utils.showDownloadButtonBackgroundPlayerKey
import com.peecock.ymusic.utils.showLikeButtonBackgroundPlayerKey
import com.peecock.ymusic.utils.skipSilenceKey
import com.peecock.ymusic.utils.startFadeAnimator
import com.peecock.ymusic.utils.thumbnail
import com.peecock.ymusic.utils.timer
import com.peecock.ymusic.utils.trackLoopEnabledKey
import com.peecock.ymusic.utils.useVolumeKeysToChangeSongKey
import com.peecock.ymusic.utils.volumeNormalizationKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import timber.log.Timber
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.ConnectException
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.time.Duration
import kotlin.math.roundToInt
import kotlin.system.exitProcess
import kotlin.time.Duration.Companion.seconds
import android.os.Binder as AndroidBinder


const val LOCAL_KEY_PREFIX = "local:"

@get:OptIn(UnstableApi::class)
val DataSpec.isLocal get() = key?.startsWith(LOCAL_KEY_PREFIX) == true

val MediaItem.isLocal get() = mediaId.startsWith(LOCAL_KEY_PREFIX)
val Song.isLocal get() = id.startsWith(LOCAL_KEY_PREFIX)

@UnstableApi
@Suppress("DEPRECATION")
class PlayerService : InvincibleService(),
    Player.Listener,
    PlaybackStatsListener.Callback,
    SharedPreferences.OnSharedPreferenceChangeListener,
    OnAudioVolumeChangedListener {
    private val coroutineScope = CoroutineScope(Dispatchers.IO) + Job()
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var cache: SimpleCache
    private lateinit var player: ExoPlayer
    private lateinit var downloadCache: SimpleCache
    private lateinit var audioVolumeObserver: AudioVolumeObserver
    //private lateinit var connectivityManager: ConnectivityManager

    private val actions = PlaybackStateCompat.ACTION_PLAY or
            PlaybackStateCompat.ACTION_PAUSE or
            PlaybackStateCompat.ACTION_PLAY_PAUSE or
            PlaybackStateCompat.ACTION_STOP or
            PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
            PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
            PlaybackStateCompat.ACTION_SKIP_TO_QUEUE_ITEM or
            PlaybackStateCompat.ACTION_SEEK_TO or
            PlaybackStateCompat.ACTION_REWIND or
            PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH


    @ExperimentalCoroutinesApi
    @FlowPreview
    private val stateBuilderWithoutCustomAction
        get() = PlaybackStateCompat.Builder().setActions(actions.let {
            if (isAtLeastAndroid12) it or PlaybackState.ACTION_SET_PLAYBACK_SPEED else it
        })

    @ExperimentalCoroutinesApi
    @FlowPreview
    private val stateBuilder
        get() = PlaybackStateCompat.Builder().setActions(actions.let {
            if (isAtLeastAndroid12) it or PlaybackState.ACTION_SET_PLAYBACK_SPEED else it
        })
            .addCustomAction(
                "SHUFFLE",
                "Shuffle",
                if (shuffleModeEnabled) R.drawable.shuffle_filled else R.drawable.shuffle
            )
            .addCustomAction(
                "LIKE",
                "Like",
                if (isLikedState.value) R.drawable.heart else R.drawable.heart_outline
            )
            .addCustomAction(
                "DOWNLOAD",
                "Download",
                if (isDownloadedState.value || isCachedState.value) R.drawable.downloaded else R.drawable.download

            )
            .addCustomAction(
                "PLAYRADIO",
                "Play radio",
                R.drawable.radio
            )

    @ExperimentalCoroutinesApi
    @FlowPreview
    private val stateBuilderWithDownloadOnly
        get() = PlaybackStateCompat.Builder().setActions(actions.let {
            if (isAtLeastAndroid12) it or PlaybackState.ACTION_SET_PLAYBACK_SPEED else it
        }).addCustomAction(
            "DOWNLOAD",
            "Download",
            if (isDownloadedState.value || isCachedState.value) R.drawable.downloaded else R.drawable.download

        )
            .addCustomAction(
                "PLAYRADIO",
                "Play radio",
                R.drawable.radio
            )

    @ExperimentalCoroutinesApi
    @FlowPreview
    private val stateBuilderWithLikeOnly
        get() = PlaybackStateCompat.Builder().setActions(actions.let {
            if (isAtLeastAndroid12) it or PlaybackState.ACTION_SET_PLAYBACK_SPEED else it
        }).addCustomAction(
            "LIKE",
            "Like",
            if (isLikedState.value) R.drawable.heart else R.drawable.heart_outline
        )
            .addCustomAction(
                "PLAYRADIO",
                "Play radio",
                R.drawable.radio
            )

    private val playbackStateMutex = Mutex()

    private val metadataBuilder = MediaMetadataCompat.Builder()

    private var notificationManager: NotificationManager? = null

    private var timerJob: TimerJob? = null

    private var radio: YouTubeRadio? = null

    private lateinit var bitmapProvider: BitmapProvider

    private var volumeNormalizationJob: Job? = null

    private var isPersistentQueueEnabled = false
    private var isclosebackgroundPlayerEnabled = false
    private var isShowingThumbnailInLockscreen = true
    override var isInvincibilityEnabled = false

    private var audioManager: AudioManager? = null
    private var audioDeviceCallback: AudioDeviceCallback? = null

    private var loudnessEnhancer: LoudnessEnhancer? = null

    private val binder = Binder()

    private var isNotificationStarted = false

    //private var exoPlayerAlternateCacheLocation = ""

    override val notificationId: Int
        get() = NotificationId

    private lateinit var notificationActionReceiver: NotificationActionReceiver
    private lateinit var audioQualityFormat: AudioQualityFormat

    /*
    private val media = MutableStateFlow<MediaItem?>(null)
    @kotlin.OptIn(ExperimentalCoroutinesApi::class)
    private val currentMedia = media.flatMapLatest { mediaItem ->
        Database.song(mediaItem?.mediaId)
    }.stateIn(coroutineScope, SharingStarted.Lazily, null)

    @kotlin.OptIn(ExperimentalCoroutinesApi::class)
    private var currentMediaLiked = media.flatMapLatest { mediaItem ->
       if (mediaItem?.mediaId?.let { Database.songliked(it) } == 0) flowOf(false) else flowOf(true)
    }.stateIn(coroutineScope, SharingStarted.Lazily, false)

    @kotlin.OptIn(ExperimentalCoroutinesApi::class)
    private var currentMediaDownloaded = media.flatMapLatest { mediaItem ->
        val downloads = DownloadUtil.downloads.value
        flowOf(downloads[mediaItem?.mediaId]?.state == Download.STATE_COMPLETED)
    }.stateIn(coroutineScope, SharingStarted.Lazily, false)
     */

    private val mediaItemState = MutableStateFlow<MediaItem?>(null)

    @ExperimentalCoroutinesApi
    @FlowPreview
    private val isLikedState = mediaItemState
        .flatMapMerge { item ->
            item?.mediaId?.let { Database.likedAt(it).distinctUntilChanged() } ?: flowOf(null)
        }
        .map { it != null }
        .stateIn(coroutineScope, SharingStarted.Eagerly, false)

    //private val mediaDownloadedItemState = MutableStateFlow<MediaItem?>(null)

    @ExperimentalCoroutinesApi
    @FlowPreview
    private val isDownloadedState = mediaItemState
        .flatMapMerge { item ->
            item?.mediaId?.let {
                val downloads = DownloadUtil.downloads.value
                flowOf(downloads[item.mediaId]?.state == Download.STATE_COMPLETED)
                //flowOf(downloadCache.isCached(it, 0, Database.formatContentLength(it)))
            } ?: flowOf(false)
        }
        //.map { true }
        .stateIn(coroutineScope, SharingStarted.Eagerly, false)

    //private val mediaCachedItemState = MutableStateFlow<MediaItem?>(null)

    @ExperimentalCoroutinesApi
    @FlowPreview
    private val isCachedState = mediaItemState
        .flatMapMerge { item ->
            item?.mediaId?.let {
                flowOf(
                    cache.isCached(it, 0, Database.formatContentLength(it))
                )
            } ?: flowOf(false)
        }
        .map { it }
        .stateIn(coroutineScope, SharingStarted.Eagerly, false)

    private var showLikeButton = true
    private var showDownloadButton = true

    private var shuffleModeEnabled = false

    override fun onBind(intent: Intent?): AndroidBinder {
        super.onBind(intent)
        return binder
    }


    @ExperimentalCoroutinesApi
    @FlowPreview
    @SuppressLint("Range")
    @UnstableApi
    override fun onCreate() {
        super.onCreate()

        runCatching {
            bitmapProvider = BitmapProvider(
                bitmapSize = (512 * resources.displayMetrics.density).roundToInt(),
                colorProvider = { isSystemInDarkMode ->
                    if (isSystemInDarkMode) Color.BLACK else Color.WHITE
                }
            )
        }.onFailure {
            Timber.e("Failed init bitmap provider in PlayerService ${it.stackTraceToString()}")
        }

        createNotificationChannel()

        preferences.registerOnSharedPreferenceChangeListener(this)

        val preferences = preferences
        isPersistentQueueEnabled = preferences.getBoolean(persistentQueueKey, false)
        isInvincibilityEnabled = preferences.getBoolean(isInvincibilityEnabledKey, false)
        isShowingThumbnailInLockscreen =
            preferences.getBoolean(isShowingThumbnailInLockscreenKey, false)

        audioQualityFormat = preferences.getEnum(audioQualityFormatKey, AudioQualityFormat.High)
        showLikeButton = preferences.getBoolean(showLikeButtonBackgroundPlayerKey, true)
        showDownloadButton = preferences.getBoolean(showDownloadButtonBackgroundPlayerKey, true)

        val exoPlayerCustomCache = preferences.getInt(exoPlayerCustomCacheKey, 32) * 1000 * 1000L

        val cacheEvictor = when (val size =
            preferences.getEnum(exoPlayerDiskCacheMaxSizeKey, ExoPlayerDiskCacheMaxSize.`32MB`)) {
            ExoPlayerDiskCacheMaxSize.Unlimited -> NoOpCacheEvictor()
            ExoPlayerDiskCacheMaxSize.Custom -> LeastRecentlyUsedCacheEvictor(exoPlayerCustomCache)
            else -> LeastRecentlyUsedCacheEvictor(size.bytes)
        }

        //val cacheEvictor = NoOpCacheEvictor()
        val exoPlayerCacheLocation = preferences.getEnum(
            exoPlayerCacheLocationKey, ExoPlayerCacheLocation.System
        )
        val directoryLocation =
            if (exoPlayerCacheLocation == ExoPlayerCacheLocation.Private) filesDir else cacheDir

        var cacheDirName = "rimusic_cache"

        val cacheSize =
            preferences.getEnum(exoPlayerDiskCacheMaxSizeKey, ExoPlayerDiskCacheMaxSize.`32MB`)

        if (cacheSize == ExoPlayerDiskCacheMaxSize.Disabled) cacheDirName = "rimusic_no_cache"

        val directory = directoryLocation.resolve(cacheDirName).also { dir ->
            if (dir.exists()) return@also

            dir.mkdir()

            directoryLocation.listFiles()?.forEach { file ->
                if (file.isDirectory && file.name.length == 1 && file.name.isDigitsOnly() || file.extension == "uid") {
                    if (!file.renameTo(dir.resolve(file.name))) {
                        file.deleteRecursively()
                    }
                }
            }

            filesDir.resolve("coil").deleteRecursively()
        }

        /*
        if (exoPlayerAlternateCacheLocation == "") {
            directory = cacheDir.resolve(cacheDirName).also { dir ->
                if (dir.exists()) return@also

                dir.mkdir()

                cacheDir.listFiles()?.forEach { file ->
                    if (file.isDirectory && file.name.length == 1 && file.name.isDigitsOnly() || file.extension == "uid") {
                        if (!file.renameTo(dir.resolve(file.name))) {
                            file.deleteRecursively()
                        }
                    }
                }

                filesDir.resolve("coil").deleteRecursively()
            }

        } else {
            // Available before android 10
            val path = File(exoPlayerAlternateCacheLocation)
            directory = path.resolve(cacheDirName).also { dir ->
                if (dir.exists()) return@also

                dir.mkdir()

                dir.listFiles()?.forEach { file ->
                    if (file.isDirectory && file.name.length == 1 && file.name.isDigitsOnly() || file.extension == "uid") {
                        if (!dir.resolve(file.name).let { file.renameTo(it) }) {
                            file.deleteRecursively()
                        }
                    }
                }

                dir.resolve("coil").deleteRecursively()
            }
        }
        */

        cache = SimpleCache(directory, cacheEvictor, StandaloneDatabaseProvider(this))
        downloadCache = DownloadUtil.getDownloadSimpleCache(applicationContext) as SimpleCache

        player = ExoPlayer.Builder(this, createRendersFactory(), createMediaSourceFactory())
            //.setRenderersFactory(DefaultRenderersFactory(this).setEnableDecoderFallback(true))
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .setUsage(C.USAGE_MEDIA)
                    .build(),
                true
            )
            //.setWakeMode(C.WAKE_MODE_LOCAL)
            .setWakeMode(C.WAKE_MODE_NETWORK)
            .setHandleAudioBecomingNoisy(true)
            //.setSeekForwardIncrementMs(5000)
            //.setSeekBackIncrementMs(5000)
            //.setUsePlatformDiagnostics(false)
            .build()

        player.repeatMode = when {
            preferences.getBoolean(trackLoopEnabledKey, false) -> Player.REPEAT_MODE_ONE
            preferences.getBoolean(queueLoopEnabledKey, true) -> Player.REPEAT_MODE_ALL
            else -> Player.REPEAT_MODE_OFF
        }

        player.skipSilenceEnabled = preferences.getBoolean(skipSilenceKey, false)
        player.addListener(this)
        player.addAnalyticsListener(PlaybackStatsListener(false, this))

        // Build a PendingIntent that can be used to launch the UI.
        val sessionActivityPendingIntent =
            packageManager?.getLaunchIntentForPackage(packageName)?.let { sessionIntent ->
                PendingIntent.getActivity(this, 0, sessionIntent, PendingIntent.FLAG_IMMUTABLE)
            }

        mediaSession = MediaSessionCompat(baseContext, "PlayerService")
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)
        mediaSession.setRatingType(RatingCompat.RATING_NONE)
        mediaSession.setSessionActivity(sessionActivityPendingIntent)
        mediaSession.setCallback(SessionCallback(player))

        if (preferences.getBoolean(useVolumeKeysToChangeSongKey, false))
            mediaSession.setPlaybackToRemote(getVolumeProvider())

        audioVolumeObserver = AudioVolumeObserver(this)
        audioVolumeObserver.register(AudioManager.STREAM_MUSIC, this)

        //connectivityManager = getSystemService()!!

        if (showLikeButton && showDownloadButton)
            mediaSession.setPlaybackState(stateBuilder.build())
        if (showLikeButton && !showDownloadButton)
            mediaSession.setPlaybackState(stateBuilderWithLikeOnly.build())
        if (showDownloadButton && !showLikeButton)
            mediaSession.setPlaybackState(stateBuilderWithDownloadOnly.build())
        if (!showLikeButton && !showDownloadButton)
            mediaSession.setPlaybackState(stateBuilderWithoutCustomAction.build())

        mediaSession.isActive = true

        updatePlaybackState()


        coroutineScope.launch {
            var first = true
            mediaItemState.zip(isLikedState) { mediaItem, _ ->
                if (first) {
                    first = false
                    return@zip
                }
                if (mediaItem == null) return@zip
                withContext(Dispatchers.Main) {
                    updatePlaybackState()
                    handler.post {
                        runCatching {
                            applicationContext.getSystemService<NotificationManager>()
                                ?.notify(NotificationId, notification())
                        }.onFailure {
                            Timber.e("Failed likedState in PlayerService ${it.stackTraceToString()}")
                        }
                    }
                }
            }.collect()
        }

        coroutineScope.launch {
            var first = true
            mediaItemState.zip(isDownloadedState) { mediaItem, _ ->
                if (first) {
                    first = false
                    return@zip
                }
                if (mediaItem == null) return@zip
                withContext(Dispatchers.Main) {
                    updatePlaybackState()
                    handler.post {
                        runCatching {
                            applicationContext.getSystemService<NotificationManager>()
                                ?.notify(NotificationId, notification())
                        }.onFailure {
                            Timber.e("Failed downloadState in PlayerService ${it.stackTraceToString()}")
                        }
                    }
                }
            }.collect()
        }

        coroutineScope.launch {
            var first = true
            mediaItemState.zip(isCachedState) { mediaItem, _ ->
                if (first) {
                    first = false
                    return@zip
                }
                if (mediaItem == null) return@zip
                withContext(Dispatchers.Main) {
                    updatePlaybackState()
                    handler.post {
                        runCatching {
                            applicationContext.getSystemService<NotificationManager>()
                                ?.notify(NotificationId, notification())
                        }.onFailure {
                            Timber.e("Failed cachedState in PlayerService ${it.stackTraceToString()}")
                        }
                    }
                }
            }.collect()
        }

        notificationActionReceiver = NotificationActionReceiver(player)


        val filter = IntentFilter().apply {
            addAction(Action.play.value)
            addAction(Action.pause.value)
            addAction(Action.next.value)
            addAction(Action.previous.value)
            addAction(Action.like.value)
            addAction(Action.download.value)
            addAction(Action.playradio.value)
            addAction(Action.shuffle.value)
        }

        ContextCompat.registerReceiver(
            this,
            notificationActionReceiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )

        maybeRestorePlayerQueue()
        //maybeRestoreFromDiskPlayerQueue()

        if (isPersistentQueueEnabled) {
            // Save persistent queue periodically
            coroutineScope.launch {
                while (isActive) {
                    delay(25.seconds)
                    withContext(Dispatchers.Main) {
                        maybeSavePlayerQueue()
                        //maybeSaveToDiskPlayerQueue()
                    }
                }
            }
        }

        maybeResumePlaybackWhenDeviceConnected()

        //workaround for android 12+
        runCatching {
            notification()?.let {
                ServiceCompat.startForeground(
                    this@PlayerService,
                    NotificationId,
                    it,
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                    } else {
                        0
                    }
                )
            }
        }.onFailure {
            Timber.e("PlayerService oncreate startForeground ${it.stackTraceToString()}")
        }

    }

    private fun getVolumeProvider(): VolumeProviderCompat {
        val audio = getSystemService(AUDIO_SERVICE) as AudioManager?

        val STREAM_TYPE = AudioManager.STREAM_MUSIC
        val currentVolume = audio?.getStreamVolume(STREAM_TYPE)
        val maxVolume = audio?.getStreamMaxVolume(STREAM_TYPE)
        val VOLUME_UP = 1
        val VOLUME_DOWN = -1

        return object :
            VolumeProviderCompat(VOLUME_CONTROL_RELATIVE, maxVolume!!, currentVolume!!) {

                override fun onAdjustVolume(direction: Int) {
                        val useVolumeKeysToChangeSong = preferences.getBoolean(useVolumeKeysToChangeSongKey, false)
                        // Up = 1, Down = -1, Release = 0
                        if (direction == VOLUME_UP) {
                            if (binder.player.isPlaying && useVolumeKeysToChangeSong) {
                                binder.player.seekToNextMediaItem()
                            } else {
                                audio?.adjustStreamVolume(
                                    STREAM_TYPE,
                                    AudioManager.ADJUST_RAISE, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE
                                )
                                if (audio != null) {
                                    setCurrentVolume(audio.getStreamVolume(STREAM_TYPE))
                                }
                            }
                        } else if (direction == VOLUME_DOWN) {
                            if (binder.player.isPlaying && useVolumeKeysToChangeSong) {
                                binder.player.seekToPreviousMediaItem()
                            } else {
                                audio?.adjustStreamVolume(
                                    STREAM_TYPE,
                                    AudioManager.ADJUST_LOWER, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE
                                )
                                if (audio != null) {
                                    setCurrentVolume(audio.getStreamVolume(STREAM_TYPE))
                                }
                            }
                        }
                }

        }
    }

    override fun onTaskRemoved(rootIntent: Intent?) {

        isclosebackgroundPlayerEnabled = preferences.getBoolean(closebackgroundPlayerKey, false)
        if (isclosebackgroundPlayerEnabled) {
            //if (!player.shouldBePlaying) {
            broadCastPendingIntent<NotificationDismissReceiver>().send()
            //}
            this.stopService(this.intent<MyDownloadService>())
            this.stopService(this.intent<PlayerService>())
            //stopSelf()
            onDestroy()
        }
        super.onTaskRemoved(rootIntent)
    }

    @UnstableApi
    override fun onDestroy() {
        runCatching {
            maybeSavePlayerQueue()

            preferences.unregisterOnSharedPreferenceChangeListener(this)

            player.removeListener(this)
            player.stop()
            player.release()

            unregisterReceiver(notificationActionReceiver)

            mediaSession.isActive = false
            mediaSession.release()
            cache.release()
            //downloadCache.release()
            loudnessEnhancer?.release()
            audioVolumeObserver.unregister()

            coroutineScope.cancel()
        }.onFailure {
            Timber.e("Failed onDestroy in PlayerService ${it.stackTraceToString()}")
        }
        super.onDestroy()
    }

    override fun shouldBeInvincible(): Boolean {
        return !player.shouldBePlaying
    }

    private var pausedByZeroVolume = false
    override fun onAudioVolumeChanged(currentVolume: Int, maxVolume: Int) {
        if (preferences.getBoolean(isPauseOnVolumeZeroEnabledKey, false)) {
            if (player.isPlaying && currentVolume < 1) {
                binder.callPause {}
                pausedByZeroVolume = true
            } else if (pausedByZeroVolume && currentVolume >= 1) {
                binder.player.play()
                pausedByZeroVolume = false
            }
        }
    }

    override fun onAudioVolumeDirectionChanged(direction: Int) {
        /*
        if (direction == 0) {
            binder.player.seekToPreviousMediaItem()
        } else {
            binder.player.seekToNextMediaItem()
        }

         */
    }


    @ExperimentalCoroutinesApi
    @FlowPreview
    override fun onConfigurationChanged(newConfig: Configuration) {
        handler.post {
            runCatching {
                if (bitmapProvider.setDefaultBitmap() && player.currentMediaItem != null) {
                    notificationManager?.notify(NotificationId, notification())
                }
            }.onFailure {
                Timber.e("Failed onConfigurationChanged in PlayerService ${it.stackTraceToString()}")
            }
        }
        super.onConfigurationChanged(newConfig)
    }

    @UnstableApi
    override fun onPlaybackStatsReady(
        eventTime: AnalyticsListener.EventTime,
        playbackStats: PlaybackStats
    ) {
        // if pause listen history is enabled, don't register statistic event
        if (preferences.getBoolean(pauseListenHistoryKey, false)) return

        val mediaItem =
            eventTime.timeline.getWindow(eventTime.windowIndex, Timeline.Window()).mediaItem

        val totalPlayTimeMs = playbackStats.totalPlayTimeMs

        if (totalPlayTimeMs > 5000) {
            query {
                Database.incrementTotalPlayTimeMs(mediaItem.mediaId, totalPlayTimeMs)
            }
        }


        val minTimeForEvent =
            preferences.getEnum(exoPlayerMinTimeForEventKey, ExoPlayerMinTimeForEvent.`20s`)

        if (totalPlayTimeMs > minTimeForEvent.ms) {
            query {
                try {
                    Database.insert(
                        Event(
                            songId = mediaItem.mediaId,
                            timestamp = System.currentTimeMillis(),
                            playTime = totalPlayTimeMs
                        )
                    )
                } catch (e: SQLException) {
                    Timber.e("PlayerService onPlaybackStatsReady SQLException ${e.stackTraceToString()}")
                }
            }

        }
    }

    @UnstableApi
    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {

        mediaItemState.update { mediaItem }
        //mediaDownloadedItemState.update { mediaItem }
        //mediaCachedItemState.update { mediaItem }

        maybeRecoverPlaybackError()
        maybeNormalizeVolume()
        maybeProcessRadio()

        if (mediaItem == null) {
            runCatching {
                bitmapProvider.listener?.invoke(null)
            }.onFailure {
                Timber.e("Failed onMediaItemTransition bitmapProvider.invoke in PlayerService ${it.stackTraceToString()}")
            }
        } else if (mediaItem.mediaMetadata.artworkUri == bitmapProvider.lastUri) {
            runCatching {
                bitmapProvider.listener?.invoke(bitmapProvider.lastBitmap)
            }.onFailure {
                Timber.e("Failed onMediaItemTransition bitmapProvider.invoke lastbitmap in PlayerService ${it.stackTraceToString()}")
            }
        }

        if (reason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO || reason == Player.MEDIA_ITEM_TRANSITION_REASON_SEEK) {
            updateMediaSessionQueue(player.currentTimeline)
        }

    }

    override fun onTimelineChanged(timeline: Timeline, reason: Int) {
        if (reason == Player.TIMELINE_CHANGE_REASON_PLAYLIST_CHANGED) {
            updateMediaSessionQueue(timeline)
        }
    }

    private fun updateMediaSessionQueue(timeline: Timeline) {
        val builder = MediaDescriptionCompat.Builder()

        val currentMediaItemIndex = player.currentMediaItemIndex
        val lastIndex = timeline.windowCount - 1
        var startIndex = currentMediaItemIndex - 7
        var endIndex = currentMediaItemIndex + 7

        if (startIndex < 0) {
            endIndex -= startIndex
        }

        if (endIndex > lastIndex) {
            startIndex -= (endIndex - lastIndex)
            endIndex = lastIndex
        }

        startIndex = startIndex.coerceAtLeast(0)

        mediaSession.setQueue(
            List(endIndex - startIndex + 1) { index ->
                val mediaItem = timeline.getWindow(index + startIndex, Timeline.Window()).mediaItem
                MediaSessionCompat.QueueItem(
                    builder
                        .setMediaId(mediaItem.mediaId)
                        .setTitle(cleanPrefix(mediaItem.mediaMetadata.title.toString()))
                        .setSubtitle(
                            if (mediaItem.mediaMetadata.albumTitle != null)
                                "${mediaItem.mediaMetadata.artist} | ${mediaItem.mediaMetadata.albumTitle}"
                            else mediaItem.mediaMetadata.artist
                        )
                        .setIconUri(mediaItem.mediaMetadata.artworkUri)
                        .setExtras(mediaItem.mediaMetadata.extras)
                        .build(),
                    (index + startIndex).toLong()
                )
            }
        )
    }

    private fun maybeRecoverPlaybackError() {
        if (player.playerError != null) {
            player.prepare()
        }
    }

    private fun maybeProcessRadio() {
        // Old feature add songs only if radio is started by user and when last song in player is played
        radio?.let { radio ->
            if (player.mediaItemCount - player.currentMediaItemIndex == 1) {
                coroutineScope.launch(Dispatchers.Main) {
                    player.addMediaItems(radio.process())
                }
            }
        }

        /* // New feature auto start radio in queue
        if (radio == null) {
            binder.setupRadio(
                NavigationEndpoint.Endpoint.Watch(
                    videoId = player.currentMediaItem?.mediaId
                )
            )
        } else {
            radio?.let { radio ->
                if (player.mediaItemCount - player.currentMediaItemIndex <= 3) {
                    coroutineScope.launch(Dispatchers.Main) {
                        player.addMediaItems(radio.process())
                    }
                }
            }
        }
        */
    }

    @ExperimentalCoroutinesApi
    @FlowPreview
    @UnstableApi
    private fun maybeRestoreFromDiskPlayerQueue() {
        if (!isPersistentQueueEnabled) return
        //Log.d("mediaItem", "QueuePersistentEnabled Restore Initial")

        runCatching {
            filesDir.resolve("persistentQueue.data").inputStream().use { fis ->
                ObjectInputStream(fis).use { oos ->
                    oos.readObject() as PersistentQueue
                }
            }
        }.onSuccess { queue ->
            //Log.d("mediaItem", "QueuePersistentEnabled Restored queue $queue")
            //Log.d("mediaItem", "QueuePersistentEnabled Restored ${queue.songMediaItems.size}")
            runBlocking(Dispatchers.Main) {
                player.setMediaItems(
                    queue.songMediaItems.map { song ->
                        song.asMediaItem.buildUpon()
                            .setUri(song.asMediaItem.mediaId)
                            .setCustomCacheKey(song.asMediaItem.mediaId)
                            .build().apply {
                                mediaMetadata.extras?.putBoolean("isFromPersistentQueue", true)
                            }
                    },
                    queue.mediaItemIndex,
                    queue.position
                )

                player.prepare()

                isNotificationStarted = true
                kotlin.runCatching {
                    startForegroundService(this@PlayerService, intent<PlayerService>())
                }.onFailure {
                    Timber.e("maybeRestoreFromDiskPlayerQueue PlayerService startForegroundService ${it.stackTraceToString()}")
                }
                runCatching {
                    //startForeground(NotificationId, notification())
                    notification()?.let {
                        ServiceCompat.startForeground(
                            this@PlayerService,
                            NotificationId,
                            it,
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                            } else {
                                0
                            }
                        )
                    }
                }.onFailure {
                    Timber.e("maybeRestoreFromDiskPlayerQueue PlayerService startForeground ${it.stackTraceToString()}")
                }
            }

        }.onFailure {
            //it.printStackTrace()
            Timber.e(it.stackTraceToString())
        }

        //Log.d("mediaItem", "QueuePersistentEnabled Restored ${player.currentTimeline.mediaItems.size}")

    }

    private fun maybeSaveToDiskPlayerQueue() {

        if (!isPersistentQueueEnabled) return
        //Log.d("mediaItem", "QueuePersistentEnabled Save ${player.currentTimeline.mediaItems.size}")

        val persistentQueue = PersistentQueue(
            title = "title",
            songMediaItems = player.currentTimeline.mediaItems.map {
                PersistentSong(
                    id = it.mediaId,
                    title = it.mediaMetadata.title.toString(),
                    durationText = it.mediaMetadata.extras?.getString("durationText").toString(),
                    thumbnailUrl = it.mediaMetadata.artworkUri.toString()
                )
            },
            mediaItemIndex = player.currentMediaItemIndex,
            position = player.currentPosition
        )

        runCatching {
            filesDir.resolve("persistentQueue.data").outputStream().use { fos ->
                ObjectOutputStream(fos).use { oos ->
                    oos.writeObject(persistentQueue)
                }
            }
        }.onFailure {
            //it.printStackTrace()
            Timber.e(it.stackTraceToString())

        }.onSuccess {
            Log.d("mediaItem", "QueuePersistentEnabled Saved $persistentQueue")
        }

    }

    private fun maybeSavePlayerQueue() {
        if (!isPersistentQueueEnabled) return
        /*
        if (player.playbackState == Player.STATE_IDLE) {
            Log.d("mediaItem", "QueuePersistentEnabled playbackstate idle return")
            return
        }
         */
        //Log.d("mediaItem", "QueuePersistentEnabled Save ${player.currentTimeline.mediaItems.size}")
        //Log.d("mediaItem", "QueuePersistentEnabled Save initial")

        val mediaItems = player.currentTimeline.mediaItems
        val mediaItemIndex = player.currentMediaItemIndex
        val mediaItemPosition = player.currentPosition

        mediaItems.mapIndexed { index, mediaItem ->
            QueuedMediaItem(
                mediaItem = mediaItem,
                position = if (index == mediaItemIndex) mediaItemPosition else null
            )
        }.let { queuedMediaItems ->
            query {
                Database.clearQueue()
                Database.insert(queuedMediaItems)
            }
        }
    }

    @ExperimentalCoroutinesApi
    @FlowPreview
    @UnstableApi
    private fun maybeRestorePlayerQueue() {
        if (!isPersistentQueueEnabled) return

        query {
            val queuedSong = Database.queue()

            if (queuedSong.isEmpty()) return@query

            val index = queuedSong.indexOfFirst { it.position != null }.coerceAtLeast(0)

            runBlocking(Dispatchers.Main) {
                player.setMediaItems(
                    queuedSong.map { mediaItem ->
                        mediaItem.mediaItem.buildUpon()
                            .setUri(mediaItem.mediaItem.mediaId)
                            .setCustomCacheKey(mediaItem.mediaItem.mediaId)
                            .build().apply {
                                mediaMetadata.extras?.putBoolean("isFromPersistentQueue", true)
                            }
                    },
                    index,
                    queuedSong[index].position ?: C.TIME_UNSET
                )
                player.prepare()

                isNotificationStarted = true
                runCatching {
                    startForegroundService(this@PlayerService, intent<PlayerService>())
                }.onFailure {
                    Timber.e("maybeRestorePlayerQueue startForegroundService ${it.stackTraceToString()}")
                }
                runCatching {
                    //startForeground(NotificationId, notification())
                    notification()?.let {
                        ServiceCompat.startForeground(
                            this@PlayerService,
                            NotificationId,
                            it,
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                            } else {
                                0
                            }
                        )
                    }
                }.onFailure {
                    Timber.e("maybeRestorePlayerQueue startForeground ${it.stackTraceToString()}")
                }
            }
        }

    }

    @UnstableApi
    private fun maybeNormalizeVolume() {
        if (!preferences.getBoolean(volumeNormalizationKey, false)) {
            loudnessEnhancer?.enabled = false
            loudnessEnhancer?.release()
            loudnessEnhancer = null
            volumeNormalizationJob?.cancel()
            player.volume = 1f
            return
        }

        runCatching {
            if (loudnessEnhancer == null) {
                loudnessEnhancer = LoudnessEnhancer(player.audioSessionId)
            }
        }.onFailure {
            Timber.e(it.stackTraceToString())
            return
        }

        player.currentMediaItem?.mediaId?.let { songId ->
            volumeNormalizationJob?.cancel()
            volumeNormalizationJob = coroutineScope.launch(Dispatchers.Main) {
                Database.loudnessDb(songId).cancellable().collectLatest { loudnessDb ->
                    try {
                        loudnessEnhancer?.setTargetGain(-((loudnessDb ?: 0f) * 100).toInt() + 500)
                        loudnessEnhancer?.enabled = true
                    } catch (_: Exception) {
                    }
                }
            }
        }
    }

    private fun maybeShowSongCoverInLockScreen() {
        val bitmap =
            if (isAtLeastAndroid13 || isShowingThumbnailInLockscreen) bitmapProvider.bitmap else null

        val uri = player.mediaMetadata.artworkUri?.toString()?.thumbnail(512)
        metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ART, bitmap)
        metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_ART_URI, uri)
        metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, bitmap)
        metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, uri)

        if (isAtLeastAndroid13 && player.currentMediaItemIndex == 0) {
            metadataBuilder.putText(
                MediaMetadataCompat.METADATA_KEY_TITLE,
                "${cleanPrefix(player.mediaMetadata.title.toString())} "
            )
        }

        mediaSession.setMetadata(metadataBuilder.build())
    }

    @SuppressLint("NewApi")
    private fun maybeResumePlaybackWhenDeviceConnected() {
        if (!isAtLeastAndroid6) return

        if (preferences.getBoolean(resumePlaybackWhenDeviceConnectedKey, false)) {
            if (audioManager == null) {
                audioManager = getSystemService(AUDIO_SERVICE) as AudioManager?
            }

            audioDeviceCallback = object : AudioDeviceCallback() {
                private fun canPlayMusic(audioDeviceInfo: AudioDeviceInfo): Boolean {
                    if (!audioDeviceInfo.isSink) return false

                    return audioDeviceInfo.type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP ||
                            audioDeviceInfo.type == AudioDeviceInfo.TYPE_WIRED_HEADSET ||
                            audioDeviceInfo.type == AudioDeviceInfo.TYPE_WIRED_HEADPHONES ||
                            audioDeviceInfo.type == AudioDeviceInfo.TYPE_USB_HEADSET
                }

                override fun onAudioDevicesAdded(addedDevices: Array<AudioDeviceInfo>) {
                    if (!player.isPlaying && addedDevices.any(::canPlayMusic)) {
                        player.play()
                    }
                }

                override fun onAudioDevicesRemoved(removedDevices: Array<AudioDeviceInfo>) = Unit
            }

            audioManager?.registerAudioDeviceCallback(audioDeviceCallback, handler)

        } else {
            audioManager?.unregisterAudioDeviceCallback(audioDeviceCallback)
            audioDeviceCallback = null
        }
    }

    @UnstableApi
    private fun sendOpenEqualizerIntent() {
        sendBroadcast(
            Intent(AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION).apply {
                putExtra(AudioEffect.EXTRA_AUDIO_SESSION, player.audioSessionId)
                putExtra(AudioEffect.EXTRA_PACKAGE_NAME, packageName)
                putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC)
            }
        )
    }


    @UnstableApi
    private fun sendCloseEqualizerIntent() {
        sendBroadcast(
            Intent(AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION).apply {
                putExtra(AudioEffect.EXTRA_AUDIO_SESSION, player.audioSessionId)
                putExtra(AudioEffect.EXTRA_PACKAGE_NAME, packageName)
            }
        )
    }

    @ExperimentalCoroutinesApi
    @FlowPreview
    private fun updatePlaybackState() = coroutineScope.launch {
        playbackStateMutex.withLock {
            withContext(Dispatchers.Main) {
                shuffleModeEnabled = player.shuffleModeEnabled

                if (showLikeButton && showDownloadButton)
                    mediaSession.setPlaybackState(
                        stateBuilder
                            .setState(player.androidPlaybackState, player.currentPosition, 1f)
                            .setBufferedPosition(player.bufferedPosition)
                            .build()
                    )
                if (showLikeButton && !showDownloadButton)
                    mediaSession.setPlaybackState(
                        stateBuilderWithLikeOnly
                            .setState(player.androidPlaybackState, player.currentPosition, 1f)
                            .setBufferedPosition(player.bufferedPosition)
                            .build()
                    )
                if (showDownloadButton && !showLikeButton)
                    mediaSession.setPlaybackState(
                        stateBuilderWithDownloadOnly
                            .setState(player.androidPlaybackState, player.currentPosition, 1f)
                            .setBufferedPosition(player.bufferedPosition)
                            .build()
                    )
                if (!showDownloadButton && !showLikeButton)
                    mediaSession.setPlaybackState(
                        stateBuilderWithoutCustomAction
                            .setState(player.androidPlaybackState, player.currentPosition, 1f)
                            .setBufferedPosition(player.bufferedPosition)
                            .build()
                    )
            }
        }


    }

    private val Player.androidPlaybackState
        get() = when (playbackState) {
            Player.STATE_BUFFERING -> if (playWhenReady) PlaybackStateCompat.STATE_BUFFERING else PlaybackStateCompat.STATE_PAUSED
            Player.STATE_READY -> if (playWhenReady) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED
            Player.STATE_ENDED -> PlaybackStateCompat.STATE_STOPPED
            Player.STATE_IDLE -> PlaybackStateCompat.STATE_NONE
            else -> PlaybackStateCompat.STATE_NONE
        }

    // legacy behavior may cause inconsistencies, but not available on sdk 24 or lower
    @ExperimentalCoroutinesApi
    @FlowPreview
    @Suppress("DEPRECATION")
    override fun onEvents(player: Player, events: Player.Events) {
        if (player.duration != C.TIME_UNSET) mediaSession.setMetadata(
            metadataBuilder
                .putText(
                    MediaMetadataCompat.METADATA_KEY_TITLE,
                    cleanPrefix(player.mediaMetadata.title.toString())
                )
                .putText(MediaMetadataCompat.METADATA_KEY_ARTIST, player.mediaMetadata.artist)
                .putText(MediaMetadataCompat.METADATA_KEY_ALBUM, player.mediaMetadata.albumTitle)
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, player.duration)
                .build()
        )


        if (events.containsAny(
                Player.EVENT_PLAYBACK_STATE_CHANGED,
                Player.EVENT_PLAY_WHEN_READY_CHANGED,
                Player.EVENT_IS_PLAYING_CHANGED,
                Player.EVENT_POSITION_DISCONTINUITY,
                Player.EVENT_IS_LOADING_CHANGED,
                Player.EVENT_MEDIA_METADATA_CHANGED
                //Player.EVENT_PLAYBACK_SUPPRESSION_REASON_CHANGED
            )
        ) {
            val notification = notification()

            if (notification == null) {
                isNotificationStarted = false
                makeInvincible(false)
                kotlin.runCatching {
                    stopForeground(false)
                }.onFailure {
                    Timber.e("Failed stopForeground in PlayerService onEvents ${it.stackTraceToString()}")
                }
                sendCloseEqualizerIntent()
                notificationManager?.cancel(NotificationId)
                return
            }

            if (player.shouldBePlaying && !isNotificationStarted) {
                isNotificationStarted = true
                kotlin.runCatching {
                    startForegroundService(this@PlayerService, intent<PlayerService>())
                }.onFailure {
                    Timber.e("Failed startForegroundService in PlayerService onEvents ${it.stackTraceToString()}")
                }
                kotlin.runCatching {
                    //startForeground(NotificationId, notification)
                    notification()?.let {
                        ServiceCompat.startForeground(
                            this@PlayerService,
                            NotificationId,
                            it,
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                            } else {
                                0
                            }
                        )
                    }
                }.onFailure {
                    Timber.e("Failed startForeground in PlayerService onEvents ${it.stackTraceToString()}")
                }
                makeInvincible(false)
                sendOpenEqualizerIntent()
            } else {
                if (!player.shouldBePlaying) {
                    isNotificationStarted = false
                    kotlin.runCatching {
                        stopForeground(false)
                    }.onFailure {
                        Timber.e("Failed stopForeground 1 in PlayerService onEvents ${it.stackTraceToString()}")
                    }
                    makeInvincible(true)
                    sendCloseEqualizerIntent()
                }
                runCatching {
                    notificationManager?.notify(NotificationId, notification)
                }.onFailure {
                    Timber.e("Failed onEvents in PlayerService notificationManager.notify ${it.stackTraceToString()}")
                }
            }
        }
        //updateNotification()
        updatePlaybackState()
    }

    override fun onPlayerError(error: PlaybackException) {
        super.onPlayerError(error)
        Timber.e("PlayerService onPlayerError ${error.stackTraceToString()}")
        //this.stopService(this.intent<MyDownloadService>())
        //this.stopService(this.intent<PlayerService>())
        //Log.d("mediaItem","onPlayerError ${error.errorCodeName}")
    }

    override fun onPlayerErrorChanged(error: PlaybackException?) {
        super.onPlayerErrorChanged(error)
        Timber.e("PlayerService onPlayerErrorChanged ${error?.stackTraceToString()}")
        //this.stopService(this.intent<MyDownloadService>())
        //this.stopService(this.intent<PlayerService>())
        //Log.d("mediaItem","onPlayerErrorChanged ${error?.errorCodeName}")
    }

    override fun onPlaybackSuppressionReasonChanged(playbackSuppressionReason: Int) {
        super.onPlaybackSuppressionReasonChanged(playbackSuppressionReason)
        //Timber.e("PlayerService onPlaybackSuppressionReasonChanged $playbackSuppressionReason")
        //Log.d("mediaItem","onPlaybackSuppressionReasonChanged $playbackSuppressionReason")
    }

    @UnstableApi
    override fun onIsPlayingChanged(isPlaying: Boolean) {
        val fadeDisabled = preferences.getEnum(playbackFadeAudioDurationKey, DurationInMilliseconds.Disabled) == DurationInMilliseconds.Disabled
        val duration = preferences.getEnum(playbackFadeAudioDurationKey, DurationInMilliseconds.Disabled).milliSeconds
        if (isPlaying && !fadeDisabled)
            startFadeAnimator(
                player = binder.player,
                duration = duration,
                fadeIn = true
            )

        //val totalPlayTimeMs = player.totalBufferedDuration.toString()
        //Log.d("mediaEvent","isPlaying "+isPlaying.toString() + " buffered duration "+totalPlayTimeMs)
        //Log.d("mediaItem","onIsPlayingChanged isPlaying $isPlaying audioSession ${player.audioSessionId}")

        super.onIsPlayingChanged(isPlaying)
    }


    @UnstableApi
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            persistentQueueKey -> if (sharedPreferences != null) {
                isPersistentQueueEnabled =
                    sharedPreferences.getBoolean(key, isPersistentQueueEnabled)
            }

            volumeNormalizationKey -> maybeNormalizeVolume()

            resumePlaybackWhenDeviceConnectedKey -> maybeResumePlaybackWhenDeviceConnected()

            isInvincibilityEnabledKey -> if (sharedPreferences != null) {
                isInvincibilityEnabled =
                    sharedPreferences.getBoolean(key, isInvincibilityEnabled)
            }

            skipSilenceKey -> if (sharedPreferences != null) {
                player.skipSilenceEnabled = sharedPreferences.getBoolean(key, false)
            }

            isShowingThumbnailInLockscreenKey -> {
                if (sharedPreferences != null) {
                    isShowingThumbnailInLockscreen = sharedPreferences.getBoolean(key, true)
                }
                maybeShowSongCoverInLockScreen()
            }

            trackLoopEnabledKey, queueLoopEnabledKey -> {
                player.repeatMode = when {
                    preferences.getBoolean(trackLoopEnabledKey, false) -> Player.REPEAT_MODE_ONE
                    preferences.getBoolean(queueLoopEnabledKey, true) -> Player.REPEAT_MODE_ALL
                    else -> Player.REPEAT_MODE_OFF
                }
            }
        }
    }

    @ExperimentalCoroutinesApi
    @FlowPreview
    override fun notification(): Notification? {
        if (player.currentMediaItem == null) return null

        val playIntent = Action.play.pendingIntent
        val pauseIntent = Action.pause.pendingIntent
        val nextIntent = Action.next.pendingIntent
        val prevIntent = Action.previous.pendingIntent
        val likeIntent = Action.like.pendingIntent
        val downloadIntent = Action.download.pendingIntent
        val playradioIntent = Action.playradio.pendingIntent
        val shuffleIntent = Action.shuffle.pendingIntent

        val mediaMetadata = player.mediaMetadata

        shuffleModeEnabled = player.shuffleModeEnabled

        val builder = if (isAtLeastAndroid8) {
            NotificationCompat.Builder(applicationContext, NotificationChannelId)
        } else {
            NotificationCompat.Builder(applicationContext)
        }
            .setContentTitle(cleanPrefix(player.mediaMetadata.title.toString()))
            .setContentText(
                if (mediaMetadata.albumTitle != null || mediaMetadata.artist != "")
                    "${mediaMetadata.artist} | ${mediaMetadata.albumTitle}"
                else mediaMetadata.artist
            )
            .setSubText(
                if (mediaMetadata.albumTitle != null || mediaMetadata.artist != "")
                    "${mediaMetadata.artist} | ${mediaMetadata.albumTitle}"
                else mediaMetadata.artist
            )
            //.setSubText(player.playerError?.message)
            .setLargeIcon(bitmapProvider.bitmap)
            .setAutoCancel(false)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .setSmallIcon(player.playerError?.let { R.drawable.alert_circle }
                ?: R.drawable.app_icon)
            .setOngoing(false)
            .setContentIntent(activityPendingIntent<MainActivity>(
                flags = PendingIntent.FLAG_UPDATE_CURRENT
            ) {
                putExtra("expandPlayerBottomSheet", true)
            })
            .setDeleteIntent(broadCastPendingIntent<NotificationDismissReceiver>())
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setShowActionsInCompactView(0, 1, 2)
                    .setMediaSession(mediaSession.sessionToken)
            )
            .addAction(R.drawable.play_skip_back, "Skip back", prevIntent)
            .addAction(
                if (player.shouldBePlaying) R.drawable.pause else R.drawable.play,
                if (player.shouldBePlaying) "Pause" else "Play",
                if (player.shouldBePlaying) pauseIntent else playIntent
            )
            .addAction(R.drawable.play_skip_forward, "Skip forward", nextIntent)

        if (showLikeButton && showDownloadButton) {
            //Prior Android 11
            builder
                .addAction(
                    if (shuffleModeEnabled) R.drawable.shuffle_filled else R.drawable.shuffle,
                    "Shuffle", shuffleIntent
                )
                .addAction(
                    if (isLikedState.value) R.drawable.heart else R.drawable.heart_outline,
                    //if (currentMediaLiked.value) R.drawable.heart else R.drawable.heart_outline,
                    "Like",
                    likeIntent
                )
                .addAction(
                    R.drawable.radio,
                    "Play radio",
                    playradioIntent
                )
                .addAction(
                    if (isDownloadedState.value || isCachedState.value) R.drawable.downloaded else R.drawable.download,
                    //if (currentMediaDownloaded.value) R.drawable.downloaded_to else R.drawable.download_to,
                    "Download", downloadIntent
                )
        }
        //Prior Android 11
        if (showLikeButton && !showDownloadButton) {
            builder
                .addAction(
                    if (isLikedState.value) R.drawable.heart else R.drawable.heart_outline,
                    "Like",
                    likeIntent
                )
                .addAction(
                    R.drawable.radio,
                    "Play radio",
                    playradioIntent
                )
        }
        //Prior Android 11
        if (!showLikeButton && showDownloadButton) {
            builder
                .addAction(
                    if (isDownloadedState.value || isCachedState.value) R.drawable.downloaded else R.drawable.download,
                    "Download", downloadIntent
                )
                .addAction(
                    R.drawable.radio,
                    "Play radio",
                    playradioIntent
                )
        }


        runCatching {
            bitmapProvider.load(mediaMetadata.artworkUri) { bitmap ->
                maybeShowSongCoverInLockScreen()
                handler.post {
                    runCatching {
                        notificationManager?.notify(
                            NotificationId,
                            builder.setLargeIcon(bitmap).build()
                        )
                    }.onFailure {
                        Timber.e("Failed notification() bitmapProvider.invoke in PlayerService ${it.stackTraceToString()}")
                    }
                }
            }
        }.onFailure {
            Timber.e("Failed notification() load in bitmapProvider in PlayerService ${it.stackTraceToString()}")
        }


        return builder.build()
    }


    private fun createNotificationChannel() {
        notificationManager = getSystemService()

        if (!isAtLeastAndroid8) return

        notificationManager?.run {
            if (getNotificationChannel(NotificationChannelId) == null) {
                createNotificationChannel(
                    NotificationChannel(
                        NotificationChannelId,
                        "Now playing",
                        NotificationManager.IMPORTANCE_LOW
                    ).apply {
                        setSound(null, null)
                        enableLights(false)
                        enableVibration(false)
                    }
                )
            }

            if (getNotificationChannel(SleepTimerNotificationChannelId) == null) {
                createNotificationChannel(
                    NotificationChannel(
                        SleepTimerNotificationChannelId,
                        "Sleep timer",
                        NotificationManager.IMPORTANCE_LOW
                    ).apply {
                        setSound(null, null)
                        enableLights(false)
                        enableVibration(false)
                    }
                )
            }
        }
    }

    private fun okHttpClient(): OkHttpClient {
        ProxyPreferences.preference?.let {
            return OkHttpClient.Builder()
                .proxy(
                    Proxy(
                        it.proxyMode,
                        InetSocketAddress(it.proxyHost, it.proxyPort)
                    )
                )
                .connectTimeout(Duration.ofSeconds(16))
                .readTimeout(Duration.ofSeconds(8))
                .build()
        }
        return OkHttpClient.Builder()
            .connectTimeout(Duration.ofSeconds(16))
            .readTimeout(Duration.ofSeconds(8))
            .build()
    }


    /** NEW METHOD **/
    private fun createMediaSourceFactory() = DefaultMediaSourceFactory(
        createDataSourceResolverFactory(
            mediaItemToPlay = { videoId ->
                runBlocking(Dispatchers.Main) {
                    player.findNextMediaItemById(videoId)
                }
            }
        ),
        DefaultExtractorsFactory()
        //getExtractorsFactory()
    )

    private fun getExtractorsFactory(): ExtractorsFactory = ExtractorsFactory {
        arrayOf(
            MatroskaExtractor(
                DefaultSubtitleParserFactory()
            ),
            FragmentedMp4Extractor(
                DefaultSubtitleParserFactory()
            ),
            androidx.media3.extractor.mp4.Mp4Extractor(
                DefaultSubtitleParserFactory()
            ),
        )
    }

    private fun createRendersFactory(): RenderersFactory {
        val minimumSilenceDuration = preferences.getLong(
            minimumSilenceDurationKey, 2_000_000L) //PlayerPreferences.minimumSilence.coerceIn(1000L..2_000_000L)
        val audioSink = DefaultAudioSink.Builder(applicationContext)
            .setEnableFloatOutput(false)
            .setEnableAudioTrackPlaybackParams(false)
            .setAudioOffloadSupportProvider(DefaultAudioOffloadSupportProvider(applicationContext))
            .setAudioProcessorChain(
                DefaultAudioProcessorChain(
                    emptyArray(),
                    SilenceSkippingAudioProcessor(
                        /* minimumSilenceDurationUs = */ minimumSilenceDuration,
                        /* silenceRetentionRatio = */ 0.01f,
                        /* maxSilenceToKeepDurationUs = */ minimumSilenceDuration,
                        /* minVolumeToKeepPercentageWhenMuting = */ 0,
                        /* silenceThresholdLevel = */ 256
                    ),
                    /*
                    SilenceSkippingAudioProcessor(
                        2_000_000,
                        20_000,
                        256
                    ),
                     */
                    SonicAudioProcessor()
                )
            )
            .build()
            .apply {
                if (isAtLeastAndroid10) setOffloadMode(AudioSink.OFFLOAD_MODE_DISABLED)
            }

        return RenderersFactory { handler: Handler?, _, audioListener: AudioRendererEventListener?, _, _ ->
            arrayOf(
                MediaCodecAudioRenderer(
                    this,
                    MediaCodecSelector.DEFAULT,
                    handler,
                    audioListener,
                    audioSink
                )
            )
        }
    }


    private fun createDataSourceResolverFactory(
        mediaItemToPlay: (videoId: String) -> MediaItem?
    ): ResolvingDataSource.Factory {
        val ringBuffer = RingBuffer<Pair<String, Uri>?>(2) { null }
        val chunkLength = 512 * 1024L

        return ResolvingDataSource.Factory(
            CacheDataSource.Factory()
                .setCache(downloadCache)
                .setUpstreamDataSourceFactory(
                    CacheDataSource.Factory()
                        .setCache(cache)
                        .setUpstreamDataSourceFactory(
                            DefaultDataSource.Factory(
                                this,
                                OkHttpDataSource.Factory(okHttpClient())
                                    .setUserAgent("Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Mobile Safari/537.36")
                            )
                        )
                )
                .setCacheWriteDataSinkFactory(null)
                .setFlags(FLAG_IGNORE_CACHE_ON_ERROR)
            /*
            ConditionalCacheDataSourceFactory(
                cacheDataSourceFactory = CacheDataSource.Factory().setCache(downloadCache)
                    .setCacheWriteDataSinkFactory(null),
                upstreamDataSourceFactory = CacheDataSource.Factory()
                    .setCache(cache)
                    .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
                    .setUpstreamDataSourceFactory(
                        DefaultDataSource.Factory(
                            this,
                            OkHttpDataSource.Factory(okHttpClient())
                                .setUserAgent("Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Mobile Safari/537.36")
                            //.setUserAgent("Mozilla/5.0 (Windows NT 10.0; rv:91.0) Gecko/20100101 Firefox/91.0")
                        )
                    )
            ) { !it.isLocal }
            */
        ) { dataSpec ->
            Timber.i( "PlayerService createDataSourceResolverFactory dataSpec " + dataSpec.toString())
            val videoId = dataSpec.key?.removePrefix("https://youtube.com/watch?v=")
                ?: error("No media videoId in dataSpec")

            Timber.i(
                "PlayerService createDataSourceResolverFactory dataSpec isLocal ${dataSpec.isLocal} videoId $videoId all $dataSpec"
            )
            Timber.i(
                "PlayerService createDataSourceResolverFactory dataSpec isInCache ${cache.isCached(videoId, dataSpec.position, chunkLength)}"
            )
            Timber.i(
                "PlayerService createDataSourceResolverFactory dataSpec isInDownloadCache ${downloadCache.isCached(videoId, dataSpec.position, chunkLength)}"
            )
            Timber.i(
                "PlayerService createDataSourceResolverFactory dataSpec videoId == ringBuffer0 ${videoId == ringBuffer.getOrNull(0)?.first}"
            )
            Timber.i(
                "PlayerService createDataSourceResolverFactory dataSpec videoId == ringBuffer1 ${videoId == ringBuffer.getOrNull(1)?.first}"
            )

            when {
                dataSpec.isLocal ||
                        //cache.isCached(videoId, dataSpec.position, chunkLength ?: (512 * 1024L)) ||
                        //downloadCache.isCached(videoId, dataSpec.position, chunkLength ?: (512 * 1024L))
                        cache.isCached(videoId, dataSpec.position, chunkLength) ||
                        downloadCache.isCached(videoId, dataSpec.position, if (dataSpec.length >= 0) dataSpec.length else 1)
                -> dataSpec

                videoId == ringBuffer.getOrNull(0)?.first ->
                    dataSpec.withUri(ringBuffer.getOrNull(0)!!.second)

                videoId == ringBuffer.getOrNull(1)?.first ->
                    dataSpec.withUri(ringBuffer.getOrNull(1)!!.second)

                else -> {
                    Timber.i("PlayerService createDataSourceResolverFactory videoId $videoId")
                    /*
                    val body = runBlocking(Dispatchers.IO) {
                        Innertube.player(PlayerBody(videoId = videoId))
                    }?.getOrThrow()
                     */


                    val body = runBlocking(Dispatchers.IO) {
                        Innertube.player(PlayerBody(videoId = videoId))
                    }?.getOrElse { throwable ->
                        when (throwable) {
                            is ConnectException, is UnknownHostException -> throw NoInternetException()
                            is SocketTimeoutException -> throw TimeoutException()
                            else -> throw UnknownException()
                        }

                    }

                    if (body?.videoDetails?.videoId != videoId) throw VideoIdMismatchException()

                    Timber.i( "PlayerService createDataSourceResolverFactory bodyVideoId ${body.videoDetails?.videoId} videoId $videoId")

                    Timber.i("PlayerService createDataSourceResolverFactory adaptiveFormats available ${body.streamingData?.adaptiveFormats}")

                    //println("mediaItem adaptive ${body.streamingData?.adaptiveFormats}")
                    //val format = body.streamingData?.highestQualityFormat

/*
                    val format = when (audioQualityFormat) {
                        AudioQualityFormat.Auto -> body?.streamingData?.autoMaxQualityFormat
                        AudioQualityFormat.High -> body?.streamingData?.highestQualityFormat
                        AudioQualityFormat.Medium -> body?.streamingData?.mediumQualityFormat
                        AudioQualityFormat.Low -> body?.streamingData?.lowestQualityFormat
                    }
*/

                    val format = body.streamingData?.adaptiveFormats
                        ?.filter {
                            when (audioQualityFormat) {
                                AudioQualityFormat.Auto -> it.itag == 251 || it.itag == 141 ||
                                            it.itag == 250 || it.itag == 140 ||
                                            it.itag == 249 || it.itag == 139 ||
                                            it.itag == 171
                                AudioQualityFormat.High -> it.itag == 251 || it.itag == 141
                                AudioQualityFormat.Medium -> it.itag == 250 || it.itag == 140 || it.itag == 171
                                AudioQualityFormat.Low -> it.itag == 249 || it.itag == 139
                            }

                        }
                        ?.sortedByDescending { it.bitrate }
                        ?.firstOrNull()
                        /*
                        ?.maxByOrNull {
                            (it.bitrate?.times(
                                when (audioQualityFormat) {
                                    AudioQualityFormat.Auto -> if (connectivityManager.isActiveNetworkMetered) -1 else 1
                                    AudioQualityFormat.High -> 1
                                    AudioQualityFormat.Medium -> -1
                                    AudioQualityFormat.Low -> -1
                                }
                            ) ?: -1) + (if (it.mimeType.startsWith("audio/webm")) 10240 else 0)
                        }
                         */
                    /*
                    val format =
                        body?.streamingData?.adaptiveFormats
                            //?.filter { it.isAudio }
                            ?.maxByOrNull {
                            (it.bitrate?.times(
                                when (audioQualityFormat) {
                                    AudioQualityFormat.Auto -> if (connectivityManager.isActiveNetworkMetered) -1 else 1
                                    AudioQualityFormat.High -> 1
                                    AudioQualityFormat.Medium  -> -1
                                    AudioQualityFormat.Low  -> -1
                                }
                            ) ?: -1) + (if (it.mimeType.startsWith("audio/webm")) 10240 else 0) // prefer opus stream
                        }

                     */



                    Timber.i("PlayerService createDataSourceResolverFactory audioQualityFormat selected $format")
                    println("mediaItem PlayerService createDataSourceResolverFactory audioQualityFormat selected $format")


                    if (format == null) throw PlayableFormatNotFoundException()


                    val url = when (val status = body.playabilityStatus?.status) {
                        "OK" -> format.let { formatIn ->
                            val mediaItem = mediaItemToPlay(videoId)
                            if (mediaItem?.mediaMetadata?.extras?.getString("durationText") == null)
                                formatIn.approxDurationMs?.div(1000)
                                    ?.let(DateUtils::formatElapsedTime)?.removePrefix("0")
                                    ?.let { durationText ->
                                        mediaItem?.mediaMetadata?.extras?.putString(
                                            "durationText",
                                            durationText
                                        )
                                        Database.updateDurationText(videoId, durationText)
                                    }

                            query {
                                mediaItem?.let(Database::insert)

                                Database.insert(
                                    Format(
                                        songId = videoId,
                                        itag = formatIn.itag,
                                        mimeType = formatIn.mimeType,
                                        bitrate = formatIn.bitrate,
                                        loudnessDb = body.playerConfig?.audioConfig?.normalizedLoudnessDb,
                                        contentLength = formatIn.contentLength,
                                        lastModified = formatIn.lastModified
                                    )
                                )
                            }

                            formatIn.url
                        } ?: throw PlayableFormatNotFoundException()

                        "UNPLAYABLE" -> throw UnplayableException()
                        "LOGIN_REQUIRED" -> throw LoginRequiredException()

                        else -> {
                            Timber.i("PlayerService createDataSourceResolverFactory status $status")
                            throw UnknownException()
                            //throw PlaybackException(status, null, PlaybackException.ERROR_CODE_REMOTE_ERROR)
                        }
                    }
                    ringBuffer.append(videoId to url.toUri())
                    //ringBuffer += videoId to url.toUri()
                    dataSpec.buildUpon()
                        .setKey(videoId)
                        .setUri(url.toUri())
                        .build()
                        .let { spec ->
                            (chunkLength ?: format.contentLength)?.let {
                                spec.subrange(dataSpec.uriPositionOffset, it)
                            } ?: spec
                        }
                }
            }
        }
    }

    /** NEW METHOD **/

    inner class Binder : AndroidBinder() {
        val player: ExoPlayer
            get() = this@PlayerService.player

        val cache: Cache
            get() = this@PlayerService.cache

        val downloadCache: Cache
            get() = this@PlayerService.downloadCache

        val mediaSession
            get() = this@PlayerService.mediaSession

        val sleepTimerMillisLeft: StateFlow<Long?>?
            get() = timerJob?.millisLeft

        private var radioJob: Job? = null

        var isLoadingRadio by mutableStateOf(false)
            private set

        //var mediaItems = mutableListOf<MediaItem>()

        fun setBitmapListener(listener: ((Bitmap?) -> Unit)?) {
            bitmapProvider.listener = listener
        }

        fun startSleepTimer(delayMillis: Long) {
            timerJob?.cancel()



            timerJob = coroutineScope.timer(delayMillis) {
                val notification = NotificationCompat
                    .Builder(this@PlayerService, SleepTimerNotificationChannelId)
                    .setContentTitle("Sleep timer ended")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true)
                    .setOnlyAlertOnce(true)
                    .setShowWhen(true)
                    .setSmallIcon(R.drawable.app_icon)
                    .build()

                notificationManager?.notify(SleepTimerNotificationId, notification)

                stopSelf()
                exitProcess(0)
            }
        }

        fun cancelSleepTimer() {
            timerJob?.cancel()
            timerJob = null
        }

        @UnstableApi
        fun setupRadio(endpoint: NavigationEndpoint.Endpoint.Watch?) =
            startRadio(endpoint = endpoint, justAdd = true)

        @UnstableApi
        fun playRadio(endpoint: NavigationEndpoint.Endpoint.Watch?) =
            startRadio(endpoint = endpoint, justAdd = false)

        @UnstableApi
        fun getRadioMediaItems(endpoint: NavigationEndpoint.Endpoint.Watch?): List<MediaItem> {
            YouTubeRadio(
                endpoint?.videoId,
                endpoint?.playlistId,
                endpoint?.playlistSetVideoId,
                endpoint?.params,
                false,
                applicationContext
            ).let {
                var mediaItems = listOf<MediaItem>()
                runBlocking {
                    mediaItems = it.process()
                    return@runBlocking mediaItems
                }
                return mediaItems
            }
        }

        @UnstableApi
        fun getRadioSongs(endpoint: NavigationEndpoint.Endpoint.Watch?): List<Song> {
            YouTubeRadio(
                endpoint?.videoId,
                endpoint?.playlistId,
                endpoint?.playlistSetVideoId,
                endpoint?.params,
                false,
                applicationContext
            ).let {
                var songs = listOf<Song>()
                runBlocking {
                    songs = it.process().map(MediaItem::asSong)
                    return@runBlocking songs
                }
                return songs
            }
        }

        @UnstableApi
        fun setRadioMediaItems(endpoint: NavigationEndpoint.Endpoint.Watch?) {
            radioJob?.cancel()
            radio = null
            YouTubeRadio(
                endpoint?.videoId,
                endpoint?.playlistId,
                endpoint?.playlistSetVideoId,
                endpoint?.params,
                false,
                applicationContext
            ).let {
                isLoadingRadio = true
                coroutineScope.launch(Dispatchers.Main) {
                    player.clearMediaItems()
                    player.addMediaItems(it.process())
                    //Log.d("mediaItem", "binder ${it.process()}")
                }
                radio = it
                isLoadingRadio = false
                //Log.d("mediaItem", "binder $mediaItems")
                //return mediaItems
                /*
                isLoadingRadio = true
                radioJob = coroutineScope.launch(Dispatchers.Main) {
                    if (justAdd) {
                        player.addMediaItems(it.process().drop(1))
                    } else {
                        player.forcePlayFromBeginning(it.process())
                    }
                    radio = it
                    isLoadingRadio = false
                }
                 */
            }
        }


        @UnstableApi
        private fun startRadio(endpoint: NavigationEndpoint.Endpoint.Watch?, justAdd: Boolean) {
            radioJob?.cancel()
            radio = null
            val isDiscoverEnabled =  applicationContext.preferences.getBoolean(discoverKey, false)
            YouTubeRadio(
                endpoint?.videoId,
                endpoint?.playlistId,
                endpoint?.playlistSetVideoId,
                endpoint?.params,
                isDiscoverEnabled,
                applicationContext
            ).let {
                isLoadingRadio = true
                radioJob = coroutineScope.launch(Dispatchers.Main) {

                    val songs = it.process()

                    songs.forEach {
                        transaction {
                            Database.insert(it)
                        }
                    }

                    if (justAdd) {
                        player.addMediaItems(songs.drop(1))
                    } else {
                        player.forcePlayFromBeginning(songs)
                    }
                    radio = it
                    isLoadingRadio = false
                }
            }
        }

        fun stopRadio() {
            isLoadingRadio = false
            radioJob?.cancel()
            radio = null
        }

        fun playFromSearch(query: String) {
            coroutineScope.launch {
                Innertube.searchPage(
                    body = SearchBody(
                        query = query,
                        params = Innertube.SearchFilter.Song.value
                    ),
                    fromMusicShelfRendererContent = Innertube.SongItem.Companion::from
                )?.getOrNull()?.items?.firstOrNull()?.info?.endpoint?.let { playRadio(it) }
            }
        }

        /**
         * This method should ONLY be called when the application (sc. activity) is in the foreground!
         */
        fun restartForegroundOrStop() {
            player.pause()
            isInvincibilityEnabled = false
            stopSelf()
        }

        @ExperimentalCoroutinesApi
        @FlowPreview
        fun toggleLike() = mediaItemState.value?.let { mediaItem ->
            //mediaItemToggleLike(mediaItem)
            transaction {
                Database.like(
                    mediaItem.mediaId,
                    if (isLikedState.value) null else System.currentTimeMillis()
                )
            }
            updatePlaybackState()
        }


        @ExperimentalCoroutinesApi
        @FlowPreview
        fun toggleDownload() = mediaItemState.value?.let { mediaItem ->
            val downloads = DownloadUtil.downloads.value
            manageDownload(
                context = this@PlayerService,
                songId = mediaItem.mediaId,
                songTitle = cleanPrefix(mediaItem.mediaMetadata.title.toString()),
                downloadState = downloads[mediaItem.mediaId]?.state == Download.STATE_COMPLETED
            )
            updatePlaybackState()
        }

        @kotlin.OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
        fun toggleShuffle() {
            player.shuffleModeEnabled = !player.shuffleModeEnabled
            updatePlaybackState()
        }

        fun refreshPlayer() {
            coroutineScope.launch {
                withContext(Dispatchers.Main) {
                    if (player.isPlaying) {
                        player.pause()
                        player.play()
                    } else {
                        player.play()
                        player.pause()
                    }
                }
            }
        }

        fun callPause(onPause: () -> Unit) {
            val fadeDisabled = preferences.getEnum(playbackFadeAudioDurationKey, DurationInMilliseconds.Disabled) == DurationInMilliseconds.Disabled
            val duration = preferences.getEnum(playbackFadeAudioDurationKey, DurationInMilliseconds.Disabled).milliSeconds
            //println("mediaItem callPause fadeDisabled $fadeDisabled duration $duration")
            if (player.isPlaying) {
                if (fadeDisabled) {
                    player.pause()
                    onPause()
                } else {
                    //fadeOut
                    startFadeAnimator(player, duration, false) {
                        player.pause()
                        onPause()
                    }
                }
            }
        }

        fun isCached(song: SongEntity) =
            song.contentLength?.let { cache.isCached(song.song.id, 0L, it) } ?: false

    }

    private inner class SessionCallback(private val player: Player) :
        MediaSessionCompat.Callback() {
        override fun onPlay() = player.play()
        //override fun onPause() = player.pause()
        override fun onPause() = binder.callPause({})
        override fun onSkipToPrevious() = runCatching(player::seekToPreviousMediaItem).let { }
        override fun onSkipToNext() = runCatching(player::seekToNextMediaItem).let { }
        override fun onSeekTo(pos: Long) = player.seekTo(pos)
        //override fun onStop() = player.pause()
        override fun onStop() = binder.callPause({} )
        override fun onRewind() = runCatching {player.seekToDefaultPosition()}.let { }
        override fun onSkipToQueueItem(id: Long) =
            runCatching { player.seekToDefaultPosition(id.toInt()) }.let { }


        @ExperimentalCoroutinesApi
        @FlowPreview
        override fun onCustomAction(action: String, extras: Bundle?) {
            super.onCustomAction(action, extras)
            //println("mediaItem $action")
            if (action == "LIKE") {
                binder.toggleLike()
                refreshPlayer()
            }
            if (action == "DOWNLOAD") {
                binder.toggleDownload()
                refreshPlayer()
            }
            if (action == "SHUFFLE") {
                binder.toggleShuffle()
                refreshPlayer()
            }
            if (action == "PLAYRADIO") {
                    binder.stopRadio()
                    binder.playRadio(NavigationEndpoint.Endpoint.Watch(videoId = binder.player.currentMediaItem?.mediaId))
            }
            updatePlaybackState()
        }

        override fun onPlayFromSearch(query: String?, extras: Bundle?) {
            if (query.isNullOrBlank()) return
            binder.playFromSearch(query)
        }


        override fun onMediaButtonEvent(mediaButtonEvent: Intent?): Boolean {
            mediaButtonEvent?.let {
                if (it.action == Intent.ACTION_MEDIA_BUTTON) {
                    if (it.extras?.getBoolean(Intent.EXTRA_KEY_EVENT) == true) {
                        val keyEvent = it.extras?.getParcelable<KeyEvent>(Intent.EXTRA_KEY_EVENT)
                        when(keyEvent?.keyCode) {
                            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> {
                                if (player.isPlaying)
                                    onPause()
                                else onPlay()

                                return true
                            }
                            KeyEvent.KEYCODE_MEDIA_NEXT -> {
                                onSkipToNext()
                                return true
                            }
                            KeyEvent.KEYCODE_MEDIA_PREVIOUS -> {
                                onSkipToPrevious()
                                return true
                            }
                            KeyEvent.KEYCODE_MEDIA_STOP -> {
                                onStop()
                                return true
                            }
                            KeyEvent.KEYCODE_MEDIA_PLAY -> {
                                onPlay()
                                return true
                            }
                            KeyEvent.KEYCODE_MEDIA_PAUSE -> {
                                onPause()
                                return true
                            }
                        }
                    }
                }
            }
            return super.onMediaButtonEvent(mediaButtonEvent)
        }


    }


    private fun refreshPlayer() {
        coroutineScope.launch {
            withContext(Dispatchers.Main) {
                if (player.isPlaying) {
                    player.pause()
                    player.play()
                } else {
                    player.play()
                    player.pause()
                }
            }
        }
    }

    inner class NotificationActionReceiver(private val player: Player) : BroadcastReceiver() {


        @ExperimentalCoroutinesApi
        @FlowPreview
        // Prior Android 11
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                //Action.pause.value -> player.pause()
                Action.pause.value -> binder.callPause({ player.pause() } )
                Action.play.value -> player.play()
                Action.next.value -> player.seekToNextMediaItem() //player.forceSeekToNext()
                Action.previous.value -> player.seekToPreviousMediaItem() //player.forceSeekToPrevious()
                Action.like.value -> {
                    binder.toggleLike()
                    refreshPlayer()
                }

                Action.download.value -> {
                    binder.toggleDownload()
                    refreshPlayer()
                }

                Action.playradio.value -> {
                        binder.stopRadio()
                        binder.playRadio(NavigationEndpoint.Endpoint.Watch(videoId = binder.player.currentMediaItem?.mediaId))
                }

                Action.shuffle.value -> {
                    binder.toggleShuffle()
                    refreshPlayer()
                }
            }

            updatePlaybackState()
        }

    }


    class NotificationDismissReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            kotlin.runCatching {
                context.stopService(context.intent<MyDownloadService>())
            }.onFailure {
                Timber.e("Failed NotificationDismissReceiver stopService in PlayerService (MyDownloadService) ${it.stackTraceToString()}")
            }
            kotlin.runCatching {
                context.stopService(context.intent<PlayerService>())
            }.onFailure {
                Timber.e("Failed NotificationDismissReceiver stopService in PlayerService (PlayerService) ${it.stackTraceToString()}")
            }
        }
    }


    @JvmInline
    private value class Action(val value: String) {
        context(Context)
        val pendingIntent: PendingIntent
            get() = PendingIntent.getBroadcast(
                this@Context,
                100,
                Intent(value).setPackage(packageName),
                PendingIntent.FLAG_UPDATE_CURRENT.or(if (isAtLeastAndroid6) PendingIntent.FLAG_IMMUTABLE else 0)
            )

        companion object {

            val pause = Action("com.peecock.ymusic.pause")
            val play = Action("com.peecock.ymusic.play")
            val next = Action("com.peecock.ymusic.next")
            val previous = Action("com.peecock.ymusic.previous")
            val like = Action("com.peecock.ymusic.like")
            val download = Action("com.peecock.ymusic.download")
            val playradio = Action("com.peecock.ymusic.playradio")
            val shuffle = Action("com.peecock.ymusic.shuffle")
        }
    }

    private companion object {
        const val NotificationId = 1001
        const val NotificationChannelId = "default_channel_id"

        const val SleepTimerNotificationId = 1002
        const val SleepTimerNotificationChannelId = "sleep_timer_channel_id"


    }


}


