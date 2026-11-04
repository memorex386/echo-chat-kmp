package com.echochat.core.data

import android.content.ContentValues
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

// Into Pictures/Echo Chat, through MediaStore, as the original did.
internal actual suspend fun saveToPhotoLibrary(path: String, name: String): Boolean = withContext(Dispatchers.IO) {
    // Before Android 10 this would need storage permission, so Save isn't offered there.
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return@withContext false
    val source = File(path)
    val resolver = FileKit.context.contentResolver
    val details = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, "$name.${source.extension}")
        put(MediaStore.Images.Media.MIME_TYPE, MimeTypeMap.getSingleton().getMimeTypeFromExtension(source.extension))
        put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/Echo Chat")
        put(MediaStore.Images.Media.IS_PENDING, 1)
    }
    val target = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, details) ?: return@withContext false
    runCatching {
        resolver.openOutputStream(target).use { output -> source.inputStream().use { it.copyTo(requireNotNull(output)) } }
        resolver.update(target, ContentValues().apply { put(MediaStore.Images.Media.IS_PENDING, 0) }, null, null)
    }.onFailure {
        resolver.delete(target, null, null)
    }.isSuccess
}
