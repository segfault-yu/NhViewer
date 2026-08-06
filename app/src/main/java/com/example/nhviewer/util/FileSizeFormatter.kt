package com.example.nhviewer.util

import java.util.Locale
import kotlin.math.log10
import kotlin.math.pow

/**
 * 文件体积格式化工具类
 */
object FileSizeFormatter {

    private val UNITS = listOf("B", "KB", "MB", "GB")

    /**
     * 将字节数转换为带单位的可读字符串
     */
    fun format(bytes: Long): String {
        if (bytes <= 0) return "0.0 B"
        // 上限取末位单位，避免超大数值越界
        val digitGroups = log10(bytes.toDouble()).div(log10(1024.0)).toInt()
            .coerceIn(0, UNITS.lastIndex)
        val value = bytes / 1024.0.pow(digitGroups.toDouble())
        return String.format(Locale.US, "%.1f %s", value, UNITS[digitGroups])
    }
}
