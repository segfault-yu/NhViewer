package com.example.nhviewer.util.i18n

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LanguageManagerTest {

    @Test
    fun `zh 映射为简体中文 Locale`() {
        val locales = LanguageManager.localesFor("zh")
        val locale = locales.get(0)

        assertEquals("zh", locale?.language)
        val tags = locales.toLanguageTags().lowercase()
        assertTrue(tags.contains("hans"))
    }

    @Test
    fun `en 映射为英文 Locale`() {
        val locales = LanguageManager.localesFor("en")

        assertEquals("en", locales.toLanguageTags())
    }

    @Test
    fun `system 映射为空 LocaleList 跟随系统`() {
        val locales = LanguageManager.localesFor("system")

        assertTrue(locales.isEmpty)
    }

    @Test
    fun `未知取值也回退为跟随系统`() {
        val locales = LanguageManager.localesFor("unknown")

        assertTrue(locales.isEmpty)
    }
}
