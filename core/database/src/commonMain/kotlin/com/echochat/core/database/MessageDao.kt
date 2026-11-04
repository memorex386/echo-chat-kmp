package com.echochat.core.database

import kotlinx.coroutines.flow.Flow

/** Reads and writes messages. Shaped like a Room DAO, so the storage behind it can change. */
interface MessageDao {
    /** Every message, oldest first. Emits again whenever they change. */
    fun observeAll(): Flow<List<MessageEntity>>

    /** Stores [message] and returns its new id. */
    suspend fun insert(message: MessageEntity): Long

    /** Stores [reply] and marks message [repliedToId] as answered, in one step. */
    suspend fun insertReply(reply: MessageEntity, repliedToId: Long)
}
