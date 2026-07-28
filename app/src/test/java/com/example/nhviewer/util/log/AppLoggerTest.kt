package com.example.nhviewer.util.log

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.io.RandomAccessFile
import java.util.concurrent.TimeUnit

class AppLoggerTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @After
    fun restoreDefaultLevel() {
        // AppLogger 是单例，级别会跨用例残留
        AppLogger.setLogLevel("info")
    }

    // ---------- 脱敏 ----------

    @Test
    fun `脱敏 Bearer 令牌但保留键名`() {
        val result = AppLogger.sanitize("Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.payload.sig")

        assertEquals("Authorization: Bearer ***", result)
    }

    @Test
    fun `脱敏 Cookie 整行`() {
        val result = AppLogger.sanitize("Cookie: sessionid=abc123; csrftoken=xyz789")

        assertEquals("Cookie: ***", result)
    }

    @Test
    fun `脱敏 Set-Cookie 响应头`() {
        val result = AppLogger.sanitize("Set-Cookie: refresh=deadbeef; HttpOnly")

        assertEquals("Set-Cookie: ***", result)
    }

    @Test
    fun `脱敏 JSON 中的令牌字段`() {
        val result = AppLogger.sanitize("""{"access_token":"s3cr3t","expires_in":3600}""")

        assertEquals("""{"access_token":"***","expires_in":3600}""", result)
        assertFalse(result.contains("s3cr3t"))
    }

    @Test
    fun `脱敏 query 参数中的密码`() {
        val result = AppLogger.sanitize("POST /login?password=hunter2&next=/home")

        assertEquals("POST /login?password=***&next=/home", result)
    }

    @Test
    fun `脱敏 api_key 与 secret`() {
        assertFalse(AppLogger.sanitize("api_key=AKIA1234567890").contains("AKIA1234567890"))
        assertFalse(AppLogger.sanitize("secret: topsecret").contains("topsecret"))
    }

    @Test
    fun `普通业务日志不被误伤`() {
        val message = "画廊 177013 第 3 页加载失败 (sort=popular)"

        assertEquals(message, AppLogger.sanitize(message))
    }

    @Test
    fun `含 token 词但无键值结构的文本不被误伤`() {
        val message = "Token refreshed successfully"

        assertEquals(message, AppLogger.sanitize(message))
    }

    @Test
    fun `空字符串脱敏后仍为空`() {
        assertEquals("", AppLogger.sanitize(""))
    }

    // ---------- 级别过滤 ----------

    @Test
    fun `级别 none 时任何级别都不可记录`() {
        AppLogger.setLogLevel("none")

        assertFalse(AppLogger.isLoggable(AppLogger.LEVEL_ERROR))
        assertFalse(AppLogger.isLoggable(AppLogger.LEVEL_WARN))
        assertFalse(AppLogger.isLoggable(AppLogger.LEVEL_INFO))
        assertFalse(AppLogger.isLoggable(AppLogger.LEVEL_DEBUG))
    }

    @Test
    fun `级别 warn 只放行 warn 与 error`() {
        AppLogger.setLogLevel("warn")

        assertTrue(AppLogger.isLoggable(AppLogger.LEVEL_ERROR))
        assertTrue(AppLogger.isLoggable(AppLogger.LEVEL_WARN))
        assertFalse(AppLogger.isLoggable(AppLogger.LEVEL_INFO))
        assertFalse(AppLogger.isLoggable(AppLogger.LEVEL_DEBUG))
    }

    @Test
    fun `级别 debug 放行全部`() {
        AppLogger.setLogLevel("debug")

        assertTrue(AppLogger.isLoggable(AppLogger.LEVEL_DEBUG))
        assertTrue(AppLogger.isLoggable(AppLogger.LEVEL_INFO))
    }

    @Test
    fun `无法识别的级别回退为 info`() {
        AppLogger.setLogLevel("不存在的级别")

        assertTrue(AppLogger.isLoggable(AppLogger.LEVEL_INFO))
        assertFalse(AppLogger.isLoggable(AppLogger.LEVEL_DEBUG))
    }

    // ---------- 清理策略 ----------

    @Test
    fun `超过保留天数的日志被删除`() {
        val dir = tempFolder.newFolder("logs")
        val stale = createLogFile(dir, "app_log_20200101_000000.log", sizeBytes = 128)
        val fresh = createLogFile(dir, "app_log_20991231_235959.log", sizeBytes = 128)
        assertTrue(stale.setLastModified(daysAgo(6)))
        assertTrue(fresh.setLastModified(daysAgo(1)))

        AppLogger.cleanUpLogs(dir)

        assertFalse("超期文件应被删除", stale.exists())
        assertTrue("未超期文件应保留", fresh.exists())
    }

    @Test
    fun `非日志文件不受清理影响`() {
        val dir = tempFolder.newFolder("logs")
        val unrelated = File(dir, "crash_dump.txt")
        unrelated.writeText("keep me")
        assertTrue(unrelated.setLastModified(daysAgo(30)))

        AppLogger.cleanUpLogs(dir)

        assertTrue("前缀不匹配的文件不应被删除", unrelated.exists())
    }

    @Test
    fun `目录超配额时按修改时间从旧到新淘汰`() {
        val dir = tempFolder.newFolder("logs")
        // 单文件 8MB，三个合计 24MB，超出 20MB 目录配额
        val oldest = createLogFile(dir, "app_log_20250101_000000.log", sizeBytes = 8L * 1024 * 1024)
        val middle = createLogFile(dir, "app_log_20250102_000000.log", sizeBytes = 8L * 1024 * 1024)
        val newest = createLogFile(dir, "app_log_20250103_000000.log", sizeBytes = 8L * 1024 * 1024)
        assertTrue(oldest.setLastModified(daysAgo(3)))
        assertTrue(middle.setLastModified(daysAgo(2)))
        assertTrue(newest.setLastModified(daysAgo(1)))

        AppLogger.cleanUpLogs(dir)

        assertFalse("最旧的文件应被淘汰", oldest.exists())
        assertTrue(middle.exists())
        assertTrue(newest.exists())
    }

    @Test
    fun `未超配额时不删除任何文件`() {
        val dir = tempFolder.newFolder("logs")
        val a = createLogFile(dir, "app_log_20250101_000000.log", sizeBytes = 1024)
        val b = createLogFile(dir, "app_log_20250102_000000.log", sizeBytes = 1024)
        assertTrue(a.setLastModified(daysAgo(2)))
        assertTrue(b.setLastModified(daysAgo(1)))

        AppLogger.cleanUpLogs(dir)

        assertTrue(a.exists())
        assertTrue(b.exists())
    }

    // ---------- 轮转 ----------

    @Test
    fun `轮转不会复用已写满的文件`() {
        val dir = tempFolder.newFolder("logs")
        val first = AppLogger.nextLogFile(dir)
        // 撑到 2MB 单文件上限
        RandomAccessFile(first, "rw").use { it.setLength(2L * 1024 * 1024) }

        val second = AppLogger.nextLogFile(dir)

        assertNotEquals("写满后必须换新文件", first.absolutePath, second.absolutePath)
    }

    @Test
    fun `轮转产生的文件名仍符合日志命名约定`() {
        val dir = tempFolder.newFolder("logs")

        val file = AppLogger.nextLogFile(dir)

        assertTrue(file.name.startsWith("app_log_"))
        assertTrue(file.name.endsWith(".log"))
    }

    private fun createLogFile(dir: File, name: String, sizeBytes: Long): File {
        val file = File(dir, name)
        RandomAccessFile(file, "rw").use { it.setLength(sizeBytes) }
        return file
    }

    private fun daysAgo(days: Long): Long =
        System.currentTimeMillis() - TimeUnit.DAYS.toMillis(days)
}
