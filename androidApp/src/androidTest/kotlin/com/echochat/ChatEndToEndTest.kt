package com.echochat

import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE as LANDSCAPE
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT as PORTRAIT
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED as UNSPECIFIED
import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import android.content.res.Configuration.ORIENTATION_PORTRAIT
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

// Runs on a real device or emulator, with the real 5-second wait. Messages live as long as
// the app process, so each test sends text of its own.
@RunWith(AndroidJUnit4::class)
class ChatEndToEndTest {

    @get:Rule
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

    private fun orientation(): Int {
        var orientation = 0
        composeRule.activityRule.scenario.onActivity { orientation = it.resources.configuration.orientation }
        return orientation
    }
}
