# Echo Chat, multiplatform

My Echo chat take-home, rebuilt in Kotlin Multiplatform so I can learn it. The original was an Android app: a chat with one contact, Echo, who repeats back everything you say after 5 seconds. This version runs the same shared code on Android, desktop and iOS.

The brief asked for two things: send a text message that appears on the right, and get a simulated reply on the left 5 seconds later. The original added photos, notifications, message actions and keyboard handling on top. Those come across here one slice at a time.

## Where it is
| Slice | Android | Desktop | iOS |
|---|---|---|---|
| 1. Text chat: send, typing dots, echo after 5 s | ✅ | ✅ | ✅ |
| 2 to 6. Photos, polish, notifications, message actions, keyboard | ⬜ | ⬜ | ⬜ |

The criteria and their status are in [docs/spec.md](docs/spec.md), and the choices behind the build are in [docs/decisions.md](docs/decisions.md).

## Running it
- `./gradlew allTests` runs every shared test, on the JVM on Windows and on the iOS simulator on a Mac. No emulator needed.
- `./gradlew :androidApp:connectedDebugAndroidTest` runs the Android device tests on a running emulator or phone.
- `./gradlew :androidApp:installDebug` installs the Android app. It runs on Android 8.0 and later.
- `./gradlew :desktopApp:run` runs the desktop app.
- iOS, on a Mac: open `iosApp/iosApp.xcodeproj` in Xcode and run the `iosApp` scheme. Xcode builds the shared code through Gradle, so the Mac needs JDK 21 too. Test runs the iOS end-to-end test.

Built with JDK 21, Android SDK 37, Xcode 27, Kotlin 2.4.21 and Compose Multiplatform 1.12.2.

## Ambiguities
Carried over from the original:
- **What the reply says.** Echo repeats the message: "You said: Hello".
- **Messages sent close together.** Each message gets its own reply, 5 seconds after that message. You can keep sending while replies are on their way.
- **"Beneath the previous message".** Replies are added to the bottom in the order they arrive. If I send two messages quickly, both come before both replies.
- **Empty messages.** Send stays disabled until there is text, and spaces around a message are dropped.
- **Persistence.** In memory, as the brief allows, so messages are gone when the app ends.

New in this version:
- **Platforms.** Android, desktop (on the JVM, checked on Windows) and iOS, from one Kotlin codebase with Compose Multiplatform.
