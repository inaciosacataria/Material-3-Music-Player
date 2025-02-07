package com.omar.musica.settings.utils

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import com.omar.musica.settings.encriptation.SevenTAG
import java.io.File
import java.io.FileOutputStream

fun copyUriToFile(context: Context, uri: Uri): File? {
    try {
        val contentResolver = context.contentResolver
        val fileName = getFileNameFromUri(context, uri) ?: return null
        val tempFile = File(context.cacheDir, fileName)

        contentResolver.openInputStream(uri).use { inputStream ->
            FileOutputStream(tempFile).use { outputStream ->
                inputStream?.copyTo(outputStream)
            }
        }
        return tempFile
    } catch (e: Exception) {
        Log.e(SevenTAG, "Error copying file", e)
        return null
    }
}


fun getFileNameFromUri(context: Context, uri: Uri): String? {
    val cursor = context.contentResolver.query(uri, null, null, null, null)
    cursor?.use {
        if (it.moveToFirst()) {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex != -1) {
                return it.getString(nameIndex)
            }
        }
    }
    return null
}