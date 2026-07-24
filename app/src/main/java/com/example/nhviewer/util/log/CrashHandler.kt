package com.example.nhviewer.util.log

class CrashHandler(
    private val defaultHandler: Thread.UncaughtExceptionHandler?
) : Thread.UncaughtExceptionHandler {

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        AppLogger.e("FATAL_CRASH", "Uncaught exception in thread ${thread.name}", throwable)
        defaultHandler?.uncaughtException(thread, throwable)
    }

    companion object {
        fun init() {
            val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
            Thread.setDefaultUncaughtExceptionHandler(CrashHandler(defaultHandler))
        }
    }
}
