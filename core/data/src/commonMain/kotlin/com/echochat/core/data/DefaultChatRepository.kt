package com.echochat.core.data

import com.echochat.core.database.MessageDao
import com.echochat.core.database.MessageEntity
import com.echochat.core.database.asMessage
import com.echochat.core.model.Author
import com.echochat.core.model.Image
import com.echochat.core.model.Message
import com.echochat.core.network.ChatService
import com.echochat.core.notifications.Notifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class DefaultChatRepository(
    private val messageDao: MessageDao,
    private val chatService: ChatService,
    private val notifier: Notifier,
    private val appScope: CoroutineScope,
) : ChatRepository {

    override val messages: Flow<List<Message>> =
        messageDao.observeAll().map { rows -> rows.filterNot(MessageEntity::deleted).map(MessageEntity::asMessage) }

    // Deleted messages count too: a reply already on its way still arrives (ACTION-06).
    override val isReplying: Flow<Boolean> =
        messageDao.observeAll().map { rows -> rows.any(MessageEntity::awaitingReply) }.distinctUntilChanged()

    override suspend fun send(text: String, image: Image?) {
        val sent = MessageEntity(
            author = Author.Me,
            text = text.trim(),
            imagePath = image?.path,
            awaitingReply = true,
        )
        require(sent.text.isNotEmpty() || image != null) { "A message needs text or an image" }
        val id = messageDao.insert(sent)

        // The reply belongs to the app, not to the screen that sent the message, so it still
        // arrives after a rotation or a trip to the photo viewer.
        appScope.launch {
            val reply = chatService.send(sent.copy(id = id).asMessage())
            val stored = MessageEntity(author = Author.Echo, text = reply.text, imagePath = reply.image?.path)
            val replyId = messageDao.insertReply(stored, repliedToId = id)
            // The notifier decides whether to show it: not while the chat is on screen.
            notifier.notifyReply(stored.copy(id = replyId).asMessage())
        }
    }

    override suspend fun delete(id: Long) = messageDao.setDeleted(id, deleted = true)

    override suspend fun undoDelete(id: Long) = messageDao.setDeleted(id, deleted = false)
}
