package com.example.nhviewer.presentation.common

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
    onTriggerSearch: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val parser = remember { SearchQueryParser() }
    val parsedQuery = remember(rawQuery) { parser.parse(rawQuery) }

    val languages = listOf("chinese", "english", "japanese")
    var languageMenuExpanded by remember { mutableStateOf(false) }
    var uploadedMenuExpanded by remember { mutableStateOf(false) }

    var showPagesDialog by remember { mutableStateOf(false) }
    var showFavoritesDialog by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Language Dropdown
            Box {
                val currentLang = parsedQuery.includedTags.find { it.startsWith("language:") }?.removePrefix("language:")
                FilterChip(
                    selected = currentLang != null,
                    onClick = { languageMenuExpanded = true },
                    label = { Text(if (currentLang != null) "${stringResource(R.string.search_advanced_language)}: $currentLang" else stringResource(R.string.search_advanced_language)) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Translate, contentDescription = null, modifier = Modifier.size(FilterChipDefaults.IconSize))
                    },
                    trailingIcon = {
                        Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(FilterChipDefaults.IconSize))
                    }
                )
                DropdownMenu(
                    expanded = languageMenuExpanded,
                    onDismissRequest = { languageMenuExpanded = false }
                ) {
                    languages.forEach { lang ->
                        DropdownMenuItem(
                            text = { Text(lang) },
                            onClick = {
                                val tag = "language:$lang"
                                val newTags = parsedQuery.includedTags.filter { !it.startsWith("language:") } + tag
                                val newQuery = parsedQuery.copy(includedTags = newTags)
                                val queryStr = newQuery.toRawString()
                                onQueryChanged(queryStr)
                                onTriggerSearch(queryStr)
                                languageMenuExpanded = false
                            }
                        )
                    }
                    if (currentLang != null) {
                        Divider()
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.common_clear)) },
                            onClick = {
                                val newTags = parsedQuery.includedTags.filter { !it.startsWith("language:") }
                                val newQuery = parsedQuery.copy(includedTags = newTags)
                                val queryStr = newQuery.toRawString()
                                onQueryChanged(queryStr)
                                onTriggerSearch(queryStr)
                                languageMenuExpanded = false
                            }
                        )
                    }
                }
            }

            // Uploaded Date Dropdown
            Box {
                val currentUploaded = parsedQuery.uploadedCondition
                val label = when(currentUploaded) {
                    "<1d" -> stringResource(R.string.search_advanced_today)
                    "<7d" -> stringResource(R.string.search_advanced_this_week)
                    "<30d" -> stringResource(R.string.search_advanced_this_month)
                    else -> stringResource(R.string.search_advanced_uploaded_within).dropLast(1)
                }
                FilterChip(
                    selected = currentUploaded != null,
                    onClick = { uploadedMenuExpanded = true },
                    label = { Text(label) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(FilterChipDefaults.IconSize))
                    },
                    trailingIcon = {
                        Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(FilterChipDefaults.IconSize))
                    }
                )
                DropdownMenu(
                    expanded = uploadedMenuExpanded,
                    onDismissRequest = { uploadedMenuExpanded = false }
                ) {
                    val dates = listOf(
                        stringResource(R.string.search_advanced_today) to "<1d",
                        stringResource(R.string.search_advanced_this_week) to "<7d",
                        stringResource(R.string.search_advanced_this_month) to "<30d"
                    )
                    dates.forEach { (textLabel, value) ->
                        DropdownMenuItem(
                            text = { Text(textLabel) },
                            onClick = {
                                val newQuery = parsedQuery.copy(uploadedCondition = value)
                                val queryStr = newQuery.toRawString()
                                onQueryChanged(queryStr)
                                onTriggerSearch(queryStr)
                                uploadedMenuExpanded = false
                            }
                        )
                    }
                    if (currentUploaded != null) {
                        Divider()
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.common_clear)) },
                            onClick = {
                                val newQuery = parsedQuery.copy(uploadedCondition = null)
                                val queryStr = newQuery.toRawString()
                                onQueryChanged(queryStr)
                                onTriggerSearch(queryStr)
                                uploadedMenuExpanded = false
                            }
                        )
                    }
                }
            }

            // Pages Dialog Trigger / Display
            FilterChip(
                selected = parsedQuery.pagesCondition != null,
                onClick = {
                    if (parsedQuery.pagesCondition != null) {
                        val newQuery = parsedQuery.copy(pagesCondition = null)
                        val queryStr = newQuery.toRawString()
                        onQueryChanged(queryStr)
                        onTriggerSearch(queryStr)
                    } else {
                        showPagesDialog = true
                    }
                },
                label = { Text(parsedQuery.pagesCondition?.let { "Pages: $it" } ?: stringResource(R.string.search_advanced_min_pages)) },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(FilterChipDefaults.IconSize))
                }
            )

            // Favorites Dialog Trigger / Display
            FilterChip(
                selected = parsedQuery.favoritesCondition != null,
                onClick = {
                    if (parsedQuery.favoritesCondition != null) {
                        val newQuery = parsedQuery.copy(favoritesCondition = null)
                        val queryStr = newQuery.toRawString()
                        onQueryChanged(queryStr)
                        onTriggerSearch(queryStr)
                    } else {
                        showFavoritesDialog = true
                    }
                },
                label = { Text(parsedQuery.favoritesCondition?.let { "Favs: $it" } ?: stringResource(R.string.search_advanced_min_favorites)) },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Favorite, contentDescription = null, modifier = Modifier.size(FilterChipDefaults.IconSize))
                }
            )

            // Dynamic Chips (Included)
            parsedQuery.includedTags.forEach { tag ->
                if (!tag.startsWith("language:")) {
                    FilterChipToggle(
                        label = tag,
                        tag = tag,
                        parsedQuery = parsedQuery,
                        onToggle = { newQuery ->
                            val queryStr = newQuery.toRawString()
                            onQueryChanged(queryStr)
                            onTriggerSearch(queryStr)
                        }
                    )
                }
            }

            // Dynamic Chips (Excluded)
            parsedQuery.excludedTags.forEach { tag ->
                FilterChipToggle(
                    label = tag,
                    tag = tag,
                    parsedQuery = parsedQuery,
                    onToggle = { newQuery ->
                        val queryStr = newQuery.toRawString()
                        onQueryChanged(queryStr)
                        onTriggerSearch(queryStr)
                    },
                    isExclude = true
                )
            }
        }
    }

    if (showPagesDialog) {
        SimpleInputDialog(
            title = stringResource(R.string.search_advanced_min_pages),
            isNumber = true,
            onDismiss = { showPagesDialog = false },
            onApply = { value ->
                val newQuery = parsedQuery.copy(pagesCondition = ">$value")
                val queryStr = newQuery.toRawString()
                onQueryChanged(queryStr)
                onTriggerSearch(queryStr)
                showPagesDialog = false
            }
        )
    }

    if (showFavoritesDialog) {
        SimpleInputDialog(
            title = stringResource(R.string.search_advanced_min_favorites),
            isNumber = true,
            onDismiss = { showFavoritesDialog = false },
            onApply = { value ->
                val newQuery = parsedQuery.copy(favoritesCondition = ">=$value")
                val queryStr = newQuery.toRawString()
                onQueryChanged(queryStr)
                onTriggerSearch(queryStr)
                showFavoritesDialog = false
            }
        )
    }
}

