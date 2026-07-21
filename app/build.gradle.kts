plugins {
    id("com.android.application")
    id("com.google.gms.google-services") // આ લાઈન પ્લગઈન એક્ટિવેટ કરે છે
}

android {
    namespace = "com.example.all_in_one_bazaar"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.all_in_one_bazaar"
        minSdk = 24
        targetSdk = 36
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
    // આ વર્ઝન વાપરવાથી AGP 8.2.2 સાથે એરર નહીં આવે
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.activity:activity:1.8.0")

    // બાકીની લાઈબ્રેરીઓ જે તમે પહેલા એડ કરી હતી
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)

    implementation(platform("com.google.firebase:firebase-bom:33.0.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-database")

    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation("com.google.android.gms:play-services-auth:21.1.1")

    // Firebase Cloud Messaging — version managed by BOM above
    implementation("com.google.firebase:firebase-messaging")
}