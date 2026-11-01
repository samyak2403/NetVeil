package com.samyak.netveil.util

import android.content.Context
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

object LogUtils {
    private const val TAG = "LogUtils"

    suspend fun collectLogs(): String? = withContext(Dispatchers.IO) {
        try {
            val process = Runtime.getRuntime().exec(arrayOf("logcat", "*:D", "-d"))
            process.inputStream.bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to collect logs", e)
            null
        }
    }

    suspend fun saveLogsToUri(context: Context, uri: Uri, logs: String): Boolean = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openOutputStream(uri)?.use { output ->
                output.write(logs.toByteArray())
                true
            } ?: false
        } catch (e: IOException) {
            Log.e(TAG, "Failed to save logs to URI", e)
            false
        }
    }
}

