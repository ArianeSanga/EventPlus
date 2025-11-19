package com.arianesanga.event.utils

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

object FileStorage {

    fun saveEventImageCopy(context: Context, uri: Uri): String? {
        return try {
            val input = context.contentResolver.openInputStream(uri) ?: return null

            val dir = File(context.filesDir, "event_images")
            if (!dir.exists()) dir.mkdirs()

            val file = File(dir, "event_${System.currentTimeMillis()}.jpg")

            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }

            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}