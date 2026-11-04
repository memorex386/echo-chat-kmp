package com.echochat

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.uikit.LocalUIViewController
import platform.UIKit.UIViewController

// In the app, MainViewController() hosts Compose in a UIKit view controller, which the photo
// picker presents itself from. A test has no such host, so it gets a plain one.
@Composable
internal actual fun AppTestHost(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalUIViewController provides UIViewController(), content = content)
}
