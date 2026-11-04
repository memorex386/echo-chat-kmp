package com.echochat.core.data

import com.echochat.core.model.Image
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.absolutePath
import io.github.vinceglb.filekit.cacheDir
import io.github.vinceglb.filekit.copyTo
import io.github.vinceglb.filekit.createDirectories
import io.github.vinceglb.filekit.div
import kotlin.uuid.Uuid

/**
 * Keeps pictures in the app's cache folder. On Android and iOS that folder is private to
 * the app; on desktop it is in the user's own app-data folder.
 */
class CacheImageStore : ImageStore {

    override suspend fun import(photo: PlatformFile): Image {
        val copy = newFile()
        photo copyTo copy
        return Image(copy.absolutePath())
    }

    // The camera app saves straight into the store, so its photo needs no copy.
    override fun newCameraShot(): CameraShot {
        val file = newFile()
        return CameraShot(file, Image(file.absolutePath()))
    }

    private fun newFile(): PlatformFile {
        val directory = FileKit.cacheDir / "images"
        directory.createDirectories()
        return directory / Uuid.random().toString()
    }
}
