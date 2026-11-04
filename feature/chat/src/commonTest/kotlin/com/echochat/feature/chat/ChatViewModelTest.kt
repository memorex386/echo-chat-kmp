package com.echochat.feature.chat

import com.echochat.core.testing.TestChatRepository
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
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModelTest {

    private val chatRepository = TestChatRepository()
    private lateinit var viewModel: ChatViewModel

    @BeforeTest
    fun setUp() {
        // viewModelScope runs on Dispatchers.Main, which a test has to supply on every platform.
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = ChatViewModel(chatRepository)
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
        assertEquals(listOf("Hello"), chatRepository.sent)
    }
}
