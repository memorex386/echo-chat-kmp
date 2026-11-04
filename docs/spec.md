# Spec: Echo chat, multiplatform

## Outcome
The same chat as my Android take-home, built once in Kotlin Multiplatform and run on Android, desktop and iOS. I type a message, or attach a photo from the gallery or camera, and press Send. My message appears on the right, at the bottom of the list, and the input clears. While Echo is "typing", three bouncing dots show on the left. Five seconds after I sent it, Echo's reply appears on the left beneath my message, echoing what I sent. Tapping a photo opens it full screen. Messages are kept in memory while the app runs, as the brief allows.

The behaviour is the original's. The criteria keep their original IDs, so each one can be compared with the Android-only version. They are rebuilt one slice at a time, in the original's order.

Out of scope: keeping messages after the app ends, sign-in, more than one conversation, timestamps, read receipts and a real network.

## Acceptance criteria
Each criterion has a test whose name starts with its ID.
Status: ⬜ not started · 🔴 test written and failing · ✅ passing.

Where tests run:
- **common**: `commonTest`. It runs on the JVM here (`allTests`) and on the iOS simulator on a Mac.
- **device**: `androidApp` device tests, on an Android phone or emulator.
- **iOS app**: `iosAppUITests`, on the iOS simulator.

### Slice 1: text chat
| ID | Criterion | Tests (module, where) | Status |
|---|---|---|---|
| CHAT-01 | When I send "Hello", "Hello" becomes the last message and it is mine. | `ChatRepositoryTest` (core:data, common) | ✅ |
| CHAT-02 | With no text and no photo attached, Send is disabled. Whitespace counts as no text. | `ChatViewModelTest`, `ChatScreenTest` (feature:chat, common) | ✅ |
| CHAT-03 | After I send, the input is cleared. | `ChatViewModelTest` (feature:chat, common) | ✅ |
| CHAT-04 | After I send "Hello", no reply exists at 4.999 s. At 5 s the reply "You said: Hello" is added after it. | `ChatRepositoryTest` (core:data, common) | ✅ |
| CHAT-05 | When I send "A" and then "B" one second later, the messages end up in the order A, B, reply to A, reply to B. | `ChatRepositoryTest` (core:data, common) | ✅ |
| CHAT-06 | My messages sit against the right edge and replies against the left edge. | `ChatScreenTest` (feature:chat, common) | ✅ |
| CHAT-07 | When a new message arrives, or I send one, the list scrolls smoothly to show it. | `ChatScreenTest` (feature:chat, common) | ✅ |
| CHAT-08 | After the screen rotates, the messages and the unsent text are still there. Android only: desktop and iOS don't rebuild the screen when it turns. | `ChatEndToEndTest` (androidApp, device) | ✅ |
| CHAT-09 | With no messages, the screen says that replies arrive after 5 seconds. | `ChatScreenTest` (feature:chat, common) | ✅ |
| CHAT-10 | From the moment I send until the last waiting reply arrives, typing dots show below the messages. One indicator covers any number of waiting replies. | `ChatRepositoryTest` (core:data), `ChatScreenTest` (feature:chat), both common | ✅ |
| CHAT-11 | The top bar shows Echo's name and avatar, and each reply and the typing dots show Echo's avatar. My messages have none. | `ChatScreenTest` (feature:chat, common) | ✅ |
| CHAT-12 | A reply still arrives if I open the photo viewer, or the chat screen is recreated, while it is on its way. | `ChatRepositoryTest` (core:data, common) | ✅ |
| CHAT-13 | Emoji survive the round trip: sending "👋🏽" gets the reply "You said: 👋🏽". | `ChatRepositoryTest` (core:data, common) | ✅ |

End-to-end checks, in `ChatEndToEndTest` (androidApp) and `ChatEndToEndUITests` (iosApp): each types "Hello", sends it, sees the typing dots, then sees "You said: Hello" within 6 s of real time.

### Slice 2: photos
| ID | Criterion | Tests (module, where) | Status |
|---|---|---|---|
| PHOTO-01 | When I pick a photo from the gallery, or take one with the camera, it shows as a preview above the input with a remove button, and Send is enabled even without text. Desktop has no camera app, so it offers only the gallery, as a file dialog for images. | `ChatViewModelTest`, `ChatScreenTest` (feature:chat, common) | ✅ |
| PHOTO-02 | Pressing remove on the preview drops the photo. | `ChatViewModelTest`, `ChatScreenTest` (feature:chat, common) | ✅ |
| PHOTO-03 | Sending a photo with an optional caption adds one message holding both, and clears the preview. | `ChatRepositoryTest` (core:data), `ChatViewModelTest` (feature:chat), both common | ✅ |
| PHOTO-04 | Echo's reply to a photo carries the same photo, with "You said: caption" or, without a caption, "You sent a photo". | `SimulatedChatServiceTest` (core:network, common) | ✅ |
| PHOTO-05 | Tapping a photo opens it full screen. Back returns to the chat where I left it. | `ChatNavigationTest` (shared, common) | ✅ |

