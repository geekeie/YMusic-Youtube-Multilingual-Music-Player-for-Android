package com.peecock.ymusic.utils


import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

import androidx.compose.ui.platform.LocalContext

import androidx.core.net.toUri
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.offline.DownloadService

import com.peecock.ymusic.Database
import com.peecock.ymusic.LocalDownloader
import com.peecock.ymusic.LocalPlayerServiceBinder
import com.peecock.ymusic.models.Format
import com.peecock.ymusic.service.DownloadUtil
import com.peecock.ymusic.service.MyDownloadService
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged

@UnstableApi
@Composable
fun InitDownloader () {
    val context = LocalContext.current
    DownloadUtil.getDownloadManager(context)
    DownloadUtil.getDownloads()

/*
    //val idVideo="initVideoId"
    val idVideo="8n4S1-ctsZw"
    //val contentUri = "https://$idVideo".toUri()
    val contentUri = "https://www.youtube.com/watch?v=8n4S1-ctsZw".toUri()

    //val idVideo="8n4S1-ctsZw"
    //val contentUri = "https://www.youtube.com/watch?v=$idVideo".toUri()

    val downloadRequest = DownloadRequest
        .Builder(
            idVideo,
            contentUri
        )
        .setCustomCacheKey(idVideo)
        .setData(idVideo.toByteArray())
        .build()


    runCatching {
            DownloadService.sendAddDownload(
                context,
                MyDownloadService::class.java,
                downloadRequest,
                false
            )
        }.onFailure {
        Log.d("downloadInit","Downloader initialized $it" )
    }
*/
}


@UnstableApi
@Composable
fun downloadedStateMedia ( mediaId: String ): Boolean {
    val context = LocalContext.current
   // if (!checkInternetConnection()) return false
    /*
    if (!isNetworkAvailableComposable()) {
        //context.toast("No connection")
        return false
    }
     */

    val binder = LocalPlayerServiceBinder.current
    //val downloader = LocalDownloader.current


    val downloadCache: SimpleCache
    try {
        downloadCache = DownloadUtil.getDownloadSimpleCache(context) as SimpleCache
    } catch (e: Exception) {
        //context.toast(e.toString())
        return false
    }


    val cachedBytes by remember(mediaId) {
        mutableStateOf(binder?.cache?.getCachedBytes(mediaId, 0, -1))
    }

    var format by remember {
        mutableStateOf<Format?>(null)
    }

    var isDownloaded by remember {
        mutableStateOf(false)
    }

    //val download by downloader.getDownload(mediaId).collectAsState(initial = null)


    LaunchedEffect(mediaId) {
        Database.format(mediaId).distinctUntilChanged().collectLatest { currentFormat ->
            format = currentFormat
        }
    }

    val download = format?.contentLength?.let {
        try {
            downloadCache.isCached(mediaId, 0, it)
        } catch (e: Exception) {
            //context.toast(e.toString())
            false
        }
    }

    //isDownloaded = (format?.contentLength == cachedBytes) || (download?.state == Download.STATE_COMPLETED)
    isDownloaded = (format?.contentLength == cachedBytes) || download == true

//    Log.d("mediaItem", "cachedBytes ${cachedBytes} contentLength ${format?.contentLength} downloadState ${isDownloaded}")

    return isDownloaded

}


@UnstableApi
fun manageDownload (
    context: android.content.Context,
    songId: String,
    songTitle: String,
    downloadState: Boolean = false
) {
//Log.d("mediaItem","managedownload checkinternet ${isNetworkAvailable(context)}")

    //if (isNetworkAvailable(context)) {
        //try {
            if (downloadState)
                DownloadService.sendRemoveDownload(
                    context,
                    MyDownloadService::class.java,
                    songId,
                    false
                )
            else {
                if (isNetworkAvailable(context)) {
                    val contentUri =
                        "https://www.youtube.com/watch?v=${songId}".toUri()
                    val downloadRequest = DownloadRequest
                        .Builder(
                            songId,
                            contentUri
                        )
                        .setCustomCacheKey(songId)
                        .setData(cleanPrefix(songTitle).toByteArray())
                        .build()

                    DownloadService.sendAddDownload(
                        context,
                        MyDownloadService::class.java,
                        downloadRequest,
                        false
                    )
                }
            }
        //} catch (e: Exception) {
        //    e.printStackTrace()
        //}
    //}
}



@UnstableApi
@Composable
fun getDownloadState(mediaId: String): Int {
    val downloader = LocalDownloader.current
    //if (!checkInternetConnection()) return 3
    if (!isNetworkAvailableComposable()) return 3

    return downloader.getDownload(mediaId).collectAsState(initial = null).value?.state
        ?: 3
}

