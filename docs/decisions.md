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
| `iosApp` | The Xcode project, its Swift entry point and the iOS end-to-end test. Built on the Mac (D12). |
| `shared` | `EchoChatApp()`: the theme, the Navigation 3 back stack and the Koin start-up. iOS gets it as one framework, `Shared`. |
| `feature:chat` | Chat screen, photo viewer, `ChatViewModel` |
| `core:model` | `Message`, `Author`, `Image`. No dependencies. |
| `core:data` | `ChatRepository`, the only place messages change, and `ImageStore` |
| `core:database` | `MessageDao` and its in-memory implementation |
| `core:network` | `ChatService` and the simulated echo service |
| `core:designsystem` | Theme, avatar, typing dots, icons |
| `core:notifications` | `Notifier`, the shared rules, and each platform's notifications. Added in slice 4 (D22). |
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
| Android's photo picker and camera contracts, and a `FileProvider` | FileKit (D13) |
| Coil on Android | Coil 3, which is multiplatform, with FileKit's Coil support (D13) |

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

## D12: The iOS app, built and checked on the Mac from Windows (2026-11-04)
- **The Xcode project** is small: one app target whose folder Xcode reads as it is, so new Swift files need no project edits. A build phase runs Gradle's `embedAndSignAppleFrameworkForXcode`, which builds the `Shared` framework for whichever simulator or phone Xcode is building for. The Swift side only starts Koin and shows `MainViewController()`.
- **Java on the Mac.** That build phase runs Gradle, so the Mac has JDK 21 in `~/Library/Java/JavaVirtualMachines`, where macOS's own `java` finds it. Nothing in the project names a JDK path.
- **Checked without hands.** The Mac is reached over SSH from my Windows PC, where nothing can tap the simulator. So the iOS app has its own end-to-end test, `ChatEndToEndUITests`, the counterpart of Android's device test. It finds Compose's controls through iOS accessibility: the 4-line input shows up as a text view labelled "Message". The keyboard's return key is labelled Send as well, and the test sends with it.
- **Its own simulator.** The test runs on "Echo Chat iPhone 18 Pro", so it never touches the simulators other projects on the Mac use.
- **iOS 17 and later.** Xcode warns that two of Compose's ICU data files were built for iOS 18.5. They hold text-processing data, not code, and the rest of the framework targets iOS 14 and 15, so the warning is accepted.

## D13: FileKit for the pickers, Coil for showing photos (2026-11-04)
Chosen in review, over writing each platform's picker ourselves.
- **FileKit 0.17.0** picks a photo with each platform's own picker: Android's photo picker, iOS's photo picker and a native file dialog for images on desktop. None needs a permission. Its camera opens the phone's own camera app on Android and iOS. It also gives common code one file type, `PlatformFile`, and the app's cache folder on every platform.
- **Coil 3.6.3**, as in the original, now multiplatform. It loads photos as `PlatformFile`s through FileKit's Coil support, so one kind of path works everywhere, Windows paths included.
- Both are built for Kotlin 2.4.21 and Compose Multiplatform 1.12.2, the versions this project uses.

## D14: Where photos are kept (2026-11-04)
As in the original, a picked photo is copied into the app's cache, under `images`, so it stays readable after the picker's access ends. The camera saves straight into a new file there, so its photo needs no copy; it is attached only if the camera saved it.
- **Android:** the app's private cache, which Android sandboxes and encrypts. The copy is `-rw-------`.
- **iOS:** the app's sandboxed Caches folder.
- **Desktop:** `%LOCALAPPDATA%\EchoChat\Cache\images` on Windows. Desktop has no app sandbox, so other programs the user runs can read it. `desktopApp` names the folder with `FileKit.init(appId = "EchoChat")`.

## D15: The camera lives in a mobile-only source set (2026-11-04)
Desktop has no camera app to hand off to, so it offers only the gallery. `rememberTakePhoto()` is declared once in `commonMain` and implemented twice: in `mobileMain`, a source set shared by Android and iOS only, and in `jvmMain`, where it is null. `ChatScreen` hides the Camera tile when it is null.
- `mobileMain` is made with Kotlin's hierarchy template, which is marked experimental.
- Its `withAndroidTarget()` doesn't match AGP 9's Android library target, so Android is picked out by platform type instead. The first build caught it: Android had no `actual`.

## D16: Platform hosts in tests (2026-11-04)
The whole-app test for PHOTO-05 failed on the iOS simulator only: FileKit's picker needs the UIKit view controller that hosts Compose, which a bare test doesn't have. `AppTestHost` is declared in `commonTest` and implemented per platform: on iOS it provides a plain view controller, and on the JVM it adds nothing. Tests of a single screen don't need it, because the screen takes its pickers as callbacks.

## D17: Light status-bar icons over the photo viewer (2026-11-04)
The viewer is black, so its status-bar icons should be light. `LightStatusBarIcons()` does this on Android, as in the original. Desktop has no status bar. On the iOS 27 simulator the system turned the icons light by itself; earlier iOS versions aren't checked.

## D18: Polish carried over from the original (2026-11-04)
These choices from the original's polish slice are unchanged:
- **Follow new messages only from the bottom,** or when the new message is mine. Otherwise a round down-arrow appears above the input, at the right.
- **Two thirds of the width in landscape,** 85% in portrait.
- **Motion only for new arrivals.** New bubbles fade, rise 12 dp and grow from their own corner over 220 ms. A saved watermark stops old bubbles replaying when they scroll back into view. Photos fade in through Coil's crossfade.
- **Photos stand on their own,** with any caption in a bubble of its own below.

