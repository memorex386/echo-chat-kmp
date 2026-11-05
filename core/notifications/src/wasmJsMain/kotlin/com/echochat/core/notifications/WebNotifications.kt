package com.echochat.core.notifications

/** The browser's notifications, behind a small interface so tests can stand in for the browser. */
interface WebNotifications {
    /** Whether I've let this page notify. */
    val allowed: Boolean

    /** Shows a notification. Clicking it brings the page's tab forward, then runs [onClick]. */
    fun show(title: String, body: String, onClick: () -> Unit): ShownNotification
}

/** A notification on screen, which the page can take back. */
fun interface ShownNotification {
    fun close()
}

/** The real browser, through the Notification API. */
@OptIn(ExperimentalWasmJsInterop::class)
object BrowserNotifications : WebNotifications {
    override val allowed: Boolean
        get() = notificationsAllowed()

    override fun show(title: String, body: String, onClick: () -> Unit): ShownNotification {
        val notification = showNotification(title, body, EchoIcon, onClick)
        return ShownNotification { notification.close() }
    }

    // Echo's avatar, which the web app serves next to its page.
    private const val EchoIcon = "echo.svg"
}

@OptIn(ExperimentalWasmJsInterop::class)
private external interface JsNotification : JsAny {
    fun close()
}

@OptIn(ExperimentalWasmJsInterop::class)
private fun notificationsAllowed(): Boolean =
    js("typeof Notification !== 'undefined' && Notification.permission === 'granted'")

@OptIn(ExperimentalWasmJsInterop::class)
@Suppress("UNUSED_PARAMETER")
private fun showNotification(title: String, body: String, icon: String, onClick: () -> Unit): JsNotification =
    js("""(() => {
        const notification = new Notification(title, { body: body, icon: icon });
        notification.onclick = () => { window.focus(); notification.close(); onClick(); };
        return notification;
    })()""")
