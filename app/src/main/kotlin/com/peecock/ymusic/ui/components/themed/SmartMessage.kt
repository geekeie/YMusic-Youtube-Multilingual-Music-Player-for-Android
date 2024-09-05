package com.peecock.ymusic.ui.components.themed

import android.content.Context
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.compose.ui.graphics.Color
import androidx.media3.common.util.UnstableApi
import es.dmoral.toasty.Toasty
import com.peecock.ymusic.enums.MessageType
import com.peecock.ymusic.enums.PopupType
import com.peecock.ymusic.utils.getEnum
import com.peecock.ymusic.utils.messageTypeKey
import com.peecock.ymusic.utils.preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(UnstableApi::class)
fun SmartMessage(
    message: String,
    type: PopupType? = PopupType.Info,
    backgroundColor: Color? = Color.DarkGray,
    durationLong: Boolean? = false,
    context: Context,
) {
    CoroutineScope(Dispatchers.Main).launch {
        val length = if (durationLong == true) Toast.LENGTH_LONG else Toast.LENGTH_SHORT

        if (context.preferences.getEnum(messageTypeKey, MessageType.Modern) == MessageType.Modern) {
            when (type) {
                PopupType.Info -> Toasty.info(context, message, length, true).show()
                PopupType.Success -> Toasty.success(context, message, length, true).show()
                PopupType.Error -> Toasty.error(context, message, length, true).show()
                PopupType.Warning -> Toasty.warning(context, message, length, true).show()
                null -> Toasty.normal(context, message, length).show()
            }

        } else
        //if (durationLong == true) context.toastLong(message) else context.toast(message)
            Toasty.normal(context, message, length).show()
    }
}

