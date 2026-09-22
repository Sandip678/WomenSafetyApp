plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.services) // Firebase Plugin
}

android {
    namespace = "com.womensefty.womensafetyapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.womensefty.womensafetyapp"
        minSdk = 21
        targetSdk = 35
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
}

dependencies {
    // Firebase
    implementation(libs.firebase.auth) // Firebase Authentication
    implementation(libs.firebase.database) // Firebase Realtime Database

    // Other dependencies
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.credentials)
    implementation(libs.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.firebase.firestore)

    implementation(libs.firebase.bom) // Firebase BoM
    implementation(libs.firebase.appcheck) // Firebase App Check

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}