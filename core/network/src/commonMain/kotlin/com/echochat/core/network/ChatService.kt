package com.echochat.core.network

import com.echochat.core.model.Message

/** The other end of the conversation. A real app would call its chat server here. */
interface ChatService {
    /** Delivers [message] and suspends until the reply comes back. */
    suspend fun send(message: Message): Reply
}

data class Reply(
    val text: String,
)
