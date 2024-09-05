package com.peecock.ymusic.enums

import kotlin.time.Duration
import kotlin.time.Duration.Companion.days

enum class MaxTopPlaylistItems {
    `10`,
    `20`,
    `30`,
    `40`,
    `50`,
    `70`,
    `90`,
    `100`,
    `150`,
    `200`;

    val number: Long
        get() = when (this) {
            `10` -> 10
            `20` -> 20
            `30` -> 30
            `40` -> 40
            `50` -> 50
            `70` -> 70
            `90` -> 90
            `100` -> 100
            `150` -> 150
            `200` -> 200
        }

}

enum class TopPlaylistPeriod {
    PastDay,
    PastWeek,
    PastMonth,
    PastYear,
    AllTime;

    val duration: Duration
        get() = when (this) {
            PastDay -> 1.days
            PastWeek -> 7.days
            PastMonth -> 30.days
            PastYear -> 365.days
            AllTime -> Duration.INFINITE
        }
}