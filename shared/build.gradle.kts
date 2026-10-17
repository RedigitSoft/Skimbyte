import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.roborazzi)
    alias(libs.plugins.kotlinSerialization)
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
            implementation(libs.ktor.client.okhttp)
            implementation(libs.androidx.browser)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
        jvmMain.dependencies {
            implementation(libs.ktor.client.cio)
        }
        jvmTest.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.compose.uiTest)
        }
        jsMain.dependencies {
            implementation(libs.wrappers.browser)
            implementation(libs.ktor.client.js)
        }
        wasmJsMain.dependencies {
            implementation(libs.ktor.client.js)
        }
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.compose.materialIconsExtended)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.material3.adaptive)
            implementation(libs.material3.adaptiveLayout)
            implementation(libs.material3.adaptiveNavigationSuite)
            implementation(libs.material3.adaptiveNavigation3)
            implementation(libs.navigation3.ui)
            implementation(libs.androidx.lifecycle.viewmodelNavigation3)
            implementation(libs.kotlinx.serializationJson)
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.composeViewmodel)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.contentNegotiation)
            implementation(libs.ktor.serialization.kotlinxJson)
            implementation(libs.ktor.client.auth)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.client.mock)
            implementation(libs.multiplatformSettings)
            implementation(libs.multiplatformSettings.noArg)
            implementation(libs.multiplatformSettings.coroutines)
            implementation(libs.markdownRenderer.m3)
            implementation(libs.coil3.compose)
            implementation(libs.coil3.networkKtor)
            implementation(libs.kotlinx.datetime)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutinesTest)
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

// Generates AuthConfig.kt from local.properties (gitignored) so the auth-server base URL and the
// Google OAuth mobile-redirect URI aren't hardcoded into committed source — see
// local.properties.example for the keys this reads (authBaseUrl, googleAuthRedirectUri) and for
// the real values to copy in. Left blank by default (same convention as ApiConfig.BASE_URL) so no
// real endpoint ever lands in committed source, including this file.
val generatedAuthConfigDir = layout.buildDirectory.dir("generated/authConfig/kotlin")

val generateAuthConfig by tasks.registering {
    val localProperties = rootProject.file("local.properties")
    val outputDir = generatedAuthConfigDir
    inputs.file(localProperties).optional()
    outputs.dir(outputDir)
    doLast {
        val props = Properties()
        if (localProperties.exists()) {
            localProperties.inputStream().use { props.load(it) }
        }
        val authBaseUrl = props.getProperty("authBaseUrl", "")
        val googleRedirectUri = props.getProperty("googleAuthRedirectUri", "")

        val file = outputDir.get().file("io/redigit/skimbyte/auth/AuthConfig.kt").asFile
        file.parentFile.mkdirs()
        file.writeText(
            """
            package io.redigit.skimbyte.auth

            // GENERATED by :shared:generateAuthConfig from local.properties — do not edit directly.
            object AuthConfig {
                const val AUTH_BASE_URL = "$authBaseUrl"
                const val GOOGLE_REDIRECT_URI = "$googleRedirectUri"
            }

            """.trimIndent(),
        )
    }
}

kotlin {
    sourceSets {
        commonMain {
            kotlin.srcDir(generateAuthConfig.map { generatedAuthConfigDir })
        }
    }
}

// Golden images live alongside the tests that produce them so they're committed to version
// control, instead of Roborazzi's default build/ location which is gitignored.
roborazzi {
    outputDir.set(layout.projectDirectory.dir("src/androidHostTest/roborazzi"))
}

// Known Roborazzi/Robolectric bug with GraphicsMode.Mode.NATIVE (takahirom/roborazzi#912): only
// the first captureRoboImage() call per JVM test-worker process renders correctly; every later
// call in the same process comes back blank/corrupted. Forking a fresh JVM per test works around
// it at the cost of slower screenshot test runs.
tasks.withType<Test>().configureEach {
    forkEvery = 1
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