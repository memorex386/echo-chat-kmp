package com.echochat.core.testing

import com.echochat.core.data.ChatRepository
import com.echochat.core.model.Message
import kotlinx.coroutines.flow.MutableStateFlow

/** Records what was sent, and lets a test set the messages a screen sees. */
class TestChatRepository : ChatRepository {
    override val messages = MutableStateFlow(emptyList<Message>())
    override val isReplying = MutableStateFlow(false)

    val sent = mutableListOf<String>()

    override suspend fun send(text: String) {
        sent += text
    }
}
