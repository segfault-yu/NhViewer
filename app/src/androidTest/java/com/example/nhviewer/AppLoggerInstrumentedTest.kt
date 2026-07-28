package com.example.nhviewer

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.nhviewer.util.log.AppLogger
import com.example.nhviewer.util.runCatchingCancelable
import kotlinx.coroutines.CancellationException
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

// 单元测试跑不到真实写盘链路，这里验证 init → 写入 → 脱敏落盘 → flushBlocking
@RunWith(AndroidJUnit4::class)
class AppLoggerInstrumentedTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        AppLogger.clearAllLogs(context)
        AppLogger.init(context)
        AppLogger.setLogLevel("debug")
    }

    @Test
    fun 日志写入文件并完成脱敏() {
        AppLogger.e("TestTag", "Authorization: Bearer supersecrettoken123", RuntimeException("boom"))

        assertTrue("flushBlocking 应在超时前完成", AppLogger.flushBlocking(5000L))

        val files = AppLogger.getLogFiles(context)
        assertTrue("应生成日志文件", files.isNotEmpty())

        val content = files.joinToString("\n") { it.readText() }
        assertFalse("原始令牌不得落盘", content.contains("supersecrettoken123"))
        assertTrue("键名应保留并掩码", content.contains("Authorization: Bearer ***"))
        assertTrue("异常堆栈应一并写入", content.contains("boom"))
        assertTrue("级别与标签应写入", content.contains("[ERROR] [TestTag]"))
    }

    // Repository 层 45 处调用都经由这个统一出口埋点
    @Test
    fun 仓库失败经统一出口写入日志() {
        val result = runCatchingCancelable("TestRepo") { throw IOException("模拟网络中断") }

        assertTrue("异常应被转成 failure", result.isFailure)
        assertTrue(AppLogger.flushBlocking(5000L))

        val content = AppLogger.getLogFiles(context).joinToString("\n") { it.readText() }
        assertTrue("应带上仓库标签", content.contains("[TestRepo]"))
        assertTrue("应记录异常类型", content.contains("操作失败: IOException"))
        assertTrue("应写入堆栈", content.contains("模拟网络中断"))
    }

    @Test
    fun 协程取消不被记为故障() {
        try {
            runCatchingCancelable("TestRepo") { throw CancellationException("正常取消") }
        } catch (e: CancellationException) {
            // 取消必须原样重抛
        }
        AppLogger.flushBlocking(5000L)

        val content = AppLogger.getLogFiles(context).joinToString("\n") { it.readText() }
        assertFalse("协程取消不应产生日志噪音", content.contains("正常取消"))
    }

    @Test
    fun 级别为none时不产生任何日志文件() {
        AppLogger.setLogLevel("none")

        AppLogger.e("TestTag", "这条不应被记录")
        AppLogger.flushBlocking(5000L)

        val content = AppLogger.getLogFiles(context).joinToString("\n") { it.readText() }
        assertFalse("关闭日志后不应写入", content.contains("这条不应被记录"))
    }
}
