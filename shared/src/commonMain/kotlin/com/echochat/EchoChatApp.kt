package com.echochat

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.map.Mapper
import coil3.request.crossfade
import com.echochat.core.data.ImageStore
import com.echochat.core.designsystem.theme.EchoChatTheme
import com.echochat.core.model.Image
import com.echochat.core.notifications.ChatRequests
import com.echochat.feature.chat.ChatRoute
import com.echochat.feature.chat.PhotoViewerScreen
import com.echochat.feature.chat.navigation.ChatKey
import com.echochat.feature.chat.navigation.PhotoKey
import com.echochat.feature.chat.navigation.chatNavKeys
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.coil.addPlatformFileSupport
import org.koin.compose.koinInject

private val navKeys = SavedStateConfiguration {
    serializersModule = chatNavKeys
}

/**
 * The app's screens, shown from a Navigation 3 back stack. Every platform shows this.
 * [onChatRequested] lets a platform bring its window forward when a notification asks for the chat.
 */
@Composable
fun EchoChatApp(onChatRequested: () -> Unit = {}) {
    // The screens hand Coil a message's Image, and the photo store says which file holds it.
    // Coil then loads it as a FileKit file, which works on every platform, Windows paths
    // included. Photos fade in as they load.
    val imageStore = koinInject<ImageStore>()
    setSingletonImageLoaderFactory { context ->
        ImageLoader.Builder(context)
            .components {
                add(Mapper<Image, PlatformFile> { image, _ -> imageStore.fileFor(image) })
                addPlatformFileSupport()
            }
            .crossfade(true)
            .build()
    }
    EchoChatTheme {
        val backStack = rememberNavBackStack(navKeys, ChatKey)
        // A tap on one of Echo's notifications asks for the chat, closing the photo viewer if it's open.
        val chatRequests = koinInject<ChatRequests>()
        val bringForward by rememberUpdatedState(onChatRequested)
        LaunchedEffect(chatRequests) {
            chatRequests.requested.collect {
                backStack.retainAll { it is ChatKey }
                bringForward()
            }
        }
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
