package com.echochat.feature.chat

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.echochat.core.data.CameraShot
import com.echochat.core.data.ChatRepository
import com.echochat.core.data.ImageStore
import com.echochat.core.model.Image
import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChatViewModel(
    private val chatRepository: ChatRepository,
    private val imageStore: ImageStore,
) : ViewModel() {

    val uiState: StateFlow<ChatUiState> =
        combine(chatRepository.messages, chatRepository.isReplying, ::ChatUiState)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ChatUiState())

    // Compose state rather than a flow: a text field has to see its own edits straight away.
    var draft by mutableStateOf("")
        private set

    var attachment by mutableStateOf<Image?>(null)
        private set

    val canSend: Boolean
        get() = draft.isNotBlank() || attachment != null

    private var cameraShot: CameraShot? = null

    fun onDraftChange(text: String) {
        draft = text
    }

    fun onPhotoPicked(photo: PlatformFile) {
        viewModelScope.launch { attachment = imageStore.import(photo) }
    }

    /** Returns where the camera app should save the next photo. */
    fun startCamera(): PlatformFile = imageStore.newCameraShot().also { cameraShot = it }.file

    fun onCameraResult(saved: Boolean) {
        if (saved) attachment = cameraShot?.image
        cameraShot = null
    }

    fun removeAttachment() {
        attachment = null
    }

    fun send() {
        if (!canSend) return
        val text = draft
        val image = attachment
        draft = ""
        attachment = null
        viewModelScope.launch { chatRepository.send(text, image) }
    }
}
