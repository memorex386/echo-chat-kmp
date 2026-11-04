import SwiftUI
import UIKit
import Shared

/// The shared Compose chat, which Kotlin hands over as a UIKit view controller.
struct ContentView: View {
    var body: some View {
        ComposeView()
            // Compose lays out around the notch, home indicator and keyboard itself.
            .ignoresSafeArea()
    }
}

private struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}
