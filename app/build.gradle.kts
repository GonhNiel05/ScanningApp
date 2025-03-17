plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.textrecognitionactivity"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.textrecognitionactivity"
        minSdk = 24
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // To recognize Latin script
    implementation ("com.google.mlkit:text-recognition:16.0.1")
    // To recognize Chinese script
    implementation ("com.google.mlkit:text-recognition-chinese:16.0.1")
    // To recognize Devanagari script
    implementation ("com.google.mlkit:text-recognition-devanagari:16.0.1")
    // To recognize Japanese script
    implementation ("com.google.mlkit:text-recognition-japanese:16.0.1")
    // To recognize Korean script
    implementation ("com.google.mlkit:text-recognition-korean:16.0.1")
}