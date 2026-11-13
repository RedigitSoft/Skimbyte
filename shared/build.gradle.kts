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
    alias(libs.plugins.dokka)
    alias(libs.plugins.aboutLibraries)
}

// License metadata for the in-app "Open source licenses" screen. The exported JSON is committed
// (read at runtime via compose resources), so re-run :shared:exportLibraryDefinitions whenever
// dependencies change in gradle/libs.versions.toml.
aboutLibraries {
    export {
        outputFile = file("src/commonMain/composeResources/files/aboutlibraries.json")
        prettyPrint = true
    }
}

dokka {
    moduleName.set("Skimbyte")
    dokkaPublications.html {
        // Served at /api/ on the docs site (see .github/workflows/deploy-site.yml);
        // the landing page is the MkDocs site in docs-site/.
        suppressInheritedMembers.set(true)
    }
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
            implementation(libs.compose.animation)
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
            implementation(libs.aboutLibraries.composeM3)
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

// Generates BackendConfig.kt from local.properties (gitignored) so the auth-server/skimbyte-server
// base URLs and the Google OAuth mobile-redirect URI aren't hardcoded into committed source — see
// local.properties.example for the keys this reads (authBaseUrl, googleAuthRedirectUri,
// skimbyteApiBaseUrl) and for the real values to copy in. Left blank by default (same convention
// as ApiConfig.BASE_URL) so no real endpoint ever lands in committed source, including this file.
val generatedAuthConfigDir = layout.buildDirectory.dir("generated/backendConfig/kotlin")

val generateAuthConfig by tasks.registering {
    val localProperties = rootProject.file("local.properties")
    val outputDir = generatedAuthConfigDir
    // inputs.files (not .file) so a missing local.properties is an empty input, not an error —
    // CI builds the demo configuration without the file existing at all.
    inputs.files(localProperties)
    outputs.dir(outputDir)
    doLast {
        val props = Properties()
        if (localProperties.exists()) {
            localProperties.inputStream().use { props.load(it) }
        }
        val authBaseUrl = props.getProperty("authBaseUrl", "")
        val googleRedirectUri = props.getProperty("googleAuthRedirectUri", "")
        val skimbyteApiBaseUrl = props.getProperty("skimbyteApiBaseUrl", "")
        // Host for relative image paths the API returns (e.g. /assets/placeholder-kotlin.png).
        // Defaults to the API base itself — override only if assets move to a CDN/bucket.
        val skimbyteAssetBaseUrl = props.getProperty("skimbyteAssetBaseUrl", skimbyteApiBaseUrl)
        // DO Spaces CDN domain for avatar/banner images — kept out of committed source like the
        // other endpoints (see local.properties.example); blank disables remote avatar images.
        val spacesCdnBaseUrl = props.getProperty("spacesCdnBaseUrl", "")
        val productId = props.getProperty("skimbyteProductId", "")

        val file = outputDir.get().file("io/redigit/skimbyte/data/remote/BackendConfig.kt").asFile
        file.parentFile.mkdirs()
        file.writeText(
            """
            package io.redigit.skimbyte.data.remote

            // GENERATED by :shared:generateAuthConfig from local.properties — do not edit directly.
            object BackendConfig {
                const val AUTH_BASE_URL = "$authBaseUrl"
                const val GOOGLE_REDIRECT_URI = "$googleRedirectUri"
                // Blank means no live posts/bookmarks API is configured — ApiConfig.DEMO_MODE
                // falls back to the mock network module automatically in that case.
                const val API_BASE_URL = "$skimbyteApiBaseUrl"
                // Base for relative imageUrl paths ("/assets/…") — skimbyteAssetBaseUrl in
                // local.properties, falling back to API_BASE_URL when unset.
                const val ASSET_BASE_URL = "$skimbyteAssetBaseUrl"
                const val SPACES_CDN_BASE_URL = "$spacesCdnBaseUrl"
                // The auth server's product id for this app — provisioned via POST /admin/products.
                const val PRODUCT_ID = "$productId"
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

// Writes iosApp/Configuration/Local.xcconfig (gitignored) from local.properties so machine-local
// Xcode settings — the Apple signing team — stay out of committed files, same convention as
// BackendConfig above. Config.xcconfig ends with `#include? "Local.xcconfig"`, so these values
// override its committed defaults; without an iosTeamId the file is still written (empty) so
// simulator builds work and Xcode never warns about a missing include.
val generateIosLocalXcconfig by tasks.registering {
    val localProperties = rootProject.file("local.properties")
    val outputFile = rootProject.file("iosApp/Configuration/Local.xcconfig")
    // inputs.files (not .file) so a missing local.properties is an empty input, not an error —
    // CI builds the demo configuration without the file existing at all.
    inputs.files(localProperties)
    outputs.file(outputFile)
    doLast {
        val props = Properties()
        if (localProperties.exists()) {
            localProperties.inputStream().use { props.load(it) }
        }
        val teamId = props.getProperty("iosTeamId", "")
        outputFile.writeText(
            buildString {
                appendLine("// GENERATED by :shared:generateIosLocalXcconfig from local.properties — do not edit directly.")
                if (teamId.isNotBlank()) appendLine("TEAM_ID=$teamId")
            },
        )
    }
}

// Xcode's "Compile Kotlin Framework" phase runs embedAndSign* through Gradle, so a device build
// refreshes Local.xcconfig; prepareKotlinIdeaImport does the same on every IDE sync. (A TEAM_ID
// change lands on the *next* Xcode build — build settings are resolved before script phases run.)
tasks.matching { it.name.startsWith("embedAndSign") || it.name == "prepareKotlinIdeaImport" }
    .configureEach { dependsOn(generateIosLocalXcconfig) }

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