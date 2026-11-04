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
        let allowPhotos = springboard.alerts.buttons["Allow"]
        if allowPhotos.waitForExistence(timeout: 5) {
            keepScreenshot(of: springboard, named: "photos-permission")
            allowPhotos.tap()
        }
        XCTAssertTrue(app.staticTexts["Saved to Photos"].waitForExistence(timeout: 5))
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
