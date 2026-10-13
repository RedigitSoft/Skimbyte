import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    jvm()

    js {
        browser()
        binaries.executable()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":shared"))

            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.material3.adaptiveNavigation3)
            implementation(libs.navigation3.ui)
            implementation(libs.androidx.lifecycle.viewmodelNavigation3)
            implementation(libs.kotlinx.serializationJson)
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
        }
        jsMain.dependencies {
            implementation(libs.wrappers.browser)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
        }
    }
}

compose.desktop {
    application {
        mainClass = "io.redigit.skimbyte.designsheet.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "io.redigit.skimbyte.designsheet"
            packageVersion = "1.0.0"
        }
    }
}
