package com.peecock.innertube.models.bodies

import com.peecock.innertube.models.Context
import com.peecock.innertube.models.WatchEndpoint
import kotlinx.serialization.Serializable


@Serializable
data class BrowseBodyWithLocale(
    val context: Context = Context.DefaultWebWithLocale,
    val browseId: String,
    val params: String? = null,
    val countryCode: String? = null,
    val formData: FormData? = null,
    val enablePersistentPlaylistPanel: Boolean? = null,
    val isAudioOnly: Boolean? = null,
    val tunerSettingValue: String? = null,
    val playlistId: String? = null,
    val watchEndpointMusicSupportedConfigs: WatchEndpoint.WatchEndpointMusicSupportedConfigs? = null
)
