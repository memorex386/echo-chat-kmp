package com.echochat.feature.chat

import androidx.compose.runtime.Composable
import com.echochat.core.model.Message

// A browser saves a photo by downloading it, and shows the download itself.
@Composable
internal actual fun rememberSavePhoto(viewModel: ChatViewModel): ((Message) -> Unit)? = viewModel::downloadPhoto
