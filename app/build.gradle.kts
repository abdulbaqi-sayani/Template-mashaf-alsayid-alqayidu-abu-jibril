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

        // ✅ العربية فقط (يقلل الحجم)
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
            // ✅ تصغير الحجم في النسخة النهائية
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    // ✅ متوافق مع AGP 7.0.x (بديل packaging الحديثة)
    packagingOptions {
        exclude("META-INF/DEPENDENCIES")
        exclude("META-INF/LICENSE")
        exclude("META-INF/LICENSE.txt")
        exclude("META-INF/NOTICE")
        exclude("META-INF/NOTICE.txt")
        exclude("META-INF/*.kotlin_module")
    }

    // ✅ Gradle 7.0.x يحتاج Java 11
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    // ✅ يمنع تضارب Kotlin (Duplicate classes)
    implementation(platform("org.jetbrains.kotlin:kotlin-bom:1.8.22"))

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
}
