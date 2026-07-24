package com.example.nhviewer.util.log

import android.content.Context
import android.util.Log
import com.example.nhviewer.BuildConfig
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

object AppLogger {
    private const val TAG = "AppLogger"
    private const val MAX_FILE_SIZE_BYTES = 2 * 1024 * 1024L // 2MB
    private const val MAX_DIRECTORY_SIZE_BYTES = 20 * 1024 * 1024L // 20MB
    private const val MAX_FILE_AGE_DAYS = 5L

    private val executor = Executors.newSingleThreadExecutor()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)
    private val fileDateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)

    private val sensitivePatterns = listOf(
        Pattern.compile("(?i)(authorization:\\s*bearer\\s+)[^\\s,]+"),
        Pattern.compile("(?i)(cookie:\\s*)[^\\r\\n]+")
    )

    private var logDir: File? = null
    private var currentFile: File? = null
    private var fileWriter: FileWriter? = null
    private var currentFileSize: Long = 0L

    @Volatile
    private var minLogLevelRank: Int = 3 // 0: NONE, 1: ERROR, 2: WARN, 3: INFO, 4: DEBUG

    fun setLogLevel(level: String) {
        minLogLevelRank = when (level.lowercase()) {
            "none" -> 0
            "error" -> 1
            "warn" -> 2
            "debug" -> 4
            else -> 3 // "info"
        }
    }

    fun init(context: Context) {
        executor.execute {
            try {
                val dir = File(context.filesDir, "logs")
                if (!dir.exists()) {
                    dir.mkdirs()
                }
                logDir = dir
                cleanUpLogsInternal(dir)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize log directory", e)
            }
        }
    }

    fun d(tag: String, message: String) {
        if (BuildConfig.DEBUG && minLogLevelRank >= 4) {
            Log.d(tag, message)
            writeLog("DEBUG", tag, message, null)
        }
    }

    fun i(tag: String, message: String) {
        Log.i(tag, message)
        if (minLogLevelRank >= 3) {
            writeLog("INFO", tag, message, null)
        }
    }

    fun w(tag: String, message: String, throwable: Throwable? = null) {
        Log.w(tag, message, throwable)
        if (minLogLevelRank >= 2) {
            writeLog("WARN", tag, message, throwable)
        }
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        Log.e(tag, message, throwable)
        if (minLogLevelRank >= 1) {
            writeLog("ERROR", tag, message, throwable)
        }
    }

    private fun writeLog(level: String, tag: String, message: String, throwable: Throwable?) {
        val sanitizedMessage = message
        val timestamp = dateFormat.format(Date())
        val logLine = buildString {
            append("$timestamp [$level] [$tag]: $sanitizedMessage\n")
            throwable?.let {
                append(Log.getStackTraceString(it))
                append("\n")
            }
        }

        executor.execute {
            try {
                val dir = logDir ?: return@execute
                ensureWriter(dir)
                fileWriter?.let { writer ->
                    writer.write(logLine)
                    writer.flush()
                    currentFileSize += logLine.toByteArray(Charsets.UTF_8).size
                    if (currentFileSize >= MAX_FILE_SIZE_BYTES) {
                        closeWriter()
                        cleanUpLogsInternal(dir)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error writing log to file", e)
            }
        }
    }

    private fun ensureWriter(dir: File) {
        if (fileWriter == null || currentFile == null || !currentFile!!.exists()) {
            val fileName = "app_log_${fileDateFormat.format(Date())}.log"
            val file = File(dir, fileName)
            currentFile = file
            fileWriter = FileWriter(file, true)
            currentFileSize = file.length()
        }
    }

    private fun closeWriter() {
        try {
            fileWriter?.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error closing log writer", e)
        } finally {
            fileWriter = null
            currentFile = null
            currentFileSize = 0L
        }
    }

    private fun cleanUpLogsInternal(dir: File) {
        val files = dir.listFiles { _, name -> name.startsWith("app_log_") && name.endsWith(".log") }
            ?: return

        val now = System.currentTimeMillis()
        val maxAgeMs = TimeUnit.DAYS.toMillis(MAX_FILE_AGE_DAYS)

        val activeFileName = currentFile?.name

        // 1. Delete files older than MAX_FILE_AGE_DAYS
        for (file in files) {
            if (file.name == activeFileName) continue
            if (now - file.lastModified() > maxAgeMs) {
                file.delete()
            }
        }

        // 2. FIFO quota check (<= 20MB)
        val remainingFiles = dir.listFiles { _, name -> name.startsWith("app_log_") && name.endsWith(".log") }
            ?.sortedBy { it.lastModified() } ?: return

        var totalSize = remainingFiles.sumOf { it.length() }
        for (file in remainingFiles) {
            if (totalSize <= MAX_DIRECTORY_SIZE_BYTES) break
            if (file.name == activeFileName) continue
            val size = file.length()
            if (file.delete()) {
                totalSize -= size
            }
        }
    }

    fun getLogFiles(context: Context): List<File> {
        val dir = File(context.filesDir, "logs")
        if (!dir.exists()) return emptyList()
        return dir.listFiles { _, name -> name.startsWith("app_log_") && name.endsWith(".log") }
            ?.sortedByDescending { it.lastModified() }
            ?: emptyList()
    }

    fun getTotalLogSize(context: Context): Long {
        return getLogFiles(context).sumOf { it.length() }
    }

    fun clearAllLogs(context: Context) {
        executor.execute {
            try {
                closeWriter()
                val dir = File(context.filesDir, "logs")
                if (dir.exists()) {
                    dir.listFiles()?.forEach { it.delete() }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error clearing log files", e)
            }
        }
    }
}
