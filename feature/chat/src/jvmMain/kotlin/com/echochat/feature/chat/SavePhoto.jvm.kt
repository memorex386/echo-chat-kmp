package com.echochat.feature.chat

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.echochat.core.model.Message
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings
import io.github.vinceglb.filekit.dialogs.compose.rememberFileSaverLauncher
import io.github.vinceglb.filekit.extension
import io.github.vinceglb.filekit.picturesDir
import kotlin.time.Clock

// Desktop has no photo library, so Save opens a Save dialog at the Pictures folder, as desktop
// chat apps do. Cancelling saves nothing.
@Composable
internal actual fun rememberSavePhoto(viewModel: ChatViewModel): ((Message) -> Unit)? {
    var saving by remember { mutableStateOf<Message?>(null) }
    val saveAs = rememberFileSaverLauncher(FileKitDialogSettings()) { destination ->
        val message = saving
        saving = null
        if (message != null && destination != null) viewModel.savePhotoAs(message, destination)
    }
    return { message ->
        message.image?.let { image ->
            saving = message
            saveAs.launch(
                suggestedName = "Echo Chat ${Clock.System.now().toEpochMilliseconds()}",
                extension = PlatformFile(image.path).extension,
                directory = FileKit.picturesDir,
            )
        }
    }
}
