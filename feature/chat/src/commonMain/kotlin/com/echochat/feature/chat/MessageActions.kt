package com.echochat.feature.chat

import com.echochat.core.model.Message

/** What a bubble's long-press menu asks for. */
sealed interface MessageAction {
    val message: Message

    data class CopyText(override val message: Message) : MessageAction

    data class CopyPhoto(override val message: Message) : MessageAction

    data class SavePhoto(override val message: Message) : MessageAction

    data class Delete(override val message: Message) : MessageAction
}

/** Things the chat tells me about once, in a snackbar. */
sealed interface ChatEvent {
    data class Deleted(val message: Message) : ChatEvent

    data object PhotoSaved : ChatEvent

    data object PhotoNotSaved : ChatEvent
}
