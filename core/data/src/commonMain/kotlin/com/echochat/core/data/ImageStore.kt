package com.echochat.core.data

import com.echochat.core.model.Image
import io.github.vinceglb.filekit.PlatformFile

/** Keeps the pictures attached to messages in the app's own storage. */
interface ImageStore {
    /** Copies a photo the user picked, so it stays readable after the picker's access ends. */
    suspend fun import(photo: PlatformFile): Image

    /** A new, empty file for the camera app to save a photo into. */
    fun newCameraShot(): CameraShot
}

/** Where the camera app writes the photo ([file]), and how the chat then shows it ([image]). */
data class CameraShot(
    val file: PlatformFile,
    val image: Image,
)