@Composable
private fun SimpleInputDialog(
    title: String,
    isNumber: Boolean = false,
    onDismiss: () -> Unit,
    onApply: (String) -> Unit
) {
    var value by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = value,
                onValueChange = { value = it },
                singleLine = true,
                keyboardOptions = if (isNumber) KeyboardOptions(keyboardType = KeyboardType.Number) else KeyboardOptions.Default,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    if (value.isNotBlank()) {
                        onApply(value)
                    }
                }
            ) {
                Text(stringResource(R.string.common_ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_cancel))
            }
        }
    )
}

@Composable
private fun FilterChipToggle(
    label: String,
    tag: String,
    parsedQuery: SearchQuery,
    onToggle: (SearchQuery) -> Unit,
    isExclude: Boolean = false
) {
    val isSelected = if (isExclude) {
        parsedQuery.excludedTags.contains(tag)
    } else {
        parsedQuery.includedTags.contains(tag)
    }

    FilterChip(
        selected = isSelected,
        onClick = {
            val newQuery = if (isExclude) {
                val newTags = if (isSelected) parsedQuery.excludedTags.filter { it != tag } else parsedQuery.excludedTags + tag
                parsedQuery.copy(excludedTags = newTags)
            } else {
                val newTags = if (isSelected) parsedQuery.includedTags.filter { it != tag } else parsedQuery.includedTags + tag
                parsedQuery.copy(includedTags = newTags)
            }
            onToggle(newQuery)
        },
        label = { Text(label) }
    )
}