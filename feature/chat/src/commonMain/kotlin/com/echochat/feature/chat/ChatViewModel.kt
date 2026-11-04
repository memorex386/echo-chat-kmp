package com.echochat.feature.chat

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.echochat.core.data.CameraShot
import com.echochat.core.data.ChatRepository
import com.echochat.core.data.ImageStore
import com.echochat.core.model.Image
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.absolutePath
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChatViewModel(
    private val savedState: SavedStateHandle,
    private val chatRepository: ChatRepository,
    private val imageStore: ImageStore,
) : ViewModel() {

    val uiState: StateFlow<ChatUiState> =
        combine(chatRepository.messages, chatRepository.isReplying, ::ChatUiState)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ChatUiState())

    // Compose state rather than a flow: a text field has to see its own edits straight away.
    // Every change is also written to savedState, so unsent work survives Android ending the app.
    private val draftState = mutableStateOf(savedState.get<String>(DraftKey).orEmpty())
    private val attachmentState = mutableStateOf(savedState.get<String>(AttachmentKey)?.let(::Image))

    var draft: String
        get() = draftState.value
        private set(value) {
            draftState.value = value
            savedState[DraftKey] = value
        }

    var attachment: Image?
        get() = attachmentState.value
        private set(value) {
            attachmentState.value = value
            savedState[AttachmentKey] = value?.path
        }

    val canSend: Boolean
        get() = draft.isNotBlank() || attachment != null

    // Android often ends the app while the camera app is open, so the shot in progress is saved too.
    private var cameraShot: CameraShot?
        get() = savedState.get<String>(CameraFileKey)?.let { file ->
            savedState.get<String>(CameraImageKey)?.let { image -> CameraShot(PlatformFile(file), Image(image)) }
        }
        set(value) {
            savedState[CameraFileKey] = value?.file?.absolutePath()
            savedState[CameraImageKey] = value?.image?.path
        }

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

// Only plain strings are saved, which every platform can keep.
private const val DraftKey = "draft"
private const val AttachmentKey = "attachment"
private const val CameraFileKey = "camera.file"
private const val CameraImageKey = "camera.image"
