package com.peecock.ymusic.utils

import androidx.compose.material3.MenuItemColors
import androidx.compose.runtime.Composable
import com.peecock.ymusic.ui.styling.LocalAppearance
import com.peecock.ymusic.ui.styling.favoritesIcon

@Composable
fun menuItemColors(): MenuItemColors {
    val (colorPalette, _) = LocalAppearance.current
    return MenuItemColors(
        leadingIconColor =  colorPalette.favoritesIcon,
        trailingIconColor =  colorPalette.favoritesIcon,
        textColor = colorPalette.textSecondary,
        disabledTextColor = colorPalette.text,
        disabledLeadingIconColor = colorPalette.text,
        disabledTrailingIconColor = colorPalette.text,
    )

}