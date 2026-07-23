package com.example.nhviewer.util

import java.util.concurrent.TimeUnit

/**
 * 相对时间格式化工具类
 */
object RelativeTimeFormatter {

    /**
     * 将毫秒时间戳转换为相对时间字符串
     */
    fun format(timestamp: Long, now: Long = System.currentTimeMillis()): String {
        val diffMillis = now - timestamp
        if (diffMillis < 0) return "刚刚"

        val minutes = TimeUnit.MILLISECONDS.toMinutes(diffMillis)
        val hours = TimeUnit.MILLISECONDS.toHours(diffMillis)
        val days = TimeUnit.MILLISECONDS.toDays(diffMillis)

        return when {
            minutes < 1 -> "刚刚"
            minutes < 60 -> "${minutes} 分钟前"
            hours < 24 -> "${hours} 小时前"
            days < 2 -> "昨天"
            days < 30 -> "${days} 天前"
            days < 365 -> "${days / 30} 个月前"
            else -> "${days / 365} 年前"
        }
    }
}
