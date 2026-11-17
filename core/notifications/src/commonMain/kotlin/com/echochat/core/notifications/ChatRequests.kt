package com.echochat.core.notifications

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

/** Asks the app to bring the chat forward, as tapping one of Echo's notifications does. */
class ChatRequests {
    // Holds one request until the chat reads it. Android can rebuild the app from a tap, and the
    // tap arrives before the rebuilt chat is listening.
    private val requests = Channel<Unit>(Channel.CONFLATED)

    /** Requests, for the app's one listener. Each is read once; several made before it listens count as one. */
    val requested: Flow<Unit> = requests.receiveAsFlow()

    fun request() {
        requests.trySend(Unit)
    }
}
