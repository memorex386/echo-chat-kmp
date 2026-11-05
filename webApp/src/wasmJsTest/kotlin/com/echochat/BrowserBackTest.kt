package com.echochat

import androidx.navigationevent.NavigationEventDispatcher
import androidx.navigationevent.NavigationEventHandler
import androidx.navigationevent.NavigationEventInfo
import kotlin.test.Test
import kotlin.test.assertEquals

class BrowserBackTest {

    private val history = FakeHistory()
    private val panel = Closable()
    private val input = BrowserBackInput(history)

    init {
        val dispatcher = NavigationEventDispatcher()
        dispatcher.addInput(input)
        dispatcher.addHandler(panel)
        history.onBack(input::onBrowserBack)
    }

    @Test
    fun `KEY-02 the browser's Back closes what's open rather than leaving the page`() {
        panel.isBackEnabled = true
        assertEquals(1, history.entriesAdded, "Opening something adds a history entry")

        history.pressBack()

        assertEquals(1, panel.closed)
        assertEquals(0, history.entriesAdded, "Nothing is left open, so the next Back leaves the page")
    }

    @Test
    fun `KEY-02 with nothing to close the browser's Back leaves the page`() {
        assertEquals(0, history.entriesAdded)
    }

    @Test
    fun `KEY-02 closing in the app takes its history entry away again`() {
        panel.isBackEnabled = true

        panel.isBackEnabled = false

        assertEquals(0, history.entriesAdded)
        assertEquals(0, panel.closed, "Taking the entry away isn't a Back in the app")
    }

    @Test
    fun `KEY-02 with two things open the browser's Back closes them one at a time`() {
        panel.isBackEnabled = true
        panel.stayOpenFor = 1

        history.pressBack()
        assertEquals(1, panel.closed)
        assertEquals(1, history.entriesAdded, "Something is still open, so Back stays on the page")

        history.pressBack()
        assertEquals(2, panel.closed)
        assertEquals(0, history.entriesAdded)
    }
}

/** Something the app's Back closes, such as the attach panel. It can take more than one Back. */
private class Closable : NavigationEventHandler<NavigationEventInfo>(NavigationEventInfo.None, isBackEnabled = false) {
    var closed = 0
    var stayOpenFor = 0

    override fun onBackCompleted() {
        closed++
        if (stayOpenFor > 0) stayOpenFor-- else isBackEnabled = false
    }
}

/** The browser's history as entries this page added, and its Back button. */
private class FakeHistory : BrowserHistory {
    var entriesAdded = 0
    private var listener: () -> Unit = {}

    override fun push() {
        entriesAdded++
    }

    override fun back() = pressBack()

    override fun onBack(onBack: () -> Unit): () -> Unit {
        listener = onBack
        return { listener = {} }
    }

    /** Back leaves the page once the page's own entries are used up; this page then goes away. */
    fun pressBack() {
        if (entriesAdded > 0) entriesAdded--
        listener()
    }
}
