package com.peecock.ymusic.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.navigation.NavController
import com.peecock.ymusic.enums.NavigationBarPosition
import com.peecock.ymusic.enums.UiType
import com.peecock.ymusic.ui.components.themed.appBar
import com.peecock.ymusic.ui.styling.Dimensions
import com.peecock.ymusic.ui.styling.LocalAppearance
import com.peecock.ymusic.utils.UiTypeKey
import com.peecock.ymusic.utils.navigationBarPositionKey
import com.peecock.ymusic.utils.rememberPreference

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleScaffold(
    navController: NavController,
    content: @Composable () -> Unit
) {
    val (colorPalette, typography) = LocalAppearance.current
    val navigationBarPosition by rememberPreference(navigationBarPositionKey, NavigationBarPosition.Bottom)
    val uiType  by rememberPreference(UiTypeKey, UiType.RiMusic)
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val customModifier = if(uiType == UiType.RiMusic)
        Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    else Modifier


    androidx.compose.material3.Scaffold(
        modifier = customModifier,
        containerColor = colorPalette.background0,
        topBar = {
            if(uiType == UiType.RiMusic) {
                appBar(navController)
            }
        },
        bottomBar = {}
    ) { paddingValues ->
        Row(
            modifier = Modifier
                .padding(paddingValues)
                .background(colorPalette.background0)
                .fillMaxSize()
        ) {

            Surface(
                modifier = Modifier
                    .fillMaxWidth(
                        if (navigationBarPosition == NavigationBarPosition.Left ||
                            navigationBarPosition == NavigationBarPosition.Top ||
                            navigationBarPosition == NavigationBarPosition.Bottom
                        ) 1f
                        else Dimensions.contentWidthRightBar
                    ),
                content = content
            )
        }
    }
}