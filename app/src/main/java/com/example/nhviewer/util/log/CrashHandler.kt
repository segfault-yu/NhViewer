package com.example.nhviewer.util.log

class CrashHandler(
    private val defaultHandler: Thread.UncaughtExceptionHandler?
) : Thread.UncaughtExceptionHandler {

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        try {
            AppLogger.e("FATAL_CRASH", "线程 ${thread.name} 发生未捕获异常", throwable)
            // 进程随后即被终止，异步写盘来不及完成，必须阻塞等待
            AppLogger.flushBlocking(FLUSH_TIMEOUT_MS)
        } catch (e: Throwable) {
            // 崩溃处理自身绝不能再抛出，否则原始崩溃信息会被覆盖
        } finally {
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }

    companion object {
        private const val FLUSH_TIMEOUT_MS = 2000L

        fun init() {
            val current = Thread.getDefaultUncaughtExceptionHandler()
            // 重复初始化会让处理器层层嵌套，每层都阻塞等待落盘
            if (current is CrashHandler) return
            Thread.setDefaultUncaughtExceptionHandler(CrashHandler(current))
        }
    }
}
