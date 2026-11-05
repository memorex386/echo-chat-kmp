import XCTest

// Runs the real app on an iOS simulator, with the real 5-second wait: the iOS counterpart of
// androidApp's ChatEndToEndTest. It finds Compose's controls through iOS accessibility.
final class ChatEndToEndUITests: XCTestCase {

    private let springboard = XCUIApplication(bundleIdentifier: "com.apple.springboard")

    func testSendingHelloShowsTypingDotsThenTheEcho() {
        let app = XCUIApplication()
        app.launch()

        send("Hello", in: app)

        XCTAssertTrue(app.staticTexts["Hello"].waitForExistence(timeout: 2))
        XCTAssertTrue(app.descendants(matching: .any)["Echo is typing"].waitForExistence(timeout: 2))
        keepScreenshot(of: app, named: "echo-typing")
        XCTAssertTrue(app.staticTexts["You said: Hello"].waitForExistence(timeout: 6))
        keepScreenshot(of: app, named: "replied")
        allowNotificationsIfAsked()

        // ACTION-01 and ACTION-05: long-press, Delete, then Undo puts it back.
        app.staticTexts["Hello"].press(forDuration: 1.0)
        XCTAssertTrue(app.staticTexts["Delete"].waitForExistence(timeout: 3))
        keepScreenshot(of: app, named: "text-menu")
        app.staticTexts["Delete"].tap()
        XCTAssertTrue(app.staticTexts["Message deleted"].waitForExistence(timeout: 3))
        XCTAssertFalse(app.staticTexts["Hello"].exists)
        keepScreenshot(of: app, named: "deleted")
        app.buttons["Undo"].tap()
        XCTAssertTrue(app.staticTexts["Hello"].waitForExistence(timeout: 3))
    }

    // Picks the newest photo in the simulator's library, which comes with sample photos.
    func testAPhotoFromTheGalleryIsSentEchoedAndOpenedFullScreen() {
        let app = XCUIApplication()
        app.launch()

        XCTAssertTrue(app.buttons["Attach"].waitForExistence(timeout: 10))
        app.buttons["Attach"].tap()
        app.staticTexts["Gallery"].tap()
        let newestPhoto = app.images.matching(NSPredicate(format: "label BEGINSWITH 'Photo, '")).firstMatch
        XCTAssertTrue(newestPhoto.waitForExistence(timeout: 20))
        newestPhoto.tap()

        XCTAssertTrue(app.buttons["Remove photo"].waitForExistence(timeout: 10))
        keepScreenshot(of: app, named: "photo-attached")
        app.buttons["Send"].firstMatch.tap()
        allowNotificationsIfAsked()
        XCTAssertTrue(app.staticTexts["You sent a photo"].waitForExistence(timeout: 8))
        keepScreenshot(of: app, named: "photo-echo")

        app.descendants(matching: .any)["Open photo from Echo"].firstMatch.tap()
        XCTAssertTrue(app.descendants(matching: .any)["Photo from Echo"].waitForExistence(timeout: 5))
        keepScreenshot(of: app, named: "photo-viewer")
        app.buttons["Back"].tap()
        XCTAssertTrue(app.staticTexts["You sent a photo"].waitForExistence(timeout: 5))

        // ACTION-04: Save puts Echo's photo in Photos, after iOS asks once.
        app.descendants(matching: .any)["Open photo from Echo"].firstMatch.press(forDuration: 1.0)
        XCTAssertTrue(app.staticTexts["Save"].waitForExistence(timeout: 3))
        keepScreenshot(of: app, named: "photo-menu")
        app.staticTexts["Save"].tap()
        // iOS asks only until it's allowed. After that the save is instant, and "Saved to Photos"
        // shows for about 4 s, so watch for both at once rather than waiting out the question first.
        let allowPhotos = springboard.alerts.buttons["Allow"]
        let saved = app.staticTexts["Saved to Photos"]
        XCTAssertTrue(eventually(timeout: 8) { allowPhotos.exists || saved.exists })
        if allowPhotos.exists {
            keepScreenshot(of: springboard, named: "photos-permission")
            allowPhotos.tap()
            XCTAssertTrue(saved.waitForExistence(timeout: 5))
        }
        keepScreenshot(of: app, named: "photo-saved")
    }

    // NOTIFY-01, -04 and -05 on iOS: a reply while the app is in the background notifies, and
    // tapping the notification brings the chat back.
    func testAReplyWhileAwayNotifiesAndTappingItOpensTheChat() {
        let app = XCUIApplication()
        app.launch()

        // The first send may stop for the prompt, so the reply to watch for comes from a second one.
        send("Are you there", in: app)
        allowNotificationsIfAsked()
        XCTAssertTrue(app.staticTexts["You said: Are you there"].waitForExistence(timeout: 6))
        send("Are you still there", in: app)
        XCUIDevice.shared.press(.home)

        let banner = springboard.descendants(matching: .any)
            .matching(NSPredicate(format: "label CONTAINS 'You said: Are you still there'")).firstMatch
        XCTAssertTrue(banner.waitForExistence(timeout: 10))
        keepScreenshot(of: springboard, named: "notification")
        banner.tap()

        XCTAssertTrue(app.wait(for: .runningForeground, timeout: 5))
        XCTAssertTrue(app.staticTexts["You said: Are you still there"].waitForExistence(timeout: 5))
        keepScreenshot(of: app, named: "opened-from-notification")
    }

