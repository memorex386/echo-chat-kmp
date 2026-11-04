package com.echochat.core.testing

import com.echochat.core.data.CameraShot
import com.echochat.core.data.ImageStore
import com.echochat.core.model.Image
import io.github.vinceglb.filekit.PlatformFile

/** Hands out fixed images instead of touching the file system. */
class TestImageStore : ImageStore {
    override suspend fun import(photo: PlatformFile) = Imported

    override fun newCameraShot() = Shot

    companion object {
        val Imported = Image("/cache/images/imported.jpg")
        val Shot = CameraShot(
            file = PlatformFile("/cache/images/shot.jpg"),
            image = Image("/cache/images/shot.jpg"),
        )
    }
}
