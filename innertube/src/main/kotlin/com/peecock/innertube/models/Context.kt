package com.peecock.innertube.models

import io.ktor.http.headers
import com.peecock.innertube.utils.LocalePreferences
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Serializable
data class Context(
    val client: Client,
    val thirdParty: ThirdParty? = null,
) {
    @Serializable
    data class Client(
        val clientName: String,
        val clientVersion: String,
        //val platform: String,
        val hl: String? = "en",
        //val gl: String? = "US",
        //val hl: String = Locale.getDefault().toLanguageTag(), //"en",
        //val hl: String = Innertube.localeHl,
        val visitorData: String = "CgtEUlRINDFjdm1YayjX1pSaBg%3D%3D",
        val androidSdkVersion: Int? = null,
        val userAgent: String? = null,
        val referer: String? = null,
        val api_key: String,
    )

    @Serializable
    data class ThirdParty(
        val embedUrl: String,
    )


    fun apply() {
        client.userAgent

        headers {
            client.referer?.let { append("Referer", it) }
            append("X-Youtube-Bootstrap-Logged-In", "false")
            append("X-YouTube-Client-Name", client.clientName)
            append("X-YouTube-Client-Version", client.clientVersion)
        }
    }

    companion object {

        private const val USER_AGENT_WEB = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/115.0.0.0 Safari/537.36"
        private const val USER_AGENT_ANDROID = "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Mobile Safari/537.36"

        private const val REFERER_YOUTUBE_MUSIC = "https://music.youtube.com/"
        private const val REFERER_YOUTUBE = "https://www.youtube.com/"

        /*
        val DefaultWeb = Context(
            client = Client(
                clientName = "WEB_REMIX",
                clientVersion = "1.20220918",
                platform = "DESKTOP",
            )
        )


        val DefaultAndroid = Context(
            client = Client(
                clientName = "ANDROID_MUSIC",
                clientVersion = "5.28.1",
                platform = "MOBILE",
                androidSdkVersion = 30,
                userAgent = "com.google.android.apps.youtube.music/5.28.1 (Linux; U; Android 11) gzip"
            )
        )
        */

        val DefaultWeb = Context(
            client = Client(
                clientName = "WEB_REMIX",
                //clientVersion = "1.20220606.03.00",
                clientVersion = "1.20230731.00.00",
                //platform = "DESKTOP",
                //userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.157 Safari/537.36",
                userAgent = USER_AGENT_WEB,
                referer = REFERER_YOUTUBE_MUSIC,
                //visitorData = "AIzaSyC9XL3ZjWddXya6X74dJoCTL-WEYFDNX30",
                api_key = "AIzaSyC9XL3ZjWddXya6X74dJoCTL-WEYFDNX30",

                //Locale is managed with DefaultWebWithLocale
                //hl = LocalePreferences.preference?.hl,
                //gl = LocalePreferences.preference?.gl

            )
        )


        //val hl = if (LocalePreferences.preference?.useLocale == true) LocalePreferences.preference!!.hl else ""
        val hl = LocalePreferences.preference?.hl
        //val gl = LocalePreferences.preference?.gl


        val DefaultWebWithLocale = Context(
            client = Client(
                clientName = "WEB_REMIX",
                //clientVersion = "1.20220606.03.00",
                clientVersion = "1.20230731.00.00",
                //platform = "DESKTOP",
                //userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.157 Safari/537.36",
                userAgent = USER_AGENT_WEB,
                referer = REFERER_YOUTUBE_MUSIC,
                //visitorData = "AIzaSyC9XL3ZjWddXya6X74dJoCTL-WEYFDNX30",
                //hl = Locale.getDefault().toLanguageTag()
                hl = hl,
                //gl = gl,
                api_key = "AIzaSyC9XL3ZjWddXya6X74dJoCTL-WEYFDNX30"
            )
        )


        val DefaultAndroid = Context(
            client = Client(
                clientName = "ANDROID_MUSIC",
                //clientVersion = "5.01",
                clientVersion = "6.33.52",
                //platform = "MOBILE",
                //visitorData = "AIzaSyAOghZGza2MQSZkY_zfZ370N-PUdXEo8AI",
                //androidSdkVersion = 30,
                //userAgent = "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Mobile Safari/537.36"
                userAgent = USER_AGENT_ANDROID,
                api_key = "AIzaSyAOghZGza2MQSZkY_zfZ370N-PUdXEo8AI"
            )
        )

        val DefaultSimpleAndroid = Context(
            client = Client(
                clientName = "ANDROID",
                clientVersion = "17.13.3",
                api_key = "AIzaSyA8eiZmM1FaDVjRy-df2KTyQ_vz_yYM39w",
                userAgent = USER_AGENT_ANDROID
            )
        )

        val Web = Context(
            client = Client(
                clientName = "WEB",
                clientVersion = "2.2021111",
                api_key = "AIzaSyC9XL3ZjWddXya6X74dJoCTL-WEYFDNX3",
                userAgent = USER_AGENT_WEB
            )
        )

        val TVHTML5 = Context(
            client = Client(
                clientName = "TVHTML5_SIMPLY_EMBEDDED_PLAYER",
                clientVersion = "2.0",
                api_key = "AIzaSyDCU8hByM-4DrUqRUYnGn-3llEO78bcxq8",
                userAgent = "Mozilla/5.0 (PlayStation 4 5.55) AppleWebKit/601.2 (KHTML, like Gecko)"
            )
        )

        val SimpleClient = Context(
            client = Client(
                clientName = "67",
                clientVersion = "1.${
                    SimpleDateFormat("yyyyMMdd", Locale.ENGLISH).apply {
                        timeZone = TimeZone.getTimeZone("UTC")
                    }.format(
                        Date()
                    )
                }.00.00",
                api_key = "AIzaSyC9XL3ZjWddXya6X74dJoCTL-WEYFDNX30",
                userAgent = USER_AGENT_WEB,
                referer = REFERER_YOUTUBE_MUSIC
            )
        )

    }
}
