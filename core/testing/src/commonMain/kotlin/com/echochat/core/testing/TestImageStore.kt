package com.echochat.core.testing

import com.echochat.core.data.CameraShot
import com.echochat.core.data.ImageStore
import com.echochat.core.model.Image
import io.github.vinceglb.filekit.PlatformFile

/** Hands out fixed images instead of touching the file system, and records what was saved. */
class TestImageStore : ImageStore {
    /** What saving answers. */
    var saveWorks = true
    val saved = mutableListOf<Image>()

    override suspend fun import(photo: PlatformFile) = Imported

    override fun newCameraShot() = Shot

    override suspend fun saveToPhotos(image: Image): Boolean {
        if (saveWorks) saved += image
        return saveWorks
    }

    override suspend fun saveCopy(image: Image, destination: PlatformFile): Boolean {
        if (saveWorks) saved += image
        return saveWorks
    }

    companion object {
        val Imported = Image("/cache/images/imported.jpg")
        val Shot = CameraShot(
            file = PlatformFile("/cache/images/shot.jpg"),
            image = Image("/cache/images/shot.jpg"),
        )
    }
}
