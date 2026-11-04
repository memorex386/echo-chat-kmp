package com.echochat.core.testing

import androidx.compose.ui.platform.ClipEntry

actual fun ClipEntry.plainText(): String? = clipData.getItemAt(0)?.text?.toString()
