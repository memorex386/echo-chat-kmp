# Spec: Echo chat, multiplatform

## Outcome
The same chat as my Android take-home, built once in Kotlin Multiplatform and run on Android, desktop and iOS. I type a message and press Send. My message appears on the right, at the bottom of the list, and the input clears. While Echo is "typing", three bouncing dots show on the left. Five seconds after I sent it, Echo's reply appears on the left beneath my message, echoing what I sent. Messages are kept in memory while the app runs, as the brief allows.

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
| CHAT-02 | With no text, Send is disabled. Whitespace counts as no text. | `ChatViewModelTest`, `ChatScreenTest` (feature:chat, common) | ✅ |
| CHAT-03 | After I send, the input is cleared. | `ChatViewModelTest` (feature:chat, common) | ✅ |
| CHAT-04 | After I send "Hello", no reply exists at 4.999 s. At 5 s the reply "You said: Hello" is added after it. | `ChatRepositoryTest` (core:data, common) | ✅ |
| CHAT-05 | When I send "A" and then "B" one second later, the messages end up in the order A, B, reply to A, reply to B. | `ChatRepositoryTest` (core:data, common) | ✅ |
| CHAT-06 | My messages sit against the right edge and replies against the left edge. | `ChatScreenTest` (feature:chat, common) | ✅ |
| CHAT-07 | When a new message arrives, or I send one, the list scrolls smoothly to show it. | `ChatScreenTest` (feature:chat, common) | ✅ |
| CHAT-08 | After the screen rotates, the messages and the unsent text are still there. Android only: desktop and iOS don't rebuild the screen when it turns. | `ChatEndToEndTest` (androidApp, device) | ✅ |
| CHAT-09 | With no messages, the screen says that replies arrive after 5 seconds. | `ChatScreenTest` (feature:chat, common) | ✅ |
| CHAT-10 | From the moment I send until the last waiting reply arrives, typing dots show below the messages. One indicator covers any number of waiting replies. | `ChatRepositoryTest` (core:data), `ChatScreenTest` (feature:chat), both common | ✅ |
| CHAT-11 | The top bar shows Echo's name and avatar, and each reply and the typing dots show Echo's avatar. My messages have none. | `ChatScreenTest` (feature:chat, common) | ✅ |
| CHAT-12 | A reply still arrives if the chat screen is recreated, or I leave it, while it is on its way. | `ChatRepositoryTest` (core:data, common) | ✅ |
| CHAT-13 | Emoji survive the round trip: sending "👋🏽" gets the reply "You said: 👋🏽". | `ChatRepositoryTest` (core:data, common) | ✅ |

End-to-end checks, in `ChatEndToEndTest` (androidApp) and `ChatEndToEndUITests` (iosApp): each types "Hello", sends it, sees the typing dots, then sees "You said: Hello" within 6 s of real time.

### Later slices
Each is ported from the original spec when its slice starts, with any platform differences written into the criterion.

| Slice | Criteria | Status |
|---|---|---|
| 2. Photos: gallery and camera, photo echo, full-screen viewer | PHOTO-01 to PHOTO-05 | ⬜ |
| 3. Polish: jump arrow, unsent work kept, landscape width, long text, photo captions | CHAT-14 to CHAT-18, PHOTO-06 | ⬜ |
| 4. Notifications | NOTIFY-01 to NOTIFY-05 | ⬜ |
| 5. Message actions: copy, save, delete with Undo | ACTION-01 to ACTION-06 | ⬜ |
| 6. Keyboard: attach panel, rotation, landscape typing, Enter on desktop | KEY-01 to KEY-05 | ⬜ |

New in this version, built with slice 6:

| ID | Criterion | Tests (module, where) | Status |
|---|---|---|---|
| KEY-05 | On desktop, Enter sends the message and Shift+Enter starts a new line. Found while checking slice 1: Enter only added a line, and a desktop keyboard has no Send key. | Not written yet | ⬜ |

## Screen states
The states and the look are the original's, in [mocks/chat.html](mocks/chat.html). Slice 1 covers S-01 Empty, S-02 Typing, S-03 Echo is typing, S-04 Replied, S-05 Quick sends, S-06 Long chat and S-08 Dark.
