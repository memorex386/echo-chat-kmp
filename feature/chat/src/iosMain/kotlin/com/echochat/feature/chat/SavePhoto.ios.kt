package com.echochat.feature.chat

import androidx.compose.runtime.Composable
import com.echochat.core.model.Message

@Composable
internal actual fun rememberSavePhoto(viewModel: ChatViewModel): ((Message) -> Unit)? = viewModel::savePhoto
