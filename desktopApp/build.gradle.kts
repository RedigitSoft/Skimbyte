import org.jetbrains.compose.desktop.application.dsl.TargetFormat

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
            // The user-visible app name (window manager, installer, /Applications);
            // the reverse-DNS identity lives in macOS.bundleID below.
            packageName = "Skimbyte"
            packageVersion = "1.0.0"
            macOS {
                bundleID = "io.redigit.skimbyte"
                iconFile.set(project.file("icons/skimbyte.icns"))
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