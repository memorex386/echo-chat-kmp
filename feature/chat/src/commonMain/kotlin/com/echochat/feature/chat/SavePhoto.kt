package com.echochat.feature.chat

import androidx.compose.runtime.Composable
import com.echochat.core.model.Message

/** Save on a photo's menu, done the platform's way. Null where Save isn't offered. */
@Composable
internal expect fun rememberSavePhoto(viewModel: ChatViewModel): ((Message) -> Unit)?