End-to-end check on iOS, in `ChatEndToEndUITests` (iosApp): it picks the newest photo in the simulator's library, sends it, sees "You sent a photo", opens Echo's photo full screen and comes back.

### Slice 3: polish
| ID | Criterion | Tests (module, where) | Status |
|---|---|---|---|
| CHAT-14 | When I've scrolled up and a reply arrives, the list stays where I am, and a round down-arrow appears just above the input. Tapping it scrolls to the newest message, and the arrow goes away at the bottom. Sending my own message always scrolls to it. | `ChatScreenTest` (feature:chat, common) | ✅ |
| CHAT-15 | A half-written message, an attached photo and a photo the camera is still taking all survive rotating and leaving the app. On Android they also survive Android ending the app in the background; iOS and desktop don't end an app that way and bring it back. | `ChatViewModelTest` (feature:chat, common); ending the app is also checked on an Android device | ✅ |
| CHAT-16 | In landscape, bubbles use at most two thirds of the width. Landscape means the chat is wider than it is tall: a phone on its side, or a wide desktop window. | `ChatScreenTest` (feature:chat, common) | ✅ |
| CHAT-17 | A very long message wraps inside its bubble, even a word longer than a line. The input grows to 4 lines, then scrolls. | `ChatScreenTest` (feature:chat, common) | ✅ |
| CHAT-18 | When the keyboard opens, the input grows or the screen rotates while I'm at the bottom, the newest message stays in view, with no jump arrow. | `ChatScreenTest` (feature:chat, common) | ✅ |
| PHOTO-06 | A photo stands on its own as a rounded image, not inside a bubble. Its caption, if any, gets a bubble of its own just below, on the same side, as in Google Messages. | `ChatScreenTest` (feature:chat, common) | ✅ |

CHAT-08 is checked by rotating to landscape and back, on an Android device.

