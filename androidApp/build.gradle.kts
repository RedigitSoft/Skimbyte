import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.net.URI
import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}
dependencies {
    implementation(project(":shared"))

    implementation(libs.androidx.activity.compose)

    implementation(libs.compose.uiToolingPreview)
    debugImplementation(libs.compose.uiTooling)
}

// Single source of truth for the app version across all platforms (Android here, iOS via
// Config.xcconfig's #include of the same file, desktop via desktopApp/build.gradle.kts).
// Deliberately comment-free: the file doubles as a Properties file and an xcconfig include.
val appVersion = Properties().apply {
    rootProject.file("VERSION").inputStream().use { load(it) }
}
val appVersionName: String = appVersion.getProperty("versionName")
val appVersionCode: Int = appVersion.getProperty("versionCode").toInt()

// Local, gitignored configuration — see local.properties.example for the keys to copy in.
val localProps = Properties().apply {
    val localProperties = rootProject.file("local.properties")
    if (localProperties.exists()) localProperties.inputStream().use { load(it) }
}

// :shared's generateAuthConfig task reads the same key for AuthConfig.GOOGLE_REDIRECT_URI.
// A blank fallback here just disables the intent-filter's matching (empty scheme/host), rather
// than ever hardcoding a real URI in committed source.
val googleAuthRedirectUri = URI(localProps.getProperty("googleAuthRedirectUri", ""))

// Play upload-key signing. Resolution order: local.properties, then environment (for CI).
// All values stay out of the repo; without them the release build is simply unsigned, so
// open-source clones build without any setup.
fun signingProp(key: String): String? =
    localProps.getProperty(key) ?: System.getenv(key.replace(Regex("([a-z])([A-Z])"), "$1_$2").uppercase())

android {
    namespace = "io.redigit.skimbyte"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "io.redigit.skimbyte"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = appVersionCode
        versionName = appVersionName

        // Feeds the OAuth-callback intent-filter in AndroidManifest.xml. Falls back to a
        // placeholder scheme (rather than null, which manifestPlaceholders can't take) when
        // local.properties hasn't been configured — the intent-filter just won't match anything
        // real until it is.
        manifestPlaceholders["authRedirectScheme"] = googleAuthRedirectUri.scheme ?: "unconfigured"
        manifestPlaceholders["authRedirectHost"] = googleAuthRedirectUri.host ?: "unconfigured"
        manifestPlaceholders["authRedirectPathPrefix"] = googleAuthRedirectUri.path.ifEmpty { "/" }
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    signingConfigs {
        create("release") {
            val path = signingProp("uploadKeystorePath")
            if (path != null) {
                storeFile = file(path)
                storePassword = signingProp("uploadKeystorePassword")
                keyAlias = signingProp("uploadKeyAlias") ?: "upload"
                // PKCS12 keystores share one password; allow an override for JKS ones.
                keyPassword = signingProp("uploadKeyPassword") ?: signingProp("uploadKeystorePassword")
            }
        }
    }
    buildTypes {
        release {
            // R8 shrinking also embeds the deobfuscation map in the AAB
            // (BUNDLE-METADATA/), so Play can symbolicate crash reports.
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release").takeIf { it.storeFile != null }
            // Package symbol tables for the bundled native libs so Play can
            // symbolicate native crashes/ANRs (FULL would be much larger).
            ndk { debugSymbolLevel = "SYMBOL_TABLE" }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

// Collect the Play-ready bundle into <repo root>/release/ (gitignored), named with the
// version so uploads are unambiguous: Skimbyte-<versionName>-<versionCode>.aab.
val collectReleaseBundle by tasks.registering(Copy::class) {
    // Local val, not the script-level properties: the rename lambda must not capture the
    // build script itself or the configuration cache refuses to serialize the task.
    val bundleName = "Skimbyte-$appVersionName-$appVersionCode.aab"
    from(layout.buildDirectory.file("outputs/bundle/release/androidApp-release.aab"))
    into(rootProject.layout.projectDirectory.dir("release"))
    rename { bundleName }
}
// bundleRelease is created by AGP after evaluation, so hook it up lazily.
tasks.matching { it.name == "bundleRelease" }.configureEach { finalizedBy(collectReleaseBundle) }