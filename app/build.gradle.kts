plugins {
    id("com.android.application")
    // Pastikan plugin Google Services ada
    id("com.google.gms.google-services")
}

android {
    namespace = "com.f52123078.aplikasibelajarmandiri"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.f52123078.aplikasibelajarmandiri"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    // Mengaktifkan ViewBinding (Wajib untuk kode Activity Anda)
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // --- FIREBASE ---
    implementation(platform("com.google.firebase:firebase-bom:34.4.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")

    // --- UI COMPONENTS ---
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // --- ANIMATION ---
    implementation("com.airbnb.android:lottie:6.4.1")

    // --- PERBAIKAN ERROR VERSI SDK ---
    // Kita kunci versi activity ke 1.9.3 agar tidak error minta SDK 36
    implementation("androidx.activity:activity:1.9.3")
    implementation("androidx.activity:activity-ktx:1.9.3")

    // --- GAMBAR & UPLOAD ---
    // Cloudinary (Untuk upload ke server gratis)
    implementation("com.cloudinary:cloudinary-android:2.3.1")

    // Glide (Untuk menampilkan gambar dari URL)
    implementation("com.github.bumptech.glide:glide:4.16.0")

    // Google Sign In
    implementation("com.google.android.gms:play-services-auth:21.0.0")
}