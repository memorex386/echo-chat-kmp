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

### Later slices
Each is ported from the original spec when its slice starts, with any platform differences written into the criterion.

| Slice | Criteria | Status |
|---|---|---|
| 4. Notifications | NOTIFY-01 to NOTIFY-05 | ⬜ |
| 5. Message actions: copy, save, delete with Undo | ACTION-01 to ACTION-06 | ⬜ |
| 6. Keyboard: attach panel, rotation, landscape typing, Enter on desktop | KEY-01 to KEY-05 | ⬜ |

New in this version, built with slice 6:

| ID | Criterion | Tests (module, where) | Status |
|---|---|---|---|
| KEY-05 | On desktop, Enter sends the message and Shift+Enter starts a new line. Found while checking slice 1: Enter only added a line, and a desktop keyboard has no Send key. | Not written yet | ⬜ |

## Screen states
The states and the look are the original's, in [mocks/chat.html](mocks/chat.html). Slice 1 covers S-01 Empty, S-02 Typing, S-03 Echo is typing, S-04 Replied, S-05 Quick sends, S-06 Long chat and S-08 Dark. Slice 2 adds S-09 Photo attached, S-10 Photos and S-11 Photo viewer. Slice 3 adds S-07 Long text and S-13 Scrolled up when a reply arrives, and draws S-10 with each photo apart from its caption, which the mock doesn't show.
