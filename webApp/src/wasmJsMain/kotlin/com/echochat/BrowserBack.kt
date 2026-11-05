package com.echochat

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.navigationevent.NavigationEventInput
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner

/**
 * Connects the browser's Back button to the app's Back (D35). While something in the app can go
 * back, such as the attach panel or the photo viewer, the page adds an entry to the browser's
 * history, so the browser's Back stays on the page and goes back in the app instead. With nothing
 * left to close, the entry goes, and the browser's Back leaves the page as usual.
 */
@Composable
internal fun BrowserBack(history: BrowserHistory = WindowHistory) {
    val dispatcher = LocalNavigationEventDispatcherOwner.current?.navigationEventDispatcher ?: return
    DisposableEffect(dispatcher, history) {
        val input = BrowserBackInput(history)
        dispatcher.addInput(input)
        val stopListening = history.onBack(input::onBrowserBack)
        onDispose {
            stopListening()
            dispatcher.removeInput(input)
        }
    }
}

/** The browser's history, behind a small interface so tests can stand in for the browser. */
internal interface BrowserHistory {
    /** Adds an entry, so the browser's next Back stays on the page. */
    fun push()

    /** Goes back one entry, as the browser's Back button does. */
    fun back()

    /** Calls [onBack] each time the browser goes back, until the returned function is called. */
    fun onBack(onBack: () -> Unit): () -> Unit
}

/** Turns the browser's Back into the app's Back, and keeps a history entry while there's something to go back to. */
internal class BrowserBackInput(private val history: BrowserHistory) : NavigationEventInput() {
    private var appCanGoBack = false
    private var entryAdded = false
    private var takingEntryAway = false

    override fun onHasEnabledHandlersChanged(hasEnabledHandlers: Boolean) {
        appCanGoBack = hasEnabledHandlers
        if (hasEnabledHandlers) addEntry() else takeEntryAway()
    }

    /** The browser went back. */
    fun onBrowserBack() {
        // Our own step back, taking away an entry the app no longer needs: not a Back in the app.
        if (takingEntryAway) {
            takingEntryAway = false
            return
        }
        if (!entryAdded) return
        entryAdded = false
        dispatchOnBackCompleted()
        // Something else is still open, such as the panel behind the photo viewer.
        if (appCanGoBack) addEntry()
    }

    private fun addEntry() {
        if (entryAdded) return
        history.push()
        entryAdded = true
    }

    private fun takeEntryAway() {
        if (!entryAdded) return
        entryAdded = false
        takingEntryAway = true
        history.back()
    }
}

@OptIn(ExperimentalWasmJsInterop::class)
internal object WindowHistory : BrowserHistory {
    override fun push() = pushHistoryEntry()

    override fun back() = goBackInHistory()

    override fun onBack(onBack: () -> Unit): () -> Unit {
        val listener = addPopStateListener(onBack)
        return { removePopStateListener(listener) }
    }
}

@OptIn(ExperimentalWasmJsInterop::class)
private fun pushHistoryEntry(): Unit = js("history.pushState(null, '')")

@OptIn(ExperimentalWasmJsInterop::class)
private fun goBackInHistory(): Unit = js("history.back()")

@OptIn(ExperimentalWasmJsInterop::class)
@Suppress("UNUSED_PARAMETER")
private fun addPopStateListener(onBack: () -> Unit): JsAny =
    js("(() => { const listener = () => onBack(); window.addEventListener('popstate', listener); return listener; })()")

@OptIn(ExperimentalWasmJsInterop::class)
@Suppress("UNUSED_PARAMETER")
private fun removePopStateListener(listener: JsAny): Unit = js("window.removeEventListener('popstate', listener)")
