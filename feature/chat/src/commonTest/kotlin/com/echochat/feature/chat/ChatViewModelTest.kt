package com.echochat.feature.chat

import androidx.lifecycle.SavedStateHandle
import com.echochat.core.testing.TestChatRepository
import com.echochat.core.testing.TestChatRepository.Sent
import com.echochat.core.testing.TestImageStore
import com.echochat.core.testing.TestNotifier
import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModelTest {

    private val chatRepository = TestChatRepository()
    private val savedState = SavedStateHandle()
    private val pickedPhoto = PlatformFile("/picker/1.jpg")
    private lateinit var viewModel: ChatViewModel

    @BeforeTest
    fun setUp() {
        // viewModelScope runs on Dispatchers.Main, which a test has to supply on every platform.
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = newViewModel(savedState)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun newViewModel(savedState: SavedStateHandle) =
        ChatViewModel(savedState, chatRepository, TestImageStore(), TestNotifier())

    /** What a new ViewModel gets after Android ends the app: only the values the old one saved. */
    private fun afterAndroidEndsTheApp() =
        newViewModel(SavedStateHandle(savedState.keys().associateWith { savedState.get<Any?>(it) }))

    @Test
    fun `CHAT-15 a half-written message survives Android ending the app`() {
        viewModel.onDraftChange("Half a mess")

        assertEquals("Half a mess", afterAndroidEndsTheApp().draft)
    }

    @Test
    fun `CHAT-15 an attached photo survives Android ending the app`() {
        viewModel.onPhotoPicked(pickedPhoto)

        assertEquals(TestImageStore.Imported, afterAndroidEndsTheApp().attachment)
    }

    @Test
    fun `CHAT-15 a photo the camera is still taking is attached after Android ends the app`() {
        viewModel.startCamera()

        val restored = afterAndroidEndsTheApp()
        restored.onCameraResult(saved = true)

        assertEquals(TestImageStore.Shot.image, restored.attachment)
    }

    @Test
    fun `CHAT-15 nothing is restored once the message is sent`() {
        viewModel.onDraftChange("Sunset")
        viewModel.onPhotoPicked(pickedPhoto)
        viewModel.send()

        val restored = afterAndroidEndsTheApp()
        assertEquals("", restored.draft)
        assertNull(restored.attachment)
    }

    @Test
    fun `CHAT-02 whitespace alone can't be sent`() {
        viewModel.onDraftChange("   ")
        assertFalse(viewModel.canSend)

        viewModel.onDraftChange("Hi")
        assertTrue(viewModel.canSend)
    }

    @Test
    fun `CHAT-03 sending clears the draft and hands the text to the repository`() {
        viewModel.onDraftChange("Hello")

        viewModel.send()

        assertEquals("", viewModel.draft)
        assertEquals(listOf(Sent("Hello")), chatRepository.sent)
    }

    @Test
    fun `PHOTO-01 a picked photo is attached as the app's own copy and can be sent without text`() {
        viewModel.onPhotoPicked(pickedPhoto)

        assertEquals(TestImageStore.Imported, viewModel.attachment)
        assertTrue(viewModel.canSend)
    }

    @Test
    fun `PHOTO-01 a camera photo is saved straight into the store and attached only once the camera has saved it`() {
        assertEquals(TestImageStore.Shot.file, viewModel.startCamera())
        viewModel.onCameraResult(saved = false)
        assertNull(viewModel.attachment)

        viewModel.startCamera()
        viewModel.onCameraResult(saved = true)
        assertEquals(TestImageStore.Shot.image, viewModel.attachment)
    }

    @Test
    fun `PHOTO-02 removing the photo drops it`() {
        viewModel.onPhotoPicked(pickedPhoto)

        viewModel.removeAttachment()

        assertNull(viewModel.attachment)
        assertFalse(viewModel.canSend)
    }

    @Test
    fun `PHOTO-03 sending a photo clears the preview and sends the photo with its caption`() {
        viewModel.onDraftChange("Sunset")
        viewModel.onPhotoPicked(pickedPhoto)

        viewModel.send()

        assertEquals("", viewModel.draft)
        assertNull(viewModel.attachment)
        assertEquals(listOf(Sent("Sunset", TestImageStore.Imported)), chatRepository.sent)
    }
}
