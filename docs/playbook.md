# Playbook

How each slice goes from the original spec to done on every platform. It is short enough to follow by hand.

## Building a slice
1. **Spec.** Port the slice's criteria from the original spec into [spec.md](spec.md), keeping their IDs. Write down anything that differs by platform, and log the reason in [decisions.md](decisions.md).
2. **Tests and stubs.** Port the tests into `commonTest`, or into `androidApp` device tests for Android-only behaviour, with the real types stubbed out. Run them and check each fails for the expected reason.
3. **Code.** Write the smallest shared code that passes, then tidy it while the tests pass. Platform code only where shared code can't do the job.
4. **Checkpoint.** Run `./gradlew allTests` on Windows and on the Mac, run the Android device tests and the iOS end-to-end test, and check the slice by hand on an Android phone, on desktop and in a browser. Write up the results in [verification](verification/) with screenshots. Mark the rows ✅.
5. **Commit.** Only after the code has been read and approved.

## Changing behaviour later
Add or edit the criterion first, write its failing test, make it pass, then update the spec status and any decision it affects.

## Definition of done for a slice
- `./gradlew allTests` passes on Windows, where it includes headless Chrome, and on the Mac, where it adds the iOS simulator.
- `./gradlew :androidApp:connectedDebugAndroidTest` passes on a phone or emulator, and the iOS end-to-end test passes on the simulator.
- Every criterion in the slice is ✅, and the app does the same on Android, desktop, iOS and the web.
