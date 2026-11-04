import SwiftUI
import Shared

@main
struct iOSApp: App {
    init() {
        // Kotlin's top-level initKoin() in Koin.kt; Swift sees it as KoinKt.doInitKoin().
        KoinKt.doInitKoin()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
