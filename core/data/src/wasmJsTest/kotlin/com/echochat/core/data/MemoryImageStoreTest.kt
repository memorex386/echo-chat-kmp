package com.echochat.core.data

import io.github.vinceglb.filekit.BrowserFile
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.WebFile
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

@OptIn(ExperimentalWasmJsInterop::class)
class MemoryImageStoreTest {

    private val downloads = mutableListOf<Pair<PlatformFile, String>>()
    private val store = MemoryImageStore(download = { file, name -> downloads += file to name })

    @Test
    fun `PHOTO-01 a picked photo is kept and found again by its image`() = runTest {
        val picked = browserFile("sunset.jpg")

        val image = store.import(picked)

        assertSame(picked, store.fileFor(image))
        assertTrue(image.path.endsWith(".jpg"), "The image keeps the photo's extension: ${image.path}")
    }

    @Test
    fun `PHOTO-01 two photos with the same name are kept apart`() = runTest {
        val first = browserFile("IMG_0001.jpg")
        val second = browserFile("IMG_0001.jpg")

        val one = store.import(first)
        val two = store.import(second)

        assertNotEquals(one, two)
        assertSame(first, store.fileFor(one))
        assertSame(second, store.fileFor(two))
    }

    @Test
    fun `ACTION-04 saving has the browser download the photo named Echo Chat and the time`() = runTest {
        val picked = browserFile("sunset.jpg")
        val image = store.import(picked)

        assertTrue(store.saveToPhotos(image))

        val (file, name) = downloads.single()
        assertSame(picked, file)
        assertTrue(Regex("""Echo Chat \d+\.jpg""").matches(name), "Downloaded as $name")
    }

    @Test
    fun `ACTION-04 a download that fails reports it`() = runTest {
        val failing = MemoryImageStore(download = { _, _ -> error("The browser refused") })
        val image = failing.import(browserFile("sunset.jpg"))

        assertFalse(failing.saveToPhotos(image))
    }

    private fun browserFile(name: String) = PlatformFile(WebFile.FileWrapper(BrowserFile(JsArray(), name)))
}
