package com.peecock.ymusic.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.isContainer
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun SimpleDockedSearchBar() {
    var text by rememberSaveable { mutableStateOf("") }
    var active by rememberSaveable { mutableStateOf(false) }

    Box(Modifier.fillMaxSize()) {
        // Talkback focus order sorts based on x and y position before considering z-index. The
        // extra Box with semantics and fillMaxWidth is a workaround to get the search bar to focus
        // before the content.
        Box(Modifier
            .semantics { isContainer = true }
            .zIndex(1f)
            .fillMaxWidth()) {
            SearchBar(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 8.dp),
                query = text,
                onQueryChange = { text = it },
                onSearch = { active = false },
                active = active,
                onActiveChange = { active = it },
                placeholder = { Text("Hinted search text") },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                trailingIcon = { Icon(Icons.Rounded.MoreVert, contentDescription = null) },
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(4) { idx ->
                        val resultText = "Suggestion $idx"
                        ListItem(
                            modifier = Modifier.clickable {
                                text = resultText
                                active = false
                            },
                            headlineContent = {
                                Text(
                                    text = resultText,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            },
                            supportingContent = {
                                Text(
                                    text = "Additional info",
                                    style = MaterialTheme.typography.titleMedium
                                )
                            },
                            leadingContent = { Icon(Icons.Rounded.Star, contentDescription = null) },
                        )
                    }
                }
            }
        }

        // Want to display value by default ? Let's do it here
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, top = 72.dp, end = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val list = List(100) { "Text $it" }
            items(count = list.size) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    text = list[it],
                    style = MaterialTheme.typography.titleMedium.copy(Color.White)
                )
            }
        }
    }
}