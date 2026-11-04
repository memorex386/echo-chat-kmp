package com.echochat.core.database

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.concurrent.atomics.AtomicLong
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.incrementAndFetch

/**
 * Keeps messages in memory for as long as the app process lives.
 *
 * The brief says in-memory storage is enough, so this stands in for a database. To keep
 * messages across launches, make [MessageEntity] a Room `@Entity` and [MessageDao] a Room
 * `@Dao`, then bind Room's implementation in `databaseModule` instead of this class. Room
 * runs on Android, desktop and iOS.
 */
@OptIn(ExperimentalAtomicApi::class)
class InMemoryMessageDao : MessageDao {

    private val rows = MutableStateFlow(emptyList<MessageEntity>())
    private val lastId = AtomicLong(0)

    override fun observeAll(): Flow<List<MessageEntity>> = rows.asStateFlow()

    override suspend fun insert(message: MessageEntity): Long {
        val id = lastId.incrementAndFetch()
        rows.update { it + message.copy(id = id) }
        return id
    }

    override suspend fun insertReply(reply: MessageEntity, repliedToId: Long): Long {
        val id = lastId.incrementAndFetch()
        rows.update { messages ->
            messages.map { if (it.id == repliedToId) it.copy(awaitingReply = false) else it } + reply.copy(id = id)
        }
        return id
    }

    override suspend fun setDeleted(id: Long, deleted: Boolean) {
        rows.update { messages -> messages.map { if (it.id == id) it.copy(deleted = deleted) else it } }
    }
}
