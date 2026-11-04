package com.echochat.core.database

import com.echochat.core.model.Author
import com.echochat.core.model.Image
import com.echochat.core.model.Message

/** One stored message, flattened into the columns a database table would have. */
data class MessageEntity(
    val id: Long = 0,
    val author: Author,
    val text: String,
    val imagePath: String? = null,
    val awaitingReply: Boolean = false,
)

fun MessageEntity.asMessage() = Message(
    id = id,
    author = author,
    text = text,
    image = imagePath?.let(::Image),
)
