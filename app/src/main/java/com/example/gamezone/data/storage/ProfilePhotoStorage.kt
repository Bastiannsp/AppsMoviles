package com.example.gamezone.data.storage

import android.content.Context
import android.graphics.Bitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class ProfilePhotoStorage(private val context: Context) {

    private val directory: File by lazy {
        File(context.filesDir, "profile_photos").apply { if (!exists()) mkdirs() }
    }

    suspend fun saveProfilePhoto(bitmap: Bitmap, userKey: String): String = withContext(Dispatchers.IO) {
        val sanitizedKey = userKey.replace(Regex("[^a-zA-Z0-9_]"), "_")
        val fileName = "profile_${sanitizedKey}_${System.currentTimeMillis()}.png"
        val outputFile = File(directory, fileName)
        FileOutputStream(outputFile).use { stream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 95, stream)
        }
        pruneOldPhotos(sanitizedKey, outputFile)
        outputFile.absolutePath
    }

    private fun pruneOldPhotos(sanitizedKey: String, keep: File) {
        directory.listFiles { file ->
            file.isFile && file != keep && file.name.startsWith("profile_${sanitizedKey}_")
        }?.forEach { it.delete() }
    }
}
