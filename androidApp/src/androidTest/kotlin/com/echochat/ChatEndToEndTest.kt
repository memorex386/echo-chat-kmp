package com.echochat

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
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
    fun `CHAT-08_messagesAndUnsentTextSurviveARotation`() {
        composeRule.onNode(hasSetTextAction()).performTextInput("Before rotating")
        composeRule.onNodeWithContentDescription("Send").performClick()
        composeRule.onNode(hasSetTextAction()).performTextInput("Still typing")

        composeRule.activityRule.scenario.recreate()

        composeRule.onNodeWithText("Before rotating").assertIsDisplayed()
        composeRule.onNode(hasSetTextAction()).assert(hasText("Still typing"))
    }
}
