# AGENTS.md

Kotlin Multiplatform rebuild of my Echo chat take-home: a chat with one contact, Echo. You send a message, it appears on the right, typing dots show, and Echo's echo reply appears on the left five seconds later. One shared codebase runs on Android, desktop and iOS. I'm building it to learn KMP, one slice at a time.

## Read before changing anything
- [docs/spec.md](docs/spec.md): the outcome, acceptance criteria (`CHAT-xx`, `PHOTO-xx`, `NOTIFY-xx`, `ACTION-xx`, `KEY-xx`) and which slice each belongs to.
- [docs/decisions.md](docs/decisions.md): choices already made, and why. D3 has the module map.
- [docs/playbook.md](docs/playbook.md): how work moves from spec to passing tests.
- [docs/mocks/chat.html](docs/mocks/chat.html): the agreed look and flow, from the original app. Open it in a browser.

## Commands
Use `gradlew.bat` on Windows.

| Command | What it does |
|---|---|
| `./gradlew allTests` | Every module's `commonTest`, once per target this computer can run: the JVM on Windows, plus the iOS simulator on a Mac. |
| `./gradlew :androidApp:connectedDebugAndroidTest` | The Android device tests, on a running emulator or phone. |
| `./gradlew :desktopApp:run` | Runs the desktop app. |
| `./gradlew :androidApp:installDebug` | Installs the Android app. |
| `./gradlew compileTestKotlinIosSimulatorArm64` | Compiles the shared code and its tests for iOS. Works on Windows; linking and running need a Mac. |

`./gradlew test` doesn't run the shared tests. In a multiplatform build, `allTests` does.

## Rules
- **Spec first.** No behaviour without a criterion, and no criterion without a test whose name starts with its ID.
- **Test first.** Write the test, watch it fail for the expected reason, then write the code.
- **Shared first.** Code goes in `commonMain`. Platform code goes in `androidMain`, `jvmMain` or `iosMain` only when shared code can't do the job, behind an interface or `expect`/`actual`.
- **Layers.** `feature` modules depend on `core` modules, never on each other or on `shared`. Only `core:data` uses `core:database` and `core:network`. `core:model` has no dependencies.
- **Thin apps.** `androidApp`, `desktopApp` and `iosApp` only start the app. Everything they show comes from `shared`.
- **Earn each piece.** Add a module, class or interface only when a feature or test needs it, and record why in `docs/decisions.md`.
- **In memory.** The brief allows in-memory storage. Keep it behind `MessageDao`, and keep the comment saying why.
- **Self-contained tests.** Use virtual time for the 5 s delay, and fakes from `core:testing`. No test touches the network.
- **Names every platform accepts.** Test names in `commonTest` must compile for iOS too: no commas.
- **Code explains itself.** Comment only a non-obvious why.
- **Record choices.** New decisions go in `docs/decisions.md`.
- **Versions** live only in `gradle/libs.versions.toml`. Shared build setup lives in `build-logic`.
- **No binaries** except the Gradle wrapper jar and the screenshots in `docs/verification`. No build output, IDE files or `local.properties` in git.
- **Commits.** Each slice is committed only after I've read and approved it.
