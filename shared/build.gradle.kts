import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.roborazzi)
}

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }
    
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
    
    android {
       namespace = "io.redigit.skimbyte.shared"
       compileSdk = libs.versions.android.compileSdk.get().toInt()
       minSdk = libs.versions.android.minSdk.get().toInt()
    
       compilerOptions {
           jvmTarget = JvmTarget.JVM_11
       }
       androidResources {
           enable = true
       }
       withHostTest {
           isIncludeAndroidResources = true
       }
       withDeviceTestBuilder {
           sourceSetTreeName = "test"
       }.configure {
           instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
       }
    }
    
    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.compose.uiTooling)
        }
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.material3.adaptive)
            implementation(libs.material3.adaptiveLayout)
            implementation(libs.material3.adaptiveNavigationSuite)
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jsMain.dependencies {
            implementation(libs.wrappers.browser)
        }
        getByName("androidHostTest").dependencies {
            implementation(libs.roborazzi)
            implementation(libs.roborazzi.compose)
            implementation(libs.roborazzi.junitRule)
            implementation(libs.robolectric)
            implementation(libs.androidx.compose.uiTestJunit4)
            implementation(libs.androidx.compose.uiTestManifest)
            implementation(libs.androidx.testExt.junit)
        }
    }
}

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
}

// Golden images live alongside the tests that produce them so they're committed to version
// control, instead of Roborazzi's default build/ location which is gitignored.
roborazzi {
    outputDir.set(layout.projectDirectory.dir("src/androidHostTest/roborazzi"))
}

// Robolectric (via Roborazzi's androidHostTest screenshot tests) reflectively shims JDK-internal
// classes; JDK 17+'s module system blocks that access unless explicitly opened.
tasks.withType<Test>().configureEach {
    jvmArgs(
        "--add-opens=java.base/java.lang=ALL-UNNAMED",
        "--add-opens=java.base/java.util=ALL-UNNAMED",
        "--add-opens=java.base/java.io=ALL-UNNAMED",
        "--add-opens=java.base/java.security=ALL-UNNAMED",
        "--add-opens=java.base/java.text=ALL-UNNAMED",
        "--add-opens=java.base/java.lang.invoke=ALL-UNNAMED",
        "--add-opens=java.base/jdk.internal.access=ALL-UNNAMED",
        "--add-exports=java.base/jdk.internal.access=ALL-UNNAMED",
    )
}