### Motion
Checked by screen recording, not by tests.
- A new bubble fades in, rises 12 dp and grows from 92% to full size, from its own bottom corner (bottom-right for mine, bottom-left for Echo's), over 220 ms, slowing at the end. Only messages that arrive while the chat is showing animate, and only once. A bubble scrolled back into view does not.
- Bubbles already on screen slide up to make room. The typing dots fade out as the reply replaces them.
- Photos fade in as they load, over a faint placeholder of the same size.
- The list scrolls smoothly to new messages. The down-arrow fades and scales in and out.
- All of it follows the system's animation setting, so "Remove animations" on Android turns it off.

### Slice 4: notifications
| ID | Criterion | Tests (module, where) | Status |
|---|---|---|---|
| NOTIFY-01 | When a reply arrives while the chat isn't on screen, a notification from Echo shows the reply. Not on screen means the app is in the background, the photo viewer is open, or, on desktop, the chat window isn't focused. | `ChatRepositoryTest` (core:data), `ChatNotifierTest` (core:notifications), both common; `AndroidNotificationTrayTest` (core:notifications, Android local); `ChatEndToEndUITests` (iOS simulator) | ✅ |
| NOTIFY-02 | While the chat is on screen, replies don't notify, and showing the chat clears any notification from Echo. Desktop can't take a notification back once Windows has shown it. | `ChatNotifierTest` (core:notifications), `ChatNavigationTest` (shared), both common; `AndroidNotificationTrayTest` (Android local) | ✅ |
| NOTIFY-03 | Replies that arrive while I'm away stay together, in the platform's own way. Android stacks them in one conversation notification, oldest first. iOS groups them in one Echo stack, newest on top, as Messages does. Windows lists each one under the app. | `ChatNotifierTest` (core:notifications, common); `AndroidNotificationTrayTest` (Android local) | ✅ |
| NOTIFY-04 | Tapping the notification opens the chat, even if the photo viewer was open. On desktop, clicking it brings the chat window to the front. | `ChatNavigationTest` (shared, common); `AndroidNotificationTrayTest` (Android local); `ChatEndToEndUITests` (iOS simulator) | ✅ |
| NOTIFY-05 | On Android 13 and later and on iOS, the app asks for permission to notify the first time I send a message. If I refuse, the chat works the same and nothing is posted. Desktop needs no permission. | `AndroidNotificationTrayTest` (Android local); `ChatEndToEndUITests` (iOS simulator) allows it; refusing was checked by hand on the emulator | ✅ |

"Android local" tests run on the JVM with Robolectric, as `./gradlew allTests` runs them, for the parts only Android has.

### Slice 5: message actions
| ID | Criterion | Tests (module, where) | Status |
|---|---|---|---|
| ACTION-01 | Long-pressing a message opens a small menu at that bubble: Copy and Delete for text, and Save, Copy and Delete for a photo. A photo's caption is text, so its bubble gets the text menu. On desktop, right-clicking opens the same menu. | `ChatScreenTest` (feature:chat, common) | ✅ |
| ACTION-02 | Copy on text puts the message's text on the clipboard. Where the system doesn't confirm a copy itself (iOS, desktop, and Android before 13), the app says "Copied". | `ChatScreenTest` (feature:chat), `MessageActionsTest` (shared), both common | ✅ |
| ACTION-03 | Copy on a photo puts the picture on the clipboard, so it can be pasted into apps that accept images. Android shares the photo's file through a content link, as the original did. iOS and desktop copy the image itself. | `ChatScreenTest` (feature:chat, common); `PhotoClipTest` (feature:chat, desktop and iOS); `ChatEndToEndTest` (androidApp, device: the real content link) | ✅ |
| ACTION-04 | Save on a photo keeps a copy where the platform keeps pictures. Android saves to Pictures/Echo Chat and says "Saved to Photos"; it needs Android 10 or later, and Save isn't offered before that. iOS saves to Photos, asking once for permission to add photos, and says "Saved to Photos". Desktop opens a Save dialog at the Pictures folder; cancelling saves nothing. Each says when it couldn't save. | `ChatViewModelTest`, `ChatScreenTest` (feature:chat, common); `ChatEndToEndUITests` (iOS simulator: Save to Photos); the saved file is checked by hand on Android and desktop | ✅ |
| ACTION-05 | Delete removes the message, the whole message if it has a photo and caption, straight away, and shows "Message deleted" with Undo for about 4 seconds. Undo puts it back in the same place. Only the pressed message goes: an echo stays unless I delete it too. | `ChatRepositoryTest` (core:data), `ChatViewModelTest` (feature:chat), `MessageActionsTest` (shared), all common; `ChatEndToEndUITests` (iOS simulator) | ✅ |
| ACTION-06 | Deleting a message before its reply arrives doesn't stop the reply. Echo keeps typing, and the reply arrives on time. | `ChatRepositoryTest` (core:data, common) | ✅ |

### Slice 6: keyboard
| ID | Criterion | Tests (module, where) | Status |
|---|---|---|---|
| KEY-01 | Tapping the input while the attach panel is open closes the panel, so the panel and the keyboard never show together. | `ChatScreenTest` (feature:chat, common); `ChatEndToEndUITests` (iOS simulator) | ✅ |
| KEY-02 | Back closes the attach panel. Only after that does Back leave the chat. Back is Android's Back, Esc on desktop, and the swipe in from the screen's edge on iOS. | `ChatScreenTest` (feature:chat, common); `ChatEndToEndUITests` (iOS simulator) | ✅ |
| KEY-03 | If the keyboard was up when the screen rotates, it comes back up afterwards, with the cursor at the end of what I'd typed. If I had closed it, it stays closed. Android rebuilds the screen when it rotates, which is where this matters. iOS keeps the screen and the keyboard through a rotation. Desktop has neither. | `ChatScreenTest` (feature:chat, common); `ChatEndToEndTest` (androidApp, device); `ChatEndToEndUITests` (iOS simulator) | ✅ |
| KEY-04 | In landscape on Android, typing uses the keyboard's full-screen text box with its own Send button, as in Google Messages, rather than squeezing the chat to a strip. The chat shows again when the keyboard closes. iPhone keyboards have no full-screen box, so iOS keeps the chat above the keyboard, as Messages does. | `ChatEndToEndTest` (androidApp, device); the full-screen box is checked by hand | ✅ |
| KEY-05 | On desktop, Enter sends the message and Shift+Enter starts a new line. New in this version: found while checking slice 1, where Enter only added a line and a desktop keyboard has no Send key. Phones and tablets keep their own keyboard's Send key, and Enter on a hardware keyboard starts a new line there, as before. | `EnterToSendTest` (feature:chat, desktop) | ✅ |

Known issue on iOS: after a rotation with the keyboard up, the chat sits too high until the keyboard closes (D33).

## Screen states
The states and the look are the original's, in [mocks/chat.html](mocks/chat.html). Slice 1 covers S-01 Empty, S-02 Typing, S-03 Echo is typing, S-04 Replied, S-05 Quick sends, S-06 Long chat and S-08 Dark. Slice 2 adds S-09 Photo attached, S-10 Photos and S-11 Photo viewer. Slice 3 adds S-07 Long text and S-13 Scrolled up when a reply arrives, and draws S-10 with each photo apart from its caption, which the mock doesn't show. Slice 5 adds S-14 Message menu open and S-15 Just deleted, which the mock doesn't show either: see the slice's checkpoint.
