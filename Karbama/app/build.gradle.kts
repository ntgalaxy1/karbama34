import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

// پلاگین Firebase فقط وقتی فعال می‌شود که فایل google-services.json را گذاشته باشی.
// این کار باعث می‌شود پروژه حتی قبل از تنظیم Firebase هم کامپایل و اجرا شود.
if (file("google-services.json").exists()) {
    apply(plugin = "com.google.gms.google-services")
}

android {
    namespace = "com.karbama.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.karbama.app"
        minSdk = 24
        targetSdk = 35
        // نسخه‌گذاری (به خواست کاربر از v52.0.1): هر نسخه‌ی تازه فقط عدد آخر +۱ → 52.0.2، 52.0.3 …
        // versionCode هم هر بار +۱ (اندروید برای نصب به‌روزرسانی لازم دارد)
        versionCode = 53
        versionName = "52.0.1"
        vectorDrawables { useSupportLibrary = true }
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
            // برای تست سریع روی گوشی؛ قبل از انتشار در Google Play کلید امضای خودت را بگذار
            signingConfig = signingConfigs.getByName("debug")
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
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.storage)

    implementation(libs.coil.compose)
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
        it.maxHeapSize = "1536m"
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
