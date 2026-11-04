package com.echochat.core.data

import com.echochat.core.database.MessageDao
import com.echochat.core.database.MessageEntity
import com.echochat.core.database.asMessage
import com.echochat.core.model.Author
import com.echochat.core.model.Message
import com.echochat.core.network.ChatService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class DefaultChatRepository(
    private val messageDao: MessageDao,
    private val chatService: ChatService,
    private val appScope: CoroutineScope,
) : ChatRepository {

    override val messages: Flow<List<Message>> =
        messageDao.observeAll().map { rows -> rows.map(MessageEntity::asMessage) }

    override val isReplying: Flow<Boolean> =
        messageDao.observeAll().map { rows -> rows.any(MessageEntity::awaitingReply) }.distinctUntilChanged()

    override suspend fun send(text: String) {
        val sent = MessageEntity(author = Author.Me, text = text.trim(), awaitingReply = true)
        require(sent.text.isNotEmpty()) { "A message needs text" }
        val id = messageDao.insert(sent)

        // The reply belongs to the app, not to the screen that sent the message, so it still
        // arrives after a rotation or a trip to another screen.
        appScope.launch {
            val reply = chatService.send(sent.copy(id = id).asMessage())
            messageDao.insertReply(
                reply = MessageEntity(author = Author.Echo, text = reply.text),
                repliedToId = id,
            )
        }
    }
}
