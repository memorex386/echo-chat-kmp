package com.echochat.core.testing

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.ClipEntry

@OptIn(ExperimentalComposeUiApi::class)
actual fun ClipEntry.plainText(): String? = getPlainText()
