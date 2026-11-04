package com.echochat.feature.chat

import com.echochat.core.model.Message

data class ChatUiState(
    val messages: List<Message> = emptyList(),
    val isReplying: Boolean = false,
)
