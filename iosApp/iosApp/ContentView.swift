import SwiftUI
import ComposeApp

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        #if DEBUG
        let isDebug = true
        #else
        let isDebug = false
        #endif
        return MainViewControllerKt.MainViewController(isDebugBuild: isDebug)
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    var body: some View {
        // Draw edge-to-edge. Insetting here would stop the Compose canvas above the home
        // indicator, leaving the window background showing below the navigation bar;
        // Compose applies the safe area itself (Scaffold/NavigationBar window insets).
        ComposeView()
            .ignoresSafeArea()
    }
}
