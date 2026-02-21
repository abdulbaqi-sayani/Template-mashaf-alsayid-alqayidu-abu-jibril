plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    namespace = "com.abdulbaqi.mashaf"
    compileSdk = 31

    defaultConfig {
        applicationId = "com.abdulbaqi.mashaf"
        minSdk = 21
        targetSdk = 31
        versionCode = 1
        versionName = "1.0"
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

    buildFeatures {
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }

    // (اختياري) لتجنب مشاكل META-INF في بعض المكتبات
    packagingOptions {
        resources.excludes.addAll(
            setOf(
                "META-INF/DEPENDENCIES",
                "META-INF/LICENSE",
                "META-INF/LICENSE.txt",
                "META-INF/NOTICE",
                "META-INF/NOTICE.txt",
                "META-INF/*.kotlin_module"
            )
        )
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.7.0")
    implementation("androidx.appcompat:appcompat:1.4.1")
    implementation("com.google.android.material:material:1.5.0")

    implementation("androidx.recyclerview:recyclerview:1.2.1")

    // ✅ هذا هو المهم لحل layout_constraint...
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
}
