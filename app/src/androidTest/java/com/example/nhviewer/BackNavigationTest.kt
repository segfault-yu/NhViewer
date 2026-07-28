package com.example.nhviewer

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.center
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.test.espresso.Espresso.pressBack
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * 预见式返回改造的返回语义回归测试：只验证返回键触发的状态转换是否正确
 * （展开态/工具栏/抽屉/搜索结果各自收起或弹栈到位），不验证动画像素或手势跟手效果。
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class BackNavigationTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    private fun string(resId: Int): String = composeTestRule.activity.getString(resId)

    // 卡片几何中心会落在"同人志"等标签 Chip 上（Chip 自己也有空 onClick，会吞掉点击），
    // 必须点封面缩略图所在区域（卡片左侧、贯穿整个高度、没有嵌套点击目标）才能触发卡片自身的导航
    private fun clickFirstGalleryCard() {
        composeTestRule.onAllNodesWithTag("gallery_card")[0].performTouchInput {
            click(Offset(80f, 60f))
        }
    }

    @Test
    fun drawerOpen_backClosesDrawerOnly() {
        composeTestRule.onNodeWithContentDescription("Menu").performClick()
        composeTestRule.onNodeWithText(string(R.string.nav_home)).assertIsDisplayed()

        pressBack()

        // ModalNavigationDrawer 关闭后抽屉内容仍留在组合树里（只是滑出屏幕外），
        // 用 assertIsNotDisplayed 而非 assertDoesNotExist 判断
        composeTestRule.onNodeWithText(string(R.string.nav_home)).assertIsNotDisplayed()
        composeTestRule.onNodeWithText(string(R.string.home_search_placeholder)).assertIsDisplayed()
    }

    @Test
    fun searchExpanded_backCollapsesSearchBar() {
        composeTestRule.onNodeWithText(string(R.string.home_search_placeholder)).performClick()
        composeTestRule.onNodeWithText(string(R.string.home_tab_latest)).assertDoesNotExist()

        // 展开态下第一次返回键可能先被系统用于收起软键盘，SearchBar 自身的收起靠 M3 内建
        // PredictiveBackHandler；两种情形都兼容，最多按两次
        pressBack()
        val collapsedAfterFirstPress =
            composeTestRule.onAllNodesWithText(string(R.string.home_tab_latest)).fetchSemanticsNodes().isNotEmpty()
        if (!collapsedAfterFirstPress) {
            pressBack()
        }

        composeTestRule.onNodeWithText(string(R.string.home_tab_latest)).assertIsDisplayed()
    }

    @Test
    fun searchResults_backClearsQueryAndReturnsToTab() {
        val searchField = composeTestRule.onNodeWithText(string(R.string.home_search_placeholder))
        searchField.performClick()
        searchField.performTextInput("test")
        composeTestRule.onNodeWithContentDescription("Search").performClick()

        composeTestRule.onNodeWithText(string(R.string.home_tab_latest)).assertDoesNotExist()

        pressBack()

        composeTestRule.onNodeWithText(string(R.string.home_tab_latest)).assertIsDisplayed()
        composeTestRule.onNodeWithText(string(R.string.home_search_placeholder)).assertIsDisplayed()
    }

    @Test
    fun detailScreen_backReturnsToHomeList() {
        composeTestRule.waitUntil(timeoutMillis = 15_000) {
            composeTestRule.onAllNodesWithTag("gallery_card").fetchSemanticsNodes().isNotEmpty()
        }
        clickFirstGalleryCard()
        composeTestRule.waitUntil(timeoutMillis = 15_000) {
            composeTestRule.onAllNodesWithText(string(R.string.detail_title)).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText(string(R.string.detail_title)).assertIsDisplayed()

        pressBack()

        composeTestRule.waitUntil(timeoutMillis = 15_000) {
            composeTestRule.onAllNodesWithText(string(R.string.detail_title)).fetchSemanticsNodes().isEmpty()
        }
        composeTestRule.onNodeWithText(string(R.string.home_search_placeholder)).assertIsDisplayed()
    }

    @Test
    fun readerToolbar_backHidesToolbarBeforeExitingReader() {
        composeTestRule.waitUntil(timeoutMillis = 15_000) {
            composeTestRule.onAllNodesWithTag("gallery_card").fetchSemanticsNodes().isNotEmpty()
        }
        clickFirstGalleryCard()

        // 若该画廊之前已经读过（本机测试数据里可能残留阅读历史），按钮文案是"继续阅读"而非"开始阅读"，
        // 两者共享"阅读"二字，用子串匹配兼容这两种情形
        composeTestRule.waitUntil(timeoutMillis = 15_000) {
            composeTestRule.onAllNodesWithText("阅读", substring = true).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("阅读", substring = true).performClick()

        composeTestRule.waitUntil(timeoutMillis = 15_000) {
            composeTestRule.onAllNodesWithText(string(R.string.detail_title)).fetchSemanticsNodes().isEmpty()
        }

        // 阅读器工具栏初始是隐藏的，点一下屏幕中间把它显示出来。
        // 此时 ReaderScreen 自己的 loadGallery 网络请求可能还没返回（还在转圈），
        // 那个状态下屏幕上没有任何点击处理逻辑，点了也没用，所以要等真正加载完再点，
        // 加一个短重试兜底避免恰好卡在两者交界处
        var overlaysShown = false
        repeat(10) {
            if (!overlaysShown) {
                composeTestRule.onRoot().performTouchInput { click(center) }
                overlaysShown =
                    composeTestRule.onAllNodesWithContentDescription("Settings").fetchSemanticsNodes().isNotEmpty()
                if (!overlaysShown) Thread.sleep(1_000)
            }
        }
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithContentDescription("Settings").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithContentDescription("Settings").assertIsDisplayed()

        pressBack()

        // 第一级返回只收工具栏，不应该已经弹回详情页
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithContentDescription("Settings").fetchSemanticsNodes().isEmpty()
        }
        composeTestRule.onNodeWithText(string(R.string.detail_title)).assertDoesNotExist()

        pressBack()

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithText(string(R.string.detail_title)).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText(string(R.string.detail_title)).assertIsDisplayed()
    }
}
