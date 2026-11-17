package com.echochat.feature.chat

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.echochat.core.data.ChatRepository
import com.echochat.core.data.ImageStore
import com.echochat.core.model.Image
import com.echochat.core.model.Message
import com.echochat.core.notifications.Notifier
import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChatViewModel(
    private val savedState: SavedStateHandle,
    private val chatRepository: ChatRepository,
    private val imageStore: ImageStore,
    private val notifier: Notifier,
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

    // While a picked photo is being copied in, Send waits, so the photo goes with this message.
    private var importing by mutableStateOf(false)

    val canSend: Boolean
        get() = (draft.isNotBlank() || attachment != null) && !importing

    // Android often ends the app while the camera app is open, so the photo in progress is saved too.
    private var cameraImage: Image?
        get() = savedState.get<String>(CameraImageKey)?.let(::Image)
        set(value) {
            savedState[CameraImageKey] = value?.path
        }

    fun onDraftChange(text: String) {
        draft = text
    }

    fun onChatShown() = notifier.onChatShown()

    fun onChatHidden() = notifier.onChatHidden()

    fun onPhotoPicked(photo: PlatformFile) {
        importing = true
        viewModelScope.launch {
            try {
                attachment = imageStore.import(photo)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // A cloud photo while offline, or a full disk. Left uncaught, it would end the app.
                pendingEvents.send(ChatEvent.PhotoNotAttached)
            } finally {
                importing = false
            }
        }
    }

    /** Returns where the camera app should save the next photo. */
    fun startCamera(): PlatformFile = imageStore.newCameraShot().also { cameraImage = it.image }.file

    fun onCameraResult(saved: Boolean) {
        if (saved) attachment = cameraImage
        cameraImage = null
    }

    /** The file that holds [image], for copying it. */
    fun photoFile(image: Image): PlatformFile = imageStore.fileFor(image)

    fun removeAttachment() {
        attachment = null
    }

    /** Sends the draft and the attached photo. Returns whether there was anything to send. */
    fun send(): Boolean {
        if (!canSend) return false
        val text = draft
        val image = attachment
        draft = ""
        attachment = null
        viewModelScope.launch { chatRepository.send(text, image) }
        return true
    }

    private val pendingEvents = Channel<ChatEvent>(Channel.BUFFERED)

    /** Snackbars to show once: whether a photo was saved, or that one couldn't be attached. */
    val events: Flow<ChatEvent> = pendingEvents.receiveAsFlow()

    // State, not an event: a screen rebuilt before the offer ends, after a rotation or the photo
    // viewer, shows it again.
    private val pendingUndoState = MutableStateFlow<Message?>(null)

    /** The deleted message the chat offers to bring back, until I take the offer or it runs out. */
    val pendingUndo: StateFlow<Message?> = pendingUndoState.asStateFlow()

    fun delete(message: Message) {
        viewModelScope.launch {
            chatRepository.delete(message.id)
            pendingUndoState.value = message
        }
    }

    fun undoDelete(message: Message) {
        undoOfferEnded(message)
        viewModelScope.launch { chatRepository.undoDelete(message.id) }
    }

    /** The offer to bring back [message] is over. An offer for a later delete stays. */
    fun undoOfferEnded(message: Message) {
        pendingUndoState.compareAndSet(message, null)
    }

    /** Saves a copy of the message's photo to the phone's photos, and says whether it worked. */
    fun savePhoto(message: Message) {
        val image = message.image ?: return
        viewModelScope.launch {
            pendingEvents.send(if (imageStore.saveToPhotos(image)) ChatEvent.PhotoSaved else ChatEvent.PhotoNotSaved)
        }
    }

    /** Has the browser download a copy of the message's photo. The browser shows the download itself, so only a failure is mentioned. */
    fun downloadPhoto(message: Message) {
        val image = message.image ?: return
        viewModelScope.launch {
            if (!imageStore.saveToPhotos(image)) pendingEvents.send(ChatEvent.PhotoNotSaved)
        }
    }

    /** Saves a copy of the message's photo where a Save dialog chose. That choice is confirmation enough, so only a failure is mentioned. */
    fun savePhotoAs(message: Message, destination: PlatformFile) {
        val image = message.image ?: return
        viewModelScope.launch {
            if (!imageStore.saveCopy(image, destination)) pendingEvents.send(ChatEvent.PhotoNotSaved)
        }
    }
}

// Only plain strings are saved, which every platform can keep.
private const val DraftKey = "draft"
private const val AttachmentKey = "attachment"
private const val CameraImageKey = "camera.image"