## D19: Unsent work in saved state, on every platform (2026-11-04)
As in the original, the draft, an attached photo and a camera shot in progress are written to the ViewModel's `SavedStateHandle` as they change, as plain strings. `SavedStateHandle` is multiplatform now. Koin makes it from the screen's saved state when it builds the ViewModel, so `ChatViewModel` simply asks for one.
- **Android** saves it with the activity, so it survives Android ending the app in the background. Checked on the emulator by killing the app's process, including while the camera app was open.
- **iOS and desktop** keep it while the app runs. iOS doesn't restore a Compose app's saved state after ending it, and desktop doesn't end apps that way. CHAT-15 says so.

## D20: Landscape is the chat screen's own shape (2026-11-04)
The original read Android's screen orientation. Shared code has no orientation, so the chat measures its own size: wider than tall is landscape. That covers a phone on its side and a wide desktop window. It is measured on the whole screen, not the list, so the keyboard opening doesn't change it.

## D21: Rotation in shared tests (2026-11-04)
Compose's `StateRestorationTester` is in common code, but its save step is a stub on desktop and iOS (`TODO("Not yet implemented")`, JetBrains issue CMP-7992). `ChatScreenTest` has a small `Rotation` helper that does the same through Compose's common `SaveableStateRegistry`: it saves the screen's state, takes the screen away, and shows it again from that state. It has to bring the screen back in the same place, because saved values are matched by their place in the tree.

Two other test details turned up:
- **`assertIsNotDisplayed()` needs the node to exist.** On desktop a list doesn't create items that are off screen, so "the reply isn't shown" is checked by the top of the list still being shown.
- **`getBoundsInRoot()` is clipped to what is visible.** To check where a tall message really ends, the test uses `getUnclippedBoundsInRoot()`. With the clipped bounds, a test that should have failed passed.

## D22: Shared rules for notifications, thin platform code (2026-11-04)
Chosen in review. As in the original, `ChatRepository` hands every reply to a `Notifier`, which decides whether to show it. The deciding is now common code; only the posting is per platform.
- **`ChatNotifier`** (common) knows whether the chat is on screen and keeps the replies that came while it wasn't. It shows them all, oldest first, and forgets them when the chat is shown again. `ChatNotifierTest` checks these rules once, on every platform.
- **`NotificationTray`** is the per-platform part. It only shows the unread replies or clears them:
  - **Android:** the original's notification, unchanged: one MessagingStyle conversation from Echo, on a high-importance channel, replaced as replies arrive.
  - **iOS:** one notification per reply. They share a thread, so iOS stacks them as one Echo group, newest on top, as Messages does.
  - **Desktop:** a Windows notification from an icon in the system tray (D24).
- **`core:notifications`** is a new core module, as in the original. `core:data` uses `Notifier` to hand over replies and `feature:chat` uses it to say when the chat is on screen. `ChatNotifier` guards its list with a lock from atomicfu, because replies arrive on a background thread while the screen reports from the main one.

## D23: What "on screen" means on each platform (2026-11-04)
The chat screen tells the notifier with `LifecycleResumeEffect`: shown when its lifecycle reaches resumed, hidden when it drops below. Each platform maps this its own way:
- **Android:** the activity is in front.
- **iOS:** the app is active. Going Home, or pulling down Notification Centre, drops it below resumed.
- **Desktop:** the window is focused. An unfocused window is started, and a minimized one is created, so both count as away. Chosen in review over notifying only when minimized, because a chat behind another window isn't being read either.

The photo viewer counts as away on every platform: it takes the chat's place on screen, so the chat reports itself hidden. `ChatNavigationTest` checks that.

## D24: Platform differences in notifications (2026-11-04)
- **Asking first.** Android 13 and later and iOS ask on the first send, as the original did. Android uses its permission launcher, iOS asks the notification centre. Desktop needs no permission. A refusal is left alone.
- **Opening the chat.** `ChatRequests` is a shared signal that `EchoChatApp` listens to: it closes anything above the chat. Android's notification opens `MainActivity` with an `OPEN_CHAT` intent, which it turns into a request in `onNewIntent`. On iOS the notification centre's delegate makes the request. On desktop the tray icon's click does, and the window is also brought forward.
- **iOS over the photo viewer.** iOS hides notifications while their app is active, so the delegate asks for a banner anyway. It runs only when the notifier has already decided the chat isn't on screen.
- **Desktop can't take a notification back.** Java can only show one from a system-tray icon, and has no call to remove it, so `clear()` does nothing there and Windows lists each reply under the app. The icon appears with the first notification rather than at start-up, so it isn't there until it is needed. Run from Gradle, Windows names the sender after the Java runtime, "OpenJDK Platform binary"; a packaged app would carry its own name.

## D25: Notifications in tests (2026-11-04)
- **Android local tests, in one module.** Posting a notification can only be checked against Android's own notification manager, so `core:notifications` turns on Android local tests and runs `AndroidNotificationTrayTest` with Robolectric, as the original did. It has no Compose UI tests, which Android local tests can't run (D6). Robolectric's Android 16 runtime needs one JDK package opened to it.
- **Device and simulator tests allow notifications.** Android's end-to-end test grants the permission before it starts, so the first send doesn't stop for the prompt. The iOS test taps Allow when iOS asks, and a new iOS test sends, goes Home, sees the banner, taps it and lands back in the chat.
- **The notification centre needs an app.** `UNUserNotificationCenter` fails in the shared tests on the iOS simulator, which run without an app bundle. So the iOS tray looks it up only when it first posts, and the iOS app installs the delegate as it starts, rather than Koin.
- **Swift doesn't see Kotlin's default arguments.** `initKoin` now takes the platform's own set-up, such as Android's context, so Swift calls a separate `initKoin()` with none.
