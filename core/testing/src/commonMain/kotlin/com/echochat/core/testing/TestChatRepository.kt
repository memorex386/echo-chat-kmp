package com.echochat.core.testing

import com.echochat.core.data.ChatRepository
import com.echochat.core.model.Image
import com.echochat.core.model.Message
import kotlinx.coroutines.flow.MutableStateFlow

/** Records what was sent and deleted, and lets a test set the messages a screen sees. */
class TestChatRepository : ChatRepository {
    override val messages = MutableStateFlow(emptyList<Message>())
    override val isReplying = MutableStateFlow(false)

    val sent = mutableListOf<Sent>()
    val deleted = mutableListOf<Long>()

    override suspend fun send(text: String, image: Image?) {
        sent += Sent(text, image)
    }

    override suspend fun delete(id: Long) {
        deleted += id
    }

    override suspend fun undoDelete(id: Long) {
        deleted -= id
    }

    data class Sent(val text: String, val image: Image? = null)
}
