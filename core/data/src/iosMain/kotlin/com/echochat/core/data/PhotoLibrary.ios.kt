package com.echochat.core.data

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.extension
import io.github.vinceglb.filekit.saveImageToGallery

// Into Photos. iOS asks once for permission to add photos, using the app's
// NSPhotoLibraryAddUsageDescription.
internal actual suspend fun saveToPhotoLibrary(path: String, name: String): Boolean {
    val photo = PlatformFile(path)
    return FileKit.saveImageToGallery(photo, "$name.${photo.extension}").isSuccess
}
