package com.echochat

import androidx.compose.runtime.Composable

/** Gives the whole app what its platform's real window would, so a test can show it. */
@Composable
internal expect fun AppTestHost(content: @Composable () -> Unit)
