package com.echochat.feature.chat

import com.echochat.core.testing.TestChatRepository
import com.echochat.core.testing.TestChatRepository.Sent
import com.echochat.core.testing.TestImageStore
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
    private val pickedPhoto = PlatformFile("/picker/1.jpg")
    private lateinit var viewModel: ChatViewModel

    @BeforeTest
    fun setUp() {
        // viewModelScope runs on Dispatchers.Main, which a test has to supply on every platform.
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = ChatViewModel(chatRepository, TestImageStore())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
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
