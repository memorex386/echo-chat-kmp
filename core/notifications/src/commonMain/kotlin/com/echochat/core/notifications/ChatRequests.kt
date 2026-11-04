package com.echochat.core.notifications

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/** Asks the app to bring the chat forward, as tapping one of Echo's notifications does. */
class ChatRequests {
    private val requests = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    /** One item for each request. A request while nothing is listening is dropped: the chat is the first screen anyway. */
    val requested: SharedFlow<Unit> = requests.asSharedFlow()

    fun request() {
        requests.tryEmit(Unit)
    }
}
