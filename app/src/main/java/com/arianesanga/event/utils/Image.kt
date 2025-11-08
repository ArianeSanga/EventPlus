package com.arianesanga.event.utils

import android.content.Context
import android.net.Uri
import java.io.File

object Image {

    fun saveImageLocally(context: Context, uri: Uri, userUid: String): String {
        val file = File(context.filesDir, "$userUid.jpg")
        context.contentResolver.openInputStream(uri)?.use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        return file.absolutePath
    }
}