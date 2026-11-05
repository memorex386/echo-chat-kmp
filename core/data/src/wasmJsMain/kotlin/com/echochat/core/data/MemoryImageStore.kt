package com.echochat.core.data

import com.echochat.core.model.Image
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.download
import io.github.vinceglb.filekit.extension
import kotlin.time.Clock
import kotlin.uuid.Uuid

internal actual fun platformImageStore(): ImageStore = MemoryImageStore()

/**
 * Keeps pictures in memory, as the messages are (D10): a web page has no folders of its own.
 * Each picture is the file the browser picked, which it keeps readable while the page is open,
 * under a key of its own. Saving one has the browser download it.
 */
class MemoryImageStore(
    private val download: suspend (file: PlatformFile, name: String) -> Unit = { file, name -> FileKit.download(file, name) },
) : ImageStore {

    private val photos = mutableMapOf<String, PlatformFile>()

    // The key keeps the photo's extension, as the cache's file names do, so a download gets it too.
    override suspend fun import(photo: PlatformFile): Image {
        val key = "${Uuid.random()}.${photo.extension.ifEmpty { "jpg" }}"
        photos[key] = photo
        return Image(key)
    }

    override fun newCameraShot(): CameraShot = error("A browser has no camera app to hand off to")

    override suspend fun saveToPhotos(image: Image): Boolean = runCatching {
        val name = "Echo Chat ${Clock.System.now().toEpochMilliseconds()}.${image.path.substringAfterLast('.')}"
        download(fileFor(image), name)
    }.isSuccess

    // A browser saves by downloading, so it has no Save dialog to save into.
    override suspend fun saveCopy(image: Image, destination: PlatformFile): Boolean = false

    override fun fileFor(image: Image): PlatformFile = photos.getValue(image.path)
}
