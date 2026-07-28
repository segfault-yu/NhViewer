package com.example.nhviewer.util.log

import android.content.Context
import android.util.Log
import com.example.nhviewer.BuildConfig
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.CountDownLatch
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

object AppLogger {

    const val LEVEL_NONE = 0
    const val LEVEL_ERROR = 1
    const val LEVEL_WARN = 2
    const val LEVEL_INFO = 3
    const val LEVEL_DEBUG = 4

    private const val TAG = "AppLogger"
    private const val LOG_DIR_NAME = "logs"
    private const val LOG_FILE_PREFIX = "app_log_"
    private const val LOG_FILE_SUFFIX = ".log"
    private const val MAX_FILE_SIZE_BYTES = 2 * 1024 * 1024L
    private const val MAX_DIRECTORY_SIZE_BYTES = 20 * 1024 * 1024L
    private const val MAX_FILE_AGE_DAYS = 5L
    private const val QUEUE_CAPACITY = 1000

    // 替换串中的 $1 保留脱敏项的键名，便于排查时仍能看出泄露点
    private const val MASK_REPLACEMENT = "\$1***"

    // 队列满时丢弃新任务而非最旧任务，保证 init 排队的目录初始化不被挤掉
    private val executor = ThreadPoolExecutor(
        1,
        1,
        0L,
        TimeUnit.MILLISECONDS,
        LinkedBlockingQueue(QUEUE_CAPACITY),
        { runnable -> Thread(runnable, "AppLogger").apply { isDaemon = true } },
        ThreadPoolExecutor.DiscardPolicy()
    )

