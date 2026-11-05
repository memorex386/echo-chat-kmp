package com.echochat.core.data

import com.echochat.core.model.Image
import io.github.vinceglb.filekit.PlatformFile

/** Keeps the pictures attached to messages in the app's own storage. */
interface ImageStore {
    /** Copies a photo the user picked, so it stays readable after the picker's access ends. */
    suspend fun import(photo: PlatformFile): Image

    /** A new, empty file for the camera app to save a photo into. */
    fun newCameraShot(): CameraShot

    /** Saves a copy of [image] to the phone's photos. Returns whether it worked. */
    suspend fun saveToPhotos(image: Image): Boolean

    /** Saves a copy of [image] at [destination], a file chosen in a Save dialog. Returns whether it worked. */
    suspend fun saveCopy(image: Image, destination: PlatformFile): Boolean

    /** The file that holds [image], for showing or copying it. */
    fun fileFor(image: Image): PlatformFile
}

/** This platform's own store. */
internal expect fun platformImageStore(): ImageStore

/** Where the camera app writes the photo ([file]), and how the chat then shows it ([image]). */
data class CameraShot(
    val file: PlatformFile,
    val image: Image,
)
