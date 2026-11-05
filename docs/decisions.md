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
| `webApp` | `main()`, the web page and the browser's Back button. Web only. Added in slice 7 (D37). |
| `shared` | `EchoChatApp()`: the theme, the Navigation 3 back stack and the Koin start-up. iOS gets it as one framework, `Shared`. |
| `feature:chat` | Chat screen, photo viewer, `ChatViewModel` |
| `core:model` | `Message`, `Author`, `Image`. No dependencies. |
| `core:data` | `ChatRepository`, the only place messages change, and `ImageStore` |
| `core:database` | `MessageDao` and its in-memory implementation |
| `core:network` | `ChatService` and the simulated echo service |
| `core:designsystem` | Theme, avatar, typing dots, icons |
| `core:notifications` | `Notifier`, the shared rules, and each platform's notifications. Added in slice 4 (D22). |
| `core:testing` | Fakes shared by the modules' tests |

Convention plugins in `build-logic` hold the shared setup: `echochat.kmp.library`, `echochat.kmp.compose`, `echochat.kmp.feature`, one each for the Android, desktop and web apps, and `echochat.web.tools` for Kotlin's web tools (D38).

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

## D26: Message actions carried over, with right-click on desktop (2026-11-04)
The original's message actions are unchanged:
- Long-pressing a bubble opens a small menu at it: Copy and Delete for text, and Save, Copy and Delete for a photo. A caption is text, so it gets the text menu.
- Delete removes only the pressed message. The store marks it as deleted rather than removing it, so Undo brings it back in the same place. A reply already on its way still arrives.
- The Undo snackbar is Material's short one, about 4 seconds, set explicitly: with an action, Material's default is to stay until tapped.

New in this version:
- **Right-click opens the same menu**, chosen in review, because desktop users expect it. It works wherever there is a mouse, so on Android and iPad too.
- **The menu is Material's on every platform.** It isn't Windows' or iOS's own context menu.

## D27: Copying on each platform (2026-11-04)
- **Text** goes through Compose's multiplatform clipboard as each platform's own clip: Android's `ClipData`, iOS's plain text, and AWT's `StringSelection` on desktop.
- **Photos:**
  - **Android** copies a content link through a `FileProvider`, as the original did, and Android lets the app that pastes it read the photo. The provider, `<package>.images`, is declared in `feature:chat` and covers only the cache's `images` folder.
  - **iOS** puts the picture on `UIPasteboard`, because Compose's iOS clip holds text only. Compose 1.12 deprecates `Clipboard.nativeClipboard` in favour of platform extensions but has none for iOS yet, so that one use is suppressed.
  - **Desktop** copies the picture, for apps like Paint and Word, and the file, for File Explorer. Java can't read every format as a picture (HEIC, for one), so the file is there to fall back on.
- **"Copied":** Android 13 and later confirm a copy themselves. iOS and desktop don't, so the app says "Copied" there, and on older Android.
- **Real file extensions.** Photos in the cache now keep the picked photo's extension, or `.jpg` from the camera, so the apps they are pasted or saved into can tell their type.

## D28: Saving on each platform (2026-11-04)
- **Android** is unchanged from the original: a copy goes into Pictures/Echo Chat through `MediaStore`, on Android 10 and later only. FileKit can save to the gallery too, but into a folder of its own name, so the original's code was kept.
- **iOS** saves into Photos with FileKit. iOS asks once for permission to add photos, with the reason in the app's `NSPhotoLibraryAddUsageDescription`.
- **Desktop** opens Windows' Save dialog at the Pictures folder, chosen in review over saving straight into Pictures, as desktop chat apps do. The suggested name is "Echo Chat" and the time.
  - Cancel saves nothing.
  - A save says nothing afterwards, because choosing the place was confirmation enough.
  - A failure says "Couldn't save the photo", as on the phones.

