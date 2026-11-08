plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "org.slauridev.brightnessfix"
    compileSdk = 34

    defaultConfig {
        applicationId = "org.slauridev.brightnessfix"
        minSdk = 27
        targetSdk = 34
        versionCode = 4
        versionName = "0.4-beta"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.github.topjohnwu.libsu:core:5.2.2")
}