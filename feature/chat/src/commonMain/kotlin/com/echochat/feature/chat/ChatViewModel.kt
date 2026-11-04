package com.echochat.feature.chat

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.echochat.core.data.ChatRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChatViewModel(
    private val chatRepository: ChatRepository,
) : ViewModel() {

    val uiState: StateFlow<ChatUiState> =
        combine(chatRepository.messages, chatRepository.isReplying, ::ChatUiState)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ChatUiState())

    // Compose state rather than a flow: a text field has to see its own edits straight away.
    var draft by mutableStateOf("")
        private set

    val canSend: Boolean
        get() = draft.isNotBlank()

    fun onDraftChange(text: String) {
        draft = text
    }

    fun send() {
        if (!canSend) return
        val text = draft
        draft = ""
        viewModelScope.launch { chatRepository.send(text) }
    }
}
