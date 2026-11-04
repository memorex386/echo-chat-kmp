package com.echochat.feature.chat

import android.content.ClipData
import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.compose.ui.platform.Clipboard
import androidx.compose.ui.platform.toClipEntry
import androidx.core.content.FileProvider
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.context
import java.io.File

internal actual suspend fun Clipboard.copyText(text: String, label: String) =
    setClipEntry(ClipData.newPlainText(label, text).toClipEntry())

// A content link, as the original did: Android lets the app that pastes it read the photo.
internal actual suspend fun Clipboard.copyPhoto(path: String, label: String) {
    val context = FileKit.context
    setClipEntry(ClipData.newUri(context.contentResolver, label, sharedPhotoUri(context, path)).toClipEntry())
}

// Android 13 and later show their own "Copied".
internal actual val systemConfirmsCopy: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

/** A content link other apps can read the photo at [path] through. Public for the device test. */
fun sharedPhotoUri(context: Context, path: String): Uri =
    FileProvider.getUriForFile(context, "${context.packageName}.images", File(path))