## D29: Testing message actions (2026-11-04)
- **A test clipboard.** `TestClipboard` in `core:testing` stands in for the system clipboard through `LocalClipboard`, and `plainText()` reads a clip the way each platform keeps it. Compose's `Clipboard` interface brings `compose-ui` into `core:testing`, but no UI.
- **Copying a photo is tested on each platform.** Desktop and iOS have their own `PhotoClipTest`. The iOS one uses a pasteboard of its own, so the simulator's clipboard is left alone. Android's runs on a device, through the real `FileProvider`, which Robolectric can't run on Windows, as the original found.
- **The whole app with the real repository.** `MessageActionsTest` starts the app's real Koin modules and swaps in fakes only for the clipboard, image store and notifier. Delete and Undo go through the real store.
- **Saving to the real Photos or Pictures** is checked by the iOS UI test and by hand.

## D30: The keyboard, the attach panel and landscape, carried over (2026-11-04)
The original's choices, from its check with Samsung Keyboard on a Galaxy S23, are unchanged:
- **The attach panel and the keyboard take turns.** Opening the panel takes the cursor out of the input, which closes the keyboard. Putting the cursor back in the input closes the panel. Back closes the panel before it leaves the chat.
- **The keyboard survives a rotation.** The screen saves whether the keyboard is up, and that value changes only while the screen is resumed, because a rotation takes the keyboard down as it rebuilds the screen. If it was up, the input takes the cursor again, at the end of the draft, which brings the keyboard back.
- **Full-screen typing in landscape**, as Google Messages does. Compose asks keyboards never to go full screen, and the input lifts that through `InterceptPlatformTextInput`, which is marked experimental.

How each piece works across platforms:
- **Back** is Navigation Event's `NavigationBackHandler`, the multiplatform back handling Navigation 3 is built on. Android's Back, Esc on desktop and iOS's swipe in from the screen's edge all reach it. Its library is declared at 1.1.0, the version Compose and Navigation 3 already resolve to.
- **Whether the keyboard is up** comes from `isKeyboardUp()`: `isImeVisible` on Android, which isn't in Compose Multiplatform's common code, the keyboard's inset on iOS, and always false on desktop, which has no on-screen keyboard. `ChatScreen` takes it as a parameter, so shared tests can say when the keyboard is up.
- **A rotation** only rebuilds the screen on Android. iOS keeps the screen, and the keyboard with it, which the iOS UI test checks, though Compose then lays the chat out too high (D33). Desktop has neither.
- **The full-screen keyboard** is Android only. iPhone keyboards have no full-screen text box, so iOS keeps the chat above the keyboard, as Messages does.
- **Device tests instead of Robolectric.** The original checked the full-screen request and its Send key with Robolectric. `feature:chat` has no Android local tests (D6), so `ChatEndToEndTest` checks them on a device, next to the test that rebuilds the activity while typing.

## D31: Enter sends on desktop (2026-11-04)
This carries out D11. On desktop, Enter and the number pad's Enter send, and Shift+Enter starts a new line, as in desktop chat apps. With nothing to send, Enter does nothing rather than adding an empty line. The input catches Enter before the text field does, through `onPreviewKeyEvent`. Phones and tablets are unchanged: their keyboard's Send key sends, and Enter on a hardware keyboard starts a new line.

## D32: The iOS simulator keeps its on-screen keyboard (2026-11-04)
The iOS UI tests tap the keyboard's Send key, so they need the on-screen keyboard. During this slice the simulator stopped showing it, even for the last commit's code and after a restart: its keyboard settings had `AutomaticMinimizationEnabled` on, which collapses the keyboard when iOS thinks a hardware keyboard is attached. Echo's own simulator now has it off:

`xcrun simctl spawn <simulator> defaults write com.apple.keyboard.preferences AutomaticMinimizationEnabled -bool NO`

A new simulator needs the same before the UI tests run. It is a setting of the test device, not of the app.

## D33: Known issue: on iOS, a rotation with the keyboard up slides the chat up (2026-11-04)
After the iPhone turns to landscape and back with the keyboard up, the keyboard stays up and the draft is kept, but the whole chat sits higher than it should, with the top bar off the screen. It is back in place once the keyboard closes. It isn't new in this slice: the last commit's code does the same.

