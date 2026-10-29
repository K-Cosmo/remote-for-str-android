plugins {
    id("com.android.application")
}

android {
    namespace = "app.strremote.android"
    compileSdk = 37

    defaultConfig {
        applicationId = "app.strremote.android"
        minSdk = 26
        targetSdk = 37
        versionCode = 11
        versionName = "0.3.0"
    }

    buildTypes {
        debug {
            // Separate package for pre-release validation beside the signed production app.
            applicationIdSuffix = ".poc"
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

// Host-side tests only; no additional runtime dependency in the release APK.
dependencies {
    testImplementation("junit:junit:4.13.2")
}