    // 时间戳在调用方线程生成，SimpleDateFormat 非线程安全，按线程隔离
    private val dateFormat = object : ThreadLocal<SimpleDateFormat>() {
        override fun initialValue() = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)
    }

    // 仅在 executor 单线程内使用，无需隔离
    private val fileDateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)

    private val sensitivePatterns = listOf(
        Pattern.compile("(?i)(authorization\\s*[:=]\\s*bearer\\s+)\\S+"),
        Pattern.compile("(?i)((?:set-)?cookie\\s*[:=]\\s*)[^\\r\\n]+"),
        Pattern.compile(
            "(?i)([\"']?(?:access_token|refresh_token|token|password|passwd|api_key|apikey|secret)[\"']?\\s*[:=]\\s*[\"']?)[^\"',&\\s}]+"
        )
    )

    private var logDir: File? = null
    private var currentFile: File? = null
    private var fileWriter: FileWriter? = null
    private var currentFileSize: Long = 0L

    @Volatile
    private var minLogLevelRank: Int = if (BuildConfig.DEBUG) LEVEL_DEBUG else LEVEL_INFO

    fun setLogLevel(level: String) {
        minLogLevelRank = when (level.lowercase(Locale.US)) {
            "none" -> LEVEL_NONE
            "error" -> LEVEL_ERROR
            "warn" -> LEVEL_WARN
            "debug" -> LEVEL_DEBUG
            else -> LEVEL_INFO
        }
    }

    @PublishedApi
    internal fun isLoggable(rank: Int): Boolean = minLogLevelRank >= rank

    fun init(context: Context) {
        val dir = File(context.filesDir, LOG_DIR_NAME)
        executor.execute {
            try {
                if (!dir.exists() && !dir.mkdirs()) {
                    Log.e(TAG, "日志目录创建失败: ${dir.absolutePath}")
                    return@execute
                }
                logDir = dir
                cleanUpLogs(dir)
            } catch (e: Exception) {
                Log.e(TAG, "初始化日志目录失败", e)
            }
        }
    }

    fun d(tag: String, message: String) = log(LEVEL_DEBUG, tag, message, null)

    fun i(tag: String, message: String) = log(LEVEL_INFO, tag, message, null)

    fun w(tag: String, message: String, throwable: Throwable? = null) =
        log(LEVEL_WARN, tag, message, throwable)

    fun e(tag: String, message: String, throwable: Throwable? = null) =
        log(LEVEL_ERROR, tag, message, throwable)

    // 懒求值重载：级别未命中时不构造消息字符串
    inline fun d(tag: String, message: () -> String) {
        if (isLoggable(LEVEL_DEBUG)) log(LEVEL_DEBUG, tag, message(), null)
    }

    inline fun i(tag: String, message: () -> String) {
        if (isLoggable(LEVEL_INFO)) log(LEVEL_INFO, tag, message(), null)
    }

    inline fun w(tag: String, throwable: Throwable? = null, message: () -> String) {
        if (isLoggable(LEVEL_WARN)) log(LEVEL_WARN, tag, message(), throwable)
    }

    inline fun e(tag: String, throwable: Throwable? = null, message: () -> String) {
        if (isLoggable(LEVEL_ERROR)) log(LEVEL_ERROR, tag, message(), throwable)
    }

    // logcat 与文件的唯一出口：同一道级别判断、同一份脱敏文本
    fun log(rank: Int, tag: String, message: String, throwable: Throwable?) {
        if (minLogLevelRank < rank) return

        val safeMessage = sanitize(message)
        val safeStackTrace = throwable?.let { sanitize(stackTraceOf(it)) }
        val logcatText = if (safeStackTrace != null) "$safeMessage\n$safeStackTrace" else safeMessage

        when (rank) {
            LEVEL_ERROR -> Log.e(tag, logcatText)
            LEVEL_WARN -> Log.w(tag, logcatText)
            LEVEL_INFO -> Log.i(tag, logcatText)
            else -> Log.d(tag, logcatText)
        }

        writeLog(levelName(rank), tag, safeMessage, safeStackTrace)
    }

    private fun levelName(rank: Int): String = when (rank) {
        LEVEL_ERROR -> "ERROR"
        LEVEL_WARN -> "WARN"
        LEVEL_INFO -> "INFO"
        else -> "DEBUG"
    }

    internal fun sanitize(raw: String): String {
        if (raw.isEmpty()) return raw
        return try {
            var result = raw
            for (pattern in sensitivePatterns) {
                result = pattern.matcher(result).replaceAll(MASK_REPLACEMENT)
            }
            result
        } catch (e: Exception) {
            // 脱敏失败时整体掩码：异常路径绝不能回退成原文
            "***"
        }
    }

    // 不用 Log.getStackTraceString，避免单元测试下静态桩返回 null
    private fun stackTraceOf(throwable: Throwable): String {
        return try {
            val writer = StringWriter()
            PrintWriter(writer).use { throwable.printStackTrace(it) }
            writer.toString()
        } catch (e: Exception) {
            throwable.toString()
        }
    }

    private fun writeLog(level: String, tag: String, message: String, stackTrace: String?) {
        val timestamp = dateFormat.get()?.format(Date()) ?: return
        val logLine = buildString {
            append(timestamp).append(" [").append(level).append("] [").append(tag).append("]: ")
            append(message).append('\n')
            if (stackTrace != null) {
                append(stackTrace).append('\n')
            }
        }
        val lineSize = logLine.toByteArray(Charsets.UTF_8).size.toLong()

        executor.execute {
            try {
                val dir = logDir ?: return@execute
                ensureWriter(dir)
                val writer = fileWriter ?: return@execute
                writer.write(logLine)
                writer.flush()
                currentFileSize += lineSize
                if (currentFileSize >= MAX_FILE_SIZE_BYTES) {
                    closeWriter()
                    cleanUpLogs(dir)
                }
            } catch (e: Exception) {
                Log.e(TAG, "写入日志文件失败", e)
            }
        }
    }

    // 阻塞等待队列排空，供崩溃场景确保日志落盘；返回是否在超时内完成
    fun flushBlocking(timeoutMs: Long): Boolean {
        val latch = CountDownLatch(1)
        return try {
            executor.execute {
                try {
                    fileWriter?.flush()
                } catch (e: Exception) {
                    Log.e(TAG, "刷新日志缓冲失败", e)
                } finally {
                    latch.countDown()
                }
            }
            latch.await(timeoutMs, TimeUnit.MILLISECONDS)
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
            false
        } catch (e: Exception) {
            false
        }
    }

    private fun ensureWriter(dir: File) {
        val existing = currentFile
        if (fileWriter != null && existing != null && existing.exists()) return
        val file = nextLogFile(dir)
        currentFile = file
        fileWriter = FileWriter(file, true)
        currentFileSize = file.length()
    }

    // 同一秒内触发轮转时追加序号，避免 append 回已写满的同名文件
    internal fun nextLogFile(dir: File): File {
        val base = LOG_FILE_PREFIX + fileDateFormat.format(Date())
        var candidate = File(dir, base + LOG_FILE_SUFFIX)
        var index = 1
        while (candidate.exists() && candidate.length() >= MAX_FILE_SIZE_BYTES) {
            candidate = File(dir, "${base}_$index$LOG_FILE_SUFFIX")
            index++
        }
        return candidate
    }

    private fun closeWriter() {
        try {
            fileWriter?.close()
        } catch (e: Exception) {
            Log.e(TAG, "关闭日志写入器失败", e)
        } finally {
            fileWriter = null
            currentFile = null
            currentFileSize = 0L
        }
    }

    private fun isLogFile(name: String): Boolean =
        name.startsWith(LOG_FILE_PREFIX) && name.endsWith(LOG_FILE_SUFFIX)

    internal fun cleanUpLogs(dir: File) {
        val activeFileName = currentFile?.name

        // 超期清理
        val files = dir.listFiles { _, name -> isLogFile(name) } ?: return
        val expireBefore = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(MAX_FILE_AGE_DAYS)
        for (file in files) {
            if (file.name == activeFileName) continue
            if (file.lastModified() < expireBefore) {
                file.delete()
            }
        }

        // 目录配额：按最后修改时间 FIFO 淘汰
        val remaining = dir.listFiles { _, name -> isLogFile(name) }
            ?.sortedBy { it.lastModified() } ?: return
        var totalSize = remaining.sumOf { it.length() }
        for (file in remaining) {
            if (totalSize <= MAX_DIRECTORY_SIZE_BYTES) break
            if (file.name == activeFileName) continue
            val size = file.length()
            if (file.delete()) {
                totalSize -= size
            }
        }
    }

    // 已初始化时复用 logDir，避免路径出现第二个真相源
    private fun resolveLogDir(context: Context): File =
        logDir ?: File(context.filesDir, LOG_DIR_NAME)

    fun getLogFiles(context: Context): List<File> {
        return try {
            val dir = resolveLogDir(context)
            if (!dir.exists()) return emptyList()
            dir.listFiles { _, name -> isLogFile(name) }
                ?.sortedByDescending { it.lastModified() }
                ?: emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "读取日志文件列表失败", e)
            emptyList()
        }
    }

    fun getTotalLogSize(context: Context): Long = getLogFiles(context).sumOf { it.length() }

    fun clearAllLogs(context: Context) {
        val dir = resolveLogDir(context)
        executor.execute {
            try {
                closeWriter()
                if (dir.exists()) {
                    dir.listFiles()?.forEach { it.delete() }
                }
            } catch (e: Exception) {
                Log.e(TAG, "清空日志文件失败", e)
            }
        }
    }
}
