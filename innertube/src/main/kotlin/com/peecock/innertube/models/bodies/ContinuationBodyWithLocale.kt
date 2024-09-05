package com.peecock.innertube.models.bodies

import com.peecock.innertube.models.Context
import kotlinx.serialization.Serializable


@Serializable
data class ContinuationBodyWithLocale(
    val context: Context = Context.DefaultWebWithLocale,
    val continuation: String,
)
