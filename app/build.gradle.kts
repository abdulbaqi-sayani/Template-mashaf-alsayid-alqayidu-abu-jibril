plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    namespace = "com.abdulbaqi.mashaf"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.abdulbaqi.mashaf"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures {
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
            isShrinkResources = false
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")

    // ✅ لحل أخطاء layout_constraint*
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // RecyclerView
    implementation("androidx.recyclerview:recyclerview:1.3.2")

    // ✅ لحل lifecycleScope
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")

    // ✅ لحل Dispatchers / withContext / kotlinx
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
}
```0
