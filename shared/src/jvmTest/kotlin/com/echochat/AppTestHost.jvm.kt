package com.echochat

import androidx.compose.runtime.Composable

@Composable
internal actual fun AppTestHost(content: @Composable () -> Unit) = content()
