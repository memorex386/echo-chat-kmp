package com.echochat.feature.chat

import android.os.Build
import androidx.compose.runtime.Composable
import com.echochat.core.model.Message

// Saving to the shared photos needs Android 10 or later; before that it would need storage permission.
@Composable
internal actual fun rememberSavePhoto(viewModel: ChatViewModel): ((Message) -> Unit)? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) viewModel::savePhoto else null
