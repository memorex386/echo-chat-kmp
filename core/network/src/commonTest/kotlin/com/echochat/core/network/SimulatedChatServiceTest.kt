package com.echochat.core.network

import com.echochat.core.model.Author
import com.echochat.core.model.Image
import com.echochat.core.model.Message
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SimulatedChatServiceTest {

    private val service = SimulatedChatService()
    private val sunset = Image("/cache/images/sunset.jpg")

    @Test
    fun `PHOTO-04 the reply to a captioned photo carries the photo and echoes the caption`() = runTest {
        val reply = service.send(Message(id = 1, author = Author.Me, text = "Sunset", image = sunset))

        assertEquals(Reply(text = "You said: Sunset", image = sunset), reply)
    }

    @Test
    fun `PHOTO-04 the reply to a photo without a caption says you sent a photo`() = runTest {
        val reply = service.send(Message(id = 1, author = Author.Me, text = "", image = sunset))

        assertEquals(Reply(text = "You sent a photo", image = sunset), reply)
    }
}
