package com.echochat.core.data

import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.createDirectories
import io.github.vinceglb.filekit.delete
import io.github.vinceglb.filekit.div
import io.github.vinceglb.filekit.exists
import io.github.vinceglb.filekit.isDirectory
import io.github.vinceglb.filekit.list
import io.github.vinceglb.filekit.readBytes
import io.github.vinceglb.filekit.write
import kotlinx.coroutines.test.runTest
import kotlinx.io.files.SystemTemporaryDirectory
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

// The real store, on the JVM and the iOS simulator, in a folder of its own instead of the app's cache.
class CacheImageStoreTest {

    private val root = PlatformFile(SystemTemporaryDirectory) / "echo-test-${Uuid.random()}"
    private val folder = root / "images"
    private val store = CacheImageStore(folder)

    @BeforeTest
    fun setUp() {
        root.createDirectories()
    }

    @AfterTest
    fun tearDown() = runTest { deleteTree(root) }

    @Test
    fun `PHOTO-01 a picked photo is copied into the store as a file of the same kind`() = runTest {
        val picked = photoOutsideTheStore("sunset.png")

        val image = store.import(picked)

        assertContentEquals(Photo, store.fileFor(image).readBytes())
        assertTrue(image.path.endsWith(".png"), "Other apps tell a picture's kind by its extension")
        assertEquals(1, folder.list().size)
        assertTrue(picked.exists(), "The picked photo is copied, not moved")
    }

    @Test
    fun `PHOTO-01 the camera saves its photo straight into the store`() = runTest {
        val shot = store.newCameraShot()

        shot.file.write(Photo) // What the camera app does.

        assertContentEquals(Photo, store.fileFor(shot.image).readBytes())
    }

    // Desktop's Save dialog.
    @Test
    fun `ACTION-04 saving a copy where a Save dialog chose says whether it worked`() = runTest {
        val image = store.import(photoOutsideTheStore("sunset.jpg"))
        val pictures = (root / "Pictures").also { it.createDirectories() }

        assertTrue(store.saveCopy(image, pictures / "sunset.jpg"))
        assertContentEquals(Photo, (pictures / "sunset.jpg").readBytes())

        assertFalse(store.saveCopy(image, root / "Gone" / "sunset.jpg"))
    }

    @Test
    fun `PHOTO-08 clearing removes the store's photos and nothing else`() = runTest {
        val picked = photoOutsideTheStore("sunset.jpg")
        val image = store.import(picked)
        val shot = store.newCameraShot().also { it.file.write(Photo) }

        store.clear()

        assertFalse(store.fileFor(image).exists())
        assertFalse(store.fileFor(shot.image).exists())
        assertTrue(picked.exists(), "Only the store's own folder is cleared")
        assertContentEquals(Photo, store.fileFor(store.import(picked)).readBytes(), "The store still works after")
    }

    // The app's first start: no photo has made the folder yet.
    @Test
    fun `PHOTO-08 clearing a store that has no photos yet does nothing`() = runTest {
        store.clear()

        assertFalse(folder.exists())
    }

    private suspend fun photoOutsideTheStore(name: String): PlatformFile = (root / name).also { it.write(Photo) }

    private suspend fun deleteTree(file: PlatformFile) {
        if (file.isDirectory()) file.list().forEach { deleteTree(it) }
        if (file.exists()) file.delete()
    }

    private companion object {
        // The store copies files without reading them as pictures, so any bytes will do.
        val Photo = "Not really a photo".encodeToByteArray()
    }
}
