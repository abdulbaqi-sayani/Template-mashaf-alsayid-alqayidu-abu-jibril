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

        // تقليل الموارد للغة العربية فقط
        resConfigs("ar")
    }

    buildFeatures {
        viewBinding = true
    }

    buildTypes {

        debug {
            isMinifyEnabled = false
            isShrinkResources = false
        }

        release {
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    // متوافق مع AGP 7.0.x
    packagingOptions {
        exclude("META-INF/DEPENDENCIES")
        exclude("META-INF/LICENSE")
        exclude("META-INF/LICENSE.txt")
        exclude("META-INF/NOTICE")
        exclude("META-INF/NOTICE.txt")
        exclude("META-INF/*.kotlin_module")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {

    // توحيد إصدار Kotlin لمنع Duplicate classes
    implementation(platform("org.jetbrains.kotlin:kotlin-bom:1.8.22"))

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
}