The cause is in Compose Multiplatform 1.12.2. It doesn't update the keyboard's inset when the screen turns with the keyboard already up, so the chat no longer makes room for it. Compose's default then slides the whole view up to keep the input clear of the keyboard, which takes the top bar with it.

Turning that slide off (`OnFocusBehavior.DoNothing`) kept the top bar, but left the input behind the keyboard, which is worse, so the default stays. The iOS UI test records the top bar as an expected failure. It isn't strict, so a Compose release that fixes the inset passes too, and the check can then become a plain one.

Rotating with the keyboard up is uncommon on a phone, and closing the keyboard puts it right.

## D34: Photo files kept out of the code the web will share (2026-11-05)
Preparing slice 7, the web. FileKit opens a file by its path, and has a cache folder, only on Android, iOS and desktop: a browser gives a page neither. The photo code used both in `commonMain`, so it wouldn't compile for the web. Nothing changes in how the app behaves, apart from the iOS fix below.
- **The store.** `CacheImageStore` moves to `nonWebMain` in `core:data`, a source set shared by every platform but the web, made the same way as `mobileMain` (D15). It holds the whole `ios` group, not just the iOS targets, so the iOS code in `iosMain` can use it. `commonMain` asks for the platform's store through `platformImageStore()`, and the web gets its own in slice 7. Koin now keeps one store for the whole app, rather than making a new one each time, because a store that keeps photos in memory must be the same one throughout.
- **Showing photos.** The screens hand Coil the message's `Image`. A Coil mapper in `EchoChatApp` asks the store which file holds it (`ImageStore.fileFor`), and FileKit's Coil support loads that file as before. The screens no longer make a file from a path.
- **Copying a photo** gets its file from the store too, through the ViewModel.
- **The camera.** When Android ends the app with the camera open, all the ViewModel needs afterwards is the `Image` it attaches. So it now saves only that, not the file's path as well.
- **A bug this found: copying a photo on iOS copied nothing.** The store names its files with FileKit's `absolutePath()`, which on iOS is a `file://` URL. The iOS copy read the picture from that string as if it were a plain path. Its test used a plain path, so it passed anyway. The test now goes through the store, as the app does, and the copy uses FileKit's `path`.
- **The iOS end-to-end test's timing.** After Save, the test waited up to 5 s for iOS's permission question before looking for "Saved to Photos". Once permission has been given, iOS doesn't ask again, and the 4-second message was gone by the time the test looked. It passed before only because those runs started from a fresh install. It now watches for both at once.

## D35: The web, through Kotlin/Wasm, in desktop browsers (2026-11-05)
Chosen in review, so anyone can try the app without installing anything.
- **Kotlin/Wasm** compiles the shared Kotlin to WebAssembly, and Compose draws the same screens on the page. JetBrains rates it Beta, where Android, desktop and iOS are Stable. Every library the app uses already publishes a web build at the versions in use (D9).
- **Desktop browsers only:** Chrome and Edge 119, Firefox 120 and Safari 18.2 or later, the first versions with the garbage collection Kotlin/Wasm needs. Phones keep the Android and iOS apps, because Compose's handling of on-screen keyboards in phone browsers is weak.
- **No Kotlin/JS fallback** for older browsers. It would mean building everything a second time for few users, so an older browser gets a message instead (WEB-02).
- **The browser's Back button** works as the app's Back, since Compose leaves it unconnected. Left alone, it would leave the page from the photo viewer, and the chat, kept only in memory, would be lost.

## D36: Published to GitHub Pages on every push (2026-11-05)
Chosen in review. A GitHub Actions workflow runs on every push to `main`. It runs the shared tests on the JVM and in headless Chrome on a Linux runner, and only if they pass does it build the web app and publish it to GitHub Pages at https://memorex386.github.io/echo-chat-kmp/. The iOS tests stay on the Mac, because a macOS runner would take much longer. The README links to the page.
- **The wrapper.** `gradlew` is now marked executable in git, which a Linux runner needs.
- **Caching.** GitHub Pages lets browsers keep its files for 10 minutes, so for a few minutes after a publish, someone coming back may still get the old version.
- **A longer wait.** The first publish stalled three times at the action's 10-minute limit, with the tests and build passing each time, a problem others report too (deploy-pages issue #406). So the publish step waits up to 20 minutes.

