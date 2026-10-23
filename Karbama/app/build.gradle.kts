import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

// CI supplies these only for an official release. Debug builds intentionally never
// fall back to this permanent signing identity.
val releaseStoreFilePath = providers.gradleProperty("karbama.release.storeFile").orNull
val releaseStorePassword = providers.gradleProperty("karbama.release.storePassword").orNull
val releaseKeyAlias = providers.gradleProperty("karbama.release.keyAlias").orNull
val releaseKeyPassword = providers.gradleProperty("karbama.release.keyPassword").orNull
val hasReleaseSigning = listOf(releaseStoreFilePath, releaseStorePassword, releaseKeyAlias, releaseKeyPassword).all { !it.isNullOrBlank() }

// پلاگین Firebase فقط وقتی فعال می‌شود که فایل google-services.json را گذاشته باشی.
// این کار باعث می‌شود پروژه حتی قبل از تنظیم Firebase هم کامپایل و اجرا شود.
if (file("google-services.json").exists()) {
    apply(plugin = "com.google.gms.google-services")
}

android {
    namespace = "com.karbama.app"
    // Google Play requires Android 16 / API 36 for new submissions from 31 August 2026.
    compileSdk = 36

    defaultConfig {
        // Play Console reserves this new package for the separate Play listing.
        // Keep namespace/Kotlin packages stable to avoid a broad source refactor.
        applicationId = "com.ntgalaxy.karbama"
        minSdk = 24
        targetSdk = 36
        // v10.0.1 is the permanent-signing baseline. Version codes stay monotonic
        // so GitHub APK users and the future Play track can recognise later updates.
        versionCode = 10003
        versionName = "10.0.3"
        vectorDrawables { useSupportLibrary = true }
    }

    signingConfigs {
        create("permanentRelease") {
            // Leave this incomplete for ordinary local/debug builds. assembleRelease
            // is blocked below unless all four CI properties are present.
            if (hasReleaseSigning) {
                storeFile = file(releaseStoreFilePath!!)
                storePassword = releaseStorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            }
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // There is deliberately no debug-key fallback for a public APK.
            signingConfig = signingConfigs.getByName("permanentRelease")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" }
    }
}

// Refuse to emit an official APK if the permanent release signer was not supplied.
// This prevents a future accidental fallback to a newly generated debug keystore.
tasks.matching { it.name == "validateSigningRelease" }.configureEach {
    doFirst {
        check(hasReleaseSigning) {
            "Official release signing is not configured. Supply all karbama.release.* properties."
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation.compose)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.functions)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.storage)

    implementation(libs.coil.compose)
    // Pinned final osmdroid release: embedded OpenStreetMap picker/display, no Maps API key.
    implementation(libs.osmdroid.android)
    implementation(libs.androidx.exifinterface)
    implementation(libs.kotlinx.coroutines.play.services)

    debugImplementation(libs.androidx.ui.tooling)
}

// kbshots: JVM screenshot rendering (Robolectric) for comparing screens with karbama-demo.html.
// Only active with -Pkbshots — never part of the APK build.
if (project.hasProperty("kbshots")) {
    android.testOptions.unitTests.isIncludeAndroidResources = true
    android.testOptions.unitTests.all {
        it.systemProperty("robolectric.pixelCopyRenderMode", "hardware")
        // CI uses the historical 1536m default; constrained validation can lower this
        // without changing production test semantics.
        it.maxHeapSize = providers.gradleProperty("kbshots.testHeap").orNull ?: "1536m"
        it.testLogging.showStandardStreams = true
    }
    dependencies {
        add("testImplementation", "junit:junit:4.13.2")
        add("testImplementation", "org.robolectric:robolectric:4.14.1")
        add("testImplementation", platform(libs.androidx.compose.bom))
        add("testImplementation", "androidx.compose.ui:ui-test-junit4")
        add("debugImplementation", "androidx.compose.ui:ui-test-manifest")
    }
}
