package com.echochat

import com.echochat.core.data.ChatRepository
import com.echochat.feature.chat.ChatViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.koin.dsl.koinApplication
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertSame

// Koin checks its wiring when the app runs, not when it compiles, so this test builds the
// app's objects from the real modules to catch a missing binding before a device does.
@OptIn(ExperimentalCoroutinesApi::class)
class AppModulesTest {

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `the app modules build the chat screen and share one repository`() {
        val koin = koinApplication { modules(appModules) }.koin

        koin.get<ChatViewModel>()

        assertSame(koin.get<ChatRepository>(), koin.get<ChatRepository>())
    }
}