    // KEY-01 to KEY-03 on iOS: the attach panel and the keyboard take turns, the swipe in from the
    // screen's edge closes the panel, and a rotation keeps the keyboard up.
    func testTheAttachPanelAndTheKeyboardTakeTurns() {
        let app = XCUIApplication()
        app.launch()
        XCTAssertTrue(app.textViews["Message"].waitForExistence(timeout: 10))
        // The input is labelled by its placeholder only while it's empty.
        let message = app.textViews.firstMatch
        let window = app.windows.firstMatch

        message.tap()
        XCTAssertTrue(eventually { self.keyboardIsShown(in: app) })
        message.typeText("Half a thought")
        app.buttons["Attach"].tap()
        XCTAssertTrue(app.staticTexts["Gallery"].waitForExistence(timeout: 3))
        XCTAssertTrue(app.keyboards.firstMatch.waitForNonExistence(timeout: 3))
        keepScreenshot(of: app, named: "panel-open")

        message.tap()
        XCTAssertTrue(eventually { self.keyboardIsShown(in: app) })
        XCTAssertFalse(app.staticTexts["Gallery"].exists)

        app.buttons["Attach"].tap()
        XCTAssertTrue(app.staticTexts["Gallery"].waitForExistence(timeout: 3))
        let edge = app.coordinate(withNormalizedOffset: CGVector(dx: 0.0, dy: 0.5))
        edge.press(forDuration: 0.05, thenDragTo: app.coordinate(withNormalizedOffset: CGVector(dx: 0.8, dy: 0.5)))
        XCTAssertTrue(app.staticTexts["Gallery"].waitForNonExistence(timeout: 3))

        // XCTest's landscape screenshots come out turned and cut off on this simulator, so the
        // window's shape shows the turn instead.
        message.tap()
        XCTAssertTrue(eventually { self.keyboardIsShown(in: app) })
        XCUIDevice.shared.orientation = .landscapeLeft
        XCTAssertTrue(eventually { window.frame.width > window.frame.height }, "The app should turn to landscape")
        XCTAssertTrue(eventually { self.keyboardIsShown(in: app) })
        XCUIDevice.shared.orientation = .portrait
        XCTAssertTrue(eventually { window.frame.width < window.frame.height })
        XCTAssertTrue(eventually { self.keyboardIsShown(in: app) })
        XCTAssertEqual(message.value as? String, "Half a thought")

        // Known issue (D33): Compose doesn't update the keyboard's inset after a rotation, so its
        // fallback slides the whole screen up and the top bar is off it until the keyboard closes.
        // Not strict, so a Compose that fixes it passes too; then this can become a plain check.
        sleep(2) // Where the screen settles is what counts.
        let topBar = app.staticTexts["Echo"]
        let options = XCTExpectedFailure.Options()
        options.isStrict = false
        XCTExpectFailure("The top bar slides off after a rotation with the keyboard up (D33)", options: options) {
            XCTAssertTrue(topBar.exists && topBar.frame.minY >= 0)
        }
        keepScreenshot(of: app, named: "typing-after-turning")
    }

    // A minimized keyboard still exists, off the bottom of the screen (D32), so this checks where it is.
    private func keyboardIsShown(in app: XCUIApplication) -> Bool {
        let keyboard = app.keyboards.firstMatch
        return keyboard.exists && keyboard.frame.intersects(app.windows.firstMatch.frame)
    }

    private func eventually(timeout: TimeInterval = 3, _ condition: () -> Bool) -> Bool {
        let deadline = Date().addingTimeInterval(timeout)
        while Date() < deadline {
            if condition() { return true }
            RunLoop.current.run(until: Date().addingTimeInterval(0.1))
        }
        return condition()
    }

    private func send(_ text: String, in app: XCUIApplication) {
        // The input takes up to 4 lines, so iOS sees it as a text view rather than a text field.
        let message = app.textViews["Message"]
        XCTAssertTrue(message.waitForExistence(timeout: 10))
        message.tap()
        message.typeText(text)
        // The keyboard's return key is labelled Send too; it sends, as iPhone users expect.
        app.keyboards.buttons["Send"].tap()
    }

    // The first send asks to notify (NOTIFY-05). iOS asks only once per install, so the prompt may
    // not come. Looking for it takes a few seconds, so tests do it where the wait can't hide anything.
    private func allowNotificationsIfAsked() {
        let allow = springboard.alerts.buttons["Allow"]
        if allow.waitForExistence(timeout: 3) {
            keepScreenshot(of: springboard, named: "permission")
            allow.tap()
        }
    }

    // Kept in the test results, for the checkpoint write-up.
    private func keepScreenshot(of app: XCUIApplication, named name: String) {
        let screenshot = XCTAttachment(screenshot: app.screenshot())
        screenshot.name = name
        screenshot.lifetime = .keepAlways
        add(screenshot)
    }
}
