package com.echochat

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import com.echochat.core.designsystem.theme.EchoChatTheme
import com.echochat.core.model.Image
import com.echochat.feature.chat.ChatRoute
import com.echochat.feature.chat.PhotoViewerScreen
import com.echochat.feature.chat.navigation.ChatKey
import com.echochat.feature.chat.navigation.PhotoKey
import com.echochat.feature.chat.navigation.chatNavKeys
import io.github.vinceglb.filekit.coil.addPlatformFileSupport

private val navKeys = SavedStateConfiguration {
    serializersModule = chatNavKeys
}

/** The app's screens, shown from a Navigation 3 back stack. Every platform shows this. */
@Composable
fun EchoChatApp() {
    // Coil loads photos as FileKit files, so one kind of path works on every platform, Windows included.
    setSingletonImageLoaderFactory { context ->
        ImageLoader.Builder(context)
            .components { addPlatformFileSupport() }
            .build()
    }
    EchoChatTheme {
        val backStack = rememberNavBackStack(navKeys, ChatKey)
        NavDisplay(
            backStack = backStack,
            onBack = { backStack.removeLastOrNull() },
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                // Gives each screen its own ViewModels, kept across Android's rotation.
                rememberViewModelStoreNavEntryDecorator(),
            ),
            entryProvider = entryProvider {
                entry<ChatKey> {
                    ChatRoute(
                        onOpenImage = { message ->
                            message.image?.let { backStack.add(PhotoKey(it.path, message.author)) }
                        },
                    )
                }
                entry<PhotoKey> { key ->
                    PhotoViewerScreen(
                        image = Image(key.path),
                        from = key.from,
                        onBack = { backStack.removeLastOrNull() },
                    )
                }
            },
        )
    }
}
