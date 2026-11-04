package com.echochat.core.model

data class Message(
    val id: Long,
    val author: Author,
    val text: String,
    val image: Image? = null,
)

enum class Author { Me, Echo }
