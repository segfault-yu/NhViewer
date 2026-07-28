package com.example.nhviewer.presentation.common

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarColors
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.runtime.Composable
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
    SearchBar(
        inputField = {
            SearchBarDefaults.InputField(
                query = query,
                onQueryChange = onQueryChange,
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
            onDeleteHistory = onDeleteHistory,
            onClearAllHistory = onClearAllHistory
        )
    }
}
