package com.echochat.core.model

data class Message(
    val id: Long,
    val author: Author,
    val text: String,
)

enum class Author { Me, Echo }
