package com.echochat

import android.Manifest.permission.POST_NOTIFICATIONS
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE as LANDSCAPE
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT as PORTRAIT
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED as UNSPECIFIED
import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import android.content.res.Configuration.ORIENTATION_PORTRAIT
import androidx.compose.ui.test.assert
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.echochat.feature.chat.sharedPhotoUri
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

// Runs on a real device or emulator, with the real 5-second wait. Messages live as long as
// the app process, so each test sends text of its own.
@RunWith(AndroidJUnit4::class)
class ChatEndToEndTest {

    // Granted up front, so the first send doesn't stop to ask (NOTIFY-05).
    @get:Rule(order = 0)
    val notifications: GrantPermissionRule = GrantPermissionRule.grant(POST_NOTIFICATIONS)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun sendingHelloShowsTypingDotsThenTheEcho() {
        composeRule.onNode(hasSetTextAction()).performTextInput("Hello")
        composeRule.onNodeWithContentDescription("Send").performClick()

        composeRule.onNodeWithText("Hello").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Echo is typing").assertIsDisplayed()

        composeRule.waitUntil(timeoutMillis = 6_000) {
            composeRule.onAllNodesWithText("You said: Hello").fetchSemanticsNodes().isNotEmpty()
        }
    }

    // Android rebuilds the activity when the screen rotates; the shared code has to survive it.
    @Test
    fun `CHAT-08_messagesAndUnsentTextSurviveRotatingToLandscapeAndBack`() {
        composeRule.onNode(hasSetTextAction()).performTextInput("Before rotating")
        composeRule.onNodeWithContentDescription("Send").performClick()
        composeRule.onNode(hasSetTextAction()).performTextInput("Half a mess")

        try {
            for ((requested, expected) in listOf(LANDSCAPE to ORIENTATION_LANDSCAPE, PORTRAIT to ORIENTATION_PORTRAIT)) {
                composeRule.activityRule.scenario.onActivity { it.requestedOrientation = requested }
                composeRule.waitUntil(timeoutMillis = 5_000) { orientation() == expected }

                composeRule.onNodeWithText("Before rotating").assertIsDisplayed()
                composeRule.onNode(hasSetTextAction()).assert(hasText("Half a mess"))
            }
        } finally {
            composeRule.activityRule.scenario.onActivity { it.requestedOrientation = UNSPECIFIED }
        }
    }

    // ACTION-03 through the real FileProvider, which Robolectric can't run on Windows. It reads the
    // link rather than the clipboard: the emulator copies each clip to the computer and back as text.
    @Test
    fun aCopiedPhotoIsSharedAsAContentLinkOtherAppsCanOpen() {
        val app = composeRule.activity.application
        val photo = File(app.cacheDir, "images/sunset.jpg").apply { parentFile?.mkdirs(); writeBytes(byteArrayOf(1, 2, 3)) }

        val shared = sharedPhotoUri(app, photo.path)

        assertEquals("content", shared.scheme)
        assertEquals("${app.packageName}.images", shared.authority)
        assertEquals("image/jpeg", app.contentResolver.getType(shared))
        assertArrayEquals(byteArrayOf(1, 2, 3), app.contentResolver.openInputStream(shared)!!.use { it.readBytes() })
    }

    // KEY-03 on a real Android build, where rebuilding the activity tears the old screen down after its
    // state is saved, and the teardown takes the cursor and keyboard away. Shared tests can't reproduce that.
    @Test
    fun `KEY-03_theKeyboardComesBackAfterTheActivityIsRebuiltWhileTyping`() {
        composeRule.onNode(hasSetTextAction()).performClick()
        // A device with a hardware keyboard never opens the on-screen one, so tell the screen it opened.
        composeRule.runOnIdle {
            val content = composeRule.activity.findViewById<ViewGroup>(android.R.id.content)
            val insets = WindowInsetsCompat.Builder()
                .setInsets(WindowInsetsCompat.Type.ime(), Insets.of(0, 0, 0, 300))
                .setVisible(WindowInsetsCompat.Type.ime(), true)
                .build()
            ViewCompat.dispatchApplyWindowInsets(content.getChildAt(0), insets)
        }
        composeRule.waitForIdle()

        composeRule.activityRule.scenario.recreate()

        composeRule.onNode(hasSetTextAction()).assertIsFocused()
    }

    // KEY-04: Compose asks keyboards never to go full screen; the input lifts that, and the
    // keyboard's own Send key still sends. The full-screen box itself is checked by hand.
    @Test
    fun `KEY-04_theKeyboardMayGoFullScreenAndItsSendKeySends`() {
        composeRule.onNode(hasSetTextAction()).performTextInput("Sent from the keyboard")
        val editorInfo = EditorInfo()
        val connection = composeRule.runOnIdle {
            checkNotNull(composeRule.activity.currentFocus?.onCreateInputConnection(editorInfo))
        }

        assertEquals(0, editorInfo.imeOptions and EditorInfo.IME_FLAG_NO_FULLSCREEN)
        assertEquals(EditorInfo.IME_ACTION_SEND, editorInfo.imeOptions and EditorInfo.IME_MASK_ACTION)
        composeRule.runOnIdle { connection.performEditorAction(EditorInfo.IME_ACTION_SEND) }
        composeRule.onNodeWithText("Sent from the keyboard").assertIsDisplayed()
    }

    private fun orientation(): Int {
        var orientation = 0
        composeRule.activityRule.scenario.onActivity { orientation = it.resources.configuration.orientation }
        return orientation
    }
}
