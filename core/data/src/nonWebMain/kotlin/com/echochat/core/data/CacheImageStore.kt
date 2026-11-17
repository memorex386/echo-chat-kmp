package com.echochat.core.data

import com.echochat.core.model.Image
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.absolutePath
import io.github.vinceglb.filekit.cacheDir
import io.github.vinceglb.filekit.copyTo
import io.github.vinceglb.filekit.createDirectories
import io.github.vinceglb.filekit.delete
import io.github.vinceglb.filekit.div
import io.github.vinceglb.filekit.exists
import io.github.vinceglb.filekit.extension
import io.github.vinceglb.filekit.list
import kotlin.time.Clock
import kotlin.uuid.Uuid

internal actual fun platformImageStore(): ImageStore = CacheImageStore()

/**
 * Keeps pictures in [folder], by default in the app's cache folder. On Android and iOS that folder
 * is private to the app; on desktop it is in the user's own app-data folder. A web page has no
 * folders, so this serves every platform but the web.
 */
class CacheImageStore(private val folder: PlatformFile = FileKit.cacheDir / "images") : ImageStore {

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
        runCatching { fileFor(image) copyTo destination }.isSuccess

    override fun fileFor(image: Image): PlatformFile = PlatformFile(image.path)

    /**
     * Deletes every photo in the store. Only for when nothing can still show them: as desktop and iOS
     * start, since messages last only as long as the app and neither brings back a draft (PHOTO-08).
     */
    suspend fun clear() {
        if (folder.exists()) folder.list().forEach { it.delete() }
    }

    // A real extension lets other apps tell the picture's type when it is copied or saved.
    private fun newFile(extension: String): PlatformFile {
        folder.createDirectories()
        return folder / "${Uuid.random()}.$extension"
    }
}

/** Saves a copy of the picture at [path] in the phone's photos, named [name]. Returns whether it worked. */
internal expect suspend fun saveToPhotoLibrary(path: String, name: String): Boolean
