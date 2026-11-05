package com.echochat

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runComposeUiTest
import org.koin.core.context.stopKoin
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import com.echochat.core.testing.loadStrings
import com.echochat.feature.chat.resources.Res
import com.echochat.feature.chat.resources.allStringResources

@OptIn(ExperimentalTestApi::class)
class WebAppTest {

    @BeforeTest
    fun startApp() = initKoin()

    @AfterTest
    fun stopApp() = stopKoin()

    @Test
    fun `WEB-01 the web app opens on the chat`() = runComposeUiTest {
        loadStrings(Res.allStringResources)
        setContent { WebApp() }

        onNodeWithText("No messages yet").assertIsDisplayed()
    }
}
