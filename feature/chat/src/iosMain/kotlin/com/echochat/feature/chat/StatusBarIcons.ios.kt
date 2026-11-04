package com.echochat.feature.chat

import androidx.compose.runtime.Composable

// On iOS the status bar's style belongs to the UIKit view controller hosting Compose. On the
// iOS 27 simulator it turned light over the viewer's black by itself; earlier iOS is unchecked.
@Composable
internal actual fun LightStatusBarIcons() = Unit
