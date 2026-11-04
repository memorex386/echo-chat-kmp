package com.echochat.core.data

import com.echochat.core.model.Image
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.absolutePath
import io.github.vinceglb.filekit.cacheDir
import io.github.vinceglb.filekit.copyTo
import io.github.vinceglb.filekit.createDirectories
import io.github.vinceglb.filekit.div
import io.github.vinceglb.filekit.extension
import kotlin.time.Clock
import kotlin.uuid.Uuid

/**
 * Keeps pictures in the app's cache folder. On Android and iOS that folder is private to
 * the app; on desktop it is in the user's own app-data folder.
 */
class CacheImageStore : ImageStore {

    override suspend fun import(photo: PlatformFile): Image {
        val copy = newFile(photo.extension.ifEmpty { "jpg" })
        photo copyTo copy
        return Image(copy.absolutePath())
    }

    // The camera app saves straight into the store, so its photo needs no copy.
    override fun newCameraShot(): CameraShot {
        val file = newFile("jpg")
        return CameraShot(file, Image(file.absolutePath()))
    }

    override suspend fun saveToPhotos(image: Image): Boolean =
        runCatching { saveToPhotoLibrary(image.path, "Echo Chat ${Clock.System.now().toEpochMilliseconds()}") }
            .getOrDefault(false)

    override suspend fun saveCopy(image: Image, destination: PlatformFile): Boolean =
        runCatching { PlatformFile(image.path) copyTo destination }.isSuccess

    // A real extension lets other apps tell the picture's type when it is copied or saved.
    private fun newFile(extension: String): PlatformFile {
        val directory = FileKit.cacheDir / "images"
        directory.createDirectories()
        return directory / "${Uuid.random()}.$extension"
    }
}

/** Saves a copy of the picture at [path] in the phone's photos, named [name]. Returns whether it worked. */
internal expect suspend fun saveToPhotoLibrary(path: String, name: String): Boolean
