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

// Sourced from local.properties (gitignored) — see local.properties.example for the real value
// to copy in. :shared's generateAuthConfig task reads the same key for AuthConfig.GOOGLE_REDIRECT_URI.
// A blank fallback here just disables the intent-filter's matching (empty scheme/host), rather
// than ever hardcoding a real URI in committed source.
val googleAuthRedirectUri = run {
    val props = Properties()
    val localProperties = rootProject.file("local.properties")
    if (localProperties.exists()) localProperties.inputStream().use { props.load(it) }
    props.getProperty("googleAuthRedirectUri", "")
}.let(::URI)

android {
    namespace = "io.redigit.skimbyte"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "io.redigit.skimbyte"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"

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
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
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