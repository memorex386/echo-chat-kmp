package com.echochat.core.testing

import androidx.compose.ui.platform.ClipEntry

actual suspend fun ClipEntry.plainText(): String? = clipData.getItemAt(0)?.text?.toString()
