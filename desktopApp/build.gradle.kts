import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import java.util.Properties

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

dependencies {
    implementation(project(":shared"))

    implementation(compose.desktop.currentOs)
    implementation(libs.kotlinx.coroutinesSwing)

    implementation(libs.compose.uiToolingPreview)
}

compose.desktop {
    application {
        mainClass = "io.redigit.skimbyte.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            // jpackage runs jlink, which strips JDK modules not found by static analysis.
            // The desktop OAuth flow (AuthRedirect.jvm.kt) uses com.sun.net.httpserver for its
            // RFC 8252 loopback listener; that lives in jdk.httpserver, which jlink doesn't
            // detect, so packaged builds (DMG/MSI/DEB) crash with NoClassDefFoundError:
            // com/sun/net/httpserver/HttpServer. `./gradlew :desktopApp:run` hides this because
            // it uses the full JDK. Declaring the module forces jlink to include it.
            modules("jdk.httpserver")
            // The user-visible app name (window manager, installer, /Applications);
            // the reverse-DNS identity lives in macOS.bundleID below.
            packageName = "Skimbyte"
            // Overridden by the release workflow with the tag being released; otherwise read
            // from the repo-root VERSION file (shared with Android and iOS). jpackage requires
            // a MAJOR.MINOR.PATCH version with MAJOR >= 1 on macOS, which is why release.yml
            // only triggers on v1+ tags and why a two-part versionName gets a ".0" appended.
            packageVersion = providers.gradleProperty("skimbyteDesktopVersion").orNull
                ?: Properties()
                    .apply { rootProject.file("VERSION").inputStream().use { load(it) } }
                    .getProperty("versionName")
                    .let { if (it.count { c -> c == '.' } == 1) "$it.0" else it }
            macOS {
                bundleID = "io.redigit.skimbyte"
                iconFile.set(project.file("icons/skimbyte.icns"))

                // Signing/notarization come from the environment (CI secrets or a local
                // shell) so dev builds stay unsigned with zero setup. Set
                // MACOS_SIGNING_IDENTITY to a "Developer ID Application: …" identity to
                // sign; add the NOTARIZATION_* vars to notarize (all three are needed for
                // Gatekeeper to accept a DMG downloaded from the website).
                System.getenv("MACOS_SIGNING_IDENTITY")?.let { signingIdentity ->
                    signing {
                        sign.set(true)
                        identity.set(signingIdentity)
                    }
                }
                if (System.getenv("NOTARIZATION_APPLE_ID") != null) {
                    notarization {
                        appleID.set(System.getenv("NOTARIZATION_APPLE_ID"))
                        password.set(System.getenv("NOTARIZATION_PASSWORD"))
                        teamID.set(System.getenv("NOTARIZATION_TEAM_ID"))
                    }
                }
            }
            windows {
                iconFile.set(project.file("icons/skimbyte.ico"))
            }
            linux {
                // Debian package names must be lowercase; the desktop entry still shows Skimbyte.
                packageName = "skimbyte"
                iconFile.set(project.file("icons/skimbyte.png"))
            }
        }
    }
}