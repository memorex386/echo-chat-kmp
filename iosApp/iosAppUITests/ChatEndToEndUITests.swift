import XCTest

// Runs the real app on an iOS simulator, with the real 5-second wait: the iOS counterpart of
// androidApp's ChatEndToEndTest. It finds Compose's controls through iOS accessibility.
final class ChatEndToEndUITests: XCTestCase {

    func testSendingHelloShowsTypingDotsThenTheEcho() {
        let app = XCUIApplication()
        app.launch()

        // The input takes up to 4 lines, so iOS sees it as a text view rather than a text field.
        let message = app.textViews["Message"]
        XCTAssertTrue(message.waitForExistence(timeout: 10))
        message.tap()
        message.typeText("Hello")
        // The keyboard's return key is labelled Send too; it sends, as iPhone users expect.
        app.keyboards.buttons["Send"].tap()

        XCTAssertTrue(app.staticTexts["Hello"].waitForExistence(timeout: 2))
        XCTAssertTrue(app.descendants(matching: .any)["Echo is typing"].waitForExistence(timeout: 2))
        keepScreenshot(of: app, named: "echo-typing")
        XCTAssertTrue(app.staticTexts["You said: Hello"].waitForExistence(timeout: 6))
        keepScreenshot(of: app, named: "replied")
    }

    // Kept in the test results, for the checkpoint write-up.
    private func keepScreenshot(of app: XCUIApplication, named name: String) {
        let screenshot = XCTAttachment(screenshot: app.screenshot())
        screenshot.name = name
        screenshot.lifetime = .keepAlways
        add(screenshot)
    }
}
