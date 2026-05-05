plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace   = "com.example.lendloop"
    compileSdk  = 36

    defaultConfig {
        applicationId         = "com.example.lendloop"
        minSdk                = 24
        targetSdk             = 36
        versionCode           = 1
        versionName           = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {

    // ── Core ──────────────────────────────────────────────────────────────
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)

    // ── Compose ───────────────────────────────────────────────────────────
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)

    // ── Navigation ────────────────────────────────────────────────────────
    implementation(libs.navigation.compose)

    // ── Hilt ──────────────────────────────────────────────────────────────
    implementation(libs.hilt.android)
    implementation(libs.androidx.compose.ui.text.google.fonts)
    ksp(libs.hilt.android.compiler)
    implementation(libs.hilt.navigation.compose)

    // ── Room ──────────────────────────────────────────────────────────────
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // ── WorkManager + Hilt integration ────────────────────────────────────
    implementation(libs.work.runtime.ktx)
    implementation(libs.androidx.hilt.common)
    implementation(libs.androidx.hilt.work)
    ksp(libs.hilt.android.compiler)      // ✅ same compiler handles hilt-work too

    // ── Networking ────────────────────────────────────────────────────────
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp)           // ✅ only one OkHttp — removed the duplicate
    implementation(libs.okhttp.logging)
    implementation(libs.gson)

    // ── Firebase ──────────────────────────────────────────────────────────
    implementation(libs.firebase.database)

    // ── Coil ──────────────────────────────────────────────────────────────
    implementation(libs.coil.compose)

    // ── Testing ───────────────────────────────────────────────────────────
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}