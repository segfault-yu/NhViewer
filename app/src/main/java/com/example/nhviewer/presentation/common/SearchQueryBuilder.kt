package com.example.nhviewer.presentation.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.nhviewer.R
import com.example.nhviewer.domain.model.SearchQuery
import com.example.nhviewer.domain.usecase.SearchQueryParser

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchQueryBuilder(
    rawQuery: String,
    onQueryChanged: (String) -> Unit,
    onTriggerSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    val parser = remember { SearchQueryParser() }
    val parsedQuery = remember(rawQuery) { parser.parse(rawQuery) }

    var showAdvancedFilters by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        // Quick Filters LazyRow
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChipToggle(
                label = "Language: Chinese",
                tag = "language:chinese",
                parsedQuery = parsedQuery,
                onToggle = { newQuery -> onQueryChanged(newQuery.toRawString()); onTriggerSearch() }
            )
            FilterChipToggle(
                label = "Language: English",
                tag = "language:english",
                parsedQuery = parsedQuery,
                onToggle = { newQuery -> onQueryChanged(newQuery.toRawString()); onTriggerSearch() }
            )
            FilterChipToggle(
                label = "Language: Japanese",
                tag = "language:japanese",
                parsedQuery = parsedQuery,
                onToggle = { newQuery -> onQueryChanged(newQuery.toRawString()); onTriggerSearch() }
            )
            FilterChipToggle(
                label = "Category: Doujinshi",
                tag = "category:doujinshi",
                parsedQuery = parsedQuery,
                onToggle = { newQuery -> onQueryChanged(newQuery.toRawString()); onTriggerSearch() }
            )
            FilterChipToggle(
                label = "Category: Manga",
                tag = "category:manga",
                parsedQuery = parsedQuery,
                onToggle = { newQuery -> onQueryChanged(newQuery.toRawString()); onTriggerSearch() }
            )
            FilterChip(
                selected = false,
                onClick = { showAdvancedFilters = true },
                label = { Text("More Filters...") }
            )
        }
    }

    if (showAdvancedFilters) {
        ModalBottomSheet(
            onDismissRequest = { showAdvancedFilters = false }
        ) {
            AdvancedFiltersSheet(
                parsedQuery = parsedQuery,
                onApply = { newQuery ->
                    onQueryChanged(newQuery.toRawString())
                    showAdvancedFilters = false
                    onTriggerSearch()
                },
                onCancel = { showAdvancedFilters = false }
            )
        }
    }
}

@Composable
private fun FilterChipToggle(
    label: String,
    tag: String,
    parsedQuery: SearchQuery,
    onToggle: (SearchQuery) -> Unit
) {
    val isSelected = parsedQuery.includedTags.contains(tag)
    FilterChip(
        selected = isSelected,
        onClick = {
            val newTags = if (isSelected) {
                parsedQuery.includedTags.filter { it != tag }
            } else {
                parsedQuery.includedTags + tag
            }
            onToggle(parsedQuery.copy(includedTags = newTags))
        },
        label = { Text(label) }
    )
}

@Composable
private fun AdvancedFiltersSheet(
    parsedQuery: SearchQuery,
    onApply: (SearchQuery) -> Unit,
    onCancel: () -> Unit
) {
    var minPages by remember { mutableStateOf(parsedQuery.pagesCondition?.replace(">=", "")?.replace(">", "") ?: "") }
    var minFavorites by remember { mutableStateOf(parsedQuery.favoritesCondition?.replace(">=", "")?.replace(">", "") ?: "") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Advanced Filters",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )

        OutlinedTextField(
            value = minPages,
            onValueChange = { minPages = it },
            label = { Text("Minimum Pages") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = minFavorites,
            onValueChange = { minFavorites = it },
            label = { Text("Minimum Favorites") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onCancel) {
                Text(stringResource(R.string.common_cancel))
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = {
                val newPagesCond = if (minPages.isNotBlank()) ">$minPages" else null
                val newFavCond = if (minFavorites.isNotBlank()) ">=$minFavorites" else null

                onApply(
                    parsedQuery.copy(
                        pagesCondition = newPagesCond,
                        favoritesCondition = newFavCond
                    )
                )
            }) {
                Text("Apply")
            }
        }
    }
}
