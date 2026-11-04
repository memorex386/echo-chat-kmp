package com.echochat.core.data

import com.echochat.core.model.Image
import com.echochat.core.model.Message
import kotlinx.coroutines.flow.Flow

/** The conversation with Echo, and the only place messages are created. */
interface ChatRepository {
    /** Every message, oldest first. */
    val messages: Flow<List<Message>>

    /** True while any of my messages is still waiting for Echo's reply. */
    val isReplying: Flow<Boolean>

    /** Sends my message and starts waiting for the reply. [text] is trimmed; blank text needs an [image]. */
    suspend fun send(text: String, image: Image? = null)

    /** Hides message [id]. A reply it is waiting for still arrives. */
    suspend fun delete(id: Long)

    /** Brings back message [id] after [delete], in the same place. */
    suspend fun undoDelete(id: Long)
}
