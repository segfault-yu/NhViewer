package com.example.nhviewer.presentation.common

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarColors
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import com.example.nhviewer.domain.model.SearchHistory
import com.example.nhviewer.domain.model.Tag
import com.example.nhviewer.ui.theme.NhMotion

/**
 * 首页与搜索页共用的顶部搜索栏：折叠态输入框 + 展开态历史/建议面板。
 * 迁移自废弃的 `SearchBar(query, active, onActiveChange, ...)` 重载，改用
 * `SearchBar(inputField, expanded, onExpandedChange, ...)` + `SearchBarDefaults.InputField`。
 * 展开态下的预见式返回收起动画由该组件内部自带，调用方无需额外接 BackHandler。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NhSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    searchHistory: List<SearchHistory>,
    autocompleteSuggestions: List<Tag>,
    onDeleteHistory: (String) -> Unit,
    onClearAllHistory: () -> Unit,
    leadingIcon: @Composable () -> Unit,
    trailingIcon: @Composable () -> Unit,
    placeholder: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    colors: SearchBarColors = SearchBarDefaults.colors()
) {
    val textFieldState = rememberTextFieldState(initialText = query)
    val latestQuery by rememberUpdatedState(query)

    // 外部整体替换 query（建议词点击、清空按钮、筛选 chip 等）时，把光标显式放到末尾，
    // 避免 Compose 沿用旧的光标下标导致后续输入插到文字中间
    LaunchedEffect(query) {
        if (textFieldState.text.toString() != query) {
            textFieldState.setTextAndPlaceCursorAtEnd(query)
        }
    }

    // 用户在输入框内自行打字产生的变化，上报给外部状态
    LaunchedEffect(textFieldState) {
        snapshotFlow { textFieldState.text.toString() }
            .collect { text ->
                if (text != latestQuery) {
                    onQueryChange(text)
                }
            }
    }

    SearchBar(
        inputField = {
            SearchBarDefaults.InputField(
                state = textFieldState,
                onSearch = onSearch,
                expanded = expanded,
                onExpandedChange = onExpandedChange,
                placeholder = placeholder,
                leadingIcon = leadingIcon,
                trailingIcon = trailingIcon
            )
        },
        expanded = expanded,
        onExpandedChange = onExpandedChange,
        colors = colors,
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = NhMotion.Spatial.default())
    ) {
        SearchQueryBuilder(
            rawQuery = query,
            onQueryChanged = onQueryChange,
            onTriggerSearch = onSearch
        )
        SearchSuggestionPanel(
            searchQuery = query,
            searchHistory = searchHistory,
            autocompleteSuggestions = autocompleteSuggestions,
            onSearch = onSearch,
            onQueryChange = onQueryChange,
            onDeleteHistory = onDeleteHistory,
            onClearAllHistory = onClearAllHistory
        )
    }
}
