import SwiftUI
import Shared

@main
struct iOSApp: App {
    var body: some Scene {
        WindowGroup {
            ContentView()
                // The OAuth redirect (skimbyte://auth/callback?access_token=…) arrives here via
                // the CFBundleURLTypes registration in Info.plist; forward it into the shared
                // GoogleAuthCallback bridge, which completes sign-in for every provider.
                .onOpenURL { url in
                    GoogleAuthCallback.shared.onRedirect(uri: url.absoluteString)
                }
        }
    }
}