package org.zxcslauri.brightnessfix

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Logger {
    private const val TAG = "BrightnessFix"

    private fun write(
        context: Context,
        level: String,
        message: String,
        throwable: Throwable? = null
    ) {
        val timestamp = SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss.SSS",
            Locale.getDefault()
        ).format(Date())

        val line = "$timestamp [$level] $message"

        try {
            when (level) {
                "D" -> Log.d(TAG, message, throwable)
                "I" -> Log.i(TAG, message, throwable)
                "W" -> Log.w(TAG, message, throwable)
                "E" -> Log.e(TAG, message, throwable)
            }
        } catch (_: Exception) {
        }

        try {
            val file = File(context.filesDir, "app.log")

            FileWriter(file, true).use { writer ->
                writer.append(line)
                writer.append('\n')

                if (throwable != null) {
                    writer.append(
                        Log.getStackTraceString(throwable)
                    )
                }
            }
        } catch (_: Exception){
        }
    }

    fun d(context: Context, message: String) {
        write(context, "D", message)
    }

    fun i(context: Context, message: String) {
        write(context, "I", message)
    }

    fun w(context: Context, message: String) {
        write(context, "W", message)
    }

    fun e(context: Context, message: String) {
        write(context, "E", message)
    }

    //Совместимость со старым API
    fun log(context: Context, message: String) {
        i(context, message)
    }
}