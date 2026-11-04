# Decisions

A short log of choices made while building, oldest first. A decision that changes is marked as replaced rather than deleted, so the log shows how the design moved.

## D1: A fresh KMP project, ported one slice at a time (2026-11-03)
The Android-only app is finished and locked, so this is a new project in its own repo. Code and tests are ported one feature at a time, in the original's slice order, and each slice is checked again on every platform. Converting a copy in place was the other option, but Hilt and Robolectric run through every module, and AGP 9 forces the Android app into its own module anyway.

## D2: Android, desktop and iOS (2026-11-03)
- **Android and desktop** build, run and test on my Windows PC. Desktop runs on the JVM, which also gives fast UI tests.
- **iOS** runs on my Mac. On Windows the shared code and its tests still compile for iOS, so most iOS mistakes show up here. Linking the app and running the iOS tests need the Mac.
- iOS targets are `iosArm64` (phones) and `iosSimulatorArm64` (the simulator on Apple-silicon Macs). Intel-Mac simulators aren't included.

## D3: The module map (2026-11-03)
The original's modules, each now multiplatform, plus one shared app module and a thin launcher per platform.

| Module | Job |
|---|---|
| `androidApp` | `Application`, `MainActivity`. Android only. |
| `desktopApp` | `main()` and the window. JVM only. |
| `iosApp` | The Xcode project and its Swift entry point, built on the Mac. Not added yet: it comes when the app first runs there, so it is checked as it is written. |
| `shared` | `EchoChatApp()`: the theme, the Navigation 3 back stack and the Koin start-up. iOS gets it as one framework, `Shared`. |
| `feature:chat` | Chat screen, `ChatViewModel` |
| `core:model` | `Message`, `Author`. No dependencies. |
| `core:data` | `ChatRepository`, the only place messages change |
| `core:database` | `MessageDao` and its in-memory implementation |
| `core:network` | `ChatService` and the simulated echo service |
| `core:designsystem` | Theme, avatar, typing dots, icons |
| `core:testing` | Fakes shared by the modules' tests |

Convention plugins in `build-logic` hold the shared setup: `echochat.kmp.library`, `echochat.kmp.compose`, `echochat.kmp.feature`, and one each for the Android and desktop apps.

## D4: AGP 9 puts the Android app in its own module (2026-11-03)
AGP 9 doesn't let the multiplatform plugin share a module with `com.android.application`. Shared modules use `com.android.kotlin.multiplatform.library`, which is configured in an `android {}` block inside `kotlin {}`. `androidApp` is a plain Android app that depends on `shared`. Its application id is `com.echochat.kmp`, so it installs next to the original app.

## D5: Koin replaces Hilt (2026-11-03)
Hilt is Android only. Koin is the most common choice in KMP, needs no compiler plugin, and its bindings are plain Kotlin.
- Each core module has a Koin module next to its code, as each had a Hilt module before. `dataModule` includes the database and network modules, so `shared` only lists `dataModule` and `chatModule` and never sees what is behind the repository.
- The app-wide coroutine scope is a Koin single with the `ApplicationScope` qualifier, so replies outlive any one screen.
- Koin starts once per process, from each platform's entry point: `Application.onCreate`, `main()` and the Swift app's start-up. Starting it inside the UI would restart it whenever Android rebuilds the activity, and the messages would be lost.
- Hilt checked the wiring when it compiled; Koin checks it when the app runs. `AppModulesTest` builds the chat screen's ViewModel from the real modules, so a missing binding fails a test rather than the app. With `includes(...)` removed from `dataModule`, it fails as it should.

## D6: Where tests run (2026-11-03)
- **commonTest** holds the acceptance tests. `./gradlew allTests` runs them once per target this computer can run: the JVM on Windows, and the iOS simulator on a Mac. `./gradlew test` doesn't include them.
- **UI tests** use `runComposeUiTest` from Compose Multiplatform. On Windows they run on the desktop JVM, with Skia drawing for real.
- **Android local tests are off.** Common Compose UI tests can't run as Android local tests, and the logic tests would only repeat the JVM run. AGP prints a warning about this on each build.
- **Android device tests** in `androidApp` cover what only Android has: the end-to-end check with the real 5-second wait, and CHAT-08, the activity being rebuilt on rotation.
- **Virtual time** comes from kotlinx-coroutines-test, as before. Common code has no JUnit rules, so ViewModel tests set `Dispatchers.Main` in `@BeforeTest` themselves.

## D7: Test names every platform accepts (2026-11-03)
Kotlin/Native rejects some characters in function names that the JVM allows. The first iOS compile failed on a comma in `CHAT-05 quick sends are each answered, in order`. Test names in `commonTest` avoid commas.

## D8: Android-only APIs and their replacements (2026-11-03)
| Original | Multiplatform |
|---|---|
| `R.string`, `R.drawable` | Compose resources in `composeResources`. `core:designsystem` makes its `Res` class public, so features can draw its icons. |
| `java.util.concurrent.AtomicLong` | `kotlin.concurrent.atomics.AtomicLong`, still marked experimental |
| `hiltViewModel()` | `koinViewModel()` |
| Navigation 3 finding keys by reflection | Each feature lists its keys in a `SerializersModule`, because iOS has no reflection |
| Robolectric UI tests | `runComposeUiTest` on the desktop JVM, plus device tests for Android-only behaviour |

## D9: Toolchain (2026-11-03)
Each version was the latest stable release on Maven Central or Google Maven on the day the project was set up: Kotlin 2.4.21, Compose Multiplatform 1.12.2, Navigation 3 1.2.0, Lifecycle 2.11.0, Koin 4.2.2, AGP 9.5.0 and Gradle 9.8.2. One exception:
- **Material3 1.12.0-alpha03.** It is the release JetBrains pairs with Compose Multiplatform 1.12.2, because Jetpack Material3 1.5, which it is built on, is still an alpha. The only stable multiplatform Material3 is 1.9.0, which is far behind.

## D10: Carried over from the original (2026-11-03)
These are unchanged:
- The reply is an echo, "You said: Hello", 5 s after each message. You can keep sending while replies are on their way.
- Empty messages can't be sent, and text is trimmed.
- A fixed Material 3 palette, light and dark, as agreed in the mock.
- Messages are kept in memory only, behind `MessageDao`.
- The Gradle wrapper jar is committed.
- One set of typing dots while any reply is on its way, worked out from the stored messages.
- Echo's avatar is an orange "E".

## D11: Enter sends on desktop (2026-11-04)
Chosen in review of slice 1. On desktop, Enter added a new line, because the input allows 4 lines, and a desktop keyboard has no Send key to press instead. As in desktop chat apps, Enter will send and Shift+Enter will start a new line (KEY-05). It is built with the keyboard slice, so slice 1 stays as the original.
