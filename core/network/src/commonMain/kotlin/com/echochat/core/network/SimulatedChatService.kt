package com.echochat.core.network

import com.echochat.core.model.Message
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

/**
 * Stands in for a chat server, because the brief asks for a simulated reply: Echo answers
 * every message with its echo, [ReplyDelay] after receiving it.
 */
class SimulatedChatService : ChatService {

    override suspend fun send(message: Message): Reply {
        delay(ReplyDelay)
        return Reply(text = "You said: ${message.text}")
    }

    companion object {
        val ReplyDelay = 5.seconds
    }
}
