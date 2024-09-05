package com.peecock.ymusic.ui.screens.settings

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import com.peecock.ymusic.R
import com.peecock.ymusic.enums.NavigationBarPosition
import com.peecock.ymusic.ui.components.themed.HeaderWithIcon
import com.peecock.ymusic.ui.styling.Dimensions
import com.peecock.ymusic.ui.styling.LocalAppearance
import com.peecock.ymusic.utils.getVersionName
import com.peecock.ymusic.utils.navigationBarPositionKey
import com.peecock.ymusic.utils.rememberPreference
import com.peecock.ymusic.utils.secondary


@ExperimentalAnimationApi
@Composable
fun About() {
    val (colorPalette, typography) = LocalAppearance.current
    val uriHandler = LocalUriHandler.current
    val navigationBarPosition by rememberPreference(navigationBarPositionKey, NavigationBarPosition.Bottom)
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
            title = stringResource(R.string.about),
            iconId = R.drawable.information,
            enabled = false,
            showIcon = true,
            modifier = Modifier,
            onClick = {}
        )
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxSize()
        ) {
            BasicText(
                text = "YMusic v${getVersionName()} by peecock",
                style = typography.s.secondary,

                )
        }

        SettingsGroupSpacer()

        SettingsEntryGroupText(title = stringResource(R.string.social))

        SettingsEntry(
            title = "YMusic",
            text = stringResource(R.string.go_to_link),
            onClick = {
                uriHandler.openUri("https://ymusicapk.cc")
            }
        )

        SettingsGroupSpacer()

        SettingsEntryGroupText(title = stringResource(R.string.troubleshooting))

        SettingsEntry(
            title = stringResource(R.string.report_an_issue),
            text = stringResource(R.string.you_will_be_redirected_to_github),
            onClick = {
                uriHandler.openUri("https://github.com/fast4x/RiMusic/issues/new?assignees=&labels=bug&template=bug_report.yaml")
            }
        )


        SettingsEntry(
            title = stringResource(R.string.request_a_feature_or_suggest_an_idea),
            text = stringResource(R.string.you_will_be_redirected_to_github),
            onClick = {
                uriHandler.openUri("https://github.com/fast4x/RiMusic/issues/new?assignees=&labels=feature_request&template=feature_request.yaml")
            }
        )

        SettingsGroupSpacer()

        SettingsEntryGroupText(title = stringResource(R.string.contributors))
        SettingsDescription(text = stringResource(R.string.in_alphabetical_order))

        SettingsTopDescription( text ="Translator:")
        SettingsTopDescription( text =
            "2010furs \n"+

                    "peecock \n"+
                    "YMusic \n"+
                    "ABS zarzis \n"+
                    "Adam Kop \n"+
                    "agefcgo \n"+
                    "Ahmad Al Juwaisri \n"+
                    "Alnoer \n"+
                    "Ann Naser Nabil \n"+
                    "AntoniNowak \n" +
                    "beez276 \n"+
                    "CiccioDerole \n"+
                    "Clyde6790p_PH \n"+
                    "Conk \n"+
                    "Corotyest \n" +
                    "Crayz310 \n"+
                    "cultcats \n"+
                    "CUMOON \n"+
                    "DanielSevillano \n"+
                    "Dženan \n" +
                    "EMC_Translator \n"+
                    "Fabian Urra \n"+
                    "fast4x \n"+
                    "Fausta Ahmad \n"+
                    "Get100percent \n"+
                    "Glich440 \n"+
                    "HelloZebra1133 \n"+
                    "Ikanakova \n"+
                    "iOSStarWorld \n"+
                    "IvanMaksimovic77 \n"+
                    "JZITNIK-github \n"+
                    "Kjev666 \n"+
                    "Kptmx \n"+
                    "koliwan \n"+
                    "Lolozweipunktnull \n" +
                    "ManuelCoimbra) \n" +
                    "Marinkas \n"+
                    "materialred \n"+
                    "Mid_Vur_Shaan \n" +
                    "Muha Aliss \n"+
                    "Ndvok \n"+
                    "Nebula-Mechanica \n"+
                    "NEVARLeVrai \n"+
                    "NikunjKhangwal \n"+
                    "NiXT0y \n"+
                    "opcitgv \n"+
                    "OlimitLolli \n"+
                    "OrangeZXZ \n"+
                    "RegularWater \n"+
                    "Rikalaj \n" +
                    "Roklc \n"+
                    "sebbe.ekman \n"+
                    "Seryoga1984 \n" +
                    "SharkChan0622 \n"+
                    "Sharunkumar \n" +
                    "Shilave malay \n"+
                    "softinterlingua \n"+
                    "SureshTimma \n"+
                    "Siggi1984 \n"+
                    "Teaminh \n"+
                    "TeddysulaimanGL \n"+
                    "YeeTW \n"+
                    "Th3-C0der \n" +
                    "TheCreeperDuck \n"+
                    "TsyQax \n"+
                    "VINULA2007 \n" +
                    "Vladimir \n" +
                    "xSyntheticWave \n"+
                    "Zan1456 \n" +
                    "ZeroZero00 \n"
        )

        SettingsTopDescription( text ="Developer / Designer:")
        SettingsTopDescription( text =
            "25huizengek1 \n"+
                    "821938089 \n"+
                    "aneesh1122\n"+
                    "bbyeen \n"+
                "Craeckie \n"+
                "DanielSevillano \n"+
                "Fast4x \n"+
                "Ikanakova \n"+
                 "Iscle \n"+
                "JZITNIK-github \n" +
                "Locxter \n"+
                 "lrusso96 \n"+
                 "martkol \n"+
                "Roklc \n"+
                "sharunkumar \n" +
                "SuhasDissa \n" +
            "twistios \n"
        )
        SettingsGroupSpacer(
            modifier = Modifier.height(Dimensions.bottomSpacer)
        )
    }
}