## D37: How the web version works (2026-11-05)
- **`webApp`** is a thin launcher, like `desktopApp`. It starts Koin and shows `EchoChatApp()` on the page with Compose's `ComposeViewport`. The page checks for WebAssembly's garbage collection before it loads anything, and shows WEB-02's message if it's missing. It also turns off the browser's own right-click menu: the app has its own, and the browser's would take the focus and close it.
- **Photos** are kept in memory by `MemoryImageStore` (D34). Each is the file the browser picked, under a key of its own. Save has the browser download it, named "Echo Chat" and the time, and says nothing when it works, because the browser shows the download.
- **Copying a photo** converts it to a PNG with Skia first. Browsers let a page copy PNGs and no other kind of picture.
- **A refused copy.** A browser can refuse a copy, for a page without focus, for example. When I checked by hand, the refusal escaped uncaught and stopped the whole app. The copy now catches it, on every platform, and says nothing rather than "Copied".
- **Notifications** use the browser's Notification API, behind a small `WebNotifications` interface so tests can stand in for the browser. Each reply gets its own, as on Windows, and unlike Windows the page closes them when the chat shows. Clicking one brings the tab forward and asks for the chat. The app asks for permission on the first send, which is a click, as Firefox and Safari require.
- **On screen** (D23) carries over. Compose's web window pauses its lifecycle when the window loses focus, and stops it when the tab is hidden, so the chat counts as away in both cases.
- **Back.** Compose already makes Esc the app's Back on the web. The browser's Back button isn't connected, so `BrowserBackInput` adds a history entry while anything in the app can go back, and turns the browser's Back into the app's (D35).
- **Enter sends** on desktop and the web alike. That code moved to `nonMobileMain`, a source set shared by the two, with the missing camera and status bar.
- **Size.** The release build is about 13 MB before compression, 8.6 MB of it Compose's drawing engine.

## D38: Testing in a browser (2026-11-05)
- **Headless Chrome.** Every module's `commonTest` also runs in headless Chrome, through Karma, as part of `allTests`. Getting there took five fixes:
  - **Download sites.** Kotlin downloads Node.js, Yarn and Binaryen for the web, and adds their sites to each project, which `settings.gradle.kts` forbids. The settings list them instead, and `echochat.web.tools` tells Kotlin not to.
  - **An executable per Compose module.** Compose bundles its drawing engine for browser tests only into an executable, so each Compose module builds one (CMP-4906).
  - **Strings.** In a browser, Compose fetches each string the first time it's shown, and the UI tests don't wait for that, so they found no text. `loadStrings()` loads a module's strings before a test shows its screen, and a string that's already loaded shows straight away. The chat feature's `Res` class is public for this, so the tests in `shared` and `webApp` can load its strings.
  - **Serving the strings.** Karma serves the test page but not the files next to it. A shared setting in `gradle/karma.config.d` serves `composeResources`, as Compose's own resources library does.
  - **One browser at a time.** Several modules testing in headless Chrome at once starved each other until Karma gave up. So browser tests take turns, while the rest of the build stays parallel, and Karma waits 2 minutes rather than 30 seconds.
- **Web-only tests** stand in for the browser where a test can't drive it: notifications and history sit behind small interfaces, and test photos are empty browser files.
- **By hand.** The browser pane in the Claude app runs Chromium but blocks notifications and the clipboard. So a stand-in for the Notification API checked the app's side of notifications, and a stand-in file picker supplied a photo. Firefox was checked with Playwright's Firefox, headless, which used the real file picker and accepted a real copy. WebKit was checked in Safari on the iOS simulator. Desktop Safari wasn't checked.
